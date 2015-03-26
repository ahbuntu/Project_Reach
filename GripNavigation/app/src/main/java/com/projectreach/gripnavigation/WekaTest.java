package com.projectreach.gripnavigation;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.core.converters.ConverterUtils.DataSource;


public class WekaTest extends ActionBarActivity {
    private static final String TAG = "WekaTest";
    private static final String WEKA_DIRECTORY = "GripNavigation_Weka";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weka_test);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isExternalStorageWritable()) {
            File outPath = getWekaDirectory(this, WEKA_DIRECTORY);
            File dataFile = new File(outPath, "testRun.arff");
            Log.d(TAG, "weka dataset file: " + dataFile.getAbsolutePath());
            if (dataFile.exists()) {
                WekaHelper wekaHelper = new WekaHelper(this, dataFile.getAbsolutePath());
                wekaHelper.crossValidateModel();
            } else {
                Toast.makeText(this, "Dataset does not exist", Toast.LENGTH_SHORT).show();
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

    public File getWekaDirectory(Context context, String dirName) {
        // Get the directory for the public documents directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), dirName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory already exists - not created");
        }
        return file;
    }

    //region options menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weka_test, menu);
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
