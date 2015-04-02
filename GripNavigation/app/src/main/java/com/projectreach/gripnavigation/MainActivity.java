package com.projectreach.gripnavigation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.conn.scheme.Scheme;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    //pointer to my service
    private SensorReader  mBoundService;
    private boolean mIsBound = false;

    //log to file
    LogOutputWriter outputLogger = null;
    private int logOutBufferSize = 100 ;
    private List<WindowBuffer> logOutBuffer = new ArrayList<>(logOutBufferSize * 16);

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
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter mIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopSensing();
    }

    private  BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG,"BroadcastReceiver onReceive called");

            //each list item corresponds to a sampling of 20 values for a single sensor value
            //expecting that size of values will always be 15
            ArrayList<WindowBuffer> sensorValues = intent.getParcelableArrayListExtra(Constants.ARG_SENSOR_VAL);


//            outputLogger.execute(sensorValues);
            if (sensorValues.size() > 0) {
                outputLogger.logValuesToFile(sensorValues);
            }

//            List<Float> sensorMeanValues = FeatureExtractor.calculateMean(sensorValues);
//            int idx = 0;
//            for (Float sensorMean : sensorMeanValues) {
//                Log.d(TAG, "Sensor" +idx+ " mean : " + sensorMean);
//                idx++;
//            }
        }
    };

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
        if (!mIsBound) {
            prepareLogging(3); //x,y,z axis = 3

            Intent intent = new Intent(this, SensorReader.class);
            intent.putExtra("windowsize", 20); //pass windowsize in a bundle
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
