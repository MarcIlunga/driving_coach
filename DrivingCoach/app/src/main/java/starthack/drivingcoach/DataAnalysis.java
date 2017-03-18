package starthack.drivingcoach;

import com.here.android.mpa.common.RoadElement;
import android.util.Log;

/**
 * Created by marius on 18.03.2017.
 */

public class DataAnalysis {
    public static void analyse(double averageSpeed, RoadElement road){
        Log.d("<<<<<", "average speed: " + averageSpeed);
        if(road != null){
            Log.d("<<<<<", "road name: " + road.getRoadName());
            Log.d("<<<<<", "road speed limit: " + road.getSpeedLimit());
        }
    }
}
