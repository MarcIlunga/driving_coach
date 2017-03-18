package starthack.drivingcoach;

import com.here.android.mpa.common.RoadElement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marius on 18.03.2017.
 */

public class DataAnalysis {
    // Time between 2 calls
    public static final int TIMELAPSE = 5000;              // 5 s
    public static final double penalizedFastLimit = 18.0;   // 5 km/h
    public static final double penalizedSlowLimit = 180.0;  // 50 km/h

    // To keep track of the passing time ~
    private static int passedTime = 0;                      // in ms
    private static double totalSpeedNegativeScore = 0;      // We start at 0

    private static final double penalty = 1.0;
    private static List<Double> negativeSpeedDiff = new ArrayList<>();

    public static void analyse(double averageSpeed, RoadElement road){
        Log.d("<<<<<", "average speed: " + averageSpeed);
        if(road != null){
            Log.d("<<<<<", "road name: " + road.getRoadName());
            Log.d("<<<<<", "road speed limit: " + road.getSpeedLimit());
            double diff = road.getSpeedLimit() - averageSpeed;
            // Too slow
            if (diff > penalizedSlowLimit) {
                diff -= penalizedSlowLimit;
            // Too Fast
            } else if (diff < -penalizedFastLimit) {
                diff += penalizedFastLimit;
            // Correct
            } else {
                diff = 0;
            }

            negativeSpeedDiff.add(diff);
            // Compute the negative score for now
            totalSpeedNegativeScore += computeSpeedScore();
            passedTime += TIMELAPSE;
        }
    }

    private static double computeSpeedScore() {
        int speedRatio = 0;     // -1 if too slow, 0 if correct, 1 if too fast
        double score = 0.0;

        double firstD = negativeSpeedDiff.get(0);

        // Too Slow
        if (firstD > 0) {
            speedRatio = -1;
        } else if (firstD < 0) {
            // Too Fast
            speedRatio = 1;
        }


        for (int i = 1; i < negativeSpeedDiff.size(); i++) {
            double d = negativeSpeedDiff.get(i);
            int nextSpeedRatio = 0;
            // Too Slow
            if (d > 0) {
                nextSpeedRatio = -1;
            } else if (d < 0) {
                // Too Fast
                nextSpeedRatio = 1;
            }

            // We are accumulating time in a certain domain
            if (nextSpeedRatio == speedRatio) {

            // We will restart the timer again to enter the new category
            } else {
                // We remove the different element from list
                double oppos = negativeSpeedDiff.remove(negativeSpeedDiff.size()-1);
                // If   too fast    or      too slow
                if (speedRatio == 1 || speedRatio == -1) {
                    score = penalty * mean(negativeSpeedDiff) * passedTime;
                } else {
                    // If correct speed we do nothing for now
                }

                // We then create a new list
                negativeSpeedDiff = new ArrayList<>();
                negativeSpeedDiff.add(oppos);
                speedRatio = nextSpeedRatio;
            }
        }

        return score;
    }

    private static double mean (List<Double> diffs) {
        double sum = 0;

        for (double d: diffs) {
            sum += d;
        }

        return sum/diffs.size();
    }
}
