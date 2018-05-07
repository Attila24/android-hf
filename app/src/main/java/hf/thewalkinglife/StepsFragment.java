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

/**
 * The fragment showing the current step count in a pregress circle and displaying the user's daily goal.
 */
public class StepsFragment extends Fragment implements LoadTodayStepTask.TodayStepLoaderFragment {
    private static final String TAG = "StepsFragment";

    private LoadTodayStepTask loadTodayStepTask;
    private StepDataDbManager dbManager;
    private SharedPreferences sharedPreferences;

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

    /**
     * Registers to the StepService's broadcast events to react whenever a new step has occured.
     */
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(StepService.BR_NEW_STEP));
        refreshStepCount();
        refreshDailyGoal();
    }

    /**
     * Unregisters from the StepService.
     * If there was an async task still running, stops it.
     */
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);

        if (loadTodayStepTask != null) {
            loadTodayStepTask.cancel(false);
        }
    }

    public void refreshDailyGoal() {
        String dailyGoal = sharedPreferences.getString(PreferenceConstants.DAILY_GOAL, PreferenceConstants.DEFAULT_DAILY_GOAL);
        stepsProgress.setMaxValue(Float.valueOf(dailyGoal));
        dailyGoalText.setText(String.format("Your daily goal: %s", dailyGoal));
    }

    /**
     * Starts a new async task that will load today's step data.
     */
    private void refreshStepCount() {
        if (loadTodayStepTask != null) {
            loadTodayStepTask.cancel(false);
        }
        loadTodayStepTask = new LoadTodayStepTask(this, dbManager);
        loadTodayStepTask.execute();
    }

    /**
     * Receives events from the StepService.
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int steps = intent.getIntExtra(StepService.KEY_STEP_COUNT, 0);
            setStepCount(String.valueOf(steps));
        }
    };

    /**
     * Runs whenever the current step count has changed.
     * The circle progressbar requires the value to be float.
     */
    @Override
    public void setStepCount(String stepCount) {
        float value = stepCount != null ? Float.parseFloat(stepCount) : 0f;
        stepsProgress.setValue(value);
    }
}
