package com.example.sundial;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast; // Displaying messages to user
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private LocationService locationService;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationService = new LocationService(this);
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

                    // Show first toast with a shorter duration
                    //Toast.makeText(MainActivity.this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
                    Log.d("Lat/Long", "Latitude/Longitude = (" + latitude + ", " + longitude +
                            ")");

                    // Directly call displaySunPosition without delay (temporarily)
                    Log.d("Position Calculation" , "Calculating the sun's position in the sky: ");
                    displaySunPosition();

                } else {
                    //Toast.makeText(MainActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
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

    private void displaySunPosition() {
        // Get the current date and time
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // Create an instance of SolarCalculator with latitude, longitude, and current time
        SolarCalculator solarCalculator = new SolarCalculator(latitude, longitude, currentTime);

        // Retrieve altitude and azimuth
        double altitude = solarCalculator.getAltitude();
        double azimuth = solarCalculator.getAzimuth();

        // Display altitude and azimuth using Toast
        Log.d("SunPosition", "Latitude: " + latitude + ", Longitude: " + longitude);
        Log.d("SunPosition", "Altitude: " + altitude + ", Azimuth: " + azimuth);
    }

}