package com.projectreach.gripnavigation;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Filter;
import weka.core.converters.ConverterUtils.DataSource;


public class WekaTest extends ActionBarActivity
                        implements WekaHelper.WekaHelperListerner
                                    , AbsListView.OnItemClickListener  {

    private static final String TAG = "WekaTest";
    private static final String WEKA_DIRECTORY = "GripNavigation_Weka";
    private static String fileName = "";
    private static String modelName = "";
    private static Classifier activeModel = null;

    private TextView crossSummary;
    private WekaHelper wekaHelper = null;


    private ArrayAdapter<String> mSavedModelAdapter;
    private AbsListView mListView;
    private String[] savedFiles;
    private TextView textLoadModel;

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

        textLoadModel = (TextView) findViewById(R.id.text_load_model);
        ArrayList<String> savedModelsList = readModelFileList();
        savedFiles = new String[savedModelsList.size()];
        savedFiles = savedModelsList.toArray(savedFiles);
        mSavedModelAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, savedFiles);

        mListView = (AbsListView) findViewById(R.id.list_saved_models);
        mListView.setAdapter(mSavedModelAdapter);
        mListView.setOnItemClickListener(this);
    }

    public ArrayList<String> readModelFileList() {
        ArrayList<String> modelList = new ArrayList<>();
        try {
            File modelPath = getWekaDirectory(WekaTest.WEKA_DIRECTORY);
            File files[] = modelPath.listFiles();
            for (int i=0; i < files.length; i++)
            {
                String name = files[i].getName();
                String ext = name.substring((name.lastIndexOf(".") + 1), name.length());
                if (ext.equals("model")) {
                    Log.d(TAG, "Saved Model FileName:" + name);
                    modelList.add(files[i].getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        textLoadModel.setText(mListView.getItemAtPosition(position).toString());
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
                    File outPath = getWekaDirectory(WekaTest.WEKA_DIRECTORY);
//                    WekaTest.modelName = generateModelNameToSave(WekaTest.fileName);
                    WekaTest.modelName = textLoadModel.getText().toString();
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

        Button button_weka_evaluate_single = (Button) findViewById(R.id.button_weka_evaluate_single);
        button_weka_evaluate_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeModel == null) {
                    Toast.makeText(WekaTest.this, "Load model first. ABORT!", Toast.LENGTH_SHORT).show();
                } else {
                    //create instance to be classified
                    createAndTestInstance();
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
            Toast.makeText(this, "Error while trying to load the model", Toast.LENGTH_SHORT).show();
        } else {
            WekaTest.activeModel = model;
            Log.d(TAG, "retreived model: " + model.toString());
            Toast.makeText(this, "Model loaded", Toast.LENGTH_SHORT).show();
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

    public void createAndTestInstance() {
        try {
            // Declare the numeric attributes
            Attribute t1 = new Attribute("t1");
            Attribute t2 = new Attribute("t2");
            Attribute t3 = new Attribute("t3");
            Attribute t4 = new Attribute("t4");
            Attribute t5 = new Attribute("t5");
            Attribute t6 = new Attribute("t6");
            Attribute t7 = new Attribute("t7");
            Attribute t8 = new Attribute("t8");
            Attribute t9 = new Attribute("t9");
            Attribute t10 = new Attribute("t10");
            Attribute t11 = new Attribute("t11");
            Attribute t12 = new Attribute("t12");
            Attribute t13 = new Attribute("t13");
            Attribute t14 = new Attribute("t14");
            Attribute t15 = new Attribute("t15");
            Attribute t16 = new Attribute("t16");
            Attribute t17 = new Attribute("t17");
            Attribute t18 = new Attribute("t18");
            Attribute t19 = new Attribute("t19");
            Attribute t20 = new Attribute("t20");
            Attribute t21 = new Attribute("t21");

            // Declare the class attribute along with its values
            FastVector fvClassVal = new FastVector(2);
            fvClassVal.addElement("tap");
            fvClassVal.addElement("none");
            Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

            // Declare the feature vector
            FastVector fvWekaAttributes = new FastVector(4);

            fvWekaAttributes.addElement(t1);
            fvWekaAttributes.addElement(t2);
            fvWekaAttributes.addElement(t3);
            fvWekaAttributes.addElement(t4);
            fvWekaAttributes.addElement(t5);
            fvWekaAttributes.addElement(t6);
            fvWekaAttributes.addElement(t7);
            fvWekaAttributes.addElement(t8);
            fvWekaAttributes.addElement(t9);
            fvWekaAttributes.addElement(t10);
            fvWekaAttributes.addElement(t11);
            fvWekaAttributes.addElement(t12);
            fvWekaAttributes.addElement(t13);
            fvWekaAttributes.addElement(t14);
            fvWekaAttributes.addElement(t15);
            fvWekaAttributes.addElement(t16);
            fvWekaAttributes.addElement(t17);
            fvWekaAttributes.addElement(t18);
            fvWekaAttributes.addElement(t19);
            fvWekaAttributes.addElement(t20);
            fvWekaAttributes.addElement(t21);
            fvWekaAttributes.addElement(ClassAttribute);
//            fvWekaAttributes.addElement(new Attribute("@@class@@", classVal));

            Instances dataSet = new Instances("TestInstances", fvWekaAttributes, 0);
            dataSet.setClassIndex(21);
            Instance single_window;

            // Create instances for each pollutant with attribute values latitude,
            // longitude and pollutant itself
//            inst_co = new DenseInstance(data.numAttributes());
            single_window = new SparseInstance(dataSet.numAttributes());

            // // should classify as NONE (1.0)
            single_window.setValue(t1,9.270349);
            single_window.setValue(t2,9.270349);
            single_window.setValue(t3,9.270349);
            single_window.setValue(t4,9.346964);
            single_window.setValue(t5,9.346964);
            single_window.setValue(t6,9.270349);
            single_window.setValue(t7,9.270349);
            single_window.setValue(t8,9.193734);
            single_window.setValue(t9,9.270349);
            single_window.setValue(t10,9.270349);
            single_window.setValue(t11,9.346964);
            single_window.setValue(t12,9.346964);
            single_window.setValue(t13,9.346964);
            single_window.setValue(t14,9.270349);
            single_window.setValue(t15,9.270349);
            single_window.setValue(t16,9.270349);
            single_window.setValue(t17,9.270349);
            single_window.setValue(t18,9.270349);
            single_window.setValue(t19,9.193734);
            single_window.setValue(t20,9.11712);
            single_window.setValue(t21,9.11712);

            // should classify as TAP (0.0)
//            single_window.setValue(t1, 9.193734);
//            single_window.setValue(t2, 8.350976);
//            single_window.setValue(t3, 7.431602);
//            single_window.setValue(t4, 12.411542);
//            single_window.setValue(t5, 13.101071);
//            single_window.setValue(t6, 10.802638);
//            single_window.setValue(t7, 7.2783732);
//            single_window.setValue(t8, 4.2904096);
//            single_window.setValue(t9, 2.2218192);
//            single_window.setValue(t10, 2.2218192);
//            single_window.setValue(t11, 3.9839516);
//            single_window.setValue(t12, 6.3589997);
//            single_window.setValue(t13, 9.500193);
//            single_window.setValue(t14, 12.334928);
//            single_window.setValue(t15, 13.024457);
//            single_window.setValue(t16, 12.411542);
//            single_window.setValue(t17, 11.032481);
//            single_window.setValue(t18, 9.270349);
//            single_window.setValue(t19, 7.6614456);
//            single_window.setValue(t20, 6.588843);
//            single_window.setValue(t21, 6.205771);

            dataSet.add(single_window);
            single_window.setDataset(dataSet);
            // interpret as follows : 0.0 = tap, 1.0 = none
            double result = activeModel.classifyInstance(single_window);
            Log.d(TAG, "classified as " + result);
            Toast.makeText(this, "Weka classified as : " + result, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d(TAG, "like you're really suprised");
            e.printStackTrace();
        }
    }
    private class WindowClassify {


    }
}
