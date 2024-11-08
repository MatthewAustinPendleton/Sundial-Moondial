package com.example.sundial;
import java.util.Calendar;

public class SolarCalculator {

    private final double latitude;
    private final double longitude;
    private final Calendar dateTime;

    public SolarCalculator(double latitude, double longitude, Calendar dateTime) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;

    }

}