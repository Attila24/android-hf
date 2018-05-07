package hf.thewalkinglife.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A helper class that is used to run the database creation and upgrade (drop, then recreate) operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    DatabaseHelper(Context context, String name) {
        super(context, name, null, DatabaseConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseConstants.DATABASE_CREATE_ALL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseConstants.DATABASE_DROP_ALL);
        db.execSQL(DatabaseConstants.DATABASE_CREATE_ALL);
    }
}
