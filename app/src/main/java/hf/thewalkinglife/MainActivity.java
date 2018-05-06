package hf.thewalkinglife;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import hf.thewalkinglife.service.StepService;

public class MainActivity extends AppCompatActivity implements FirstRunDialogFragment.FirstRunFinishedListener {
    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;

    @BindView(R.id.helloText) TextView helloText;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                        intentSettings.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT,
                                SettingsActivity.FragmentSettingsBasic.class.getName());
                        intentSettings.putExtra(SettingsActivity.EXTRA_NO_HEADERS, true);
                        startActivity(intentSettings);
                        break;
                }
                return true;
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent stepServiceIntent = new Intent(getApplicationContext(), StepService.class);
        startService(stepServiceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getBoolean(PreferenceConstants.FIRST_RUN, true)) {
            FirstRunDialogFragment dialog = new FirstRunDialogFragment();
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), "FirstRunDialog");
        } else {
            setHelloText();
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
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

    @Override
    public void onFirstRunFinished() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferenceConstants.FIRST_RUN, false);
        editor.apply();
        setHelloText();
    }

    private void setHelloText() {
        String username = sharedPreferences.getString(PreferenceConstants.USER_NAME, "");
        String formattedWelcome = getString(R.string.helloIntro, username);
        helloText.setText(formattedWelcome);
    }
}
