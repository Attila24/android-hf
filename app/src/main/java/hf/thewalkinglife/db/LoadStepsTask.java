package hf.thewalkinglife.db;

import android.database.Cursor;
import android.os.AsyncTask;

/**
 * An async task for loading all of the step data saved in the database.
 */
public class LoadStepsTask extends AsyncTask<Void, Void, Cursor> {
    private static final String TAG = "LoadStepsTask";

    private StepDataDbManager dbManager;
    private StepsLoaderFragment fragment;

    public LoadStepsTask(StepsLoaderFragment fragment, StepDataDbManager dbManager) {
        this.fragment = fragment;
        this.dbManager = dbManager;
    }

    @Override
    protected Cursor doInBackground(Void... voids) {
        try {
            // Fetch all step data and save it in a Cursor
            Cursor cursor = dbManager.fetchAllStepData();
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
                // Return the result cursor to the originating fragment.
                fragment.setSteps(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The interface that the fragment needs to implement to use this loader task.
     */
    public interface StepsLoaderFragment {
        void setSteps(Cursor steps);
    }
}
