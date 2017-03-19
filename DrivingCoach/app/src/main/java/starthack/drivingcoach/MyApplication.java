package starthack.drivingcoach;

import android.app.Application;
import android.content.Context;

/**
 * Created by joachim on 3/18/17.
 */

public class MyApplication extends Application {
    private static MyApplication singleton;

    public static MyApplication getInstance(){
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    public static Context getContext() {
        return singleton;
    }
}