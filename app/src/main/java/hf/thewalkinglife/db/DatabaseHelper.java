package hf.thewalkinglife.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    DatabaseHelper(Context context, String name) {
        super(context, name, null, DatabaseConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, DatabaseConstants.DATABASE_CREATE_ALL);
        db.execSQL(DatabaseConstants.DATABASE_CREATE_ALL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseConstants.DATABASE_DROP_ALL);
        db.execSQL(DatabaseConstants.DATABASE_CREATE_ALL);
    }
}
