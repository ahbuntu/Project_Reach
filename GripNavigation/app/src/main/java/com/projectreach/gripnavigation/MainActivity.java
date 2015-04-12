package com.projectreach.gripnavigation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instance;


public class MainActivity extends Activity
                            implements TapEvaluation.TapEvaluatedListener{

    private static final String TAG = "MainActivity";

    //pointer to my service
    private SensorReader  mBoundService;
    private boolean mIsBound = false;

    //log to file
    LogOutputWriter outputLogger = null;
    private int logOutBufferSize = 100 ;

    private static int axisSize = 3; //TODO: these need to be configurable
    private static int windowSize = 20; //TODO: these need to be configurable

    Globals globalInstance = Globals.getInstance();
    TapEvaluation tapEvaluator = TapEvaluation.getInstance();
    private static Classifier modelClassifier;
    private TextView textModelStatus;
    private TextView textTapCounter;
    private int tapCounter = 0;

    private boolean enableTemporalLogging = false;
    private boolean enableWindowLogging = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected called");
            //cast generic IBinder interface to my service interface and get reference to my service
            mBoundService = ((SensorReader.MySensorReaderBinder)service).getService();
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected called");
            mBoundService = null;
            mIsBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textModelStatus = (TextView) findViewById(R.id.text_active_model_status);
        textTapCounter = (TextView) findViewById(R.id.text_tap_evaluated);
        Button button_Scheme1 = (Button) findViewById(R.id.button_scheme1);
        button_Scheme1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SchemeOneActivity.class);
                startActivity(intent);
            }
        });

        Button button_Scheme2 = (Button) findViewById(R.id.button_scheme2);
        button_Scheme2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SchemeTwoActivity.class);
                startActivity(intent);
            }
        });

        Button button_startService = (Button) findViewById(R.id.button_start_service);
        button_startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSensing();
            }
        });

        Button button_stopService = (Button) findViewById(R.id.button_stop_service);
        button_stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSensing();
            }
        });

        Button button_start_rfduino = (Button) findViewById(R.id.button_start_rfduino);
        button_start_rfduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(MainActivity.this, RFduinoActivity.class);
                startActivity(mIntent);
            }
        });


        Button button_stop_rfduino = (Button) findViewById(R.id.button_stop_rfduino);
        button_stop_rfduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rfduinoIntent = new Intent(MainActivity.this, RFduinoService.class);
                stopService(rfduinoIntent);
            }
        });


        Button button_launch_weka = (Button) findViewById(R.id.button_launch_weka);
        button_launch_weka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WekaTest.class);
                startActivityForResult(intent, Globals.ACTIVE_MODEL_ASSIGNED);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter mIntentFilter = new IntentFilter(Globals.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopSensing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Globals.ACTIVE_MODEL_ASSIGNED) {
            if (resultCode == RESULT_OK) {
                String message = data.getStringExtra(Globals.ARG_ACTIVE_MODE);
                if (message.isEmpty()) {
                    //do nothing
                } else {
                    //test active model as sanity check
                    Globals globalInstance = Globals.getInstance();
                    if (globalInstance.getActiveModel() != null) {
                        textModelStatus.setText(message);
                    } else {
                        Log.d(TAG, "SOMETHING VERY BAD happened");
                    }
                }
            }
        }
    }

    List<Float> prevVarianceFeatureList = null;
    private  BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG,"BroadcastReceiver onReceive called");

            //each list item corresponds to a sampling of 20 values for a single sensor value
            //expecting that size of values will always be 15
            ArrayList<WindowBuffer> sensorValues = intent.getParcelableArrayListExtra(Globals.ARG_SENSOR_VAL);

            Log.d(TAG, "#values RECEIVED = " + sensorValues.size());

            if (enableTemporalLogging) {
                outputLogger.logTemporalValuesToFile(sensorValues); // log values line by line
            }

//            List<Float> sumFeatureList =  FeatureExtractor.calculateSum(sensorValues);
            List<Float> meanFeatureList = FeatureExtractor.calculateMean(sensorValues);
            List<Float> currVarianceFeatureList = FeatureExtractor.calculateVariance(sensorValues);
//            List<Float> deltaVarianceFeatureList = new ArrayList<>();
//            if (prevVarianceFeatureList == null) {
//                prevVarianceFeatureList = currVarianceFeatureList;
//                deltaVarianceFeatureList.add(0f); //x-axis
//                deltaVarianceFeatureList.add(0f); //y-axis
//                deltaVarianceFeatureList.add(0f); //z-axis
//            } else {
//                deltaVarianceFeatureList.add(currVarianceFeatureList.get(0) - prevVarianceFeatureList.get(0));
//                deltaVarianceFeatureList.add(currVarianceFeatureList.get(1) - prevVarianceFeatureList.get(1));
//                deltaVarianceFeatureList.add(currVarianceFeatureList.get(2) - prevVarianceFeatureList.get(2));
//            }

//            float[] featureBuffer = new float[MainActivity.axisSize * 3]; //features are sum, mean, variance
            float[] featureBuffer = new float[MainActivity.axisSize * 2]; //features are mean, variance
//            float[] featureBuffer = new float[MainActivity.axisSize * 2]; //features are variance, delta_variance
            int idx = 0;
            for (int i=0; i < MainActivity.axisSize; i++) {
                //iterating over each axis
//                featureBuffer[idx++] = sumFeatureList.get(i); //sum
                featureBuffer[idx++] = meanFeatureList.get(i); //mean
                featureBuffer[idx++] = currVarianceFeatureList.get(i); //variance
//                featureBuffer[idx++] = deltaVarianceFeatureList.get(i); //delta_variance
            }

            Instance classificationInstance = new WindowClassifyInstance().getAccInstance(featureBuffer);
            try {
                double prediction = modelClassifier.classifyInstance(classificationInstance);
                Globals.Tap_Pattern predictedTap = Globals.tapAsEnum(prediction);
                tapEvaluator.evaluateIfTap(predictedTap);

                //========= LOGGING work BEGIN ===========
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < featureBuffer.length; i++) {
                    output.append(featureBuffer[i]).append(",");
                }
                output.append(prediction).append(",").append(System.currentTimeMillis());

                if (enableWindowLogging) {
                    outputLogger.logSingleLine(output.toString()); // Log.d(TAG, "WEKA window: " + output.toString());
                }
                //========= LOGGING work END ===========

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "hey somethinge went wrong here");
            }
//            // ======= DETAILED DEBUGGING ========
//            StringBuilder output = new StringBuilder();
//            for (WindowBuffer axisBuffer : sensorValues) {
//                float[] lines = axisBuffer.getSensorValues();
//                for (int i = 0; i < lines.length; i++) {
//                    output.append(lines[i]).append(",");
//                }
//                Log.d(TAG, "Window temporal: " + output.toString());
//            }
//
//            output = new StringBuilder();
//            for (int i = 0; i < featureBuffer.length; i++) {
//                output.append(featureBuffer[i]).append(",");
//            }
//            Log.d(TAG, "Window features: " + output.toString());

//            for (WindowBuffer evalWindow : sensorValues) {
//                StringBuilder output = new StringBuilder();
//                if (evalWindow  == null) {
//                    Log.d(TAG, "WHY IS THIS NuLL??");
//                }
//                float[] evalArray = evalWindow.getSensorValues();
//                for (int i = 0; i < evalArray.length; i++) {
//                    output.append(evalArray[i]).append(",");
//                }
//                Log.d(TAG, "axis : " + output.toString());
//            }
//
////            outputLogger.logSingleLine("============ SUM ============");
//            List<Float> sumFeatureList =  FeatureExtractor.calculateSum(sensorValues);
//            for (float sums : sumFeatureList) {
//                Log.d(TAG, "axis sum: " + sums);
//            }
//            // ======= DETAILED DEBUGGING ========


        }
    };

    @Override
    public void onTapEvaluated() {
        tapCounter++;
        textTapCounter.setText(tapCounter + " taps");
    }

    /**
     * creates the log file and opens it for writing
     *
     * @param sensorAxis - the number of sensor windows in a single transmission
     */
    private void prepareLogging(int sensorAxis) {
        if (outputLogger == null) {
            outputLogger = new LogOutputWriter(MainActivity.this, sensorAxis);
            outputLogger.initialize();
        }
    }

    /**
     * stops writing to the file and closes it
     *
     */
    private void ceaseLogging() {
        if (outputLogger != null) {
            outputLogger.cease();
            outputLogger = null;
        }
    }
    /**
     * starts the service to log sensor data
     */
    private void startSensing() {
        if (globalInstance.getActiveModel() == null) {
            Toast.makeText(this, "Please load a model before staring the sensor service.", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            modelClassifier = globalInstance.getActiveModel();
            tapEvaluator.registerTapEvaluatedListener(this);
        }
        if (!mIsBound) {
            prepareLogging(3); //x,y,z axis = 3
//            prepareLogging(1); // x+y+z = 1 axis

            Intent intent = new Intent(this, SensorReader.class);
            intent.putExtra(Globals.ARG_WINDOW_SIZE, windowSize); //pass windowsize in a bundle
            this.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            Toast.makeText(this, "Starting accelerometer data logging.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * stops the service to log sensor data
     */
    private void stopSensing() {
        if (mIsBound) {
            this.unbindService(mServiceConnection);
            mIsBound = false;
            Toast.makeText(this, "Stopping accelerometer data logging.", Toast.LENGTH_SHORT).show();
            ceaseLogging();
        }
    }

    public void onToggleTemporalClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            enableTemporalLogging = true;
        } else {
            enableTemporalLogging = false;
        }
    }

    public void onToggleWindowClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            enableWindowLogging = true;
        } else {
            enableWindowLogging = false;
        }
    }


    //region OptionsMenu implementation

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion


}
