package hf.thewalkinglife;

import android.app.Application;

import hf.thewalkinglife.db.StepDataDbManager;

/**
 * The class that represents the application.
 * Holds a single value: the database manager, so it's available throughout all of the fragments and activities.
 */
public class StepsApplication extends Application {
    private static StepDataDbManager dbManager;

    public static StepDataDbManager getDbManager() {
        return dbManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbManager = new StepDataDbManager(this);
        dbManager.open();
    }

    @Override
    public void onTerminate() {
        dbManager.close();
        super.onTerminate();
    }
}
