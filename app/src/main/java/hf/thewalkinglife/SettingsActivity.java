package hf.thewalkinglife;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.List;

import hf.thewalkinglife.service.StepService;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String EXTRA_NO_HEADERS = ":android:no_headers";
    public static final String EXTRA_SHOW_FRAGMENT = ":android:show_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(
                this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(
                this).unregisterOnSharedPreferenceChangeListener(this);

        super.onStop();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (key) {
            case PreferenceConstants.ENABLE_NOTIFICATIONS: {
                break;
            }
            case PreferenceConstants.ENABLE_STEPS: {
                startServiceWhenEnabled(
                        PreferenceConstants.ENABLE_STEPS,
                        sharedPreferences,
                        getApplicationContext(), StepService.class);
                break;
            }
            case PreferenceConstants.DAILY_GOAL: {
                // Even if EditText's inputType is set to "number", the value is saved as String.
                String value = sharedPreferences.getString(PreferenceConstants.DAILY_GOAL, "0");
                try {
                    Integer intValue = Integer.parseInt(value, 10);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Entered daily goal value is too high or incorrect format!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.fragmentsettings, target);
    }

    public static class FragmentSettingsBasic extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }
    }

}
