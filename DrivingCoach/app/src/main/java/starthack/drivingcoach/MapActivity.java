package starthack.drivingcoach;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
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
        checkPermissions();
    }

    private void initialize() {
        setContentView(R.layout.activity_map);

        Log.d("MapActivity", "initialize - Enter method !");

        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(
                R.id.mapfragment);
        Log.d("<<<<<", mapFragment.toString());
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error)
            {
                if (error == OnEngineInitListener.Error.NONE) {
                    positioningManager = PositioningManager.getInstance();
                    positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK);

                    positioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(positionChangedListener));

                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();

                    // Show the indicator of current position
                    map.getPositionIndicator().setVisible(true);


                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(46, 105),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel(
                            (map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                } else {
                    Log.d("MapActivity", "ERROR: Cannot initialize Map Fragment");
                }
            }
        });
    }

    public void refreshPosition(View view) {
        // Set the map center to the Vancouver region (no animation)
        GeoCoordinate pos = positioningManager.getPosition().getCoordinate();
        map.setCenter(pos,
                Map.Animation.NONE);
        Log.d("<<<<<", pos.toString());
        // Set the zoom level to the average between min and max
        map.setZoomLevel(
                (map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
    }

    @Override
    protected void onDestroy() {
        if (positioningManager != null) positioningManager.removeListener(positionChangedListener);
        map = null;

        super.onDestroy();
    }
}
