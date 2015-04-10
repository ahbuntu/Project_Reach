package com.projectreach.gripnavigation;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by ahmadulhassan on 2015-04-09.
 */
public class TapEvaluation {
    private static final String TAG = "TapEvaluation";

    public interface TapEvaluatedListener {
        public void onTapEvaluated();
    }
    public TapEvaluatedListener mListener;

    private static TapEvaluation instance;

    private static int evaluationWindow = 25;
    private static Queue<Float> evaluationQueue;

    //restrict the constructor from being initialized
    private TapEvaluation() {}
    public static synchronized TapEvaluation getInstance() {
        if (instance == null) {
            instance = new TapEvaluation();
            evaluationQueue = new ArrayDeque<>();
        }
        return instance;
    }

    public void registerTapEvaluatedListener(TapEvaluatedListener listener) {
        mListener = listener;
    }

    public void evaluateIfTap(Globals.Tap_Pattern prediction) {
        if (evaluationQueue.size() < evaluationWindow) {
            //no point of evaluating now - just append
            evaluationQueue.add(Globals.tapAsFloat(prediction));
        } else {
            //time to evaluate
            performEvaluation();
            //guaranteed that at least 1 element has been removed from the queue
            evaluationQueue.add(Globals.tapAsFloat(prediction));
        }
    }

    private void performEvaluation() {
        int numPredictedTap = 0;
        int numPredictedMotion = 0;
        int numPredictedNone = 0;

        for (float predictedVal : evaluationQueue) {
            if (Globals.tapAsEnum(predictedVal) == Globals.Tap_Pattern.NONE) {
                numPredictedNone++;
            } else if (Globals.tapAsEnum(predictedVal) == Globals.Tap_Pattern.MOTION) {
                numPredictedMotion++;
            } else if (Globals.tapAsEnum(predictedVal) == Globals.Tap_Pattern.TAP) {
                numPredictedTap++;
            }
        }

//        if ((numPredictedTap > numPredictedMotion) || (numPredictedTap > numPredictedNone)) {
        //TODO: replace with previous line when BayesNet model is being used
        if (numPredictedMotion > numPredictedNone) {
            //TAP!!!
            Log.d(TAG, "TAP detected");
            if (mListener != null) {
                mListener.onTapEvaluated();
            }
            //flush entire queue
            evaluationQueue.clear();
        } else {
            evaluationQueue.remove(); //remove the first/oldest element
        }
    }
}
