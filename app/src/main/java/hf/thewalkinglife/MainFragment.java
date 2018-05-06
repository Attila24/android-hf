package hf.thewalkinglife;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment implements FirstRunDialogFragment.FirstRunFinishedListener {
    private static final String TAG = "MainFragment";

    @BindView(R.id.helloText) TextView helloText;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPreferences.getBoolean(PreferenceConstants.FIRST_RUN, true)) {
            FirstRunDialogFragment dialog = new FirstRunDialogFragment();
            dialog.setTargetFragment(this, 1);
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), "FirstRunDialog");
        } else {
            setHelloText();
        }
    }

    @Override
    public void onFirstRunFinished() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferenceConstants.FIRST_RUN, false);
        editor.apply();
        setHelloText();
        StepsFragment stepsFragment = (StepsFragment) getChildFragmentManager().findFragmentById(R.id.steps_fragment);
        stepsFragment.refreshDailyGoal();
    }

    private void setHelloText() {
        String username = sharedPreferences.getString(PreferenceConstants.USER_NAME, "");
        String formattedWelcome = getString(R.string.helloIntro, username);
        helloText.setText(formattedWelcome);
    }
}
