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
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "Travel1.db";
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

        //景點資訊 偷偷記錄位置
        String DATABASE_CREATE_TABLE_LOCATION = "create table location("
                +"_ID INTEGER PRIMARY KEY,"+"CurrentLat BLOB,"
                +"CurrentLng BLOB"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_LOCATION);

        //景點資訊 原始資料
        String DATABASE_CREATE_TABLE_SPOTDATA_RAW = "create table spotDataRaw("
                +"_ID INTEGER PRIMARY KEY,"+"spotId TEXT,"
                +"spotName TEXT,"
                +"spotAdd TEXT,"
                +"spotLat BLOB,"
                +"spotLng BLOB,"
                +"picture1 TEXT,"
                +"picture2 TEXT,"
                +"picture3 TEXT,"
                +"openTime TEXT,"
                +"ticketInfo TEXT,"
                +"infoDetail TEXT"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_SPOTDATA_RAW);

        //景點列表 排序過後
        String DATABASE_CREATE_TABLE_SPOTDATA_SORTED = "create table spotDataSorted("
                +"_ID INTEGER PRIMARY KEY,"+"spotId TEXT,"
                +"spotName TEXT,"
                +"spotAdd TEXT,"
                +"spotLat BLOB,"
                +"spotLng BLOB,"
                +"picture1 TEXT,"
                +"picture2 TEXT,"
                +"picture3 TEXT,"
                +"openTime TEXT,"
                +"ticketInfo TEXT,"
                +"infoDetail TEXT"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_SPOTDATA_SORTED);

        //軌跡紀錄
        String DATABASE_CREATE_TABLE_TRACKROUTE = "create table trackRoute("
                +"_ID INTEGER PRIMARY KEY,"+"routesCounter INTEGER,"
                +"track_no INTEGER,"
                +"track_lat BLOB,"
                +"track_lng BLOB,"
                +"track_start INTEGER"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_TRACKROUTE);

        //旅遊日誌
        String DATABASE_CREATE_TABLE_TRAVELMEMO = "create table travelMemo("
                +"_ID INTEGER PRIMARY KEY,"+"totalCount TEXT,"
                +"id TEXT,"
                +"title TEXT,"
                +"url TEXT,"
                +"zhaiyao TEXT,"
                +"click TEXT,"
                +"addtime TEXT"
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

        String DATABASE_CREATE_TABLE_SHOPRECORD = "create table shoporder("
                +"_ID INTEGER PRIMARY KEY,"+"order_id TEXT,"
                +"order_no TEXT,"
                +"order_time TEXT,"
                +"order_name TEXT,"
                +"order_phone TEXT,"
                +"order_email TEXT,"
                +"order_money TEXT,"
                +"order_state TEXT"
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_SHOPRECORD);
        System.out.println("database CREATE");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /** TODO 正式版要修改!!
         * [0308] [BAD!] [若有相同名稱則重建] <-相同名稱是指哪個地方的名稱相同啊?! by Hua 3/12 06:08
         * **/
        db.execSQL("DROP TABLE IF EXISTS newMemorandum");

    }

}
