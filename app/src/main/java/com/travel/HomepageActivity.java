package com.travel;

/*/Users/wei/android-sdks*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.travel.ImageSlide.MainImageFragment;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;
import com.travel.Utility.MyTextview;

import java.util.ArrayList;

public class HomepageActivity extends AppCompatActivity {
    private Fragment contentFragment;
    MainImageFragment homefragment;
    LinearLayout linearLayout, buyLayout, spotLayout, recordLayout, scheduleLayout,
            memberLayout, shoprecordLayout, moreLayout, serviceLayout, goodthingLayout;
    TextView memberText, shoprecordText, moreText;
    ImageView memberImg, shoprecordImg, moreImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataBaseHelper helper = new DataBaseHelper(HomepageActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor == null || member_cursor.getCount() == 0)
            finish();
        else Log.d("3.1", "check___check!!!!!" + member_cursor.getCount());
        if(member_cursor!=null)
            member_cursor.close();
        linearLayout = (LinearLayout) findViewById(R.id.main_main_layout);

        //Goodthing
        goodthingLayout = (LinearLayout) findViewById(R.id.main_good_layout);
        goodthingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, HomepageActivity.this, HomepageActivity.this, GoodThingActivity.class, null);
            }
        });

        //service
        serviceLayout = (LinearLayout) findViewById(R.id.main_service_layout);
        serviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, HomepageActivity.this, HomepageActivity.this, ServiceActivity.class, null);
            }
        });

        spotLayout = (LinearLayout) findViewById(R.id.main_spot_layout);
        spotLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, HomepageActivity.this, HomepageActivity.this, MapsActivity.class, null);
            }
        });

        recordLayout = (LinearLayout) findViewById(R.id.main_record_layout);
        recordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, HomepageActivity.this, HomepageActivity.this, RecordActivity.class, null);
            }
        });

        buyLayout = (LinearLayout) findViewById(R.id.main_buy_layout);
        buyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 0307 TEST!!!!
                Functions.go(false, HomepageActivity.this, HomepageActivity.this, BuyActivityNew.class, null);
//                Functions.go(false, HomepageActivity.this, HomepageActivity.this, BuyActivity.class, null);
            }
        });

        scheduleLayout = (LinearLayout) findViewById(R.id.main_schedule_layout);
        scheduleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, HomepageActivity.this, HomepageActivity.this, CheckScheduleMainActivity.class, null);
            }
        });

        //------TAB------//

        //HomepageActivity.this,MemberActivity.class
        memberImg = (ImageView) findViewById(R.id.main_member_img);
        memberText = (TextView) findViewById(R.id.main_member_text);
        memberLayout = (LinearLayout) findViewById(R.id.main_member_layout);
        memberImg.setImageResource(R.drawable.member_img_click);
        memberText.setTextColor(R.color.gray);
        memberImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, HomepageActivity.this, HomepageActivity.this, MemberActivity.class, null);
//                finish();
            }
        });
        memberImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(memberImg, memberText, "member", false, event.getAction());
                return false;
            }
        });
        memberLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.ClickTouchEvent(memberImg, memberText, "member", true, 356735);
            }
        });
        memberLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Functions.ClickTouchEvent(memberImg, memberText, "member", false, event.getAction());
                return false;
            }
        });

        //======= SHOP RECORD =======//
        shoprecordImg = (ImageView) findViewById(R.id.main_shoprecord_img);
        shoprecordText = (TextView) findViewById(R.id.main_shoprecord_text);
        shoprecordLayout = (LinearLayout) findViewById(R.id.main_shoprecord_layout);
        shoprecordImg.setImageResource(R.drawable.record_img_click);
        shoprecordText.setTextColor(Color.parseColor("#555555"));
        shoprecordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, HomepageActivity.this, HomepageActivity.this, BuyRecordActivity.class, null);
//                finish();
//                TODO next Page....
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
                shoprecordImg.setImageResource(R.drawable.record_img_click);
                shoprecordText.setTextColor(Color.parseColor("#555555"));
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
        moreImg = (ImageView) findViewById(R.id.main_more_img);
        moreText = (TextView) findViewById(R.id.main_more_text);
        moreLayout = (LinearLayout) findViewById(R.id.main_more_layout);
        moreImg.setImageResource(R.drawable.more_img_click);
        moreText.setTextColor(Color.parseColor("#555555"));
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


        //TODO 跑馬燈
        MyTextview textview = new MyTextview(this);
        textview.setText("2014新竹縣客家桐花祭開幕活動  活動日期：4/26");
        textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        linearLayout.addView(textview,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        //little trick
        ((LinearLayout.LayoutParams) textview.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
        textview.scrollText(20);////開始跑囉
        textview.setTextColor(Color.BLACK);
        /////跑馬燈


        ////ImageSlide
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            if (fragmentManager.findFragmentByTag(MainImageFragment.ARG_ITEM_ID) != null) {
                homefragment = (MainImageFragment) fragmentManager
                        .findFragmentByTag(MainImageFragment.ARG_ITEM_ID);
                contentFragment = homefragment;
            }
        } else {
            homefragment = new MainImageFragment();
            switchContent(homefragment, MainImageFragment.ARG_ITEM_ID);
        }
        ////ImageSlide

    }

    //3.5 Hua
    @Override
    protected void onDestroy() {
        //stop LocationService in background
        stopService(new Intent(HomepageActivity.this, LocationService.class));

        super.onDestroy();
    }

    ////ImageSlide
    public void switchContent(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.popBackStackImmediate())
            ;

        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager
                    .beginTransaction();
            transaction.replace(R.id.content_frame, fragment, tag);
            // Only ProductDetailFragment is added to the back stack.
            if (!(fragment instanceof MainImageFragment)) {
                transaction.addToBackStack(tag);
            }
            transaction.commit();
            contentFragment = fragment;
        }
    }
    ////ImageSlide

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (contentFragment instanceof MainImageFragment) {
            outState.putString("content", MainImageFragment.ARG_ITEM_ID);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 創建退出對話框
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            // 設置對話框標題
            isExit.setTitle("系統提示");
            // 設置對話框消息
            isExit.setMessage("確定要退出嗎");
            // 添加選擇按鈕並注冊監聽
            isExit.setButton("確定", listener);
            isExit.setButton2("取消", listener);
            // 顯示對話框
            isExit.show();

        }
        return false;
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕退出程序
                    Intent MyIntent = new Intent(Intent.ACTION_MAIN);
                    MyIntent.addCategory(Intent.CATEGORY_HOME);
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
}
