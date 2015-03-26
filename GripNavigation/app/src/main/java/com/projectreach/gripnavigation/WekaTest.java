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
            File dataFile = new File(outPath, "testRun.csv");
            Log.d(TAG, "weka dataset file: " + dataFile.getAbsolutePath());
            if (dataFile.exists()) {
                runWekaTest(dataFile);
            } else {
                Toast.makeText(this, "Dataset does not exist", Toast.LENGTH_SHORT).show();
            }

        }


    }

    private void runWekaTest(File dataFile) {

        /*
        * WEKA Android libraries -
        * [1] https://www.pervasive.jku.at/Teaching/lvaInfo.php?key=346&do=uebungen (THIS IS USED)
        * [2] https://github.com/rjmarsan/Weka-for-Android
         */
        try {
            DataSource source = new DataSource(dataFile.getAbsolutePath());
            Instances data = source.getDataSet();

            // setting class attribute if the data format does not provide this information
            // For example, the XRFF format saves the class attribute information as well
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            // set the classifier
            Classifier cls = (Classifier) new SMO();


            // other options
            int seed = 12;
            int folds = 2;


            // randomize data
            Random rand = new Random(seed);
            Instances randData = new Instances(data);
            randData.randomize(rand);
            if (randData.classAttribute().isNominal())
                randData.stratify(folds);

            // perform cross-validation
            Evaluation eval = new Evaluation(randData);
            for (int n = 0; n < folds; n++) {
                Instances train = randData.trainCV(folds, n);
                Instances test = randData.testCV(folds, n);
                // the above code is used by the StratifiedRemoveFolds filter, the
                // code below by the Explorer/Experimenter:
                // Instances train = randData.trainCV(folds, n, rand);

                // build and evaluate classifier
                Classifier clsCopy = Classifier.makeCopy(cls);
                clsCopy.buildClassifier(train);
//                cls.buildClassifier(train);
                eval.evaluateModel(cls, test);
            }

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
            Log.e(TAG, "Directory not created");
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
