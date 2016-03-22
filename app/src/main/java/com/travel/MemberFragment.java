package com.travel;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberFragment extends Fragment {
    Context context;
    TextView NameText, PhoneText, EmailText, AddrText;
    LinearLayout logoutLayout;

    public MemberFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.member_activity, container, false);
        memberData(view);
        return view;
    }

    public void memberData(View view) {
        logoutLayout = (LinearLayout) view.findViewById(R.id.member_logout_layout);
        NameText = (TextView) view.findViewById(R.id.member_name_text);
        PhoneText = (TextView) view.findViewById(R.id.member_phone_text);
        EmailText = (TextView) view.findViewById(R.id.member_email_text);
        AddrText = (TextView) view.findViewById(R.id.member_addr_text);
        Boolean login=false;
        DataBaseHelper helper = new DataBaseHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
            member_cursor.moveToFirst();
//            Log.d("2.26", "DB " + member_cursor.getString(2));
            NameText.setText(member_cursor.getString(2));
            PhoneText.setText(member_cursor.getString(3));
            EmailText.setText(member_cursor.getString(4));
            AddrText.setText(member_cursor.getString(5));
            login = true;
        }
        if (member_cursor != null)
            member_cursor.close();
        if (database.isOpen())
            database.close();
        //=======Logout=======//


        final Boolean finalLogin = login;
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalLogin) {
                    //表示登入過了!
                    // 創建退出對話框
                    AlertDialog isExit = new AlertDialog.Builder(context).create();
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
            }
        });


    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕退出程序

                    DataBaseHelper helper = new DataBaseHelper(context);
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
                    getActivity().finish();

                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };
}
