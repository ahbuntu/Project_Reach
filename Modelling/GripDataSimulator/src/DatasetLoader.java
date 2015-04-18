import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ahmadul.hassan on 2015-04-15.
 */
public class DatasetLoader {

    private Double[] evaluationWindow;
    public List<Double[]> evaluationDataSet = new ArrayList<>();

    public void readFile(File fin) throws IOException {
        FileInputStream fis = new FileInputStream(fin);
        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        int lineCounter = 0;
        while ((line = br.readLine()) != null) {
            if (lineCounter != 0) {
                String[] columns = line.split(",");
                evaluationWindow = new Double[18];
                for (int i = 0; i < columns.length; i++) {
                    if (i != 0) {
                        evaluationWindow[i-1] = Double.parseDouble(columns[i]);
                    }
                }
                evaluationDataSet.add(evaluationWindow);
            }
            lineCounter++;
        }
        br.close();
    }


}
