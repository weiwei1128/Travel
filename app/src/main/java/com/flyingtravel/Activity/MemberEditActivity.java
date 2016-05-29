package com.flyingtravel.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.VoiceInteractor;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class MemberEditActivity extends AppCompatActivity {
    DataBaseHelper helper;
    SQLiteDatabase database;
    EditText nameEdt, phoneEdt, addrEdt, emailEdt;
    TextView okTxt, cancelTxt;
    String name, phone, addr, email,account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_edit_activity);
        UI();
    }

    void UI() {
        nameEdt = (EditText) findViewById(R.id.member_name_edit);
        phoneEdt = (EditText) findViewById(R.id.member_phone_edit);
        addrEdt = (EditText) findViewById(R.id.member_addr_edit);
        emailEdt = (EditText) findViewById(R.id.member_email_edit);
        okTxt = (TextView) findViewById(R.id.member_edit_ok);
        cancelTxt = (TextView) findViewById(R.id.member_edit_cancel);

        helper = DataBaseHelper.getmInstance(getBaseContext());
        database = helper.getWritableDatabase();


        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
            member_cursor.moveToFirst();
            account = member_cursor.getString(0);
            name = member_cursor.getString(2);
            phone = member_cursor.getString(3);
            email = member_cursor.getString(4);
            addr = member_cursor.getString(5);
            nameEdt.setText(member_cursor.getString(2));
            phoneEdt.setText(member_cursor.getString(3));
            emailEdt.setText(member_cursor.getString(4));
            addrEdt.setText(member_cursor.getString(5));
        }
        if (member_cursor != null)
            member_cursor.close();

        okTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameEdt.getText().toString().equals(name)
                        || !phoneEdt.getText().toString().equals(phone)
                        || !emailEdt.getText().toString().equals(email)
                        || !addrEdt.getText().toString().equals(addr)&& account !=null) {
                    //資料已更新!
                    name = nameEdt.getText().toString();
                    phone = phoneEdt.getText().toString();
                    email = emailEdt.getText().toString();
                    addr = addrEdt.getText().toString();

//                    Functions.toast(MemberEditActivity.this,MemberEditActivity.this.getString(R.string.editok_text),1000);
//                    Functions.go(true, MemberEditActivity.this, MemberEditActivity.this, HomepageActivity.class, null);
                    new editUser().execute();
                }
            }
        });
        cancelTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameEdt.getText().toString().equals(name)
                        || !phoneEdt.getText().toString().equals(phone)
                        || !emailEdt.getText().toString().equals(email)
                        || !addrEdt.getText().toString().equals(addr)) {
                    // 創建退出對話框
                    AlertDialog isExit = new AlertDialog.Builder(MemberEditActivity.this).create();
                    isExit.setTitle(MemberEditActivity.this.getResources().getString(R.string.notsend_text));
                    isExit.setMessage(MemberEditActivity.this.getResources().getString(R.string.edit_text));
                    isExit.setButton(MemberEditActivity.this.getResources().getString(R.string.ok_text), listener);
                    isExit.setButton2(MemberEditActivity.this.getResources().getString(R.string.cancel_text), listener);
                    isExit.show();
                } else
                    Functions.go(true, MemberEditActivity.this, MemberEditActivity.this, HomepageActivity.class, null);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!nameEdt.getText().toString().equals(name)
                    || !phoneEdt.getText().toString().equals(phone)
                    || !emailEdt.getText().toString().equals(email)
                    || !addrEdt.getText().toString().equals(addr)) {
                AlertDialog isExit = new AlertDialog.Builder(MemberEditActivity.this).create();
                isExit.setTitle(MemberEditActivity.this.getResources().getString(R.string.notsend_text));
                isExit.setMessage(MemberEditActivity.this.getResources().getString(R.string.edit_text));
                isExit.setButton(MemberEditActivity.this.getResources().getString(R.string.ok_text), listener);
                isExit.setButton2(MemberEditActivity.this.getResources().getString(R.string.cancel_text), listener);
                isExit.show();
            } else
                Functions.go(true, this, this, HomepageActivity.class, null);
        }
        return false;
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "確認"按鈕退出此頁
                    Functions.go(false, MemberEditActivity.this, MemberEditActivity.this, HomepageActivity.class, null);
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二個按鈕取消對話框
                    break;
                default:
                    break;
            }
        }
    };

    class editUser extends AsyncTask<String,Void,Boolean>{
        ProgressDialog dialog = new ProgressDialog(MemberEditActivity.this);
        String message;

        @Override
        protected void onPreExecute() {
            dialog.setMessage(MemberEditActivity.this.getResources().getString(R.string.loading_text));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json", new StringBody("{\"act\":\"edituser\"," +
                        "\"uid\":\""+account+"\"," +
                        "\"email\":\""+email+"\"," +
                        "\"nick_name\":\""+name+"\"," +
                        "\"telphone\":\""+phone+"\"," +
                        "\"mobile\":\""+phone+"\"," +
                        "\"address\":\""+addr+"\"," +
                        "\"area\":\""+"\",\"birthday\":\""+"\",\"city\":\"\",\"amount\":\"1\"}", charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(multipartEntity);
            HttpResponse response = null;
            String getString = null;
            try {
                response = client.execute(post);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                getString = EntityUtils.toString(response.getEntity());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
            String state = null;
            try {
                state = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            message=null;
            try {
                message = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("msg");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            Log.e("5.16","message:"+message);
            //如果讀取資料錯誤 不進行之後的動作
            return !(state == null || state.equals("0"));
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            dialog.dismiss();
            if(aVoid) {
                ContentValues cv = new ContentValues();
                cv.put("name",nameEdt.getText().toString());
                cv.put("phone",phoneEdt.getText().toString());
                cv.put("email",emailEdt.getText().toString());
                cv.put("addr",addrEdt.getText().toString());
                long result = database.update("member", cv, "account=?", new String[]{account});

            }
            Functions.toast(MemberEditActivity.this, message, 1000);
            Functions.go(true, MemberEditActivity.this, MemberEditActivity.this, HomepageActivity.class, null);
            super.onPostExecute(aVoid);
        }
    }
}
