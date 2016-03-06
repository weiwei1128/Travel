package com.travel.Utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wei on 2016/1/4.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private static DataBaseHelper mInstance = null;
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "Travel_0.db";
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
        String DATABASE_CREATE_TABLE_MEMBER = "create table member("
                +"_ID INTEGER PRIMARY KEY,"+"account TEXT,"
                +"password TEXT,"
                +"name TEXT,"//2
                +"phone TEXT,"//2
                +"email TEXT,"//2
                +"addr TEXT"//2
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_MEMBER);

        String DATABASE_CREATE_TABLE_TRAVELMEMO = "create table travelmemo("
                +"_ID INTEGER PRIMARY KEY,"+"memo_no TEXT,"//1
                +"memo_img BLOB,"//2
                +"memo_area TEXT,"//3
                +"memo_time TEXT,"//4
                +"memo_content TEXT"//5
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_TRAVELMEMO);

        String DATABASE_CREATE_TABLE_GOODS = "create table goods("
                +"_ID INTEGER PRIMARY KEY,"+"totalCount TEXT,"//1
                +"goods_id TEXT,"//2
                +"goods_title TEXT,"//3
                +"goods_url TEXT,"//4
                +"goods_content TEXT,"//5
                +"goods_addtime TEXT"//6
                +");";
        db.execSQL(DATABASE_CREATE_TABLE_GOODS);

        System.out.println("database CREATE");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE member";
        db.execSQL(SQL);
    }
}
