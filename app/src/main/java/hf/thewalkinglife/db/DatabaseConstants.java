package hf.thewalkinglife.db;

public class DatabaseConstants {
    public static final String DB_NAME = "data.db";
    public static final int DB_VERSION = 1;

    public static String DATABASE_CREATE_ALL = StepData.DATABASE_CREATE;
    public static String DATABASE_DROP_ALL = StepData.DATABASE_DROP;

    public static class StepData {
        static final String DATABASE_TABLE = "step";
        static final String KEY_ROWID = "_id";
        static final String KEY_DATE = "date";
        static final String KEY_STEPS_COUNT = "steps_count";


        static final String DATABASE_CREATE =
                "create table if not exists " + DATABASE_TABLE + " ("
                + KEY_ROWID + " integer primary key autoincrement, "
                + KEY_DATE + " text, "
                + KEY_STEPS_COUNT + " integer);";

        static final String DATABASE_DROP =
                    "drop table if exists " + DATABASE_TABLE + ";";
    }
}
