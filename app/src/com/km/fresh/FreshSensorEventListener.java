package com.km.fresh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Environment;
import android.util.Log;

public class FreshSensorEventListener implements SensorEventListener {
    //#### Constants ####
    private static final String TAG = "SensorListener";
    private static final float MOVEMENT_THRESHOLD = 0.1f;
    private static final float FLUSH_INTERVAL = 60000; //1 minute

    //#### Vars ####
    private float lastX  = 0.0f;
    private float lastY  = 0.0f;
    private float lastZ  = 0.0f;
    private float lastXDiff  = 0.0f;
    private float lastYDiff  = 0.0f;
    private float lastZDiff  = 0.0f;
    private int mCountMaj = 0;
    private int mCountMaj2 = 0;
    private int mCountMaj3 = 0;
    private int mCountKev = 0;
    private long tsOfLastFlush = 0l;

    @Override
    public void onSensorChanged(SensorEvent event) {
//        Log.d(TAG, "onSensorChanged: Values: "
//                + "x:" + event.values[0]
//                + ", y:" + event.values[1]
//                + ", z:" + event.values[2]
//                + " Type: " + getNameForSensorType(event.sensor.getType()));

        long now = System.currentTimeMillis();

        if (Math.abs(event.values[0] - lastX) > MOVEMENT_THRESHOLD
                || Math.abs(event.values[1] - lastY) > MOVEMENT_THRESHOLD
                || Math.abs(event.values[2] - lastZ) > MOVEMENT_THRESHOLD) {
            mCountMaj++;
        }

        if (((event.values[0] - lastX) > 0 && lastXDiff < 0)
                || ((event.values[1] - lastY) > 0 && lastYDiff < 0)
                || ((event.values[2] - lastZ) > 0 && lastZDiff < 0)
                || ((event.values[0] - lastX) < 0 && lastXDiff > 0)
                || ((event.values[1] - lastY) < 0 && lastYDiff > 0)
                || ((event.values[2] - lastZ) < 0 && lastZDiff > 0)){
            mCountMaj2++;
        }

        if (
            ((((event.values[0] - lastX) > 0 && lastXDiff < 0)
                    || ((event.values[0] - lastX) < 0 && lastXDiff > 0))
                        && Math.abs(event.values[0] - lastX) > MOVEMENT_THRESHOLD)
            ||
            ((((event.values[1] - lastY) > 0 && lastYDiff < 0)
                || ((event.values[1] - lastY) < 0 && lastYDiff > 0))
                    && Math.abs(event.values[1] - lastY) > MOVEMENT_THRESHOLD)
            ||
            ((((event.values[2] - lastZ) > 0 && lastZDiff < 0)
                || ((event.values[2] - lastZ) < 0 && lastZDiff > 0))
                    && Math.abs(event.values[2] - lastZ) > MOVEMENT_THRESHOLD)){
            mCountMaj3++;
        }

        if ((event.values[0] > 0 && lastX < 0)
                || (event.values[1] > 0 && lastY < 0)
                || (event.values[2] > 0 && lastZ < 0)
                || (event.values[0] < 0 && lastX > 0)
                || (event.values[1] < 0 && lastY > 0)
                || (event.values[2] < 0 && lastZ > 0)){
            mCountKev++;
        }

//        Log.d(TAG, "onSensorChanged: Values: "
//                + "x:" + Math.abs(event.values[0] - lastX)
//                + ",\ty:" + Math.abs(event.values[1] - lastY)
//                + ",\tz:" + Math.abs(event.values[2] - lastZ)
//                + ",\tcount:" + mCount
//                + ",\tts diff:" + (now - tsOfLastFlush)
//                + ",\tnow:" + now
//                + ",\ttsOfLastFlush:" + tsOfLastFlush);

        lastXDiff = event.values[0] - lastX;
        lastYDiff = event.values[1] - lastY;
        lastZDiff = event.values[2] - lastZ;
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        if (tsOfLastFlush == 0l) {
            tsOfLastFlush = now;
        }
        if ((now - tsOfLastFlush) > FLUSH_INTERVAL) {
            tsOfLastFlush = now;
            Log.d(TAG, "FLUSH: " + now + " | " + "countMaj = " + mCountMaj + " \t countMaj2 = " + mCountMaj2 + " \t countMaj3 = " + mCountMaj3 + " \t countKev = " + mCountKev);

            logDataToSdCard(now + "," + mCountMaj + "," + mCountMaj2 + "," + mCountMaj3 + "," + mCountKev + "\n");
            mCountMaj = 0;
            mCountMaj2 = 0;
            mCountMaj3 = 0;
            mCountKev = 0;
        }
    }

    public void logDataToSdCard(String line) {
        FileOutputStream fos = null;

        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fresh/" );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            final File myFile = new File(dir, "Fresh_data_" + System.currentTimeMillis() + ".txt");

            if (!myFile.exists()) {
                myFile.createNewFile();
            }

            fos = new FileOutputStream(myFile, true);
            fos.write(line.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
