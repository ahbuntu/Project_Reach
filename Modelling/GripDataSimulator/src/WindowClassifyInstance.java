
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Created by ahmadulhassan on 2015-04-04.
 */
public class WindowClassifyInstance {

    Instance single_window;
    Instances dataSet;

    public WindowClassifyInstance() {}

    private FastVector getGripInstanceAttributes() {
        // Declare the numeric attributes
        Attribute mean_left = new Attribute("meanLeft");
        Attribute mean_right = new Attribute("meanRight");
        Attribute var_s1 = new Attribute("varS1");
        Attribute var_s2 = new Attribute("varS2");
        Attribute var_s3 = new Attribute("varS3");
        Attribute var_s4 = new Attribute("varS4");
        Attribute var_s5 = new Attribute("varS5");
        Attribute var_s6 = new Attribute("varS6");
        Attribute var_s7 = new Attribute("varS7");
        Attribute var_s8 = new Attribute("varS8");
        Attribute var_s9 = new Attribute("varS9");
        Attribute var_s10 = new Attribute("varS10");
        Attribute var_s11 = new Attribute("varS11");
        Attribute var_s12 = new Attribute("varS12");
        Attribute var_s13 = new Attribute("varS13");
        Attribute var_s14 = new Attribute("varS14");
        Attribute var_s15 = new Attribute("varS15");
        Attribute var_s16 = new Attribute("varS16");

        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(3);
        fvClassVal.addElement("none");
        fvClassVal.addElement("squeeze");
        fvClassVal.addElement("reach");
        Attribute classAttribute = new Attribute("theClass", fvClassVal);

        // Declare the feature vector
        FastVector fvWekaAttributes = new FastVector(19);

        fvWekaAttributes.addElement(mean_left);
        fvWekaAttributes.addElement(mean_right);
        fvWekaAttributes.addElement(var_s1);
        fvWekaAttributes.addElement(var_s2);
        fvWekaAttributes.addElement(var_s3);
        fvWekaAttributes.addElement(var_s4);
        fvWekaAttributes.addElement(var_s5);
        fvWekaAttributes.addElement(var_s6);
        fvWekaAttributes.addElement(var_s7);
        fvWekaAttributes.addElement(var_s8);
        fvWekaAttributes.addElement(var_s9);
        fvWekaAttributes.addElement(var_s10);
        fvWekaAttributes.addElement(var_s11);
        fvWekaAttributes.addElement(var_s12);
        fvWekaAttributes.addElement(var_s13);
        fvWekaAttributes.addElement(var_s14);
        fvWekaAttributes.addElement(var_s15);
        fvWekaAttributes.addElement(var_s16);
        fvWekaAttributes.addElement(classAttribute);

        return fvWekaAttributes;
    }

    public Instance createWindowWekaInstance(Double[] windowArray) {
        FastVector instanceAttributes = getGripInstanceAttributes();
        dataSet = new Instances("WindowWekaInstance", instanceAttributes, 0);
        dataSet.setClassIndex(18);

        single_window = new SparseInstance(dataSet.numAttributes());

        for (int i=0; i < windowArray.length; i++) {
            single_window.setValue((Attribute) instanceAttributes.elementAt(i),windowArray[i]);
        }

        dataSet.add(single_window);
        single_window.setDataset(dataSet);

        return single_window;
    }
}

