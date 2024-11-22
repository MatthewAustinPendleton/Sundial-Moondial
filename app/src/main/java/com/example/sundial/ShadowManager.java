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

    public double calculateAngularWidth(double solarAzimuth) {
        // Convert solar azimuth to hours (0 at midnight, 12 at noon, 24 at midnight)
        double timeOfDay = ((solarAzimuth + 180) % 360) / 15.0;

        // Calculate how far we are from noon (regardless of AM/PM)
        double hoursFromNoon = Math.abs(12 - (timeOfDay % 24));

        // Map this to a width: thinnest at noon, widest at sunrise/sunset
        double widthRatio = hoursFromNoon / 6.0;
        if (widthRatio > 1.0) widthRatio = 1.0;

        Log.d("ShadowDebug", String.format(
                "Width calc: timeOfDay=%.2f, hoursFromNoon=%.2f, ratio=%.2f",
                timeOfDay, hoursFromNoon, widthRatio));

        return minWidth + (maxWidth - minWidth) * widthRatio;
    }

    public double calculateShadowLength(double solarAltitude, double phonePitch) {
        if (solarAltitude < 0) return 0; // No shadow below the horizon
        if (solarAltitude > maxSolarAltitude) solarAltitude = maxSolarAltitude;

        // Normalize altitude
        double normalizedAltitude = solarAltitude / maxSolarAltitude;

        // Adjust scaling: Use inverse relationship for shadow length
        double scaledLength = minLength + (1 - normalizedAltitude) * (maxLength - minLength);

        // Apply pitch correction
        double finalLength = scaledLength * Math.cos(Math.toRadians(phonePitch));

        Log.d("ShadowDebug", String.format(
                "Calculating length - solarAltitude=%.2f, normalizedAltitude=%.2f, scaledLength=%.2f, finalLength=%.2f",
                solarAltitude, normalizedAltitude, scaledLength, finalLength
        ));

        return finalLength;
    }


    public double calculateShadowDirection(double solarAzimuth, double phoneRoll) {
        // Solar azimuth: 0° = North, 90° = East, 180° = South, 270° = West
        // We want the shadow to point opposite to the sun

        // First normalize azimuth to 0-360 range
        solarAzimuth = (solarAzimuth + 360) % 360;

        // Calculate shadow direction (opposite to sun)
        double shadowDirection = (solarAzimuth + 180) % 360;

        // Account for phone rotation
        double adjustedDirection = (shadowDirection - phoneRoll + 360) % 360;

        Log.d("ShadowDebug", String.format(
                "Direction calc: solarAz=%.2f, shadowDir=%.2f, phoneRoll=%.2f, final=%.2f",
                solarAzimuth, shadowDirection, phoneRoll, adjustedDirection));

        return adjustedDirection;
    }

}