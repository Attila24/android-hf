package hf.thewalkinglife.db;

import android.database.Cursor;
import android.os.AsyncTask;

import hf.thewalkinglife.model.StepData;

/**
 * An async task for loading today's step data from the database.
 */
public class LoadTodayStepTask extends AsyncTask<Void, Void, Cursor> {
    private static final String TAG = "LoadTodayStepTask";

    private StepDataDbManager dbManager;
    private TodayStepLoaderFragment fragment;

    public LoadTodayStepTask(TodayStepLoaderFragment fragment, StepDataDbManager dbManager) {
        this.fragment = fragment;
        this.dbManager = dbManager;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        try {
            // Fetch today's step data and save it in a cursor
            Cursor cursor = dbManager.fetchTodayStepData();
            if (!isCancelled()) {
                return cursor;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);
        try {
            if (fragment != null) {
                // If the cursor exists and is not empty,
                // Get its result as a StepData, then update the fragment's value
                if (cursor != null && cursor.moveToFirst()) {
                    StepData stepData = StepDataDbManager.cursorToStepData(cursor);
                    fragment.setStepCount(String.valueOf(stepData.stepCount));
                    cursor.close();
                } else {
                    // Fallback if for some reason the cursor was empty or null
                    fragment.setStepCount("0");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The interface that the fragment needs to implement to use this loader task.
     */
    public interface TodayStepLoaderFragment {
        void setStepCount(String stepCount);
    }
}
