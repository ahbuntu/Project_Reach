package com.projectreach.gripnavigation;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class SchemeOneActivity extends Activity
                                implements SchemeOverlayImageView.TapListener{

    LinearLayout layoutUI;
    private static int SLIDE_DISTANCE = 600;
    private static int ANIM_DURATION = 1000;

//    ImageView imageUI;
    SchemeOverlayImageView imageUI;
    private static final int MAX_POINTS = 5;
    List<ActionPoint> pointsToDraw = new ArrayList<>();
    private int mPointIdx = -1; //The index of the selected point on the image

    /**
     * Called when the activity is first created.
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_one);
//        imageUI = (ImageView) findViewById(R.id.image_scheme1);
        imageUI = (SchemeOverlayImageView) findViewById(R.id.image_scheme1);
        imageUI.setImageBitmap(
                decodeSampledBitmapFromResource(getResources(), R.drawable.scheme1, 300, 500));
        imageUI.setTapListener(this);

        int xMax = 822;
        int yMax = 1416;
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        int randomX, randomY;


        for (int i =0; i<MAX_POINTS; i++) {
            randomX = rand.nextInt(xMax);
            randomY = rand.nextInt(yMax);
        }
        ActionPoint p0 = new ActionPoint(100, 50);
        pointsToDraw.add(p0);
        ActionPoint p1 = new ActionPoint(850, 250);
        pointsToDraw.add(p1);
        ActionPoint p2 = new ActionPoint(250, 666);
        pointsToDraw.add(p2);
        ActionPoint p3 = new ActionPoint(80, 1600);
        pointsToDraw.add(p3);
        ActionPoint p4 = new ActionPoint(600, 1450);
        pointsToDraw.add(p4);
        // pointsToDraw is the location of the regions of interest on the image
        // A circle is drawn at each point in the list, which can then be selected by tapping
        imageUI.setPointList(pointsToDraw);
        imageUI.invalidate();
    }

    @Override
    protected  void onResume() {
        super.onResume();
//        Button buttonStart = (Button) findViewById(R.id.dummy_button);
//        buttonStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                resizeLayout();
//            }
//        });
}

    /*
     * Callback from region overlay when tapped
     */
    @Override
    public void onTap(float x, float y) {
        //Are we near a region?
        mPointIdx = imageUI.findPointIndex(x, y);

        imageUI.clearSelection();
        if(mPointIdx != -1) {
            //Found a close region, visually mark it
            imageUI.addSelection(mPointIdx);
        }
        imageUI.invalidate(); //Re-draw
    }

    private void resizeLayout() {
        layoutUI = (LinearLayout) (findViewById(R.id.layout_scheme_one));
        SlidingLayout layoutAnim = new SlidingLayout(layoutUI);
        layoutAnim.setDuration(ANIM_DURATION);
        layoutUI.startAnimation(layoutAnim);
    }


    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
