package com.travel;

/****
//
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               佛祖保佑         永無BUG
/****/

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.travel.ImageSlide.MainImageFragment;
import com.travel.Utility.Functions;
import com.travel.Utility.HttpService;
import com.travel.Utility.LoadApiService;
import com.travel.Utility.MyTextview;

public class HomepageActivity extends FragmentActivity {
    private Fragment contentFragment;
    MainImageFragment homefragment;
    LinearLayout homeLayout, memberLayout, shoprecordLayout, moreLayout;
    TextView homeText, memberText, shoprecordText, moreText;
    ImageView homeImg, memberImg, shoprecordImg, moreImg;
    MyTextview textview;
    Bundle bundle;
    MemberFragment memberFragment = new MemberFragment();
    MainFragment mainFragment = new MainFragment();
    ShopRecordFragment shopRecordFragment = new ShopRecordFragment();


    //3.10 Hua
    GlobalVariable globalVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_test);
        UI();
        changeFragment(mainFragment);
        homeImg.setClickable(false);
        homeImg.setImageResource(R.drawable.tab_selected_home);
        homeText.setTextColor(getResources().getColor(R.color.blue_click));


        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(HomepageActivity.this)
                .build();
        ImageLoader.getInstance().init(config);

        Intent intent_LoadApiService = new Intent(HomepageActivity.this, LoadApiService.class);
        startService(intent_LoadApiService);
        Intent intent = new Intent(HomepageActivity.this, HttpService.class);
        startService(intent);
    }


    private void changeFragment(Fragment f) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_test, f);
        transaction.commit();
    }

    void UI() {

        /**TAB**/
        //======= HOME =======//
        homeImg = (ImageView) findViewById(R.id.main_home_img);
        homeText = (TextView) findViewById(R.id.main_home_text);
        homeLayout = (LinearLayout) findViewById(R.id.main_home_layout);
        homeText.setTextColor(getResources().getColor(R.color.gray));
        homeImg.setImageResource(R.drawable.tab_home);
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(mainFragment);
                memberClick(false);
                homeClick(true);
                moreClick(false);
                orderClick(false);
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

        //HomepageActivity.this,MemberActivity.class
        memberImg = (ImageView) findViewById(R.id.main_member_img);
        memberText = (TextView) findViewById(R.id.main_member_text);
        memberLayout = (LinearLayout) findViewById(R.id.main_member_layout);
        memberImg.setImageResource(R.drawable.tab_member);
        memberText.setTextColor(getResources().getColor(R.color.gray));

        memberLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Functions.ifLogin(HomepageActivity.this)) {
                    AlertDialog goLogin = new AlertDialog.Builder(HomepageActivity.this).create();

                    // 設置對話框標題
                    goLogin.setTitle("系統提示");
                    goLogin.setCancelable(false);
                    // 設置對話框消息
                    goLogin.setMessage("請先登入");
                    // 添加選擇按鈕並注冊監聽
                    goLogin.setButton("確定", listenerLogin);
                    goLogin.setButton2("取消", listenerLogin);
                    // 顯示對話框
                    if (!goLogin.isShowing())
                        goLogin.show();
                } else {
                    changeFragment(memberFragment);
                    memberClick(true);
                    homeClick(false);
                    moreClick(false);
                    orderClick(false);
                }


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
        shoprecordImg.setImageResource(R.drawable.tab_record);
        shoprecordText.setTextColor(getResources().getColor(R.color.gray));
        shoprecordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Functions.ifLogin(HomepageActivity.this)) {
                    AlertDialog goLogin = new AlertDialog.Builder(HomepageActivity.this).create();

                    // 設置對話框標題
                    goLogin.setTitle("系統提示");
                    goLogin.setCancelable(false);
                    // 設置對話框消息
                    goLogin.setMessage("請先登入");
                    // 添加選擇按鈕並注冊監聽
                    goLogin.setButton("確定", listenerLogin);
                    goLogin.setButton2("取消", listenerLogin);
                    // 顯示對話框
                    if (!goLogin.isShowing())
                        goLogin.show();
                } else {
                    changeFragment(shopRecordFragment);
                    memberClick(false);
                    homeClick(false);
                    moreClick(false);
                    orderClick(true);
                }
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
        moreImg.setImageResource(R.drawable.tab_more);
        moreText.setTextColor(getResources().getColor(R.color.gray));
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


    }

    void homeClick(Boolean yes) {
        if (yes) {
            homeText.setTextColor(getResources().getColor(R.color.blue_click));
            homeImg.setImageResource(R.drawable.tab_selected_home);
            homeLayout.setClickable(false);
        } else {
            homeText.setTextColor(getResources().getColor(R.color.gray));
            homeImg.setImageResource(R.drawable.tab_home);
            homeLayout.setClickable(true);
        }
    }

    void memberClick(Boolean yes) {
        if (yes) {
            memberText.setTextColor(getResources().getColor(R.color.blue_click));
            memberImg.setImageResource(R.drawable.tab_selected_member);
            memberLayout.setClickable(false);

        } else {
            memberText.setTextColor(getResources().getColor(R.color.gray));
            memberImg.setImageResource(R.drawable.tab_member);
            memberLayout.setClickable(true);
        }
    }

    void orderClick(Boolean yes) {
        if (yes) {
            shoprecordText.setTextColor(getResources().getColor(R.color.blue_click));
            shoprecordImg.setImageResource(R.drawable.tab_selected_record);
            shoprecordLayout.setClickable(false);
        } else {
            shoprecordText.setTextColor(getResources().getColor(R.color.gray));
            shoprecordImg.setImageResource(R.drawable.tab_record);
            shoprecordLayout.setClickable(true);
        }
    }

    void moreClick(Boolean yes) {
        if (yes) {
            moreText.setTextColor(getResources().getColor(R.color.blue_click));
            moreImg.setImageResource(R.drawable.tab_selected_more);
            moreLayout.setClickable(false);
        } else {
            moreText.setTextColor(getResources().getColor(R.color.gray));
            moreImg.setImageResource(R.drawable.tab_more);
            moreLayout.setClickable(true);
        }
    }

    //3.5 Hua
/*    @Override
    protected void onDestroy() {
        //stop LocationService in background
        stopService(new Intent(HomepageActivity.this, LocationService.class));

        super.onDestroy();
    }
*/


    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
    DialogInterface.OnClickListener listenerLogin = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕前往登入
                    Functions.go(false, HomepageActivity.this, HomepageActivity.this, LoginActivity.class, null);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };
}
