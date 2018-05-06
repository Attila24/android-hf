package hf.thewalkinglife.db;

import android.database.Cursor;
import android.os.AsyncTask;

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
                fragment.setSteps(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface StepsLoaderFragment {
        public void setSteps(Cursor steps);
    }
}
