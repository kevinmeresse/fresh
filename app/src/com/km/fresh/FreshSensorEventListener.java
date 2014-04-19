package com.km.fresh;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class FreshSensorEventListener implements SensorEventListener {
    private static final String TAG = "SensorListener";

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged: Values: "
                + "x:" + event.values[0]
                + ", y:" + event.values[1]
                + ", z:" + event.values[2]
                + " Type: " + getNameForSensorType(event.sensor.getType()));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private String getNameForSensorType(int type) {
        switch (type) {
        case Sensor.TYPE_ACCELEROMETER:
            return "Accelerometer";
        case Sensor.TYPE_GYROSCOPE:
            return "Gyroscope";
        case Sensor.TYPE_MAGNETIC_FIELD:
            return "Magnetic Field";
        }
        return "Other";
    }
}
