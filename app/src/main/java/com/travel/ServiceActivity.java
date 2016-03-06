package com.travel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.travel.Utility.Functions;

public class ServiceActivity extends AppCompatActivity {
    ImageView backImg, moreImg;
    EditText commentEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_activity);
        backImg = (ImageView) findViewById(R.id.service_backImg);
        moreImg = (ImageView) findViewById(R.id.service_moreImg);
        commentEdt = (EditText) findViewById(R.id.service_edit);

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("1/29", "SERVICE" + commentEdt.getText().toString());
                if (!commentEdt.getText().toString().equals("")) {
                    // 創建退出對話框
                    AlertDialog isExit = new AlertDialog.Builder(ServiceActivity.this).create();
                    // 設置對話框標題
                    isExit.setTitle("訊息尚未送出");
                    // 設置對話框消息
                    isExit.setMessage("確定要離開此頁嗎");
                    // 添加選擇按鈕並注冊監聽
                    isExit.setButton("確定", listener);
                    isExit.setButton2("取消", listener);
                    // 顯示對話框
                    isExit.show();
                } else {
                    Functions.go(true,ServiceActivity.this, ServiceActivity.this, HomepageActivity.class, null);
                }
            }
        });


    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕退出此頁
                    Functions.go(false,ServiceActivity.this, ServiceActivity.this, HomepageActivity.class, null);
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
            Functions.go(true,ServiceActivity.this, ServiceActivity.this, HomepageActivity.class, null);
        }
        return false;
    }
}
