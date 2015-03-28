package com.projectreach.gripnavigation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmadul.hassan on 2015-03-28.
 */
public final class FeatureExtractor {
    private FeatureExtractor() {
        //prevents instantiation
    }


    /**
     * calculates the mean of the provided array
     * @param array
     * @return
     */
    private static float getMean(float[] array) {
        float sum = 0;
        for (int i=0; i < array.length; i++) {
            sum += array[i]; //calculates the sum of the array
        }
        return (sum/array.length);
    }
    public static List<Float> calculateMean(List<WindowBuffer> sensorValues) {
        List<Float> meanValues = new ArrayList<>(sensorValues.size());
        for (WindowBuffer window : sensorValues) {
            //iterating over each sensor's window
            meanValues.add(getMean(window.getSensorValues()));
        }
        return meanValues;
    }

    public static List<Float> calculateSquaredMean(List<WindowBuffer> sensorValues) {
        List<Float> meanValues = new ArrayList<>(sensorValues.size());
        for (WindowBuffer window : sensorValues) {
            //iterating over each sensor's window
            float[] windowValues = window.getSensorValues();
            for (int i=0; i < windowValues.length; i++) {
                windowValues[i] = windowValues[i] * windowValues[i];
            }
            meanValues.add(getMean(windowValues));
        }
        return meanValues;
    }

    public static List<Float> calculateVariance(List<WindowBuffer> sensorValues) {
        List<Float> varValues = new ArrayList<>(sensorValues.size());
        for (WindowBuffer window : sensorValues) {
            //iterating over each sensor's window
            float[] windowValues = window.getSensorValues();
            float mean = getMean(windowValues);
            for (int i=0; i < windowValues.length; i++) {
                float diff = windowValues[i] - mean;
                windowValues[i] = diff * diff;
            }
            varValues.add(getMean(windowValues));
        }
        return varValues;
    }
}
