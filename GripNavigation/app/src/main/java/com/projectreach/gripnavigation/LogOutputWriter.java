package com.projectreach.gripnavigation;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmadul.hassan on 2015-03-21.
 */
public class LogOutputWriter extends AsyncTask<List<WindowBuffer>, Integer, Integer> {
    private static final String TAG = "LogOutputWriter";
    private static final String LOG_DIRECTORY = "GripNavigation_Logs";
    private int numberOfAxis = 16; //by default
    Context mContext;

    File logFile;
    FileOutputStream f;
    PrintWriter pw;

    public LogOutputWriter(Context context, int axisCount) {
        mContext = context;
        numberOfAxis = axisCount;
    }
    @Override
    protected Integer doInBackground(List<WindowBuffer>... values) {
        if (logFile != null) {
            writeToLogFile(logFile, values[0]);
        }
        return 0;
    }

    public void logValuesToFile(List<WindowBuffer> values) {
        List<WindowBuffer> valuesToLog = new ArrayList<WindowBuffer>(values);
        writeToLogFile(logFile, valuesToLog);
    }

    public void initialize() {
        if (isExternalStorageWritable()) {
            File outPath = getLogOutputStorageDir(mContext, LOG_DIRECTORY);
            logFile = new File(outPath, "log.txt");
            Log.d(TAG, "log file written to: " + logFile.getAbsolutePath());
            try {
                f = new FileOutputStream(logFile, true); //append file
                pw = new PrintWriter(f);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(TAG, "******* File not found. Did you" +
                        " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
            }
        }
    }

    public void cease() {
        if (f!= null) {
            try {
                f.close();
                pw.flush();
                pw.close();
            } catch (IOException e) {
                Log.d(TAG, "error trying to close the file");
                e.printStackTrace();
            }
        }
    }

    /**
    * Checks if external storage is available for read and write
    *
    * */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     *
     * */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getLogOutputStorageDir(Context context, String dirName) {
            // Get the directory for the public documents directory.
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), dirName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /** Method to write ascii text characters to file on SD card. Note that you must add a
     WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     a FileNotFound Exception because you won't have write permission. */

    private void writeToLogFile(File outFile, List<WindowBuffer> outBuffer){

        try {
            int sensorIndex = 0;
            for (WindowBuffer sensorWindow : outBuffer) {
                StringBuilder outputLine = new StringBuilder();
                outputLine.append(sensorIndex).append(",");
                float[] sensorValues = sensorWindow.getSensorValues();
                for (int i = 0; i < sensorValues.length; i++) {
                    outputLine.append(sensorValues[i]).append(",");
                }
                if ((sensorIndex % numberOfAxis) == 0) {
                    sensorIndex = 0; //the values will start to repeat after this
                } else {
                    sensorIndex++;
                }
                outputLine.append(System.currentTimeMillis()).append('\n');
                pw.println(outputLine.toString());
                Log.d(TAG, "DOUBLE TIME now : " + outputLine.toString());
            }
        } catch (Exception e ) {
            Log.d(TAG, "WTF exception");
            e.printStackTrace();
        }
    }
}
