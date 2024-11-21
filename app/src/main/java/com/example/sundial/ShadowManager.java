package com.example.sundial;

import android.util.Log;

public class ShadowManager {

    private final double maxLength;
    private final double minLength;
    private final double maxWidth;
    private final double minWidth;
    private final double maxSolarAltitude = 90.0;

    public ShadowManager(double maxLength, double minLength, double maxWidth, double minWidth) {

        this.maxLength = maxLength; // center to outermost circle
        this.minLength = minLength; // just before the roman numerals
        this.maxWidth = maxWidth;
        this.minWidth = minWidth;

    }

    public double calculateShadowLength(double solarAltitude, double phonePitch) {

        if (solarAltitude < 0) return 0; // No shadow below the horizon
        if (solarAltitude > maxSolarAltitude) solarAltitude = maxSolarAltitude;

        double normalizedAltitude = solarAltitude / maxSolarAltitude; // Range [0, 1]
        double scaledLength = maxLength - (normalizedAltitude * (maxLength - minLength));

        return scaledLength * Math.cos(Math.toRadians(phonePitch));

    }



    public double calculateShadowWidth(double solarAltitude) {

        // If the sun is below the horizon, no shadow should be visible
        if (solarAltitude < 0) return 0;

        if (solarAltitude > maxSolarAltitude) solarAltitude = maxSolarAltitude;

        return maxWidth - ((maxWidth - minWidth) * (solarAltitude / maxSolarAltitude));

    }

    public double calculateShadowDirection(double solarAzimuth, double phoneRoll) {
        // Map the azimuth to the sundial's coordinate system
        // For a horizontal sundial, the shadow points opposite to the sun
        double shadowAzimuth = (solarAzimuth + 180) % 360;

        return (shadowAzimuth - phoneRoll + 360) % 360;
    }

}