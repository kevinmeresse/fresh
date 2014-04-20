package com.km.fresh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

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
    private static final String DATA_RAW = "raw";
    private static final String DATA_COUNT = "count";

    //#### Vars ####
    private float lastX  = 0.0f;
    private float lastY  = 0.0f;
    private float lastZ  = 0.0f;
    private float lastXDiff  = 0.0f;
    private float lastYDiff  = 0.0f;
    private float lastZDiff  = 0.0f;
    private int simpleThresholdCount = 0;
    private int changeDirectionCount = 0;
    private int changeDirectionThresholdCount = 0;
    private int positiveNegativeCount = 0;
    private int averageOfSevenPreviousCount = 0;
    private float totalMovementCount = 0f;
    private long tsOfLastFlush = 0l;
    private static final long uniqueFileId = System.currentTimeMillis();
    private Queue<Integer> sevenPrevious = new LinkedList<Integer>();

    @Override
    public void onSensorChanged(SensorEvent event) {

        long now = System.currentTimeMillis();
        final float x = event.values[0];
        final float y = event.values[1];
        final float z = event.values[2];

        // Write logs for RAW data
        logDataToSdCard(DATA_RAW, now + "," + x + "," + y + "," + z + "\n");

        treatEvent(now, x, y, z);
    }

    public void treatEvent(final long now, final float x, final float y, final float z) {

        // Increment when moving more than threshold
        if (Math.abs(x - lastX) > MOVEMENT_THRESHOLD
                || Math.abs(y - lastY) > MOVEMENT_THRESHOLD
                || Math.abs(z - lastZ) > MOVEMENT_THRESHOLD) {
            simpleThresholdCount++;
        }

        // Increment when changing movement direction
        if (((x - lastX) > 0 && lastXDiff < 0)
                || ((y - lastY) > 0 && lastYDiff < 0)
                || ((z - lastZ) > 0 && lastZDiff < 0)
                || ((x - lastX) < 0 && lastXDiff > 0)
                || ((y - lastY) < 0 && lastYDiff > 0)
                || ((z - lastZ) < 0 && lastZDiff > 0)) {
            changeDirectionCount++;
        }

        // Increment when changing movement direction AND movement is more than threshold
        if (((((x - lastX) > 0 && lastXDiff < 0)
                    || ((x - lastX) < 0 && lastXDiff > 0))
                        && Math.abs(x - lastX) > MOVEMENT_THRESHOLD)
            ||
            ((((y - lastY) > 0 && lastYDiff < 0)
                || ((y - lastY) < 0 && lastYDiff > 0))
                    && Math.abs(y - lastY) > MOVEMENT_THRESHOLD)
            ||
            ((((z - lastZ) > 0 && lastZDiff < 0)
                || ((z - lastZ) < 0 && lastZDiff > 0))
                    && Math.abs(z - lastZ) > MOVEMENT_THRESHOLD)) {
            changeDirectionThresholdCount++;
        }

        // Increment when switching from positive to negative (and vice-versa)
        if ((x > 0 && lastX < 0)
                || (y > 0 && lastY < 0)
                || (z > 0 && lastZ < 0)
                || (x < 0 && lastX > 0)
                || (y < 0 && lastY > 0)
                || (z < 0 && lastZ > 0)) {
            positiveNegativeCount++;
        }

        // Add up all the movements to get a sense of how much you moved in total
        totalMovementCount += Math.abs(x - lastX);
        totalMovementCount += Math.abs(y - lastY);
        totalMovementCount += Math.abs(z - lastZ);

//        Log.d(TAG, "onSensorChanged: Values: "
//                + "x:" + Math.abs(event.values[0] - lastX)
//                + ",\ty:" + Math.abs(event.values[1] - lastY)
//                + ",\tz:" + Math.abs(event.values[2] - lastZ)
//                + ",\tcount:" + mCount
//                + ",\tts diff:" + (now - tsOfLastFlush)
//                + ",\tnow:" + now
//                + ",\ttsOfLastFlush:" + tsOfLastFlush);

        // Remember values
        lastXDiff = x - lastX;
        lastYDiff = y - lastY;
        lastZDiff = z - lastZ;
        lastX = x;
        lastY = y;
        lastZ = z;

        if (tsOfLastFlush == 0l) {
            tsOfLastFlush = now;
        }

        // Flush count every FLUSH_INTERVAL time
        if ((now - tsOfLastFlush) > FLUSH_INTERVAL) {
            tsOfLastFlush = now;

            // Calculate average for 7 previous minutes
            averageOfSevenPreviousCount = calculateSevenPreviousAverage();

            // Write logs for COUNT
            Log.d(TAG, "FLUSH COUNT: " + now + " | " + "Simple threshold = " + simpleThresholdCount + " \t Change direction = " + changeDirectionCount + " \t Change direction + threshold = " + changeDirectionThresholdCount + " \t Positive to negative = " + positiveNegativeCount + " \t Average of 7 previous = " + averageOfSevenPreviousCount + " \t total movement = " + totalMovementCount);
            logDataToSdCard(DATA_COUNT, now + "," + simpleThresholdCount + "," + changeDirectionCount + "," + changeDirectionThresholdCount + "," + positiveNegativeCount + "," + averageOfSevenPreviousCount + "," + totalMovementCount  + "\n");

            // Reset count values
            simpleThresholdCount = 0;
            changeDirectionCount = 0;
            changeDirectionThresholdCount = 0;
            positiveNegativeCount = 0;
            totalMovementCount = 0;
        }
    }

    public void logDataToSdCard(String dataType, String line) {
        FileOutputStream fos = null;

        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fresh/" );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File myFile = null;
            if (DATA_RAW.equals(dataType)) {
                myFile = new File(dir, "Fresh_data_raw_" + uniqueFileId + ".csv");
            } else {
                myFile = new File(dir, "Fresh_data_count_" + uniqueFileId + ".csv");
            }

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

    public int calculateSevenPreviousAverage() {
        sevenPrevious.add(simpleThresholdCount);
        if (sevenPrevious.size() == 0) {
            return simpleThresholdCount;
        } else if (sevenPrevious.size() > 7) {
            sevenPrevious.poll();
        }
        int sum = 0;
        for (Integer value : sevenPrevious) {
            sum += value;
        }

        return sum / sevenPrevious.size();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
