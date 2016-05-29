package com.flyingtravel.Utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wei on 2016/5/29.
 * <p/>
 * put spot database here
 */
public class SpotDataBaseHelper extends SQLiteOpenHelper {

    private static SpotDataBaseHelper mInstance = null;
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "Spot.db";
    private Context mcontext;

    public SpotDataBaseHelper(Context context) {
        super(context,DATABASE_NAME, null, VERSION);
    }

    public static synchronized SpotDataBaseHelper getmInstance(Context ctx) {
        //make sure do not accidentally leak Activity's context.
        if (mInstance == null) {
            mInstance = new SpotDataBaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //景點資訊 偷偷記錄位置
        String DATABASE_CREATE_TABLE_LOCATION = "create table location("
                + "_ID INTEGER PRIMARY KEY," + "CurrentLat BLOB,"
                + "CurrentLng BLOB"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_LOCATION);

        //景點資訊 原始資料
        String DATABASE_CREATE_TABLE_SPOTDATA_RAW = "create table spotDataRaw("
                + "_ID INTEGER PRIMARY KEY," + "spotName TEXT,"
                + "spotAdd TEXT,"
                + "spotLat BLOB,"
                + "spotLng BLOB,"
                + "picture1 TEXT,"
                + "picture2 TEXT,"
                + "picture3 TEXT,"
                + "openTime TEXT,"
                + "ticketInfo TEXT,"
                + "infoDetail TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_SPOTDATA_RAW);

        //景點列表 排序過後
        String DATABASE_CREATE_TABLE_SPOTDATA_SORTED = "create table spotDataSorted("
                + "_ID INTEGER PRIMARY KEY," + "spotName TEXT,"
                + "spotAdd TEXT,"
                + "spotLat BLOB,"
                + "spotLng BLOB,"
                + "picture1 TEXT,"
                + "picture2 TEXT,"
                + "picture3 TEXT,"
                + "openTime TEXT,"
                + "ticketInfo TEXT,"
                + "infoDetail TEXT"
                + ");";
        db.execSQL(DATABASE_CREATE_TABLE_SPOTDATA_SORTED);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
