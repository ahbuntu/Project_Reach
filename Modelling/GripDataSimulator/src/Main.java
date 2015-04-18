import weka.classifiers.Classifier;
import weka.core.Instance;

import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Created by ahmadul.hassan on 2015-04-15.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        //load playback dataset
        String finalReportDir = "C:\\Users\\ahmadul.hassan\\Documents\\GitHub\\Project_Reach\\Modelling\\final report";
        System.out.println(finalReportDir);
        //use . to get current directory
        File dir = new File(finalReportDir);

//        File fin = new File(dir.getCanonicalPath() + File.separator + "reach1_weka_dataset.csv");
//        File fin = new File(dir.getCanonicalPath() + File.separator + "reach4_sanitized_all_featured_window_50.csv");
//        File fin = new File(dir.getCanonicalPath() + File.separator + "reach5_sanitized_all_featured_window_50.csv");
//        File fin = new File(dir.getCanonicalPath() + File.separator + "reach6_sanitized_all_featured_window_50.csv");
//        File fin = new File(dir.getCanonicalPath() + File.separator + "reach7_sanitized_all_featured_window_50.csv");
//        File fin = new File(dir.getCanonicalPath() + File.separator + "reach8_sanitized_all_featured_window_50.csv");
//        File fin = new File(dir.getCanonicalPath() + File.separator + "reach9_sanitized_all_featured_window_50.csv");
//        File fin = new File(dir.getCanonicalPath() + File.separator + "squeeze4_sanitized_all_featured_window_50.csv");
//        File fin = new File(dir.getCanonicalPath() + File.separator + "squeeze5_sanitized_all_featured_window_50.csv");
        File fin = new File(dir.getCanonicalPath() + File.separator + "squeeze6_sanitized_all_featured_window_50.csv");
        System.out.println(fin.getAbsoluteFile());
        DatasetLoader playbackDataset = new DatasetLoader();
        playbackDataset.readFile(fin);
        System.out.println(playbackDataset.evaluationDataSet.size());

        //load model
//        String modelName = "SMO_combined_1_2_3";
//        String modelName = "BayesNet_combined_1_2_3";
        String modelName = "J48_combined_123_r456";


        Classifier activeModel = WekaDemo.loadModel(dir, modelName);
//        System.out.println(activeModel.toString());

        WindowClassifyInstance wekaInstanceFactory = new WindowClassifyInstance();
        List<Double[]> evalDataset = playbackDataset.evaluationDataSet;
        int i = 0;
        int noneCount = 0; int squeezeCount = 0; int reachCount = 0;
        for (Double[] evalWindow : evalDataset) {
            i++;
            Instance wekaWindowInstance = wekaInstanceFactory.createWindowWekaInstance(evalWindow);
            try {
                double prediction = activeModel.classifyInstance(wekaWindowInstance);
                if (prediction == 0.0) {
                    noneCount++;
                    System.out.println("Prediction for item " + i + ": NONE");
                } else if (prediction == 1.0) {
                    squeezeCount++;
                    System.out.println("Prediction for item " + i + ": SQUEEZE");
                } else if (prediction == 2.0) {
                    reachCount++;
                    System.out.println("Prediction for item " + i + ": REACH");
                }
            } catch (Exception e) {
                System.out.println("hey somethinge went wrong here");
                e.printStackTrace();
            }
        }
        System.out.println("NONE count: " + noneCount);
        System.out.println("SQUEEZE count: " + squeezeCount);
        System.out.println("REACH count: " + reachCount);

//        Double[] windowVal = evalDataset.get(0);
//        System.out.println("DEBUG");
//        for (double val : windowVal) {
//            System.out.println(val);
//        }

    }
}
