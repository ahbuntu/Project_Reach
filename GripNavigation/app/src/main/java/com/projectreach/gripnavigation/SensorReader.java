package com.projectreach.gripnavigation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SensorReader extends Service
        implements SensorEventListener {
    private static final String TAG = "SensorReader";
    private SensorManager mSensorManager;
    Sensor mSensorAcceleration, mSensorRotation;
    private NotificationManager mNotificationManager;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 19;

    private int counter = 0;

    private List<WindowBuffer> sensorData = new ArrayList<WindowBuffer>();
    private int BROADCAST_AXIS_SIZE = 3; //TODO: corresponds to number of axis - make this part of constructor
    private int broadcastCounter = 0;

    private int windowSize = 5;
    private Queue<Float> x_buffQueue = null;
    private Queue<Float> y_buffQueue = null;
    private Queue<Float> z_buffQueue = null;

    public List<WindowBuffer> getSensorData() {
        return sensorData;
    }

    public SensorReader() {

    }

    //  interface for clients that bind
    public class MySensorReaderBinder extends Binder {
        public SensorReader getService() {
            return SensorReader.this;
        }
    }
    private final IBinder mBinder = new MySensorReaderBinder();

    //region Service implementations

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

    //endregion

    //region SensorListener implementations

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        new SensorEventEvalTask().execute(event); // SensorEvent as param
    }
    //endregion

    /**
     * sends the broadcast
     */
    private void readyToBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_ACTION);
        intent.putParcelableArrayListExtra(Constants.ARG_SENSOR_VAL, (ArrayList<WindowBuffer>) sensorData);
//        sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//            Log.d(TAG, "Broadcast sent");
    }

    private class SensorEventEvalTask extends AsyncTask<SensorEvent, Integer, Integer> {
        @Override
        protected Integer doInBackground(SensorEvent... events) {
//            Log.d(TAG, "running in the background");

            SensorEvent event = events[0];

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                x_buffQueue = readAccelerationByAxis(event.values[0], x_buffQueue);
                y_buffQueue = readAccelerationByAxis(event.values[1], y_buffQueue);
                z_buffQueue = readAccelerationByAxis(event.values[2], z_buffQueue);
            }
            if (broadcastCounter == BROADCAST_AXIS_SIZE) {
                readyToBroadcast();
            }
//            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//                String[] sensorDataRotRows =  readRotation(event);
//                for (String row : sensorDataRotRows) {
//                    sensorData.add(row);
//                }
//            }
            return 0;
        }

        /**
         * manages the buffer associated with the axis
         * adds to sensor data if the buffer is full
         * adds to buffer by pushing out the oldest element if the buffer is full
         *
         * @param sensorVal
         * @param bufferQ
         */
        private Queue<Float> readAccelerationByAxis(float sensorVal, Queue<Float> bufferQ) {
            if (bufferQ == null) {
                bufferQ = new ArrayDeque<>(windowSize);
            }
            if (bufferQ.size() == windowSize) {
                //save the existing buffer
                WindowBuffer mBuffer = new WindowBuffer(windowSize);
                for (Float buffVal : bufferQ) {
                    mBuffer.add(buffVal);
                }
                sensorData.add(mBuffer);
                broadcastCounter++;
//                readyToBroadcast();
                bufferQ.remove(); //remove the first/oldest element
            }

            //add the last/newest element
            bufferQ.add(sensorVal);
            return bufferQ;
        }


        //region RotationVector implementation deprecated for now

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

        //endregion

    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        Log.d(TAG, "showNotification is called");
        CharSequence text = "SensorLogger is running";

        Notification.Builder mBuilder = new Notification.Builder(this)
                        .setSmallIcon(R.drawable.abc_btn_radio_material)
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
