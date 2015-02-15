package com.projectreach.poc;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import at.markushi.ui.CircleButton;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private FragmentManager mFragmentManager;
    private ArrayList<String> dataSet;

    TextView textStart;
    TextView textStop;
    TextView textSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            Log.d(TAG, "savedInstanceState is null");
            MainFragment mainFrag = new MainFragment();
            mFragmentManager= getFragmentManager();
            mFragmentManager.beginTransaction()
                    .add(R.id.container, mainFrag)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initTextViews();

        CircleButton btn_start = (CircleButton) findViewById(R.id.button_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDataLogging();
            }
        });

        CircleButton btn_stop = (CircleButton) findViewById(R.id.button_stop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDataLogging();
            }
        });

        CircleButton btn_save = (CircleButton) findViewById(R.id.button_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndEmail();
            }
        });
    }

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

    /**
     * starts the service to log sensor data
     */
    private void startDataLogging() {
        if (!mIsBound) {
            Intent intent = new Intent(this, SensorLogger.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            dataSet = new ArrayList<String>();
            setStartText();
        }
    }

    private void setStartText() {
        textStart.setText("Accelerometer & Rotation Vector logging started.");
        textStop.setText("");
        textSave.setText("");
    }

    /**
     * stops the service to log data
     */
    private void stopDataLogging() {
        if (mIsBound) {
            ArrayList<String> newDataset = mBoundService.getData();
            ArrayList<String> temp = newDataset;
            synchronized (temp) {
                for (String row : temp) {
                    dataSet.add(row);
                }
            }
            unbindService(mConnection);
            mIsBound = false;
            setStopText();
        }
    }

    private void setStopText() {
        textStart.setText("");
        textStop.setText("Accelerometer & Rotation Vector logging stopped.");
        textSave.setText("");
    }

    /**
     * writes the data to file first, and then emails it
     */
    private void saveAndEmail() {
        ArrayList<String> newDataset = mBoundService.getData();
        ArrayList<String> temp = newDataset;
        synchronized (temp) {
            for (String row : temp) {
                dataSet.add(row);
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "data_" + timeStamp + ".csv";
        File file   = null;
        File root   = Environment.getExternalStorageDirectory();
        if (root.canWrite()){
            File dir    =   new File (root.getAbsolutePath() + "/GripRecognition");
            dir.mkdirs();
            file   =   new File(dir, fileName );
//            Log.d(TAG, "Path of file = " + file.getAbsolutePath());
            FileOutputStream out   =   null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "FileNotFoundException: " + e.getMessage());
                e.printStackTrace();
            }

            BufferedWriter buf = null;
            try {
                buf = new BufferedWriter(new FileWriter(file));
                buf.write(dataSet.toString());
            } catch (IOException e) {
                Log.d(TAG, "IOException: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                out.close();
                buf.close();
            } catch (IOException e) {
                Log.d(TAG, "IOException: " + e.getMessage());
                e.printStackTrace();
            }
        }
        dataSet.clear();
        setSaveText(file.getAbsolutePath());
        sendEmailAttachment(file, timeStamp);
    }

    /**
     * sends an email with the specified file attached to it
     *
     * @param file - the file where the dataset was saved to
     * @param timeStamp - the timestamp on which the file was created
     */
    private void sendEmailAttachment(File file, String timeStamp) {
        Uri u1  =   null;
        try {
            u1  =   Uri.fromFile(file);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Sensor dataset " + timeStamp);
        sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
        sendIntent.setType("text/html");
        startActivity(sendIntent);
    }

    private void setSaveText(String path) {
        textStart.setText("");
        textStop.setText("");
        textSave.setText("Logging stopped. Dataset saved here " + path);
    }

    //pointer to my service
    private SensorLogger  mBoundService;
    private boolean mIsBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected called");
            //cast generic IBinder interface to my service interface and get reference to my service
            mBoundService = ((SensorLogger.MySensorLoggerBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected called");
            mBoundService = null;
        }
    };


    private void initTextViews() {
        textStart = (TextView) findViewById(R.id.text_start);
        textStop = (TextView) findViewById(R.id.text_stop);
        textSave = (TextView) findViewById(R.id.text_save);
    }
}
