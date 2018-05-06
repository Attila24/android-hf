package hf.thewalkinglife;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import hf.thewalkinglife.service.StepService;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (key) {
            case PreferenceConstants.NOTIFICATIONS_ENABLED: {
                break;
            }
            case PreferenceConstants.STEPS_ENABLED: {
                startServiceWhenEnabled(
                        PreferenceConstants.STEPS_ENABLED,
                        sharedPreferences,
                        getActivity(), StepService.class);
                break;
            }
            case PreferenceConstants.DAILY_GOAL: {
                // Even if EditText's inputType is set to "number", the value is saved as String.
                String value = sharedPreferences.getString(PreferenceConstants.DAILY_GOAL, "0");
                try {
                    Integer intValue = Integer.parseInt(value, 10);
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Entered daily goal value is too high or incorrect format!", Toast.LENGTH_SHORT).show();
                    editor.putString(PreferenceConstants.DAILY_GOAL, PreferenceConstants.DEFAULT_DAILY_GOAL);
                    editor.apply();
                }
                break;
            }
            case PreferenceConstants.USER_NAME: {
                break;
            }
        }
    }

    static void startServiceWhenEnabled(String key, SharedPreferences sharedPreferences,
                                        Context context, Class serviceClass) {
       boolean startService = sharedPreferences.getBoolean(key, false);
       Intent intent = new Intent(context, serviceClass);
       if (startService) {
           context.startService(intent);
       } else {
           context.stopService(intent);
       }
    }
}
