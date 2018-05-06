package hf.thewalkinglife.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

import hf.thewalkinglife.model.StepData;

public class StepDataDbManager {

    private static final String TAG = "StepDataDbManager";
    private Context context;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public StepDataDbManager(Context context) {
        this.context = context;
    }

    public void open() throws SQLException {
        dbHelper = new DatabaseHelper(context, DatabaseConstants.DB_NAME);
        db = dbHelper.getWritableDatabase();
        dbHelper.onCreate(db);
    }

    public void close() {
        dbHelper.close();
    }

    public Cursor fetchTodayStepData() {
        return fetchStepData(getTodayDate());
    }

    public Cursor fetchStepData(String date) {
        return db.query(DatabaseConstants.StepData.DATABASE_TABLE,
                new String[]{
                    DatabaseConstants.StepData.KEY_ROWID,
                    DatabaseConstants.StepData.KEY_STEPS_COUNT,
                    DatabaseConstants.StepData.KEY_DATE
                }, DatabaseConstants.StepData.KEY_DATE + "=" + "'" + date + "'",
                null, null, null, null);
    }

    public StepData getStepData(String date) {
        Cursor cursor = fetchStepData(date);
        if (cursor.moveToFirst()) {
            return cursorToStepData(cursor);
        } else {
            return null;
        }
    }

    static StepData cursorToStepData(Cursor c) {
        return new StepData(
                c.getString(c.getColumnIndex(DatabaseConstants.StepData.KEY_DATE)),
                c.getInt(c.getColumnIndex(DatabaseConstants.StepData.KEY_STEPS_COUNT)));
    }

    // <number of steps>: successful
    // -1 : unsuccessful
    public int createStepData() {
        ContentValues values = new ContentValues();

        String todayDate = getTodayDate();

        StepData existingStepData = getStepData(todayDate);
        boolean existing = existingStepData != null;

        int newStepCount = existing ? ++existingStepData.stepCount : 1;

        values.put(DatabaseConstants.StepData.KEY_DATE, todayDate);
        values.put(DatabaseConstants.StepData.KEY_STEPS_COUNT, newStepCount);

        boolean successful = existing ? updateStepData(values, todayDate) : insertStepData(values);
        return successful ? newStepCount : -1;
    }

    private boolean updateStepData(ContentValues values, String todayDate) {
        int rowsAffected = db.update(DatabaseConstants.StepData.DATABASE_TABLE, values,
                DatabaseConstants.StepData.KEY_DATE + "= '" + todayDate + "'", null);
        return rowsAffected == 1;
    }

    private boolean insertStepData(ContentValues values) {
        long rowId = db.insert(DatabaseConstants.StepData.DATABASE_TABLE, null, values);
        return rowId != -1;
    }

    /**
     * Make date in in YYYY/MM/DD format.
     */
    private String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%s/%s/%s",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

}
