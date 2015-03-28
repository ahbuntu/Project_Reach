package com.projectreach.gripnavigation;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.core.converters.ConverterUtils.DataSource;


public class WekaTest extends ActionBarActivity
                        implements WekaHelper.WekaHelperListerner{

    private static final String TAG = "WekaTest";
    private static final String WEKA_DIRECTORY = "GripNavigation_Weka";
    private static String fileName = "";
    private static String modelName = "";

    private TextView crossSummary;
    private WekaHelper wekaHelper = null;


    private ArrayAdapter<String> mSavedModelAdapter;
    private AbsListView mListView;
    String[] savedFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weka_test);

        crossSummary = (TextView) findViewById(R.id.text_cross_summary);
        EditText edit_file_name = (EditText) findViewById(R.id.edit_file_name);
        if (edit_file_name.getText().toString().isEmpty()) {
            fileName = edit_file_name.getHint().toString();
        } else {
            fileName = edit_file_name.getText().toString().trim();
        }


        ArrayList<String> savedModelsList = readModelFileList();
        savedFiles = new String[savedModelsList.size()];
        savedFiles = savedModelsList.toArray(savedFiles);
        mSavedModelAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, savedFiles);

        mListView = (AbsListView) findViewById(R.id.list_saved_models);
        mListView.setAdapter(mSavedModelAdapter);
//        mListView.setOnItemClickListener(this);
    }

    public ArrayList<String> readModelFileList() {
        ArrayList<String> modelList = new ArrayList<>();
        try {
            File modelPath = getWekaDirectory(WekaTest.WEKA_DIRECTORY);
            File files[] = modelPath.listFiles();
            for (int i=0; i < files.length; i++)
            {
                String ext = fileName.substring((fileName.lastIndexOf(".") + 1), fileName.length());
                if (ext.equals("model")) {
                    Log.d(TAG, "Saved Model FileName:" + files[i].getName());
                    modelList.add(files[i].getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelList;
    }


    @Override
    protected void onResume() {
        super.onResume();

        Button button_weka_crossvalidate = (Button) findViewById(R.id.button_weka_crossvalidate);
        button_weka_crossvalidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExternalStorageWritable()) {
                    File outPath = getWekaDirectory(WekaTest.WEKA_DIRECTORY);
                    File dataFile = new File(outPath, WekaTest.fileName);
                    Log.d(TAG, "weka dataset file: " + dataFile.getAbsolutePath());
                    if (dataFile.exists()) {
                        wekaHelper = new WekaHelper(WekaTest.this, dataFile.getAbsolutePath());
                        wekaHelper.crossValidateModel();
                    } else {
                        Toast.makeText(WekaTest.this, "Dataset does not exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Button button_weka_save = (Button) findViewById(R.id.button_weka_save);
        button_weka_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wekaHelper == null) {
                    Toast.makeText(WekaTest.this, "You must train and crossvalidate first", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    File outPath = getWekaDirectory(WekaTest.WEKA_DIRECTORY);
                    WekaTest.modelName = generateModelNameToSave(WekaTest.fileName);
                    File dataFile = new File(outPath, WekaTest.modelName);
                    Log.d(TAG, "weka model name: " + dataFile.getAbsolutePath());
                    if (dataFile.exists()) {
                        Toast.makeText(WekaTest.this, "Model already exists. ABORT!", Toast.LENGTH_SHORT).show();
                    } else {
//                        wekaHelper = new WekaHelper(WekaTest.this);
                        wekaHelper.saveModel(dataFile.getAbsolutePath());
                    }
                }
            }
        });

        Button button_weka_load = (Button) findViewById(R.id.button_weka_load);
        button_weka_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    //TODO: need to fix this to make more generic load implementation
                    File outPath = getWekaDirectory(WekaTest.WEKA_DIRECTORY);
                    WekaTest.modelName = generateModelNameToSave(WekaTest.fileName);
                    File dataFile = new File(outPath, WekaTest.modelName);
                    Log.d(TAG, "weka model to load: " + dataFile.getAbsolutePath());
                if (wekaHelper == null) {
                    wekaHelper = new WekaHelper(WekaTest.this, dataFile.getAbsolutePath());
                }
                    if (dataFile.exists()) {
                        wekaHelper.loadModel(dataFile.getAbsolutePath());
                    } else {
                        Toast.makeText(WekaTest.this, "Model doesn't exist. ABORT!", Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

    /**
     * generates the name of the model based on the provided filename
     * @param datasetName
     * @return
     */
    private String generateModelNameToSave(String datasetName) {
        int pos = datasetName.lastIndexOf(".");
        if (pos > 0) {
            datasetName = datasetName.substring(0, pos);
        }
        datasetName = datasetName + ".model";
        return datasetName;
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

    public File getWekaDirectory(String dirName) {
        // Get the directory for the public documents directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), dirName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory already exists - not created");
        }
        return file;
    }

    @Override
    public void onWekaModelCrossValidated(String summary){
        crossSummary.setText(summary);
    }

    @Override
    public void onWekaModelSaved(){}

    @Override
    public void onWekaModelLoaded(Classifier model){
        if (model == null) {
            Log.d(TAG, "ruhroh");
        } else {
            Log.d(TAG, "retreived model: " + model.toString());
        }
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
