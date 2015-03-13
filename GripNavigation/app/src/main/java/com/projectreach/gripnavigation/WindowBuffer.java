package com.projectreach.gripnavigation;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by ahmadul.hassan on 2015-03-12.
 */
public class WindowBuffer implements Parcelable {
    private int size = 20; //default
    private float[] sensorValues = null;
    int i = 0;

    public WindowBuffer(int windowSize) {
        size = windowSize;
        sensorValues = new float[size];
    }

    /**
     * adds a sensor value to the window buffer
     * @param value
     */
    public void add(float value) {
        sensorValues[i] = value;
        i++;
    }

    /**
     * returns the set of values for this window buffer
     * size corresponds to the size of the window
     *
     * @return
     */
    public float[] getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(float[] sensorValues) {
        this.sensorValues = sensorValues;
    }

    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloatArray(sensorValues);
    }

    public static final Parcelable.Creator<WindowBuffer> CREATOR = new Parcelable.Creator<WindowBuffer>() {
        public WindowBuffer createFromParcel(Parcel in) {
            return new WindowBuffer(in);
        }

        public WindowBuffer[] newArray(int size) {
            return new WindowBuffer[size];
        }
    };

    WindowBuffer(Parcel in) {
        setSensorValues(in.createFloatArray());
    }
}
