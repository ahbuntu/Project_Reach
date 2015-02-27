package com.projectreach.gripnavigation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout;


public class SchemeOneActivity extends Activity {

    LinearLayout layoutUI;
    private static int SLIDE_DISTANCE = 600;
    private static int ANIM_DURATION = 1000;
    /**
     * Called when the activity is first created.
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_one);
    }

    @Override
    protected  void onResume() {
        super.onResume();
        Button buttonStart = (Button) findViewById(R.id.dummy_button);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutUI = (LinearLayout) (findViewById(R.id.layout_scheme_one));
                SlidingLayout layoutAnim = new SlidingLayout(layoutUI);
                layoutAnim.setDuration(ANIM_DURATION);
                layoutUI.startAnimation(layoutAnim);
            }
        });
    }

    class SlidingLayout extends Animation
    {
        private int mViewHeight;
        private int mViewWidth;
        private int mParentHeight;
        private int mParentWidth;

        private int deltaHeight; // distance between start and end height
        private View mView;

        public SlidingLayout(View v) {
            mView = v;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//            layoutParams.setMargins(0, 100, 0, 0);
            layoutParams.topMargin = (int)(SLIDE_DISTANCE * interpolatedTime);
            mView.setLayoutParams(layoutParams);
//            mView.getLayoutParams().height = (int) (600 * interpolatedTime);

            mView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
//            initialHeight = actualHeight;
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

    }
}
