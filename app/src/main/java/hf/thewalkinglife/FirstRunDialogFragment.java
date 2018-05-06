package hf.thewalkinglife;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirstRunDialogFragment extends DialogFragment {

    private static final String TAG = "FirstRunDialogFragment";
    private FirstRunFinishedListener listener;

    @BindView(R.id.firstRunName) EditText firstRunName;
    @BindView(R.id.firstRunDailyGoal) EditText firstRunDailyGoal;
    @BindView(R.id.firstRunStride) EditText firstRunStride;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (FirstRunFinishedListener) getTargetFragment();
        } catch (ClassCastException ce) {
            Log.e(TAG, "Parent Fragment does not implement interface!");
        } catch (Exception e) {
            Log.e(TAG, "Unhandled exception!");
            e.printStackTrace();
        }
    }

    @OnClick(R.id.saveButton)
    public void save() {
        String username = firstRunName.getText().toString();
        String dailyGoal = firstRunDailyGoal.getText().toString();
        String stride = firstRunStride.getText().toString();

        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            firstRunName.setError("Username is required!");
            valid = false;
        }
        if (TextUtils.isEmpty(dailyGoal)) {
            firstRunDailyGoal.setError("Daily goal is required!");
            valid = false;
        }
        if (TextUtils.isEmpty(stride)) {
            firstRunStride.setError("Stride is required!");
            valid = false;
        }

        if (valid) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PreferenceConstants.USER_NAME, username);
            editor.putString(PreferenceConstants.DAILY_GOAL, dailyGoal);
            editor.putString(PreferenceConstants.STRIDE, stride);
            editor.putBoolean(PreferenceConstants.STEPS_ENABLED, PreferenceConstants.STEPS_ENABLED_DEFAULT);
            editor.putBoolean(PreferenceConstants.NOTIFICATIONS_ENABLED, PreferenceConstants.NOTIFICATIONS_ENABLED_DEFAULT);
            editor.apply();

            if (listener != null) {
                listener.onFirstRunFinished();
            }

            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first_run_dialog, container, false);
        ButterKnife.bind(this, view);

        getDialog().setTitle(R.string.first_run_dialog_title);
        return view;
    }

    public interface FirstRunFinishedListener {
        void onFirstRunFinished();
    }

}
