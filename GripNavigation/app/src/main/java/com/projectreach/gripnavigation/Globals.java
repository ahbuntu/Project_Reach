package com.projectreach.gripnavigation;

import weka.classifiers.Classifier;

/**
 * Created by ahmadul.hassan on 2015-03-12.
 */
public class Globals {
    private static Globals instance;

    //restrict the constructor from being initialized
    private Globals() {}
    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }

    public static final String BROADCAST_ACTION = "com.projectreach.gripnavigation.SENSOR_BROADCAST";
    public static final String ARG_SENSOR_VAL = "SENSOR_VALUES";
    public static final String ARG_WINDOW_SIZE = "WINDOW_SIZE";
    public static final String ARG_ACTIVE_MODE = "ACTIVE_MODEL";

    public static final int ACTIVE_MODEL_ASSIGNED = 201;

    private static Classifier activeModel = null;

    public enum Grip_Pattern {
        NONE, GRIP, REACH
    }

    public enum Tap_Pattern {
        NONE, MOTION, TAP
    }

    public static Tap_Pattern tapAsEnum(double val) {
        if (val == 0.0) {
            return Tap_Pattern.NONE;
        } else if (val == 1.0) {
            return Tap_Pattern.MOTION;
        } else if (val == 2.0) {
            return Tap_Pattern.TAP;
        } else {
            return Tap_Pattern.NONE;
        }
    }

    public static float tapAsFloat(Tap_Pattern val) {
        if (val == Tap_Pattern.NONE) {
            return 0.0f;
        } else if (val == Tap_Pattern.MOTION) {
            return 1.0f;
        } else if (val == Tap_Pattern.TAP) {
            return 2.0f;
        } else {
            return 0.0f;
        }
    }

    public Classifier getActiveModel() {
        return Globals.activeModel;
    }

    public static void setActiveModel(Classifier activeModel) {
        Globals.activeModel = activeModel;
    }
}
