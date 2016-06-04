package edu.coen268.myproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Suming on 3/8/16.
 */
public class DBAdapter {
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_PHOTO = "photoPath";
    public static final String COLUMN_SKETCH = "sketchPath";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_FLAG = "flag";
    public static final String COLUMN_PW = "pw";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dbNotes";
    public static final String TABLE_NAME = "notes";

    public static final String[] COLUMNS = new String[] {COLUMN_ID, COLUMN_TITLE, COLUMN_NOTE, COLUMN_PHOTO, COLUMN_SKETCH, COLUMN_DATE, COLUMN_FLAG, COLUMN_PW};

    private static final String DATABASE_CREATE = String.format(
            "CREATE TABLE %s (" +
                    "  %s integer primary key autoincrement, " +
                    "  %s text," +
                    "  %s text," +
                    "  %s text," +
                    "  %s text," +
                    "  %s text," +
                    "  %s text," +
                    "  %s text)",
            TABLE_NAME, COLUMN_ID, COLUMN_TITLE, COLUMN_NOTE, COLUMN_PHOTO, COLUMN_SKETCH, COLUMN_DATE, COLUMN_FLAG, COLUMN_PW);

    private final Context context;
    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        //Log.d("Database operations", "Open DB...");
        return this;
    }

    public void close() {
        myDBHelper.close();
    }

    public long addNote(String title, String note, String photoPath, String sketchPath, String date, String flag) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_NOTE, note);
        values.put(COLUMN_PHOTO, photoPath);
        values.put(COLUMN_SKETCH, sketchPath);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_FLAG, flag);
        return db.insert(TABLE_NAME, null, values);
    }

    public long updateNote(long rowId, String title, String note, String photoPath, String date) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_NOTE, note);
        values.put(COLUMN_PHOTO, photoPath);
        values.put(COLUMN_DATE, date);
        long val = db.update(TABLE_NAME, values, COLUMN_ID + "=" + rowId, null);
        return val;
    }


    public long lockRow(long rowId, String flag, String pw) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_FLAG, flag);
        values.put(COLUMN_PW, pw);
        long val = db.update(TABLE_NAME, values, COLUMN_ID + "=" + rowId, null);
        //Log.d("Database operations", "lock" + rowId);
        return val;
    }

    public int deleteRow(long rowId) {
        int val = db.delete(TABLE_NAME, COLUMN_ID + "=" + rowId, null);
        return val;
    }

    public Cursor getAllRows() {
        Cursor c = db.query(true, TABLE_NAME, COLUMNS, null, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    //Get searched rows by search string
    public Cursor getSearchRows(String str) {
        String where = COLUMN_TITLE + " LIKE " + "'" + str.trim() + "'";
        Cursor c = db.query(true, TABLE_NAME, COLUMNS, where, null, null, null, null, null);
        //Log.d("Database operations", where );
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    //Get a specific row by rowId
    public Cursor getRow(long rowId) {
        String where = COLUMN_ID + "=" + rowId;
        Cursor c = db.query(true, TABLE_NAME, COLUMNS, where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
