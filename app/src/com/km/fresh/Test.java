package com.km.fresh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.util.Log;

public class Test {
    private static final String TAG = "Test";

    public static void fakeEventsFromFile(String path, FreshSensorEventListener fsel) {
        BufferedReader reader = null;

        Log.d(TAG, "fakeEventsFromFile path: " + path);

        try {
            File myFile = new File(path);
            if (!myFile.exists()) {
                Log.e(TAG, "No such file or directory: " + path);
                return;
            }

            reader = new BufferedReader(new FileReader(myFile));

            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(",");
                if (values != null && values.length > 3) {
                    fsel.treatEvent(Long.parseLong(values[0]),
                                    Float.parseFloat(values[1]),
                                    Float.parseFloat(values[2]),
                                    Float.parseFloat(values[3]));
//                    Log.d(TAG, "Read line: x=" + Float.parseFloat(values[1]) + " "
//                                        + "y=" + Float.parseFloat(values[2]) + " "
//                                        + "z=" + Float.parseFloat(values[3]));
                } else {
                    Log.e(TAG, "Unable to parse this line: " + line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while trying to fake events from file: " + path);
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

