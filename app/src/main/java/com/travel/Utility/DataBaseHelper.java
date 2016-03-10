package com.travel.Utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wei on 2016/1/4.
 * 修改後記得更新資料庫 避免使用者資料遺失!!
 *
 *
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private static DataBaseHelper mInstance = null;
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "Travel2.db";
    private Context mcontext;

    public DataBaseHelper(Context context){
        super(context,DATABASE_NAME,null,VERSION);
        this.mcontext = context;

    }

    public static DataBaseHelper getmInstance(Context ctx){
        //make sure do not accidentally leak Activity's context.
        if(mInstance ==null){
            mInstance = new DataBaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //會員資料
        String DATABASE_CREATE_TABLE_MEMBER = "create table member("
                +"_ID INTEGER PRIMARY KEY,"+"account TEXT,"
                +"password TEXT,"
                +"name TEXT,"
                +"phone TEXT,"
                +"email TEXT,"
                +"addr TEXT"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_MEMBER);

        String DATABASE_CREATE_TABLE_LOCATION = "create table location("
                +"_ID INTEGER PRIMARY KEY,"+"CurrentLat BLOB,"//2
                +"CurrentLng BLOB"//3
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_LOCATION);

        String DATABASE_CREATE_TABLE_SPOTDATA_RAW = "create table spotDataRaw("
                +"_ID INTEGER PRIMARY KEY,"+"spotId TEXT,"//1
                +"spotName TEXT,"//2
                +"spotAdd TEXT,"//3
                +"spotLat BLOB,"//4
                +"spotLng BLOB,"//5
                +"picture1 TEXT,"//6
                +"picture2 TEXT,"//7
                +"picture3 TEXT,"//8
                +"openTime TEXT,"//9
                +"ticketInfo TEXT,"//10
                +"infoDetail TEXT"//11
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_SPOTDATA_RAW);

        String DATABASE_CREATE_TABLE_SPOTDATA_SORTED = "create table spotDataSorted("
                +"_ID INTEGER PRIMARY KEY,"+"spotId TEXT,"//1
                +"spotName TEXT,"//2
                +"spotAdd TEXT,"//3
                +"spotLat BLOB,"//4
                +"spotLng BLOB,"//5
                +"picture1 TEXT,"//6
                +"picture2 TEXT,"//7
                +"picture3 TEXT,"//8
                +"openTime TEXT,"//9
                +"ticketInfo TEXT,"//10
                +"infoDetail TEXT"//11
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_SPOTDATA_SORTED);

        String DATABASE_CREATE_TABLE_TRACKROUTE = "create table trackRoute("
                +"_ID INTEGER PRIMARY KEY,"+"routesCounter INTEGER,"//1
                +"track_no INTEGER,"//2
                +"track_lat BLOB,"//3
                +"track_lng BLOB,"//4
                +"track_start INTEGER"//5
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_TRACKROUTE);

        String DATABASE_CREATE_TABLE_TRAVELMEMO = "create table travelMemo("
                +"_ID INTEGER PRIMARY KEY,"+"memo_no TEXT,"//1
                +"memo_img BLOB,"//2
                +"memo_area TEXT,"//3
                +"memo_time TEXT,"//4
                +"memo_content TEXT"//5
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_TRAVELMEMO);
        //伴手禮
        String DATABASE_CREATE_TABLE_GOODS = "create table goods("
                +"_ID INTEGER PRIMARY KEY,"+"totalCount TEXT,"
                +"goods_id TEXT,"
                +"goods_title TEXT,"
                +"goods_url TEXT,"
                +"goods_money TEXT,"
                +"goods_content TEXT,"
                +"goods_click TEXT,"
                +"goods_addtime TEXT"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_GOODS);

        //即時好康
        String DATABASE_CREATE_TABLE_SPECIAL = "create table special_activity("
                +"_ID INTEGER PRIMARY KEY,"+"special_id TEXT,"
                +"title TEXT,"
                +"img TEXT,"
                +"content TEXT,"
                +"price TEXT,"
                +"click TEXT"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_SPECIAL);

        //最新消息
        String DATABASE_CREATE_TABLE_NEWS = "create table news("
                +"_ID INTEGER PRIMARY KEY,"+"title TEXT"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_NEWS);
        System.out.println("database CREATE");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /** TODO 正式版要修改!!
         * [0308] [BAD!] [若有相同名稱則重建]
         * **/
        db.execSQL("DROP TABLE IF EXISTS newMemorandum");

    }

}
