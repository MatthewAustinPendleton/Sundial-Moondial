package com.example.sundial;

public class ShadowManager {

    private final double maxLength;
    private final double maxWidth;
    private final double minWidth;
    private final double maxSolarAltitude = 90.0;

    public ShadowManager(double maxLength, double maxWidth, double minWidth) {

        this.maxLength = maxLength; // Initialize with sundial radius
        this.maxWidth = maxWidth;
        this.minWidth = minWidth;

    }

    public double calculateShadowLength(double solarAltitude, double phonePitch) {

        // Ensure altitude is within valid bounds
        if (solarAltitude < 0) return 0; // Shadow should disappear when the sun is below the horizon

        if (solarAltitude > maxSolarAltitude) solarAltitude = maxSolarAltitude;

        double baseShadowLength = maxLength * (1 - (solarAltitude / maxSolarAltitude));

        return baseShadowLength * Math.cos(Math.toRadians(phonePitch));

    }

    public double calculateShadowWidth(double solarAltitude) {
        // If the sun is below the horizon, no shadow should be visible
        if (solarAltitude < 0) return 0;

        if (solarAltitude > maxSolarAltitude) solarAltitude = maxSolarAltitude;

        return maxWidth - ((maxWidth - minWidth) * (solarAltitude / maxSolarAltitude));
    }

    public double calculateShadowDirection(double solarAzimuth, double phoneRoll) {

        return (solarAzimuth - phoneRoll + 360) % 360;

    }

}