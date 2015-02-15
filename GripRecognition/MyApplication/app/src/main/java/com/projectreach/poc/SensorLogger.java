package com.projectreach.poc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SensorLogger extends Service
        implements SensorEventListener {
    private static final String TAG = "SensorLoggerService";
    private SensorManager mSensorManager;
    Sensor mSensorAcceleration, mSensorRotation;
    private NotificationManager mNotificationManager;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 19;

    private ArrayList<String> sensorData = new ArrayList<String>();
    private String sensorDataAclRow;
    private String sensorDataRotRow;
    private int counter = 0;

    public ArrayList<String> getData() {
        return sensorData;
    }

    public SensorLogger() {
    }

    //  interface for clients that bind
    private final IBinder mBinder = new MySensorLoggerBinder ();
    public class MySensorLoggerBinder extends Binder {
        public SensorLogger getService() {
            return SensorLogger.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind is called");

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mSensorAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorRotation, SensorManager.SENSOR_DELAY_NORMAL);

        counter = 0;
        return  mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind is called");
        mSensorManager.unregisterListener(this);
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate is called");
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        new SensorEventEvalTask().execute(event); // SensorEvent as param
    }

    private class SensorEventEvalTask extends AsyncTask<SensorEvent, Integer, Integer> {
        @Override
        protected Integer doInBackground(SensorEvent... events) {
//            Log.d(TAG, "running in the background");
            counter++;
            SensorEvent event = events[0];
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                String[] sensorDataAclRows = readAcceleration(event);
                for (String row : sensorDataAclRows) {
                    sensorData.add(row);
                }
            }
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                String[] sensorDataRotRows =  readRotation(event);
                for (String row : sensorDataRotRows) {
                    sensorData.add(row);
                }
            }
            return 0;
        }

        /**
         * Reads acceleration values from the sensor event and returns them in a CSV format
         * @param event - the sensor event
         * @return
         */
        private String[] readAcceleration(SensorEvent event) {
            float x_acc = event.values[0];
            float y_acc = event.values[1];
            float z_acc = event.values[2];

            String[] accelDatasetCSV = new String[3];
            accelDatasetCSV[0] = counter + "," + System.currentTimeMillis() + ","
                    + "acc" + "," + "x" + "," + x_acc + "\n";
            accelDatasetCSV[1] = counter + "," + System.currentTimeMillis() + ","
                    + "acc" + "," + "y" + "," + y_acc + "\n";
            accelDatasetCSV[2] = counter + "," + System.currentTimeMillis() + ","
                    + "acc" + "," + "z" + "," + z_acc + "\n";
            return accelDatasetCSV;
        }

        /**
         * Reads rotation vector values from the sensor event and returns them in a CSV format
         * @param event - the sensor event
         * @return
         */
        private String[] readRotation(SensorEvent event) {
            float[] orientation = new float[3];
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, rotationMatrix);
            SensorManager.getOrientation(rotationMatrix, orientation);

            double azimuth = Math.toDegrees(orientation[0]);
            double pitch = Math.toDegrees(orientation[1]);
            double roll = Math.toDegrees(orientation[2]);

            String[] rotatDatasetCSV = new String[3];
            rotatDatasetCSV[0] = counter + "," + System.currentTimeMillis() + ","
                    + "rot" + "," + "a" + "," + azimuth + "\n";
            rotatDatasetCSV[1] = counter + "," + System.currentTimeMillis() + ","
                    + "rot" + "," + "p" + "," + pitch + "\n";
            rotatDatasetCSV[2] = counter + "," + System.currentTimeMillis() + ","
                    + "rot" + "," + "r" + "," + roll + "\n";
            return rotatDatasetCSV;
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        Log.d(TAG, "showNotification is called");
        CharSequence text = "SensorLogger is running";

        Notification.Builder mBuilder = new Notification.Builder(this)
                        .setSmallIcon(R.drawable.electrical_sensor)
                        .setContentTitle("Project Reach")
                        .setContentText(text)
                        .setWhen(System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, mBuilder.build());
    }


}
