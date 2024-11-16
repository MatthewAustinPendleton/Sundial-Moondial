package com.example.sundial;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationManager implements SensorEventListener {

    private static final String TAG = "OrientationManager";
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magnetometer;

    private float[] lastAccelerometerValues;
    private float[] lastMagnetometerValues;
    private long lastAccelerometerUpdateTime;
    private long lastMagnetometerUpdateTime;
    private static final long UPDATE_THRESHOLD_IN_MILLISECONDS = 500;
    private static final float SIGNIFICANT_CHANGE_THRESHOLD = 0.5f;

    public OrientationManager(Context context) {

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    public void startListening() {

        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (magnetometer != null) sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

    }

    public void stopListening() {

        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerData(sensorEvent);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            handleMagnetometerData(sensorEvent);
        }
    }

    private void handleAccelerometerData(SensorEvent sensorEvent) {

        long currentTime = System.currentTimeMillis();

        if (lastAccelerometerValues == null ||
                (currentTime - lastAccelerometerUpdateTime > UPDATE_THRESHOLD_IN_MILLISECONDS &&
                        hasSignificantChange(sensorEvent.values, lastAccelerometerValues))) {

            lastAccelerometerUpdateTime = currentTime;
            lastAccelerometerValues = sensorEvent.values.clone();

            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];

            Log.d(TAG, "Accelerometer data: (" + x + ", " + y + ", " + z + ")");

        }

    }

    private void handleMagnetometerData(SensorEvent sensorEvent) {

        long currentTime = System.currentTimeMillis();

        if (lastMagnetometerValues == null || (currentTime - lastAccelerometerUpdateTime > UPDATE_THRESHOLD_IN_MILLISECONDS &&
                hasSignificantChange(sensorEvent.values, lastMagnetometerValues))) {

            lastMagnetometerUpdateTime = currentTime;
            lastMagnetometerValues = sensorEvent.values.clone();

            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];

            Log.d(TAG, "Magnetometer data: (" + x + ", " + y + ", " + z + ")");

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Stubbed, don't actually need this method
    }

    private boolean hasSignificantChange(float[] newValues, float[] oldValues) {
        if (oldValues == null) {
            return true;
        }

        float dx = Math.abs(newValues[0] - oldValues[0]);
        float dy = Math.abs(newValues[1] - oldValues[1]);
        float dz = Math.abs(newValues[2] - oldValues[2]);

        return (dx > SIGNIFICANT_CHANGE_THRESHOLD || dy > SIGNIFICANT_CHANGE_THRESHOLD || dz > SIGNIFICANT_CHANGE_THRESHOLD);
    }

}