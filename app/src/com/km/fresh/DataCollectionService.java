package com.km.fresh;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class DataCollectionService extends IntentService {
    private static final String TAG = "DataCollectSrvc";

    private SensorManager mSensorManager = null;
    private PowerManager mPowerManager = null;
    private WakeLock mWakeLock = null;
    private SensorEventListener mFreshSensorListener = null;

    //###################################################################
    //# Constructor
    //###################################################################

    public DataCollectionService() {
        super(TAG);
    }

    public DataCollectionService(String name) {
        super(name);
    }

    //###################################################################
    //# Life cycle
    //###################################################################

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        // ----- Register this activity as a SensorEventListener for the Accelerometer sensor ------------
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass()
            .getName());

        //Keep the CPU on.
        mWakeLock.acquire();

        Log.d(TAG, "mSensorManager = " + mSensorManager);
        Log.d(TAG, "mPowerManager = " + mPowerManager);
        Log.d(TAG, "mWakeLock = " + mWakeLock);

        if (mSensorManager != null) {
            //Start listening to the accelerometer
            Log.d(TAG, "Start listening to the accelerometer.");
            Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mFreshSensorListener = new FreshSensorEventListener();
            Log.d(TAG, "mFreshSensorListener = " + mFreshSensorListener);
            boolean worked = mSensorManager.registerListener(mFreshSensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "It worked = " + worked);
        }
    }

    @Override
    public void onDestroy() {
        // Let the phone sleep.
        mWakeLock.release();

        if (mSensorManager != null && mFreshSensorListener != null) {
            mSensorManager.unregisterListener(mFreshSensorListener);
        }

        super.onDestroy();
    }

    //###################################################################
    //# Other
    //###################################################################


}
