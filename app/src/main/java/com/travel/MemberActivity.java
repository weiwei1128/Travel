package com.travel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

public class MemberActivity extends AppCompatActivity {

    ImageView homeImg, shoprecordImg, moreImg;
    LinearLayout homeLayout, shoprecordLayout, moreLayout, logoutLayout;
    TextView homeText, shoprecordText, moreText, NameText, PhoneText, EmailText, AddrText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_activity);

        DataBaseHelper helper = new DataBaseHelper(MemberActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();

        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor == null || member_cursor.getCount() == 0) {
            if (member_cursor != null)
                member_cursor.close();
            if (database.isOpen())
                database.close();
            Functions.go(false,MemberActivity.this,MemberActivity.this,LoginActivity.class,null);
            finish();
        }

        //======= MemberData =======//
        NameText = (TextView) findViewById(R.id.member_name_text);
        PhoneText = (TextView) findViewById(R.id.member_phone_text);
        EmailText = (TextView) findViewById(R.id.member_email_text);
        AddrText = (TextView) findViewById(R.id.member_addr_text);
        memberData();

        //======= HOME =======//
        homeImg = (ImageView) findViewById(R.id.member_home_img);
        homeText = (TextView) findViewById(R.id.member_home_text);
        homeLayout = (LinearLayout) findViewById(R.id.member_home_layout);
        homeText.setTextColor(R.color.gray);
        homeImg.setImageResource(R.drawable.click_home_img);
        homeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, MemberActivity.this, MemberActivity.this, HomepageActivity.class, null);
                finish();
            }
        });
        homeImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(homeImg, homeText, "home", false, event.getAction());
                return false;
            }
        });

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.ClickTouchEvent(homeImg, homeText, "home", true, 356735);
            }
        });
        homeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(homeImg, homeText, "home", false, event.getAction());
                return false;
            }
        });
        //======= HOME =======//


        //======= SHOP RECORD =======//
        shoprecordImg = (ImageView) findViewById(R.id.member_shoprecord_img);
        shoprecordText = (TextView) findViewById(R.id.member_shoprecord_text);
        shoprecordLayout = (LinearLayout) findViewById(R.id.member_shoprecord_layout);
        shoprecordText.setTextColor(R.color.gray);
        shoprecordImg.setImageResource(R.drawable.record_img_click);
        shoprecordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, MemberActivity.this, MemberActivity.this, ShopRecordActivity.class, null);
            }
        });
        shoprecordImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(shoprecordImg, shoprecordText, "shoprecord", false, event.getAction());
                return false;
            }
        });
        shoprecordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.ClickTouchEvent(shoprecordImg, shoprecordText, "shoprecord", true, 356735);
            }
        });
        shoprecordLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(shoprecordImg, shoprecordText, "shoprecord", false, event.getAction());
                return false;
            }
        });
        //======= SHOP RECORD =======//

        //======= MORE =======//
        moreImg = (ImageView) findViewById(R.id.member_more_img);
        moreText = (TextView) findViewById(R.id.member_more_text);
        moreLayout = (LinearLayout) findViewById(R.id.member_more_layout);
        moreText.setTextColor(R.color.gray);
        moreImg.setImageResource(R.drawable.more_img_click);
        moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO next Page....
            }
        });
        moreImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(moreImg, moreText, "more", false, event.getAction());
                return false;
            }
        });

        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Functions.ClickTouchEvent(moreImg, moreText, "more", true, 356735);
            }
        });
        moreLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(moreImg, moreText, "more", false, event.getAction());
                return false;
            }
        });
        //======= MORE =======//

        //=======Logout=======//
        logoutLayout = (LinearLayout) findViewById(R.id.member_logout_layout);

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBaseHelper helper = new DataBaseHelper(MemberActivity.this);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor member_cursor = database.query("member", new String[]{"account", "password",
                        "name", "phone", "email", "addr"}, null, null, null, null, null);
                if (member_cursor != null && member_cursor.getCount() > 0) {
                    //表示登入過了!
                    // 創建退出對話框
                    AlertDialog isExit = new AlertDialog.Builder(MemberActivity.this).create();
                    // 設置對話框標題
                    isExit.setTitle("系統提示");
                    // 設置對話框消息
                    isExit.setMessage("登出後會自動離開");
                    // 添加選擇按鈕並注冊監聽
                    isExit.setButton("確定", listener);
                    isExit.setButton2("取消", listener);
                    // 顯示對話框
                    isExit.show();

                }
                if (member_cursor != null)
                    member_cursor.close();
                if (database.isOpen())
                    database.close();
            }
        });

    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕退出程序
                    DataBaseHelper helper = new DataBaseHelper(MemberActivity.this);
                    SQLiteDatabase database = helper.getWritableDatabase();
                    database.delete("member", null, null);
                    if (database.isOpen())
                        database.close();
                    Intent MyIntent = new Intent(Intent.ACTION_MAIN);
                    MyIntent.addCategory(Intent.CATEGORY_HOME);
                    MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    MyIntent.putExtra("EXIT", true);
//                    MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(MyIntent);
                    finish();


                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };

    public void memberData() {
        DataBaseHelper helper = new DataBaseHelper(MemberActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
            member_cursor.moveToFirst();
            Log.d("2.26", "DB " + member_cursor.getString(2));
            NameText.setText(member_cursor.getString(2));
            PhoneText.setText(member_cursor.getString(3));
            EmailText.setText(member_cursor.getString(4));
            AddrText.setText(member_cursor.getString(5));
        }
        if (member_cursor != null)
            member_cursor.close();
        if (database.isOpen())
            database.close();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, MemberActivity.this, MemberActivity.this, HomepageActivity.class, null);
        }
        return false;
    }

}
