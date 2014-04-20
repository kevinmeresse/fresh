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
    private long tsOfLastFlush = 0l;
    private static final long uniqueFileId = System.currentTimeMillis();
    private Queue<Integer> sevenPrevious = new LinkedList<Integer>();

    @Override
    public void onSensorChanged(SensorEvent event) {

        long now = System.currentTimeMillis();

        // Increment when moving more than threshold
        if (Math.abs(event.values[0] - lastX) > MOVEMENT_THRESHOLD
                || Math.abs(event.values[1] - lastY) > MOVEMENT_THRESHOLD
                || Math.abs(event.values[2] - lastZ) > MOVEMENT_THRESHOLD) {
        	simpleThresholdCount++;
        }

        // Increment when changing movement direction
        if (((event.values[0] - lastX) > 0 && lastXDiff < 0)
                || ((event.values[1] - lastY) > 0 && lastYDiff < 0)
                || ((event.values[2] - lastZ) > 0 && lastZDiff < 0)
                || ((event.values[0] - lastX) < 0 && lastXDiff > 0)
                || ((event.values[1] - lastY) < 0 && lastYDiff > 0)
                || ((event.values[2] - lastZ) < 0 && lastZDiff > 0)) {
        	changeDirectionCount++;
        }

        // Increment when changing movement direction AND movement is more than threshold
        if (((((event.values[0] - lastX) > 0 && lastXDiff < 0)
                    || ((event.values[0] - lastX) < 0 && lastXDiff > 0))
                        && Math.abs(event.values[0] - lastX) > MOVEMENT_THRESHOLD)
            ||
            ((((event.values[1] - lastY) > 0 && lastYDiff < 0)
                || ((event.values[1] - lastY) < 0 && lastYDiff > 0))
                    && Math.abs(event.values[1] - lastY) > MOVEMENT_THRESHOLD)
            ||
            ((((event.values[2] - lastZ) > 0 && lastZDiff < 0)
                || ((event.values[2] - lastZ) < 0 && lastZDiff > 0))
                    && Math.abs(event.values[2] - lastZ) > MOVEMENT_THRESHOLD)) {
        	changeDirectionThresholdCount++;
        }

        // Increment when switching from positive to negative (and vice-versa) 
        if ((event.values[0] > 0 && lastX < 0)
                || (event.values[1] > 0 && lastY < 0)
                || (event.values[2] > 0 && lastZ < 0)
                || (event.values[0] < 0 && lastX > 0)
                || (event.values[1] < 0 && lastY > 0)
                || (event.values[2] < 0 && lastZ > 0)) {
        	positiveNegativeCount++;
        }
        
        // Write logs for RAW data
        logDataToSdCard(DATA_RAW, now + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");

//        Log.d(TAG, "onSensorChanged: Values: "
//                + "x:" + Math.abs(event.values[0] - lastX)
//                + ",\ty:" + Math.abs(event.values[1] - lastY)
//                + ",\tz:" + Math.abs(event.values[2] - lastZ)
//                + ",\tcount:" + mCount
//                + ",\tts diff:" + (now - tsOfLastFlush)
//                + ",\tnow:" + now
//                + ",\ttsOfLastFlush:" + tsOfLastFlush);

        // Remember values
        lastXDiff = event.values[0] - lastX;
        lastYDiff = event.values[1] - lastY;
        lastZDiff = event.values[2] - lastZ;
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        if (tsOfLastFlush == 0l) {
            tsOfLastFlush = now;
        }
        
        // Flush count every FLUSH_INTERVAL time
        if ((now - tsOfLastFlush) > FLUSH_INTERVAL) {
            tsOfLastFlush = now;
            
            // Calculate average for 7 previous minutes
            averageOfSevenPreviousCount = calculateSevenPreviousAverage();
            
            // Write logs for COUNT
            Log.d(TAG, "FLUSH COUNT: " + now + " | " + "Simple threshold = " + simpleThresholdCount + " \t Change direction = " + changeDirectionCount + " \t Change direction + threshold = " + changeDirectionThresholdCount + " \t Positive to negative = " + positiveNegativeCount + " \t Average of 7 previous = " + averageOfSevenPreviousCount);
            logDataToSdCard(DATA_COUNT, now + "," + simpleThresholdCount + "," + changeDirectionCount + "," + changeDirectionThresholdCount + "," + positiveNegativeCount + "," + averageOfSevenPreviousCount + "\n");
            
            // Reset count values
            simpleThresholdCount = 0;
            changeDirectionCount = 0;
            changeDirectionThresholdCount = 0;
            positiveNegativeCount = 0;
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
