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

    // Julian Date Reference Epoch
    // J2000 is widely used as a reference point in astronomy corresponding to January 1st, 2000,
    // at 12:00 UTC. By setting it to 2451545.0, it lets us calculate the exact difference
    // in days between any given date and this epoch.
    private static final double J2000Epoch = 2451545.0;

    private double[] cachedAltitudeAzimuth;

    public SolarCalculator(double latitude, double longitude, Calendar dateTime) {

        Log.d("Constructor","==========CONSTRUCTOR==========");
        this.latitude = latitude;
        Log.d("Constructor", "Immediately after Solar Calculator is constructed, the latitude is: " + latitude);
        this.longitude = longitude;
        Log.d("Constructor","Immediately after Solar Calculator is constructed, the longitude is: " + longitude);
        this.dateTime = dateTime;
        Log.d("Constructor", "Immediately after Solar Calculator is constructed, the dateTime is: " + dateTime.toString());
        dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        Log.d("Constructor", "                                                          ");

    }

    private double calculateJulianDate() {

        Log.d("Calculate Julian Date", "==========CALCULATE JULIAN DATE==========");
        int year = dateTime.get(Calendar.YEAR);
        Log.d("Calculate Julian Date", "The year is set to " + year);
        int month = dateTime.get(Calendar.MONTH) + 1; // Calendar months are 0-based
        Log.d("Calculate Julian Date", "The month is set to " + month);
        int day = dateTime.get(Calendar.DAY_OF_MONTH);
        Log.d("Calculate Julian Date", "The day is set to " + day);
        int hour = dateTime.get(Calendar.HOUR_OF_DAY);
        Log.d("Calculate Julian Date", "The hour is set to " + hour);
        int minute = dateTime.get(Calendar.MINUTE);
        Log.d("Calculate Julian Date", "The minute is set to " + minute);
        int second = dateTime.get(Calendar.SECOND);
        Log.d("Calculate Julian Date", "The second is set to " + second);

        // Log the date and time for debugging purposes
        Log.d("Date and Time", String.format("UTC Date and Time: %04d-%02d-%02d %02d:%02d:%02d",
                year, month, day, hour, minute, second));

        // For January and February, treat them as months 13 and 14 of the previous year
        if (month <= 2) {
            Log.d("Calculate Julian Date", "The month is less than or equal to 2, so...");
            year --;
            Log.d("Calculate Julian Date", "Subtract 1 from the year, giving us: year = " + year);
            month += 12;
            Log.d("Calculate Julian Date", "Add 12 to the month, giving us: month = " + month);
        }

        int A = year / 100;
        Log.d("Calculate Julian Date", "A is set to year / 100, giving us: A = " + A);
        int B = 2 - A + (A / 4);
        Log.d("Calculate Julian Date", "B is set to 2 - A + (A / 4), giving us: B = " + B);

        double secondsInDay = second / 86400.0;
        double minutesInDay = minute / 1440.0;
        double hoursInDay = hour / 24.0;
        double dayFraction = hoursInDay + minutesInDay + secondsInDay;
        Log.d("Calculate Julian Date", "Calculating dayFraction to be " + dayFraction);
        Log.d("Calculate Julian Date", "This was obtained via the formula: dayFraction = (hour + (minute + second / 60.0) / 60.0) / 24.0");

        double julianDate = Math.floor(365.25 * (year + 4716)) + Math.floor(30.600 * (month + 1)) + day + dayFraction + B - 1524.5;
        Log.d("Calculate Julian Date", "Calculated Julian Date is: " + julianDate);
        Log.d("Calculate Julian Date", "Given by the formula: julianDate = floor[365.25 * (year + 4716)] + floor[30.600 * (month + 1)] + day + dayFraction + B - 1524.5");
        Log.d("Calculate Julian Date", "                                                          ");
        return julianDate;
    }

    private double daysSinceJ2000(Calendar dateTime) {

        Log.d("Days Since J2000", "==========DAYS SINCE J2000==========");
        Log.d("Days Since J2000", "J2000Epoch is " + J2000Epoch);
        double epochDifference = calculateJulianDate() - J2000Epoch;
        Log.d("Days Since J2000", "JulianDate - J2000Epoch gives us: " + epochDifference);
        Log.d("Days Since J2000", "                                                          ");
        return epochDifference;

    }

    private double calculateMeanSolarAnomaly() {

        Log.d("Mean solar Anomaly", "==========CALCULATE MEAN SOLAR ANOMALY==========");
        double daysSinceEpoch = daysSinceJ2000(dateTime);
        Log.d("Mean Solar Anomaly", "Reading days since epoch from daysSinceJ2000(dateTime) as: " + daysSinceEpoch);
        double meanAnomalyDegrees = (357.5291 + 0.98564736 * daysSinceEpoch) % 360;
        Log.d("Mean Solar Anomaly", "meanAnomalyDegrees is calculated in the following way: ");
        Log.d("Mean Solar Anomaly", "meanAnomalyDegrees = (357.5291 + 0.98560028 * daysSinceEpoch) % 360 = " + meanAnomalyDegrees);
        if (meanAnomalyDegrees < 0) meanAnomalyDegrees += 360;
        Log.d("Mean Solar Anomaly", "If meanAnomalyDegrees is less than 0, that is negative, then add 360 to it to make it positive.");
        Log.d("Mean Solar Anomaly", "Mean Solar Anomaly (degrees): " + meanAnomalyDegrees);
        Log.d("Mean Solar Anomaly", "                                                   ");
        return meanAnomalyDegrees;

    }

    // Adjust for the elliptical shape of Earth's orbit via the Equation of Center
    // C = 1.9148 sin(mean-solar-anomaly-in-radians) + 0.0200 sin (2 * mean-solar-anomaly-in-radians)
    // + 0.0003 sin(3 * mean-solar-anomaly-in-radians)
    private double calculateEquationOfCenter(double meanAnomaly) {

        Log.d("Equation of Center", "==========CALCULATE EQUATION OF CENTER==========");
        double M_rad = meanAnomaly * DEGREES_TO_RADIANS;
        Log.d("Equation of Center", "Mean Solar Anomaly (radians): " + M_rad);
        double term1 = 1.9148 * Math.sin(M_rad);
        Log.d("Equation of Center", "Term 1 is 1.9148 * sin(mean-solar-anomaly-in-radians): " + term1);
        double term2 = 0.0200 * Math.sin(2 * M_rad);
        Log.d("Equation of Center", "Term 2 is 0.0200 * sin(2 * mean-solar-anomaly-in-radians): " + term2);
        double term3 = 0.0003 * Math.sin(3 * M_rad);
        Log.d("Equation of Center", "Term 3 is 0.0003 * sin(3 * mean-solar-anomlay-in-radians): " + term3);
        double C = term1 + term2 + term3;
        Log.d("Equation of Center", "Taken together, the terms become C = term1 + term2 + term3 = " + C);
        Log.d("Equation of Center", "                                                   ");
        return C;

    }

    // The Ecliptic Longitude is the mean-anomaly + equation-of-center + 102.9372
    // The number 102.9372 is the approximate longitude of the perihelion of Earth's orbit
    private double calculateEclipticLongitude(double meanAnomaly, double equationOfCenter) {

        Log.d("Ecliptic Longitude", "==========CALCULATE ECLIPTIC LONGITUDE==========");
        double lambda = (meanAnomaly + equationOfCenter + 102.9372) % 360;
        Log.d("Ecliptic Longitude", "Ecliptic Longitude = (meanAnomaly + equationOfCenter + 102.9372) = " + lambda);

        if (lambda < 0) lambda += 360;
        Log.d("Ecliptic Longitude", "If the ecliptic longitude is negative, then add 360 to it. ");
        Log.d("Ecliptic Longitude", "Final Ecliptic Longitude Calculation (degrees): " + lambda);
        Log.d("Ecliptic Longitude", "                                                   ");
        return lambda;

    }

    /* Declination and right-ascension are used to locate the position of the sun in the sky.
       These are like long/lat values but for celestial coordinates.

       Declination is the angle between the Sun's rays and the celestial equator, in degrees.
       Tells us how far north/south the sun is relative to the Earth's equator.

       Right Ascension is the celestial equivalent of longitude. It's measured in hours, minutes,
       and seconds, where 24 hours corresponds to 360 degrees. This specifies how far east and west
       the sun is along the celestial equator.
     */

    private double calculateSolarDeclination(double eclipticLongitude) {

        Log.d("Solar Declination", "==========CALCULATE SOLAR DECLINATION==========");
        double lambda_rad = eclipticLongitude * DEGREES_TO_RADIANS;
        Log.d("Solar Declination", "The ecliptic, in radians, is converted to be: " + lambda_rad);
        double delta = Math.asin(Math.sin(lambda_rad) * Math.sin(23.44 * DEGREES_TO_RADIANS));
        Log.d("Solar Declination", "The solar declination, in radians, is calculated as follows: " +
                "arcsin(sin(ecliptic-in-radians)) * sin(23.44 * DEGREES_TO_RADIANS) = " + delta);
        double delta_deg = delta * RADIANS_TO_DEGREES;
        Log.d("Solar Declination", "Solar Declination (degrees): " + delta_deg);
        Log.d("Solar Declination", "                                                    ");
        return delta;

    }

    private double calculateRightAscension(double eclipticLongitude) {

        Log.d("Right Ascension", "==========CALCULATE RIGHT ASCENSION==========");
        double lambda_rad = eclipticLongitude * DEGREES_TO_RADIANS;
        Log.d("Right Ascension", "The ecliptic in radians is currently: " + lambda_rad);
        double alpha = Math.atan2(Math.cos(23.44 * DEGREES_TO_RADIANS) * Math.sin(lambda_rad), Math.cos(lambda_rad));
        Log.d("Right Ascension", "The right ascension is calculated as follows: " +
                "Right Ascension = arctan2(cos(23.44 * DEGREES_TO_RADIANS) * sin(ecliptic-longitude), cos(ecliptic-longitude)) = " +
                alpha);
        double alpha_deg = alpha * RADIANS_TO_DEGREES;
        Log.d("Right Ascension", "Right Ascension in degrees: " + alpha_deg);
        if (alpha_deg < 0) alpha_deg += 360;
        Log.d("Right Ascension","If the right ascension, in degrees, is negative, add 360 to it!");
        Log.d("Right Ascension", "Right Ascension (degrees): " + alpha_deg);
        Log.d("Right Ascension", "                                                      ");
        return alpha_deg;

    }

    private double calculateLocalSiderealTime() {

        Log.d("Local Sidereal Time", "==========CALCULATE LOCAL SIDEREAL TIME==========");
        double jd = calculateJulianDate();
        Log.d("Local Sidereal Time", "The Julian Date relevant to this calculation is: " + jd);
        double n = jd - 2451545.0;
        Log.d("Local Sidereal Time", "The days since epoch is jd - 2451545.0 = " + n);
        double GMST = (280.46061837 + 360.98564736629 * n) % 360;
        Log.d("Local Sidereal Time", "Now we calculate the GMST, which is given as: " +
                "(280.46061837 + 360.98564736629 * days-since-epoch) % 360 = " + GMST);
        if (GMST < 0) GMST += 360;
        Log.d("Local Sidereal Time", "If GMST is negative, add 360!");
        double LMST = (GMST + longitude) % 360;
        Log.d("Local Sidereal Time", "LMST = (GMST + longitude) % 360, where longitude is " + longitude + " gives us: " + LMST);
        if (LMST < 0) LMST += 360;
        Log.d("Local Sidereal Time", "If LMST is negative, add 360!");
        Log.d("Local Sidereal Time", "Local Sidereal Time (degrees): " + LMST);
        Log.d("Local Sidereal Time", "                                                  ");
        return LMST;

    }

    private double calculateHourAngle(double rightAscension) {

        Log.d("Hour Angle", "==========CALCULATE HOUR ANGLE==========");
        double LST = calculateLocalSiderealTime();
        Log.d("Hour Angle", "LST is calculated to be " + LST);
        double H = (LST - rightAscension + 360) % 360;
        Log.d("Hour Angle", "Hour Angle = (LST - rightAscension + 360) % 360 = " + H);
        if (H > 180) H -= 360;
        Log.d("Hour Angle", "If hour angle is greater than 180, subtract 360 from it!");
        Log.d("Hour Angle", "Hour Angle (degrees): " + H);
        Log.d("Hour Angle", "                                                           ");
        return H;

    }

    /*
    The altitude, how high the sun is above the horizon, is one measure of the sun's location.
    The azimuth, the sun's compass direction, is another.
    These are determined from the declination, hour angle, and latitude.
     */
    private double[] calculateAltitudeAndAzimuth() {

        Log.d("Altitude and Azimuth", "==========CALCULATE ALTITUDE AND AZIMUTH==========");
        double meanAnomaly = calculateMeanSolarAnomaly();
        Log.d("Altitude and Azimuth", "Mean Anomaly is: " + meanAnomaly);
        double equationOfCenter = calculateEquationOfCenter(meanAnomaly);
        Log.d("Altitude and Azimuth", "Equation of Center is " + equationOfCenter);
        double eclipticLongitude = calculateEclipticLongitude(meanAnomaly, equationOfCenter);
        Log.d("Altitude and Azimuth", "Ecliptic Longitude is " + eclipticLongitude);
        double declination = calculateSolarDeclination(eclipticLongitude);
        Log.d("Altitude and Azimuth", "Declination is " + declination);
        double rightAscension = calculateRightAscension(eclipticLongitude);
        Log.d("Altitude and Azimuth", "Right ascension is " + rightAscension);
        double hourAngle = calculateHourAngle(rightAscension);
        Log.d("Altitude and Azimuth", "Hour angle is " + hourAngle);
        double latitude_rad = latitude * DEGREES_TO_RADIANS;
        Log.d("Altitude and Azimuth", "Latitude in radians is: " + latitude_rad);
        double declination_rad = declination * DEGREES_TO_RADIANS;
        Log.d("Altitude and Azimuth", "Declination in radians is: " + declination_rad);
        double hourAngle_rad = hourAngle * DEGREES_TO_RADIANS;
        Log.d("Altitude and Azimuth", "Hour Angle in radians is: " + hourAngle_rad);
        double term1 = Math.sin(latitude_rad) * Math.sin(declination_rad);
        double term2 = Math.cos(latitude_rad) * Math.cos(declination_rad) * Math.cos(hourAngle_rad);
        Log.d("Altitude and Azimuth", "Term 1 (sin(latitude-in-radians) * sin(declination-in-radians)): " + term1);
        Log.d("Altitude and Azimuth", "Term 2 (cos(latitude-in-radians) * cos(declination-in-radians) * cos(hour-angle-in-radians)): " + term2);
        double sinAltitude = term1 + term2;
        Log.d("Altitude and Azimuth", "sinAltitude = term1 + term2 = " + sinAltitude);
        double altitude_rad = Math.asin(sinAltitude);
        Log.d("Altitude and Azimuth", "Altitude, in radians, is: " + altitude_rad);
        double altitude_deg = altitude_rad * RADIANS_TO_DEGREES;
        Log.d("Altitude and Azimuth", "Altitude, in degrees, is: " + altitude_deg);
        double cosAzimuth = (Math.sin(declination_rad) - Math.sin(altitude_rad) * Math.sin(latitude_rad))
                / (Math.cos(altitude_rad) * Math.cos(latitude_rad));
        Log.d("Altitude and Azimuth", "cosAzimuth = (sin(declination-in-radians) - sin(altitude-in-radians) * sin(latitude-in-radians)) / (cos(altitude-in-radians) * cos(latitude-in-radians)) = " + cosAzimuth);
        double sinAzimuth = -Math.cos(declination_rad) * Math.sin(hourAngle_rad) / Math.cos(altitude_rad);
        Log.d("Altitude and Azimuth", "sinAzimuth = -(cos(declination-in-radians)) * sin(hour-angle-in-radians) / cos(altitude-in-radians) = " + sinAzimuth);
        double azimuth_rad = Math.atan2(sinAzimuth, cosAzimuth);
        Log.d("Altitude and Azimuth", "Azimuth-in-radians = atan2(sinAzimuth, cosAzimuth) = " + azimuth_rad);
        double azimuth_deg = (azimuth_rad * RADIANS_TO_DEGREES + 360) % 360;
        Log.d("Altitude and Azimuth", "Azimuth-in-degrees = " + azimuth_deg);
        Log.d("Altitude and Azimuth", "Hence the altitude and azimuth are: {" + altitude_deg + ", " + azimuth_deg + "}");
        Log.d("Altitude and Azimuth", "                                                 ");
        return new double[]{altitude_deg, azimuth_deg};


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