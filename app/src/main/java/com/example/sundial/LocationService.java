package com.example.sundial;

/*
This class handles the location permissions and retrieval of the user's latitude
and longitude.
 */

import android.Manifest; // Access to permission constants like ACCESS_FINE_LOCATION
import android.app.Activity; // Allows use of Android's Activity class, for apps
import android.content.pm.PackageManager; // For checking status of app permissions
import android.location.Location; // Latitude and longitude!
import android.util.Log; // LogCat!

import androidx.annotation.NonNull; // Explicitly annotate when something shouldn't be null
import androidx.core.app.ActivityCompat; // Compatibility with older android versions
import androidx.core.content.ContextCompat; // Compatibility with older android versions
import com.google.android.gms.location.FusedLocationProviderClient; // Location API
import com.google.android.gms.location.LocationServices; // Initializes FLPC
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener; // Defines a listener for handling successful responses.
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

public class LocationService {

    private final Activity activity; // Activity instance for accessing context
    private final FusedLocationProviderClient fusedLocationProviderClient; // for accessing location data
    protected static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private LocationCallback locationCallback;

    public LocationService(Activity activity) {

        this.activity = activity;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        // This connects fusedLocationProviderClient to Google Play Services

    }

    public boolean checkLocationPermission() {

        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        // Checks if the ACCESS_FINE_LOCATION is granted, if so, returns true, otherwise, false

    }

    public void requestLocationPermission() {

        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        // Requests permissions for location data if not already granted

    }

    public void getLocationUpdates(OnSuccessListener<Location> listener) {
        if (checkLocationPermission()) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        listener.onSuccess(location);
                    } else {
                        Log.d("LocationService", "Location is null.");
                    }
                }
            };

            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 10000
            ).build();

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback, null
            );
        } else {
            Log.d("LocationService", "Location permission not granted.");
        }
    }

    public void stopUpdates() {
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Log.d("LocationService", "Location updates stopped.");
        }
    }


}