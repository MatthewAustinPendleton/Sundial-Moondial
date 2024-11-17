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
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    private double pitch = 0.0;
    private double roll = 0.0;

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
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lastAccelerometerValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            lastMagnetometerValues = event.values.clone();
        }

        if (lastAccelerometerValues != null && lastMagnetometerValues != null) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometerValues, lastMagnetometerValues)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles);

                // Convert from radians to degrees
                pitch = Math.toDegrees(orientationAngles[1]); // Pitch is the second angle
                roll = Math.toDegrees(orientationAngles[2]);  // Roll is the third angle

                //Log.d(TAG, "Pitch: " + pitch + ", Roll: " + roll);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // I don't need this
    }

    public double getPitch() {
        return pitch;
    }

    public double getRoll() {
        return roll;
    }
}
