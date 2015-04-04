package com.projectreach.gripnavigation;

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

    public WindowClassifyInstance() {

    }

    private FastVector getAccInstanceAttributes() {
        // Declare the numeric attributes
        Attribute x_mean = new Attribute("x-axis mean");
        Attribute x_var = new Attribute("x-axis var");
        Attribute y_mean = new Attribute("y-axis mean");
        Attribute y_var = new Attribute("y-axis var");
        Attribute z_mean = new Attribute("z-axis mean");
        Attribute z_var = new Attribute("z-axis var");

        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("none");
        fvClassVal.addElement("tap");
        Attribute classAttribute = new Attribute("theClass", fvClassVal);

        // Declare the feature vector
        FastVector fvWekaAttributes = new FastVector(4);

        fvWekaAttributes.addElement(x_mean);
        fvWekaAttributes.addElement(x_var);
        fvWekaAttributes.addElement(y_mean);
        fvWekaAttributes.addElement(y_var);
        fvWekaAttributes.addElement(z_mean);
        fvWekaAttributes.addElement(z_var);
        fvWekaAttributes.addElement(classAttribute);

        return fvWekaAttributes;
    }

    public Instance getAccInstance(float[] windowArray) {
        FastVector instanceAttributes = getAccInstanceAttributes();
        dataSet = new Instances("AccWindowInstance", instanceAttributes, 0);
        dataSet.setClassIndex(6);

        single_window = new SparseInstance(dataSet.numAttributes());

        for (int i=0; i < windowArray.length; i++) {
            single_window.setValue((Attribute) instanceAttributes.elementAt(i),windowArray[i]);
        }

        dataSet.add(single_window);
        single_window.setDataset(dataSet);

        return single_window;
    }
}

