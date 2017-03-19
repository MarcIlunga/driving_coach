package starthack.drivingcoach;

import com.here.android.mpa.common.RoadElement;

import android.app.*;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DataAnalysis {
    // Time between 2 calls
    public static final int TIMELAPSE = 5000;              // 5 s
    public static final double penalizedFastLimit = 18.0;   // 5 km/h
    public static final double penalizedSlowLimit = 180.0;  // 50 km/h

    // To keep track of the passing time ~
    public static int passedTime = 0;                      // in ms
    public static double totalSpeedNegativeScore = 0;      // We start at 0
    public static double totalDistance = 0;
    public static double totalScore = 0;

    private static final double penalty = 1.0;
    private static List<Double> negativeSpeedDiff = new ArrayList<>();

    public static double currentAverageSpeed = 0;

    public static double biggestError = 0;
    public static RoadElement biggestErrorRoad;


    public static void init () {
        readFromFiles();
    }

    public static void analyse(double averageSpeed, RoadElement road){
        currentAverageSpeed = averageSpeed;
//        Log.d("<<<<<", "average speed: " + averageSpeed);
        if(road != null){
//            Log.d("<<<<<", "road name: " + road.getRoadName());
//            Log.d("<<<<<", "road speed limit: " + road.getSpeedLimit());
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

            if (biggestError < penalizedFastLimit) {
                biggestError = penalizedFastLimit;
                biggestErrorRoad = road;
            }

            negativeSpeedDiff.add(diff);
            // Compute the negative score for now
            totalSpeedNegativeScore += computeSpeedScore();
            passedTime += TIMELAPSE;
            totalDistance += averageSpeed * TIMELAPSE/1000.0;
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

    public static void readFromFiles () {
        if (FileWriterReader.fileExist("distance.tot", MyApplication.getContext())) {
            totalDistance = Double.parseDouble(FileWriterReader.readFile("distance.tot", MyApplication.getContext()));
        }

        if (FileWriterReader.fileExist("negscore.tot", MyApplication.getContext())) {
            totalSpeedNegativeScore = Double.parseDouble(FileWriterReader.readFile("negscore.tot", MyApplication.getContext()));
        }

        if (FileWriterReader.fileExist("speed.score", MyApplication.getContext())) {
            totalScore = Double.parseDouble(FileWriterReader.readFile("speed.score", MyApplication.getContext()));
        }
    }

    public static void writeToFile() {
        totalScore = (totalSpeedNegativeScore / (totalDistance + 1));
        FileWriterReader.writeFile("speed.score", "" + totalScore, MyApplication.getContext());
        FileWriterReader.writeFile("distance.tot", "" + totalDistance, MyApplication.getContext());
        FileWriterReader.writeFile("negscore.tot", "" + totalSpeedNegativeScore, MyApplication.getContext());
    }
}
