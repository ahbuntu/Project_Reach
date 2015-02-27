package com.projectreach.gripnavigation;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


public class SchemeTwoActivity extends Activity {
    private static float SHRINK_RATIO = 1.25f;
    private static int ANIM_DURATION = 1000;
    private LinearLayout layoutUI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_two);
    }


    @Override
    protected  void onResume() {
        super.onResume();
        Button buttonStart = (Button) findViewById(R.id.dummy_button);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutUI = (LinearLayout) (findViewById(R.id.layout_scheme_two));

                DisplayMetrics displayMetrics = v.getContext().getResources().getDisplayMetrics();
                float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
                float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

                ShrinkingLayout layoutAnim = new ShrinkingLayout(layoutUI, dpWidth, dpHeight);
                layoutAnim.setDuration(ANIM_DURATION);
                layoutUI.startAnimation(layoutAnim);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scheme_two, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ShrinkingLayout extends Animation
    {
        private float mViewWidth;
        private float mViewHeight;
        private float mParentHeight;
        private float mParentWidth;

        private View mView;

        public ShrinkingLayout(View v, float screenWidth, float screenHeight) {
            mView = v;
            mViewWidth = mView.getWidth();
            mViewHeight = mView.getHeight();
            mParentWidth = screenWidth;
            mParentHeight = screenHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            int deltaWidth = (int) (mViewWidth - (mViewWidth / SHRINK_RATIO));
            int deltaHeight = (int) (mViewHeight - (mViewHeight / SHRINK_RATIO));

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.topMargin = (int)(deltaHeight * interpolatedTime);
            layoutParams.leftMargin = (int)(deltaWidth * interpolatedTime);
            mView.setLayoutParams(layoutParams);

            mView.getLayoutParams().width = (int) (mViewWidth - deltaWidth * interpolatedTime);
            mView.getLayoutParams().height = (int) (mViewHeight - deltaHeight * interpolatedTime);

            mView.requestLayout();

//            EditText editSearch = (EditText) mView.getContext().fin
        }

        /**
         * initializes the animation so it know what dimensions to toggle between
         * @param width represents layoutWidth
         * @param height represents layoutHeight
         * @param parentWidth represents screenWidth
         * @param parentHeight represents screenHeight
         */
        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
//            mViewWidth = width;
//            mViewHeight = height;
//            mParentWidth = parentWidth;
//            mParentHeight = parentHeight;
//
//            Configuration configuration = yourActivity.getResources().getConfiguration();
//            int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
//            int smallestScreenWidthDp = configuration.smallestScreenWidthDp; //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

    }
}
