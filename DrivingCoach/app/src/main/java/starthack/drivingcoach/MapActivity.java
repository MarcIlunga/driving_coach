package starthack.drivingcoach;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import android.os.Handler;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.RoadElement;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private boolean paused = false;
    // map embedded in the map fragment
    private Map map = null;
    private BleManager blmanager;

    // map fragment embedded in this activity
    private MapFragment mapFragment = null;

    private PositioningManager positioningManager;

    private PositioningManager.OnPositionChangedListener positionChangedListener = new PositioningManager.OnPositionChangedListener() {
        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {
            if (!paused) {
                map.setCenter(geoPosition.getCoordinate(), Map.Animation.NONE);
            }
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {
        }
    };

    /**
     * permissions request code
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    /**
     * Checks the dynamically-controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }

                // all permissions were granted
                initialize();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        checkPermissions();

        //Initialize the data collection handler§
        final Handler dataHandler = new Handler();
        final int dataHandlerDelay = DataAnalysis.TIMELAPSE; //milliseconds

        dataHandler.postDelayed(new Runnable(){
            public void run(){
                if(positioningManager != null){
                    double averageSpeed = positioningManager.getAverageSpeed();
                    RoadElement road = positioningManager.getRoadElement();
                    DataAnalysis.analyse(averageSpeed, road);
                }
                dataHandler.postDelayed(this, dataHandlerDelay);
            }
        }, dataHandlerDelay);

        blmanager = new BleManager(this);
    }

    private void initialize() {
        setContentView(R.layout.activity_map);

        Log.d("MapActivity", "initialize - Enter method !");

        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(
                R.id.mapfragment);
//        Log.d("<<<<<", mapFragment.toString());
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error)
            {
                if (error == OnEngineInitListener.Error.NONE) {
                    positioningManager = PositioningManager.getInstance();
                    positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK);

                    positioningManager.addListener(new WeakReference<>(positionChangedListener));

                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();

                    // Show the indicator of current position
                    map.getPositionIndicator().setVisible(true);

                    if (FileWriterReader.fileExist("last.pos", MyApplication.getContext()) ) {
                        String data = FileWriterReader.readFile("last.pos", MyApplication.getContext());
                        Log.d("<<<<<", data);
                        String geo[] = data.split(",");

                        for (int i = 0; i < geo.length; i++) {
                            Log.d("<<<<<", geo[i]);
                        }

                        Log.d("<<<<<", geo[0] + " " + geo[1] + " " + geo[2] + " || " + data);

                        // If we have a last position saved we can center on it
                        map.setCenter(new GeoCoordinate(Double.parseDouble(geo[0].trim()),Double.parseDouble(geo[1].trim()),
                                Double.parseDouble(geo[2].trim())), Map.Animation.NONE);
                        // Set the zoom level to the average between min and max
                        map.setZoomLevel(
                                (map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                    }
                } else {
                    Log.d("MapActivity", "ERROR: Cannot initialize Map Fragment");
                }
            }
        });
    }

    public void refreshPosition(View view) {
        GeoCoordinate pos = positioningManager.getPosition().getCoordinate();
        map.setCenter(pos, Map.Animation.NONE);
        Toast.makeText(this, "Speed : " + DataAnalysis.currentAverageSpeed, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Distance : " + DataAnalysis.totalDistance, Toast.LENGTH_SHORT).show();
//        Log.d("<<<<<", pos.toString());
    }

    @Override
    protected void onDestroy() {
        if (positioningManager != null) positioningManager.removeListener(positionChangedListener);
        map = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        GeoCoordinate pos = positioningManager.getPosition().getCoordinate();
        Log.d("<<<<<", "Saved position is : " + pos.toString());
        FileWriterReader.writeFile("last.pos", pos.getLatitude() + "," + pos.getLongitude() + "," + pos.getAltitude(), MyApplication.getContext());
//        String read = FileWriterReader.readFile("last.pos", MyApplication.getContext());
//        Log.d("<<<<<<", "Readed :" + read);
        blmanager.close();
        DataEcoAnalysis.calculateEcoScore();
        DataAnalysis.writeToFile();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
