package com.km.fresh;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity {\
    private static final String TAG = "FRESH";

    //###################################################################
    //# Life cycle
    //###################################################################

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        Log.d(TAG , "Starting data collection service.");

        //Start the data collection service
        Intent in = new Intent(getApplicationContext(), DataCollectionService.class);
        startService(in);
        Log.d(TAG , "intent = " + in);
    }

    @Override
    protected void onResume() {
        super.onResume();

         SensorManager mSensorManager = null;
         PowerManager mPowerManager = null;
         WakeLock mWakeLock = null;
         SensorEventListener mFreshSensorListener = null;

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
            boolean worked = mSensorManager.registerListener(mFreshSensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            Log.d(TAG, "It worked = " + worked);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //###################################################################
    //# Overflow menu
    //###################################################################

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //###################################################################
    //# Fragment
    //###################################################################

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }
    }

}
