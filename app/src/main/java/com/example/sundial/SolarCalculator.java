package com.example.sundial;
import java.util.Calendar;
import java.util.TimeZone;

public class SolarCalculator {

    private final double latitude;
    private final double longitude;
    private final Calendar dateTime;

    private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
    private static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;

    // Julian Date Reference Epoch
    // J2000 is widely used as a reference point in astronomy corresponding to January 1st, 2000,
    // at 12:00 UTC. By setting it to 2451545.0, it lets us calculate the exact difference
    // in days between any given date and this epoch.
    private static final double J2000Epoch = 2451545.0;

    private double[] cachedAltitudeAzimuth;

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

    private double calculateMeanSolarAnomaly() {

        double days = daysSinceJ2000(dateTime);
        return (357.5291 + 0.98560028 * days) % 360; // ensures anomaly is within 0-360 degrees
        // 357.5291 represents the mean anomaly at the J2000 epoch
        // 0.98560028 adjusts the anomaly to the current date

    }

    // Adjust for the elliptical shape of Earth's orbit via the Equation of Center
    // C = 1.9148 sin(mean-solar-anomaly-in-radians) + 0.0200 sin (2 * mean-solar-anomaly-in-radians)
    // + 0.0003 sin(3 * mean-solar-anomaly-in-radians)
    private double calculateEquationOfCenter() {

        double meanAnomaly = Math.toRadians(calculateMeanSolarAnomaly());
        return (1.9148 * Math.sin(meanAnomaly) + 0.0200 * Math.sin(2 * meanAnomaly) + 0.0003 * Math.sin(3 * meanAnomaly));

    }

    // The Ecliptic Longitude is the mean-anomaly + equation-of-center + 102.9372
    // The number 102.9372 is the approximate longitude of the perihelion of Earth's orbit
    private double calculateEclipticLongitude() {

        double meanAnomaly = calculateMeanSolarAnomaly();
        double equationOfCenter = calculateEquationOfCenter();
        return (meanAnomaly + equationOfCenter + 102.9372) % 360; // 0-360

    }

    /* Declination and right-ascension are used to locate the position of the sun in the sky.
       These are like long/lat values but for celestial coordinates.

       Declination is the angle between the Sun's rays and the celestial equator, in degrees.
       Tells us how far north/south the sun is relative to the Earth's equator.

       Right Ascension is the celestial equivalent of longitude. It's measured in hours, minutes,
       and seconds, where 24 hours corresponds to 360 degrees. This specifies how far east and west
       the sun is along the celestial equator.
     */

    private double calculateSolarDeclination(double daysSinceJ2000) {

        double solarLongitude = calculateEclipticLongitude() * DEGREES_TO_RADIANS;
        return Math.asin(Math.sin(solarLongitude) * Math.sin(23.44 * DEGREES_TO_RADIANS)) * RADIANS_TO_DEGREES;

    }

    private double calculateRightAscension(double daysSinceJ2000) {

        double solarLongitude = calculateEclipticLongitude() * DEGREES_TO_RADIANS;
        double rightAscension = Math.atan2(Math.cos(23.44 * DEGREES_TO_RADIANS) * Math.sin(solarLongitude), Math.cos(solarLongitude));
        return rightAscension * RADIANS_TO_DEGREES;

    }

    private double calculateHourAngle(double rightAscension) {

        double daysSinceJ2000 = daysSinceJ2000(dateTime);
        double greenwichSiderealTime = 280.46061837 + 360.98564736629 * daysSinceJ2000;
        double localSiderealTime = greenwichSiderealTime + longitude;
        localSiderealTime = (localSiderealTime % 360 + 360) % 360; // [0, 360)
        double hourAngle = localSiderealTime - rightAscension;
        if (hourAngle > 180) hourAngle -= 360;
        if (hourAngle < -180) hourAngle += 360;
        return hourAngle;

    }

    /*
    The altitude, how high the sun is above the horizon, is one measure of the sun's location.
    The azimuth, the sun's compass direction, is another.
    These are determined from the declination, hour angle, and latitude.
     */
    private double[] calculateAltitudeAndAzimuth() {

        double latitudeInRadians = latitude * DEGREES_TO_RADIANS;
        double declinationInRadians = calculateSolarDeclination(daysSinceJ2000(dateTime)) * DEGREES_TO_RADIANS;
        double hourAngleInRadians = calculateHourAngle(calculateRightAscension(daysSinceJ2000(dateTime))) * DEGREES_TO_RADIANS;

        // Calculate the altitude
        double sinAltitude = Math.sin(latitudeInRadians) * Math.sin(declinationInRadians) +
                Math.cos(latitudeInRadians) * Math.cos(declinationInRadians) * Math.cos(hourAngleInRadians);
        double altitude = Math.asin(sinAltitude) * RADIANS_TO_DEGREES;

        // Calculate the azimuth
        double cosAzimuth = (Math.sin(declinationInRadians) - Math.sin(altitude * DEGREES_TO_RADIANS) * Math.sin(latitudeInRadians)) /
                (Math.cos(altitude * DEGREES_TO_RADIANS) * Math.cos(latitudeInRadians));
        double azimuth = Math.acos(cosAzimuth) * RADIANS_TO_DEGREES;

        if (hourAngleInRadians > 0) {
            azimuth = 360 - azimuth;
        }

        return new double[]{altitude, azimuth};

    } // returns a two-element array containing {altitude, azimuth}

    public double getAltitude() {

        if (cachedAltitudeAzimuth == null) {
            cachedAltitudeAzimuth = calculateAltitudeAndAzimuth();
        }
        return cachedAltitudeAzimuth[0];

    }

    public double getAzimuth() {

        if (cachedAltitudeAzimuth == null) {
            cachedAltitudeAzimuth = calculateAltitudeAndAzimuth();
        }
        return cachedAltitudeAzimuth[1];

    }



}