package com.projectreach.gripnavigation;

import android.content.Context;
import android.hardware.SensorEvent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    public WekaHelper(Context context, String filePath) {
        mContext = context;
        mListener = (WekaHelperListerner) mContext;

        mFilePath = filePath;
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
    private class WekaCrossValidateModel extends AsyncTask <String, Integer, Integer> {
        private static final String TAG = "WekaCrossValidateModel";
        @Override
        protected Integer doInBackground(String... filePaths) {
            String dataSetPath = filePaths[0];
            Log.d(TAG, "running in background for file @ " + dataSetPath);
            runWekaTest(dataSetPath);
            return 0;
        }

        private void runWekaTest(String dataSetPath) {
        /*
        * WEKA Android libraries -
        * [1] https://www.pervasive.jku.at/Teaching/lvaInfo.php?key=346&do=uebungen (THIS IS USED)
        * [2] https://github.com/rjmarsan/Weka-for-Android
         */
            try {
//                DataSource source = new DataSource(dataSetPath);
//                Instances data = source.getDataSet();

                BufferedReader breader = new BufferedReader(new FileReader(dataSetPath));
                Instances data = new Instances(breader);

                // setting class attribute if the data format does not provide this information
                // For example, the XRFF format saves the class attribute information as well
                if (data.classIndex() == -1)
                    data.setClassIndex(data.numAttributes() - 1);

                // set the classifier
                Classifier cls = (Classifier) new SMO();

                // other options
                int seed = 12;
                int folds = 2;

                cls.buildClassifier(data);
                Evaluation eval = new Evaluation(data);
                Random rand = new Random(seed);
                eval.crossValidateModel(cls,data,folds,rand);

                // output evaluation
                Log.d(TAG, "=== Setup ===");
                Log.d(TAG, "Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
                Log.d(TAG, "Dataset: " + data.relationName());
                Log.d(TAG, "Folds: " + folds);
                Log.d(TAG, "Seed: " + seed);
                Log.d(TAG, eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }
}
