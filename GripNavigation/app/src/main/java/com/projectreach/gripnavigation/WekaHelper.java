package com.projectreach.gripnavigation;

import android.content.Context;
import android.hardware.SensorEvent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Created by ahmadul.hassan on 2015-03-26.
 */
public class WekaHelper {
    private static final String TAG = "WekaHelper";


    public interface WekaHelperListerner {
        public void onWekaModelCrossValidated(String summary);
        public void onWekaModelSaved();
        public void onWekaModelLoaded();
    }
    private WekaHelperListerner mListener = null;

    private String mFilePath;
    private Context mContext;

    private Classifier modelCls = null;

    public WekaHelper(Context context, String crossvalidationDataPath) {
        mContext = context;
        mListener = (WekaHelperListerner) mContext;

        mFilePath = crossvalidationDataPath;
    }

    public WekaHelper(Context context) {
        mContext = context;
        mListener = (WekaHelperListerner) mContext;
    }

    public void crossValidateModel() {
        Log.d(TAG, "about to execute background crossvalidation");
        File dataFile = new File(mFilePath);
        if (dataFile.exists()) {
            WekaCrossValidateModel weka = new WekaCrossValidateModel();
            weka.execute(mFilePath);
        } else {
            Toast.makeText(mContext, "There is no file specified", Toast.LENGTH_SHORT).show();
        }
    }

    private void crossValidateModel(String filePath) {
        Log.d(TAG, "about to execute background crossvalidation");
        File dataFile = new File(filePath);
        if (dataFile.exists()) {
            WekaCrossValidateModel weka = new WekaCrossValidateModel();
            weka.execute(filePath);
        } else {
            Toast.makeText(mContext, "There is no file specified", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveModel(String filePath) {
        Log.d(TAG, "about to execute savemodel");
        File dataFile = new File(filePath);
        if (dataFile.exists()) {
            Toast.makeText(mContext, "Model already exists. ABORT!", Toast.LENGTH_SHORT).show();
        } else {
            WekaSaveModel weka = new WekaSaveModel();
            weka.execute(filePath);
        }
    }

    private class WekaCrossValidateModel extends AsyncTask <String, Integer, String> {
        private static final String TAG = "WekaCrossValidateModel";
        @Override
        protected String doInBackground(String... filePaths) {
            String dataSetPath = filePaths[0];
            Log.d(TAG, "running in background for file @ " + dataSetPath);
            return runWekaTest(dataSetPath);
        }

        private String runWekaTest(String dataSetPath) {
            /*
            * WEKA Android libraries -
            * [1] https://www.pervasive.jku.at/Teaching/lvaInfo.php?key=346&do=uebungen (THIS IS USED)
            * [2] https://github.com/rjmarsan/Weka-for-Android
             */

            StringBuilder crossSummary = new StringBuilder();
            // set the classifier
            Classifier cls = (Classifier) new SMO();

            try {
//                DataSource source = new DataSource(dataSetPath);
//                Instances data = source.getDataSet();

                BufferedReader breader = new BufferedReader(new FileReader(dataSetPath));
                Instances data = new Instances(breader);

                // setting class attribute if the data format does not provide this information
                // For example, the XRFF format saves the class attribute information as well
                if (data.classIndex() == -1)
                    data.setClassIndex(data.numAttributes() - 1);


                // other options
                int seed = 12;
                int folds = 2;

                cls.buildClassifier(data);
                Evaluation eval = new Evaluation(data);
                Random rand = new Random(seed);
                eval.crossValidateModel(cls,data,folds,rand);

                // output evaluation
//                Log.d(TAG, "=== Setup ===");
//                Log.d(TAG, "Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
//                Log.d(TAG, "Dataset: " + data.relationName());
//                Log.d(TAG, "Folds: " + folds);
//                Log.d(TAG, "Seed: " + seed);
//                Log.d(TAG, eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));

                crossSummary.append("=== Setup ===").append("\n");
                crossSummary.append("Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions())).append("\n");
                crossSummary.append("Dataset: " + data.relationName()).append("\n");
                crossSummary.append("Folds: " + folds).append("\n");
                crossSummary.append("Seed: " + seed).append("\n");
                crossSummary.append(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false)).append("\n");

            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                crossSummary.append(e.getMessage()).append("\n");
            }

            modelCls = cls;
            return crossSummary.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            mListener.onWekaModelCrossValidated(result);
        }
    }

    private class WekaSaveModel extends AsyncTask <String, Integer, Classifier> {
        private static final String TAG = "WekaSaveModel";
        private File mStorePath;

        @Override
        protected Classifier doInBackground(String... filePaths) {
            String dataSetPath = filePaths[0];
            Log.d(TAG, "saving model for file @ " + dataSetPath);
            if (modelCls == null) {
                //need to crossvalidate first
                crossValidateModel();
            }

            mStorePath = new File(dataSetPath);
            try {
                saveModel(modelCls, mStorePath);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
            return modelCls;
        }

        private void saveModel(Classifier c, File targetPath) throws Exception {
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(
                        new FileOutputStream(targetPath));
//                        new FileOutputStream("/weka_models/" + name + ".model"));

            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            oos.writeObject(c);
            oos.flush();
            oos.close();
        }

        @Override
        protected void onPostExecute(Classifier result) {
        }
    }
}
