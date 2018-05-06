package hf.thewalkinglife.db;

import android.database.Cursor;
import android.os.AsyncTask;

import hf.thewalkinglife.StepsFragment;
import hf.thewalkinglife.model.StepData;

public class LoadTodayStepTask extends AsyncTask<Void, Void, Cursor> {
    private static final String TAG = "LoadTodayStepTask";
    private StepDataDbManager dbManager;
    private StepsFragment fragment;

    public LoadTodayStepTask(StepsFragment fragment, StepDataDbManager dbManager) {
        this.fragment = fragment;
        this.dbManager = dbManager;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        try {
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
                if (cursor != null && cursor.moveToFirst()) {
                    StepData stepData = StepDataDbManager.cursorToStepData(cursor);
                    fragment.setStepCountText(String.valueOf(stepData.stepCount));
                } else {
                    fragment.setStepCountText("0");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
