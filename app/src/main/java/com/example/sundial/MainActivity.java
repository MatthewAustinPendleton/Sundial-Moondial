package com.example.sundial;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements SolarCalculator.SolarCalculatorCallback {

    private LocationService locationService;
    private double latitude;
    private double longitude;

    private OrientationManager orientationManager;
    private ShadowManager shadowManager;
    private SundialView sundialView;
    private ShadowAnimationManager shadowAnimationManager;

    private double solarAltitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize services:
        locationService = new LocationService(this);
        orientationManager = new OrientationManager(this);
        orientationManager.startListening();

        // Get the SundialView instance
        sundialView = findViewById(R.id.sundial_view);

        // Trigger onDraw to initialize variables in SundialView
        sundialView.post(() -> {
            // Force a redraw to ensure onDraw is called
            sundialView.invalidate();

            // Initialize ShadowManager after onDraw has been called
            shadowManager = new ShadowManager(
                    sundialView.getOutermostRadius() - 20,  // maxLength - shadow should reach the middle radius
                    sundialView.getOutermostRadius() - 100, // minLength - shadow's minimum length
                    40,  // maxWidth
                    10   // minWidth
            );

            Log.d("ShadowDebug", "Initializing ShadowManager with maxLength=" +
                    sundialView.getMiddleRadius() + ", minLength=" + (sundialView.getMiddleRadius() - 40));

            shadowAnimationManager = new ShadowAnimationManager(sundialView, shadowManager);
        });

        if (locationService.checkLocationPermission()) {
            requestUserLocation();
        } // If you have permission, request location
        else {
            locationService.requestLocationPermission();
        } // If you don't have permission, request permission on create
    }

    private void requestUserLocation() {
        locationService.getLocationUpdates(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    Log.d("Lat/Long", "Latitude/Longitude = (" + latitude + ", " + longitude +
                            ")");

                    // Directly call displaySunPosition without delay (temporarily)
                    Log.d("Position Calculation" , "Calculating the sun's position in the sky: ");
                    displaySunPosition();

                } else {
                    Log.d("Location unavailable", "Location unavailable!");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationService.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestUserLocation();
            } else {
                Log.d("Permission", "Permission denied to access location data...");
            }
        }
    }

    // Original method now calls the new version
    private void displaySunPosition() {

        long nowMillis = System.currentTimeMillis();

        SolarCalculator solarCalculator = new SolarCalculator(latitude, longitude, nowMillis);
        solarCalculator.calculateInBackground(this);

    }

    protected void onDestroy() {
        super.onDestroy();
        orientationManager.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationService.stopUpdates();
        orientationManager.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();

        orientationManager.startListening();

        if (locationService.checkLocationPermission()) {
            requestUserLocation();
        }
        else {
            locationService.requestLocationPermission();
        }
    }

    @Override
    public void onCalculationComplete(double altitude, double azimuth) {
        solarAltitude = altitude;

        Log.d("SolarCalculator", "Altitude: " + altitude + ", " + "Azimuth: " + azimuth);

        new Handler(Looper.getMainLooper()).post(() -> {
            double phonePitch = orientationManager.getPitch();
            double phoneRoll = orientationManager.getRoll();
            double shadowDirection = shadowManager.calculateShadowDirection(azimuth, phoneRoll);
            shadowAnimationManager.startAnimation(solarAltitude, shadowDirection, phonePitch);
        });
    }
}