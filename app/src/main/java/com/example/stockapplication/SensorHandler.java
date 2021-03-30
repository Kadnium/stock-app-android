package com.example.stockapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SensorHandler {
    Context context;
    SensorManager sensorManager;
    private SensorEventListener accelometerListener;
    private SensorEventListener lightSensorListener;

    private final int LIGHT_THRESHOLD = 25;
    private final int SHAKE_THRESHOLD = 400;

    boolean firstRun = true;
    float lastX,lastY,lastZ;
    private long lastAccelometerUpdate = 0;
    private long lastLightUpdate = 0;
    private final ArrayList<Boolean> ligthList = new ArrayList<>();

    private HelperCallback onShakeCallback;
    public SensorHandler(Context context,boolean accelometerEnabled,boolean lightSensorEnabled) {
        this.context = context;
        if(context instanceof AppCompatActivity){
            sensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            setAccelometer(accelometerEnabled);
            setLightSensor(lightSensorEnabled);

        }

    }

    public void setOnShakeCallback(HelperCallback onShakeCallback) {
        this.onShakeCallback = onShakeCallback;
    }
    /**
     * Helper function for refreshing sensors
     */
    public void updateSensors(boolean accelometerEnabled,boolean lightSensorEnabled){
        if(sensorManager != null){
            setAccelometer(accelometerEnabled);
            setLightSensor(lightSensorEnabled);
        }
    }
    /**
     * Register / unregister accelometer sensor
     */
    private void setAccelometer(boolean accelometerEnabled){
        // If accelometer sensor is enabled from settings, create EventListener for accelometer
        // If it's not enabled check if there are old instances of EventListener -> delete
        // Else do nothing
        if(accelometerEnabled && accelometerListener == null){
            Sensor accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            AppCompatActivity act = (AppCompatActivity) context;
            accelometerListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    // Keep track of times to prevent function doing checks too fast
                    long curTime = System.currentTimeMillis();
                    if ((curTime - lastAccelometerUpdate) > 100) {
                        long diffTime = (curTime - lastAccelometerUpdate);
                        lastAccelometerUpdate = curTime;
                        // Calculate shake intensity
                        float speed = Math.abs(x + y + z - lastX - lastY - lastZ)/ diffTime * 10000;
                        // If goes over threshold and let only first pass through
                        if (speed > SHAKE_THRESHOLD && firstRun) {
                            firstRun = false;
                            // Prevent calling this multiple times by timer because it enters here
                            // Multiple times if shaking
                            // Run on ui thread because will crash app otherwise
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    act.runOnUiThread(() -> {
                                        if(onShakeCallback != null){
                                            onShakeCallback.onComplete();
                                        }
                                        firstRun = true;
                                    });
                                }
                            }, 1500);
                        }
                        lastX = x;
                        lastY = y;
                        lastZ = z;
                    }

                    }


                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            sensorManager.registerListener(accelometerListener,accelometer,SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            if(accelometerListener != null){
                sensorManager.unregisterListener(accelometerListener);
                accelometerListener = null;
            }
        }


    }

    /**
     * Register / unregister lightsensor
     */
    private void setLightSensor(boolean lightSensorEnabled){
        // If light sensor is enabled from settings, create EventListener for light sensor
        // If it's not enabled check if there are old instances of EventListener -> delete
        // Else do nothing
        if(lightSensorEnabled && lightSensorListener == null){
            Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            lightSensorListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float x = event.values[0];
                    // Keep track of times so function doesn't do checks too fast
                    long curTime = System.currentTimeMillis();
                    if ((curTime - lastLightUpdate) > 100) {
                        lastLightUpdate = curTime;

                        if(x<LIGHT_THRESHOLD){
                            // UNDER THRESHOLD = true
                            // OVER THRESHOLD = false
                            // If under threshold  -> check if last element in ligthList is also under threshold
                            // If not, clear the list and add true to list
                            if(ligthList.size()>0){
                                if(!ligthList.get(ligthList.size()-1)){
                                    ligthList.clear();
                                }
                            }
                            ligthList.add(true);
                            // If current theme is not dark theme, ligthList has over 15 elements and all are under threshold -> change theme to dark
                            if(AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES && ligthList.size()>15 && ligthList.get(ligthList.size()-1) ){
                                // position 1 DARK MODE
                                AppData.setSettingToPrefs(context,AppData.SELECTED_THEME,1);
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                AppCompatActivity act = (AppCompatActivity) context;
                                act.recreate();
                            }
                        }else{
                            // If over threshold  -> check if last element in ligthList is also over threshold
                            // If not, clear the list and add false to list
                            if(ligthList.size()>0){
                                if(ligthList.get(ligthList.size()-1)){
                                    ligthList.clear();
                                }
                            }
                            ligthList.add(false);
                            // If current theme is not light theme, ligthList has over 15 elements and all are over threshold -> change theme to light
                            if(AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO  && ligthList.size()>15 && !ligthList.get(ligthList.size()-1)){
                                // position 2 LIGHT MODE
                                AppData.setSettingToPrefs(context,AppData.SELECTED_THEME,2);
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                AppCompatActivity act = (AppCompatActivity) context;
                                act.recreate();
                            }
                        }

                        if(ligthList.size()>15){
                            ligthList.clear();
                        }
                    }

                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorManager.registerListener(lightSensorListener,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            if(lightSensorListener != null){
                sensorManager.unregisterListener(lightSensorListener);
                lightSensorListener = null;
            }
        }

    }

    /**
     * Helper function to unregister EventListeners
     */
    public void unRegisterSensors(){
        if(accelometerListener != null){
            sensorManager.unregisterListener(accelometerListener);
            accelometerListener = null;
        }
        if(lightSensorListener != null){
            sensorManager.unregisterListener(lightSensorListener);
            lightSensorListener = null;
        }


    }


}
