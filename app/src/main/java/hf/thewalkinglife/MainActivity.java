package hf.thewalkinglife;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import hf.thewalkinglife.service.StepService;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivity";
    private static final String KEY_STEP_SERVICE_RUNNING = "STEP_SERVICE_RUNNING";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;

    boolean stepServiceRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // Don't start step service if the activity was reloaded just because of orientation change (savedInstanceState will be not null)
        if (savedInstanceState == null) {
            MenuItem item = navigationView.getMenu().getItem(0);
            onNavigationChanged(item);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean stepCountingEnabled = preferences.getBoolean(PreferenceConstants.STEPS_ENABLED, PreferenceConstants.STEPS_ENABLED_DEFAULT);
            if (stepCountingEnabled) {
                startStepService();
            }
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return onNavigationChanged(item);
            }
        });
    }

    private void startStepService() {
        Intent stepServiceIntent = new Intent(getApplicationContext(), StepService.class);
        startService(stepServiceIntent);
        stepServiceRunning = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_STEP_SERVICE_RUNNING, stepServiceRunning);
        super.onSaveInstanceState(outState);
    }

    public boolean onNavigationChanged(@NonNull MenuItem item) {
        item.setChecked(true);
        drawerLayout.closeDrawers();

        Fragment fragment;
        FragmentManager fragmentManager = getFragmentManager();

        switch (item.getItemId()) {
            case R.id.action_home:
                fragment = new MainFragment();
                break;
            case R.id.action_settings:
                fragment = new SettingsFragment();
                break;
            case R.id.action_map:
                fragment = new TwlMapFragment();
                break;
            case R.id.action_history:
                fragment = new HistoryFragment();
                break;
            default:
                fragment = new MainFragment();
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.content_container, fragment).commit();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
