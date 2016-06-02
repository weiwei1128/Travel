package com.flyingtravel.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.GlobalVariable;
import com.flyingtravel.Utility.RegDialog;
import com.flyingtravel.Utility.View.MyAnimation;
import com.flyingtravel.Utility.login_Data;
import com.flyingtravel.Utility.sighUp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {
    TextView accountText, passText,
            loginText, signupText,
            forgetText;
    EditText accountEdit, passEdit;
    ProgressDialog mDialog;

    /**
     * GA
     **/
    public static Tracker tracker;

    @Override
    protected void onResume() {
        super.onResume();
        /**GA**/
        tracker.setScreenName("登入");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        /**GA**/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getApplication();
        tracker = globalVariable.getDefaultTracker();


        checkLogin();


        accountText = (TextView) findViewById(R.id.home_account_text);
        passText = (TextView) findViewById(R.id.home_pass_text);
        accountEdit = (EditText) findViewById(R.id.home_account_edit);
        passEdit = (EditText) findViewById(R.id.home_pass_edit);
        loginText = (TextView) findViewById(R.id.home_login_text);
        signupText = (TextView) findViewById(R.id.home_sighup_text);
        signupText = (TextView) findViewById(R.id.home_sighup_text);
        forgetText = (TextView) findViewById(R.id.home_forgetpa_text);
        accountEdit.setVisibility(View.INVISIBLE);
        passEdit.setVisibility(View.INVISIBLE);

        accountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("11.18", "textView clicked");
                View view = accountText;
                MyAnimation test = new MyAnimation(view, 150, true);
                accountText.startAnimation(test);
                accountEdit.setVisibility(View.VISIBLE);
                //make the keyboard show
                accountEdit.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        });
        passText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("11.18", "pass clicked");
                View view = passText;
                MyAnimation myAnimation = new MyAnimation(view, 150, true);
                passText.startAnimation(myAnimation);
                passEdit.setVisibility(View.VISIBLE);
                //make the keyboard show
                passEdit.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


            }
        });
        //11.18 按下textview的動畫
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountEdit.getText().toString().equals("") || !accountEdit.isShown()
                        || passEdit.getText().toString().equals("") || !passEdit.isShown()) {
                    Toast.makeText(LoginActivity.this,
                            LoginActivity.this.getResources().getString(R.string.noaccountAndpassword_text), Toast.LENGTH_SHORT).show();
                } else {
                    /***GA**/
                    tracker.send(new HitBuilders.EventBuilder().setCategory("登入")
//                .setAction("click")
//                .setLabel("submit")
                            .build());
                    /***GA**/
//                    Log.d("1/4", "account:" + accountEdit.getText() + "_ \n password:" + passEdit.getText() + "_");
                    login_Data loginData = new login_Data(accountEdit.getText().toString(),
                            passEdit.getText().toString(),LoginActivity.this);
                    loginData.execute();
                }


            }
        });
        forgetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog findpwdDialog = new Dialog(LoginActivity.this);
                findpwdDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                findpwdDialog.setContentView(R.layout.dialog_findpwd);
                Button OK = (Button) findpwdDialog.findViewById(R.id.findpwd_OkButt);
                Button Cancel = (Button) findpwdDialog.findViewById(R.id.findpwd_CancelButt);
                final EditText accountEdit = (EditText) findpwdDialog.findViewById(R.id.findpwd_accountEdit);
                final EditText emailEdit = (EditText) findpwdDialog.findViewById(R.id.findpwd_emailEdit);
                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (findpwdDialog.isShowing())
                            findpwdDialog.dismiss();
                    }
                });
                OK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (accountEdit.getText().toString().equals("")
                                || emailEdit.getText().toString().equals("")) {
                            Toast.makeText(LoginActivity.this,
                                    LoginActivity.this.getResources().getString(R.string.InputData_text), Toast.LENGTH_SHORT).show();
                        } else {
                            /***GA**/
                            tracker.send(new HitBuilders.EventBuilder().setCategory("忘記密碼")
//                .setAction("click")
//                .setLabel("submit")
                                    .build());
                            /***GA**/
//                    Log.d("1/4
                            findPwd findPwd = new findPwd(findpwdDialog, accountEdit.getText().toString(),
                                    emailEdit.getText().toString());
                            findPwd.execute();
                        }
                    }
                });
                findpwdDialog.show();
//                Toast.makeText(LoginActivity.this, "建構中", Toast.LENGTH_SHORT).show();
            }
        });
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**GA**/
                tracker.setScreenName("註冊");
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
                /**GA**/
                RegDialog signDialog = new RegDialog(LoginActivity.this,tracker,LoginActivity.this);
                signDialog.show();
            }
        });
    } //onCreate

    void checkLogin() {
//        Log.d("5.23", "1checkLogin()!!");
        DataBaseHelper helper = DataBaseHelper.getmInstance(LoginActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr","type"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
//            Toast.makeText(LoginActivity.this, "登入過了!", Toast.LENGTH_SHORT).show();
            Timer a = new Timer();
            //如果正確才會跳到下個畫面
//            a.schedule(new TimerTask() {
//                @Override
//                public void run() {
//            Log.d("5.23", "checkLogin()!!");
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, HomepageActivity.class);
            startActivity(intent);
            finish();
//                }
//            }, 2500);
        }
        if (member_cursor != null)
            member_cursor.close();
//        else Toast.makeText(LoginActivity.this,"沒登入過!",Toast.LENGTH_SHORT).show();

    }



    class findPwd extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        Dialog mdialog;
        String maccount, memail, message;

        public findPwd(Dialog dialog, String account, String email) {
            mdialog = dialog;
            maccount = account;
            memail = email;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(LoginActivity.this.getResources().getString(R.string.loading_text));
            progressDialog.setCancelable(false);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            /**{"act":"findpwd","username":"ljd110@qq.com","email":"ljd110@qq.com"}**/
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            //"act":"login","username":"ljd110@qq.com","password":"ljd110@qq.com
            try {
                entity.addPart("json", new StringBody("{\"act\":\"findpwd\",\"username\":\""
                        + maccount + "\",\"email\":\"" + memail + "\"}", chars));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            post.setEntity(entity);
            HttpResponse resp = null;
            String result = null;
            try {
                resp = client.execute(post);
                result = EntityUtils.toString(resp.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String state = null;
            try {
                state = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
                message = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("msg");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
            return state != null && state.equals("1");


        }

        @Override
        protected void onPostExecute(Boolean s) {
            Log.e("5.16", "s:" + s + "  maccount:" + maccount + "  email:" + memail);

            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            Timer a = new Timer();
            if (s)
                a.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mdialog.isShowing())
                            mdialog.dismiss();
                    }
                }, 2500);
            progressDialog.dismiss();
            super.onPostExecute(s);
        }
    }



}
