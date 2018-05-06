package hf.thewalkinglife.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import hf.thewalkinglife.StepsApplication;
import hf.thewalkinglife.db.StepDataDbManager;

public class StepService extends Service implements SensorEventListener {
    public static final String BR_NEW_STEP = "BR_NEW_STEP";
    public static final String KEY_STEP_COUNT = "KEY_STEP_COUNT";
    private static final String TAG = "StepService";

    private SensorManager sensorManager;
    private Sensor sensor;
    private StepDataDbManager stepDataDbManager;


    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null && sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            stepDataDbManager = StepsApplication.getDbManager();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int newStepCount = stepDataDbManager.createStepData();
        if (newStepCount != -1) {
            Log.d(TAG, "new step count" + String.valueOf(newStepCount));
            Intent intent = new Intent(BR_NEW_STEP);
            intent.putExtra(KEY_STEP_COUNT, newStepCount);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
