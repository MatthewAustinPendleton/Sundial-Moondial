package com.example.sundial;
import android.util.Log;
import java.util.Calendar;
import java.util.TimeZone;

public class SolarCalculator {

    private final double latitude;
    private final double longitude;
    private final Calendar dateTime;

    private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
    private static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;

    // Constant representing the Julian Date of Jan. 1, 2000, a standard convenience for simplifying time calculations for celestial objects
    private static final double J2000Epoch = 2451545.0;

    public SolarCalculator(double latitude, double longitude, long timeInMillis) {

        this.latitude = latitude;
        this.longitude = longitude;

        Calendar utcDateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcDateTime.setTimeInMillis(timeInMillis);
        this.dateTime = utcDateTime;

    }

    // Continuous count of days and fractions of days since Jan 1., 4713 BCE, noon.  Continuous scale in decimal days.
    private double calculateJulianDate() {

        int year = dateTime.get(Calendar.YEAR);
        int month = dateTime.get(Calendar.MONTH) + 1; // Calendar months are 0-based
        int day = dateTime.get(Calendar.DAY_OF_MONTH);
        int hour = dateTime.get(Calendar.HOUR_OF_DAY);
        int minute = dateTime.get(Calendar.MINUTE);
        int second = dateTime.get(Calendar.SECOND);

        // For January and February, treat them as months 13 and 14 of the previous year
        if (month <= 2) {
            year--;
            month += 12;
        }

        // Adjusts for the difference between the Julian and Gregorian calendars.
        int centuryCorrectionFactor = year / 100;

        // Leap year correction term
        int leapYearCorrection = 2 - centuryCorrectionFactor + (centuryCorrectionFactor / 4);

        double dayFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0;

        // Number of days since Jan 1, 4713, based on the current year, including leap years.
        double daysFromYear = Math.floor(365.25 * (year + 4716));

        // Number of days since the start of the year up to the current month.
        double daysFromMonths = Math.floor(30.6001 * (month + 1));

        // Combines current day with the fraction of the day, including leap year adjustment
        double dayOffset = day + dayFraction + leapYearCorrection - 1524.5;

        return daysFromYear + daysFromMonths + dayOffset;
    }

    private double daysSinceJ2000() {

        return calculateJulianDate() - J2000Epoch;

    }

    // Angle measured from the center of an idealized circular orbit of the earth, simplifying calculations
    private double calculateMeanSolarAnomaly(double julianCenturyTime) {

        double meanSolarAnomaly = (357.52911 + 35999.05029 * julianCenturyTime) % 360;
        if (meanSolarAnomaly < 0) meanSolarAnomaly += 360;
        return meanSolarAnomaly;

    }

    // Correction term added to mean solar anomaly to account for the elliptical shape of earth's orbit
    private double calculateEquationOfCenter(double meanAnomalyDegrees) {

        double meanAnomaly_rad = meanAnomalyDegrees * DEGREES_TO_RADIANS;
        double julianCenturyTime = daysSinceJ2000() / 36525.0; // Julian Centuries

        // Correction for elliptical orbit
        double mainAdjustment = (1.914602 - 0.004817 * julianCenturyTime - 0.000014 * julianCenturyTime * julianCenturyTime) * Math.sin(meanAnomaly_rad);

        // Corrects position based on smaller orbital deviations
        double orbitalDeviationAdjustment = (0.019993 - 0.000101 * julianCenturyTime) * Math.sin(2 * meanAnomaly_rad);

        // Corrects for minute details
        double minuteDeviationAdjustment = 0.000289 * Math.sin(3 * meanAnomaly_rad);

        return mainAdjustment + orbitalDeviationAdjustment + minuteDeviationAdjustment;

    }

    // Apparent position of the sun along the ecliptic plane
    private double calculateEclipticLongitude(double julianCenturyTime, double meanSolarAnomaly) {

        // Approximate position of earth along the ecliptic plane
        double meanLongitude = (280.46646 + 36000.76983 * julianCenturyTime) % 360;
        if (meanLongitude < 0) meanLongitude += 360;

        // Correction term for the elliptical shape of the earth's orbit about the sun
        double equationOfCenter = calculateEquationOfCenter(meanSolarAnomaly);

        // Sun's apparent position as seen from earth on the celestial sphere
        double trueLongitude = meanLongitude + equationOfCenter;
        if (trueLongitude > 360) trueLongitude -= 360;

        return trueLongitude;

    }

    // Angle between the sun's rays and the equatorial plane of the earth, giving how far north or south the sun is relative to the equator
    private double calculateSolarDeclination(double epsilon_rad, double lambda_rad) {

        // Solar Declination
        return Math.asin(Math.sin(epsilon_rad) * Math.sin(lambda_rad));

    }

    // Angular measure defining the position of the sun in relation to the celestial equator - how far east the sun is along the celestial equator
    private double calculateRightAscension(double epsilon_rad, double lambda_rad) {

        double rightAscension_rad = Math.atan2(Math.cos(epsilon_rad) * Math.sin(lambda_rad), Math.cos(lambda_rad));
        if (rightAscension_rad < 0) rightAscension_rad += 2 * Math.PI;
        return rightAscension_rad * RADIANS_TO_DEGREES;

    }

    // Time scale used in astronomy
    private double calculateLocalSiderealTime() {

        double julianDate = calculateJulianDate();
        double daysPassedSinceJ2000Epoch = julianDate - 2451545.0;

        double greenwichMeanSiderealTime = (280.46061837 + 360.98564736629 * daysPassedSinceJ2000Epoch) % 360;
        if (greenwichMeanSiderealTime < 0) greenwichMeanSiderealTime += 360;

        double localSiderealTime = (greenwichMeanSiderealTime + longitude) % 360;
        if (localSiderealTime < 0) localSiderealTime += 360;
        return localSiderealTime;

    }

    // Angular distance in degrees between the observer's meridian (north-south line overhead) and the meridian containing the sun
    private double calculateHourAngle(double localSiderealTime, double rightAscensionDegrees) {

        double hourAngleDegrees = (localSiderealTime - rightAscensionDegrees + 360) % 360;
        if (hourAngleDegrees > 180) hourAngleDegrees -= 360;
        return hourAngleDegrees * DEGREES_TO_RADIANS;

    }


    // The two coordinates (altitude, azimuth) that determines the sun's position overhead in the sky, crucial for sundial functionality
    private double[] calculateAltitudeAndAzimuth() {

        double daysPassedSinceJ2000Epoch = daysSinceJ2000();
        double julianCentury = daysPassedSinceJ2000Epoch / 36525.0; // Julian Centuries
        double meanSolarAnomaly = calculateMeanSolarAnomaly(julianCentury);
        double eclipticLongitude = calculateEclipticLongitude(julianCentury, meanSolarAnomaly);

        // Tilt of the earth's axis relative to its orbit around the sun
        double obliquityOfEcliptic = 23.439291 - 0.0130042 * julianCentury;

        // Radian conversions
        double lambda_rad = eclipticLongitude * DEGREES_TO_RADIANS;
        double epsilon_rad = obliquityOfEcliptic * DEGREES_TO_RADIANS;
        double latitude_rad = latitude * DEGREES_TO_RADIANS;

        double solarDeclination = calculateSolarDeclination(epsilon_rad, lambda_rad);
        double rightAscension = calculateRightAscension(epsilon_rad, lambda_rad);
        double localSiderealTime = calculateLocalSiderealTime();
        double hourAngle_rad = calculateHourAngle(localSiderealTime, rightAscension);

        // Altitude
        double altitude_rad = Math.asin(Math.sin(latitude_rad) * Math.sin(solarDeclination) + Math.cos(latitude_rad) * Math.cos(solarDeclination) * Math.cos(hourAngle_rad));
        double altitude_deg = altitude_rad * RADIANS_TO_DEGREES;

        // Azimuth
        double azimuth_rad = Math.atan2(-Math.sin(hourAngle_rad), Math.cos(latitude_rad) * Math.tan(solarDeclination) - Math.sin(latitude_rad) * Math.cos(hourAngle_rad));
        double azimuth_deg = (azimuth_rad * RADIANS_TO_DEGREES + 360) % 360;
        return new double[]{altitude_deg, azimuth_deg};


    }

    public double getAltitude() {


        return calculateAltitudeAndAzimuth()[0];

    }

    public double getAzimuth() {

        return calculateAltitudeAndAzimuth()[1];

    }

}