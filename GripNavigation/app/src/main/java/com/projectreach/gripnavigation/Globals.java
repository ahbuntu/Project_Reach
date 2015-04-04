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

    public Classifier getActiveModel() {
        return Globals.activeModel;
    }

    public static void setActiveModel(Classifier activeModel) {
        Globals.activeModel = activeModel;
    }
}
