package com.projectreach.gripnavigation;

/**
 * Created by ahmadul.hassan on 2015-04-06.
 */
public class ActionPoint {
    public float x;
    public float y;

    ActionPoint(float new_x, float new_y) {
        x = new_x;
        y = new_y;
    }

    void normalize(float padLeft, float padRight, float width, float height) {
        x -= padLeft;
        y -= padRight;
        x /= width;
        y /= height;
    }

    void denormalize(float padLeft, float padRight, float width, float height) {
        x *= width;
        y *= height;
        x += padLeft;
        y += padRight;
    }

}
