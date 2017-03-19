package starthack.drivingcoach;


import android.util.Log;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by marius on 19.03.2017.
 */
public class DataEcoAnalysis {

    private static int nSamplesPos = 0;
    private static int nSamplesNeg = 0;
    private static int distriFullSamples = 200;
    private static List<Double[]> res = new ArrayList<>();
    private static final int listMaxSize = 200;
    private static boolean last = false;

    private static double sum[] = new double[3];

    // We get the acceleration here
    public static void analyse(int x, int y, int z) {
        Log.d("ECO: ","x: " + x + " y:" + y + " z:" + z);

        //We only need the x if the sensor is well placed in car
        if (x >= 0) {
            // Acceleration forward or at the bac
        } else {
            // Deceleration forward or at the bac
        }
        sum[0] += x;
        // y is not that helpful but keep if suddely needed
        sum[1] += y;
        // Same for usefullness
        sum[2] += z;

        if (nSamplesPos+nSamplesNeg == distriFullSamples || last) {
            Double[] mean = new Double[3];
            mean[0] = sum[0] / (nSamplesPos+nSamplesNeg);
            mean[1] = sum[1] / (nSamplesPos+nSamplesNeg);
            mean[2] = sum[2] / (nSamplesPos+nSamplesNeg);
            res.add(mean);
            // TODO: We should check if the list is full to create a min over it again
            nSamplesPos = 0;
            nSamplesNeg = 0;
        }
    }

    /*
     * For now it's an easy function
     */
    public double calculateEcoScore () {
        double score = 0;
        for (Double[] d: res) {
            score += (d[0]*d[0]+d[1]*d[1]+d[2]+d[2]) / (3 * res.size()*res.size());
        }

        FileWriterReader.writeFile("ecolo.score", score + "", MyApplication.getContext());
        return score;
>>>>>>> 00069949f15171546f0858ef611ddae99fd059ea
    }
}
