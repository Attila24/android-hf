package hf.thewalkinglife;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import hf.thewalkinglife.db.LoadTodayStepTask;
import hf.thewalkinglife.db.StepDataDbManager;
import hf.thewalkinglife.service.StepService;

public class StepsFragment extends Fragment {
    private static final String TAG = "StepsFragment";

    private LoadTodayStepTask loadTodayStepTask;
    private StepDataDbManager dbManager;
    private SharedPreferences sharedPreferences;

    private String dailyGoal;

    @BindView(R.id.steps_progress) CircleProgressView stepsProgress;
    @BindView(R.id.steps_daily_goal) TextView dailyGoalText;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_steps, container, false);
        ButterKnife.bind(this, view);
        dbManager = StepsApplication.getDbManager();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(StepService.BR_NEW_STEP));
        refreshStepCount();
        refreshDailyGoal();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);

        if (loadTodayStepTask != null) {
            loadTodayStepTask.cancel(false);
        }
    }

    private void refreshDailyGoal() {
        dailyGoal = sharedPreferences.getString(PreferenceConstants.DAILY_GOAL, PreferenceConstants.DEFAULT_DAILY_GOAL);
        stepsProgress.setMaxValue(Float.valueOf(dailyGoal));
        dailyGoalText.setText(String.format("Your daily goal: %s", dailyGoal));
    }

    private void refreshStepCount() {
        if (loadTodayStepTask != null) {
            loadTodayStepTask.cancel(false);
        }
        loadTodayStepTask = new LoadTodayStepTask(this, dbManager);
        loadTodayStepTask.execute();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int steps = intent.getIntExtra(StepService.KEY_STEP_COUNT, 0);
            Log.d(TAG, "Received new steps count = " + String.valueOf(steps));
            setStepCountText(String.valueOf(steps));
        }
    };

    public void setStepCountText(String steps) {
        Log.d(TAG, "setting step count text = " + steps);

        float value = steps != null ? Float.parseFloat(steps) : 0f;
        stepsProgress.setValue(value);
    }
}
