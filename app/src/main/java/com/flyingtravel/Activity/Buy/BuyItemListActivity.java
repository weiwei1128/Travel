package com.flyingtravel.Activity.Buy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.flyingtravel.Adapter.BuyitemAdapter;
import com.flyingtravel.Activity.LoginActivity;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;

public class BuyItemListActivity extends AppCompatActivity {

    ListView listView;
    BuyitemAdapter adapter;
    LinearLayout confirmLayout, backImg;
    int lastItem = 0;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("AfterPay", false))
            finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyitem_list_activity);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey("AfterPay") && bundle.getBoolean("AfterPay"))
            finish();
//        }
        listView = (ListView) findViewById(R.id.listview);
        backImg = (LinearLayout) findViewById(R.id.buyitemlist_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("WhichItem", lastItem);
                Functions.go(true, BuyItemListActivity.this, BuyItemListActivity.this,
                        BuyItemDetailActivity.class,
                        null
//                        bundle
                );
            }
        });
        confirmLayout = (LinearLayout) findViewById(R.id.buyitemlist_listLayout);
        confirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Functions.ifLogin(BuyItemListActivity.this)) {
                    AlertDialog goLogin = new AlertDialog.Builder(BuyItemListActivity.this).create();

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
//                    Log.d("12/30", "listconfirmLayout CLICKED!!!");
                    Bundle bundle = new Bundle();
                    bundle.putInt("WhichItem", lastItem);
                    Functions.go(false, BuyItemListActivity.this, BuyItemListActivity.this,
                            BuyItemListConfirmActivity.class, null
//                        bundle
                    );
                }
            }
        });
        adapter = new BuyitemAdapter(this);
        listView.setAdapter(adapter);
    }

    DialogInterface.OnClickListener listenerLogin = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕前往登入
                    Functions.go(false, BuyItemListActivity.this, BuyItemListActivity.this, LoginActivity.class, null);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", lastItem);
            Functions.go(true, BuyItemListActivity.this, BuyItemListActivity.this,
                    BuyItemDetailActivity.class, null
//                        bundle
            );
        }
        return false;
    }
}
