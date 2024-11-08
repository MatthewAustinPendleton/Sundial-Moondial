package com.example.sundial;
import java.util.Calendar;
import java.util.TimeZone;

public class SolarCalculator {

    private final double latitude;
    private final double longitude;
    private final Calendar dateTime;

    // Constants for astronomical calculations
    private static final double J2000Epoch = 2451545.0;
    // Julian Date Reference Epoch
    // J2000 is widely used as a reference point in astronomy corresponding to January 1st, 2000,
    // at 12:00 UTC. By setting it to 2451545.0, it lets us calculate the exact difference
    // in days between any given date and this epoch.
    private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
    private static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;

    public SolarCalculator(double latitude, double longitude, Calendar dateTime) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;

    }

    private double calculateJulianDate() {
        int year = dateTime.get(Calendar.YEAR);
        int month = dateTime.get(Calendar.MONTH) + 1; // Calendar months are 0-based
        int day = dateTime.get(Calendar.DAY_OF_MONTH);
        int hour = dateTime.get(Calendar.HOUR);
        int minute = dateTime.get(Calendar.MINUTE);
        int second = dateTime.get(Calendar.SECOND);

        // For January and February, treat them as months 13 and 14 of the previous year
        if (month <= 2) {
            year --;
            month += 12;
        }

        int centuryOffset = year / 100;
        int gregorianAdjustment = 2 - centuryOffset + (centuryOffset / 4);
        double dayFraction = hour / 24.0 + minute / 1440.0 + second / 86400.0;
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) +
                day + gregorianAdjustment - 1524.5 + dayFraction;
    }

    private double daysSinceJ2000(Calendar dateTime) {
        double julianDate = calculateJulianDate();
        return julianDate - J2000Epoch;
    }

}