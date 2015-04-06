package com.projectreach.gripnavigation;

import android.app.Notification;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ahmadul.hassan on 2015-04-06.
 */
public class SchemeOverlayImageView extends ImageView {
    private String TAG = "SchemeOverlayImageView";
    private Paint mCirclePaint;
    private Paint mSelectedPaint;
    private List<ActionPoint> mPointList;
    private Set<Integer> mSelection;
    private static float mSelectionMargin = 1.25f;
    private static float mDiameterImgFrac = 0.15f;

    private RectF imagePos = new RectF();

    private TapListener mTapListener;

    public interface TapListener {
        public void onTap(float x, float y);
    }


    public SchemeOverlayImageView(Context context) {
        super(context);

        init();
    }

    public SchemeOverlayImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void setTapListener(TapListener listener) {
        mTapListener = listener;
    }

    public void setPointList(List<ActionPoint> pointList) {
        mPointList = pointList;
    }

    public List<ActionPoint> getPointList() {
        return mPointList;
    }

    public void addPoint(float x, float y) {
        ActionPoint p = new ActionPoint(x, y);
    }

    public int findPointIndex(float x, float y) {

        for(int i = 0; i < mPointList.size(); i++) {
            ActionPoint pnt = mPointList.get(i);
            float xdist = x - pnt.x;
            float ydist = y - pnt.y;
            float xpow = (float) Math.pow(xdist, 2);
            float ypow = (float) Math.pow(ydist, 2);
            float dist = (float) Math.sqrt( xpow + ypow );
//            Log.d(TAG, String.format("Checking point %d dist %f", i, dist));

            if(dist < mSelectionMargin*(getDiameterBmp()/2.0)) {
                Log.d(TAG, String.format("Found overlapping point at index %d", i));
                return i;
            }
        }
        Log.d(TAG, String.format("Found no overlapping point"));
        return -1; //Invalid
    }

    public void clearSelection() {
        mSelection.clear();
    }

    public void addSelection(int idx) {
        mSelection.add(new Integer(idx));
    }

    private void init() {
        mPointList = new ArrayList<ActionPoint>();
        mSelection = new HashSet<Integer>();

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.RED);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5,5}, 0.0f);
        mCirclePaint.setPathEffect(dashPathEffect);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(5);

        mSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectedPaint.setColor(Color.GREEN);
        mSelectedPaint.setPathEffect(dashPathEffect);
        mSelectedPaint.setStyle(Paint.Style.STROKE);
        mSelectedPaint.setStrokeWidth(5);

    }

    @Override
    protected void onSizeChanged(int w, int h , int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop()  + getPaddingBottom());

        float padded_w = w - xpad;
        float padded_h = h - ypad;


    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Draw image first
        super.onDraw(canvas);

        //Draw after image
        if (mPointList != null) {
            for (Integer i = 0; i < mPointList.size(); i++) {
                ActionPoint pnt = mPointList.get(i);
                float[] bmp_coords = new float[] {pnt.x, pnt.y};

                float[] screen_coords = getScreenCoords(bmp_coords);

//                Log.d(TAG, String.format("Drawing Circle %d at Pnt (%f,%f) BMP (%f,%f), Screen (%f,%f)", i, pnt.x, pnt.y, bmp_coords[0], bmp_coords[1], screen_coords[0], screen_coords[1]));
                if(mSelection.contains(i)) {
                    canvas.drawCircle(screen_coords[0], screen_coords[1], getDiameterScreen() / 2, mSelectedPaint);
                } else {
                    canvas.drawCircle(screen_coords[0], screen_coords[1], getDiameterScreen() / 2, mCirclePaint);
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //User tapped the preview
            float x = event.getX();
            float y = event.getY();

            float[] bitmap_coords = getBitmapCoords(new float[] {x, y});
            float[] screen_coords = getScreenCoords(bitmap_coords);

            Log.d(TAG, String.format("User tapped overlay at (%f,%f), BMP (%f,%f) Screen (%f,%f)", x, y, bitmap_coords[0], bitmap_coords[1], screen_coords[0], screen_coords[1]));
            if(mTapListener != null) {

                mTapListener.onTap(bitmap_coords[0], bitmap_coords[1]);
            }
        }

        return super.onTouchEvent(event);
    }

    private float[] getBitmapCoords(float[] in_coords) {
        //From https://stackoverflow.com/questions/4933612/how-to-convert-coordinates-of-the-image-view-to-the-coordinates-of-the-bitmap
        final float[] coords = new float[] { in_coords[0], in_coords[1] };
        Matrix matrix = new Matrix();
        getImageMatrix().invert(matrix);
        matrix.postTranslate(getScrollX(), getScrollY());
        matrix.mapPoints(coords);
        return coords;
    }

    private float[] getScreenCoords(float[] in_coords) {
        float[] coords = new float[] {in_coords[0], in_coords[1]};
        Matrix matrix = getImageMatrix();
        matrix.mapPoints(coords);
        return coords;
    }

    private float getDiameterBmp() {
        float bmpDiameter = Math.min(mDiameterImgFrac*getDrawable().getIntrinsicWidth(), mDiameterImgFrac*getDrawable().getIntrinsicHeight());
        Log.d(TAG, "diameter of bitmap to draw : " + bmpDiameter);
        return bmpDiameter;
    }

    private float getDiameterScreen() {
        float dim_bmp = getDiameterBmp();
        float[] dim_screen = getScreenCoords(new float[] {dim_bmp, dim_bmp});

        return Math.min(dim_screen[0], dim_screen[1]);
    }
}

