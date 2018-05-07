package hf.thewalkinglife;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import hf.thewalkinglife.db.LoadTodayStepTask;
import hf.thewalkinglife.db.StepDataDbManager;
import hf.thewalkinglife.service.StepService;
import hf.thewalkinglife.util.MapTextIconFactory;

/**
 * The screen that holds the Google map showing the user's current location and the circle with the radius of what is still needed to meet the daily goal.
 */
public class TwlMapFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        LoadTodayStepTask.TodayStepLoaderFragment{

    private static final String TAG = "TwlMapFragment";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final String KEY_CURRENT_LOCATION = "CURRENT_LOCATION";
    private static final double EARTH_RADIUS = 6378137.0;

    private GoogleMap map;
    private FusedLocationProviderClient locationProviderClient;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Circle circle;
    private Marker marker;
    private float radius;
    private float stepCount;
    private SharedPreferences sharedPreferences;
    private StepDataDbManager dbManager;
    private LoadTodayStepTask loadTodayStepTask;

    @BindView(R.id.map_view) MapView mapView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the Fused Location Provider
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Initialize the request that will be made towards the provider to get the user's current location
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Initialize the callback function that will run the program received the user's current updated location (in semi real time)
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) {
                    return;
                }
                currentLocation = result.getLastLocation();
                updateUI();
            }
        };

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        dbManager = StepsApplication.getDbManager();
        setValuesFromPreviousState(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);

        // Initialize the Google Map's view
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Toast.makeText(getContext(), "An error occured during map initialization.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        return view;
    }

    /**
     * Runs after the map has been intialized.
     * Check whether location services are enabled for this application and GPS is turned on.
     * If the requirements are met, request the user's current location and update the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException {
        map = googleMap;
        if (isLocationPermissionGranted(this)) {
            if (isGPSDisabled()) {
                showGPSAlertDialog();
            } else {
                map.setMyLocationEnabled(true);
                map.setOnMyLocationButtonClickListener(this);
                map.setOnMyLocationClickListener(this);
                setRadius();
                requestCurrentLocation();
            }
        }
    }

    /**
     * Sends a request to the Fused Location provider to get the user's last known location.
     * After the location has been received, updates the UI.
     */
    private void requestCurrentLocation() throws SecurityException {
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    updateUI();
                }
            }
        });
    }

    /**
     * Starts a runtime check whether the application has access to the location services.
     * If not, shows a dialog which requests permission from the user.
     */
    private boolean isLocationPermissionGranted(Fragment fragment) {
        if (ActivityCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return false;
        }
        return true;
    }

    /**
     * Runs after the user has granted permission for using location services.
     * If it was successful, requests the user's last known location from the Fused Location Provider.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) throws SecurityException {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestCurrentLocation();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registers for step updates from StepService.
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter(StepService.BR_NEW_STEP));
        refreshStepCount();
        if (map != null && map.isMyLocationEnabled()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregisters from step updates from StepService.
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        if (map != null && map.isMyLocationEnabled()) {
            stopLocationUpdates();
        }
    }

    /**
     * Starts ongoing location requests to the Fused Location Provider.
     */
    private void startLocationUpdates() throws SecurityException {
        if (isLocationPermissionGranted(this)) {
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    /**
     * Stops the ongoing location requests.
     */
    private void stopLocationUpdates() throws SecurityException {
        if (isLocationPermissionGranted(this)) {
            locationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Default behaviour.
     */
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    /**
     * Default behavior.
     */
    @Override
    public void onMyLocationClick(@NonNull Location location) { }

    /**
     * Moves the camera to the new current location of the user.
     * Draws a circle and text on the map.
     */
    private void updateUI() {
        if (map != null && currentLocation != null) {

            // Set new camera position on the map
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.moveCamera(cameraUpdate);

            // Create or update circle
            if (circle == null) {
                circle = map.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(radius)
                            .strokeColor(Color.argb(200, 255, 174, 30))
                            .fillColor(Color.argb(30, 255, 247, 0)));
            } else {
                circle.setCenter(latLng);
                circle.setRadius(radius);
            }

            // Create or update text
            double textLatitude = currentLocation.getLatitude() + (radius / EARTH_RADIUS) * (180.0 / Math.PI);
            LatLng textLatLng = new LatLng(textLatitude, currentLocation.getLongitude());

            BitmapDescriptor text = MapTextIconFactory.createPureTextIcon(String.valueOf(radius) + " m");
            if (marker == null) {
                marker = map.addMarker(new MarkerOptions()
                            .position(textLatLng)
                            .icon(text));
            } else {
                marker.setPosition(textLatLng);
                marker.setIcon(text);
            }
        }
    }

    /**
     * Calculates the radius of the circle from the user's stride, current step count and daily goal.
     */
    private void setRadius() {
        String strideStr = sharedPreferences.getString(PreferenceConstants.STRIDE, PreferenceConstants.DEFAULT_STRIDE);
        String dailyGoalStr = sharedPreferences.getString(PreferenceConstants.DAILY_GOAL, PreferenceConstants.DEFAULT_DAILY_GOAL);

        float stride = Float.parseFloat(strideStr);
        float dailyGoal = Float.parseFloat(dailyGoalStr);
        float existingDistance = stepCount * stride;
        float remainingDistance = dailyGoal * stride - existingDistance;

        // Fallback to 0 if the value is negative for some reason.
        radius = remainingDistance >= 0 ? remainingDistance : 0;
    }

    /**
     * Saves the current location for orientation change.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_CURRENT_LOCATION, currentLocation);
        super.onSaveInstanceState(outState);
    }

    /**
     * Sets the previous location from before the orientation change.
     */
    private void setValuesFromPreviousState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_CURRENT_LOCATION)) {
                currentLocation = savedInstanceState.getParcelable(KEY_CURRENT_LOCATION);
            }
        }
    }

    /**
     * Checks whether the GPS setting is currently disabled on the device.
     */
    private boolean isGPSDisabled() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Opens a simple dialog which tells the user to turn on GPS to use the location service.
     */
    private void showGPSAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.gps_disabled)
                .setMessage(R.string.gps_disabled_dialog_message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Navigate the user to the location settings on the device.
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();
    }

    /**
     * Start a new async task to load today's step data which will be used to calculate the circle's radius.
     */
    private void refreshStepCount() {
        if (loadTodayStepTask != null) {
            loadTodayStepTask.cancel(false);
        }
        loadTodayStepTask = new LoadTodayStepTask(this, dbManager);
        loadTodayStepTask.execute();
    }

    /**
     * Receives updates from the StepService about the user's steps.
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int steps = intent.getIntExtra(StepService.KEY_STEP_COUNT, 0);
            setStepCount(String.valueOf(steps));
        }
    };

    /**
     * Sets the current new step count that comes from the StepService.
     * Runs an UI update after the new radius has been calculated.
     */
    @Override
    public void setStepCount(String stepCount) {
        this.stepCount = stepCount != null ? Float.parseFloat(stepCount) : 0f;
        setRadius();
        updateUI();
    }
}
