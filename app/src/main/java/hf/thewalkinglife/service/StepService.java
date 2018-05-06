package hf.thewalkinglife.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import hf.thewalkinglife.MainActivity;
import hf.thewalkinglife.PreferenceConstants;
import hf.thewalkinglife.R;
import hf.thewalkinglife.StepsApplication;
import hf.thewalkinglife.db.StepDataDbManager;

public class StepService extends Service implements SensorEventListener {
    private static final String TAG = "StepService";

    private static final int DAILY_GOAL_FINISHED_NOTIFICATION_ID = 101;
    private static final int SERVICE_RUNNING_NOTIFICATION_ID = 102;
    private static final String NOTIF_CHANNEL_ID = "step_service";

    public static final String BR_NEW_STEP = "BR_NEW_STEP";
    public static final String KEY_STEP_COUNT = "KEY_STEP_COUNT";

    private SensorManager sensorManager;
    private Sensor sensor;
    private StepDataDbManager stepDataDbManager;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null && sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            stepDataDbManager = StepsApplication.getDbManager();
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notifMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            CharSequence name = getString(R.string.channel_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(NOTIF_CHANNEL_ID, name, importance);
            if (notifMan != null) {
                notifMan.createNotificationChannel(mChannel);
            }
        }

        if (isNotificationsEnabled()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIF_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notif_step_service_running))
                    .setContentIntent(getContentIntent())
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setAutoCancel(true)
                    .setOngoing(true);
            startForeground(SERVICE_RUNNING_NOTIFICATION_ID, builder.build());
        }

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
            Intent intent = new Intent(BR_NEW_STEP);
            intent.putExtra(KEY_STEP_COUNT, newStepCount);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            if (isNotificationsEnabled()) {
                makeNotification(newStepCount);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void makeNotification(int stepCount) {
        String dailyGoalStr = sharedPreferences.getString(PreferenceConstants.DAILY_GOAL, PreferenceConstants.DEFAULT_DAILY_GOAL);
        int dailyGoal = Integer.parseInt(dailyGoalStr);

        if (dailyGoal == stepCount) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIF_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(getString(R.string.notif_finish_title))
                    .setContentText(getString(R.string.notif_finish_text))
                    .setVibrate(new long[]{1000,2000,1000})
                    .setContentIntent(getContentIntent())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(DAILY_GOAL_FINISHED_NOTIFICATION_ID, builder.build());
            }
        }
    }

    private boolean isNotificationsEnabled() {
        return sharedPreferences.getBoolean(PreferenceConstants.NOTIFICATIONS_ENABLED, PreferenceConstants.NOTIFICATIONS_ENABLED_DEFAULT);
    }

    private PendingIntent getContentIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(this,
                DAILY_GOAL_FINISHED_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
