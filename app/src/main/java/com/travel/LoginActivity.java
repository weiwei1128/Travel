package com.travel;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;
import com.travel.Utility.GetSpotsNSort;
import com.travel.Utility.HttpService;
import com.travel.Utility.MyAnimation;
import com.travel.Utility.TPESpotAPIFetcher;
import com.travel.Utility.TWSpotAPIFetcher;
import com.travel.Utility.TrackRouteService;

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

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {
    //11.18
    TextView accountText, passText,
            loginText, signupText,
            forgetText;
    EditText accountEdit, passEdit;
    //11.18

    //1.4
    ProgressDialog mDialog;
    //1.4

    //2.29 Hua
    GlobalVariable globalVariable;
    //2.29 Hua

    //3.9 Hua//
    final int REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.login_activity);

        //3.5 Hua
        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean GPS_enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);//NETWORK_PROVIDER);
        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!GPS_enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // Loading API
            startService(new Intent(LoginActivity.this, LocationService.class));

            globalVariable = (GlobalVariable) getApplicationContext();
            DataBaseHelper helper = new DataBaseHelper(getBaseContext());
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotId", "spotName", "spotAdd",
                            "spotLat", "spotLng", "picture1", "picture2", "picture3",
                            "openTime", "ticketInfo", "infoDetail"},
                    null, null, null, null, null);
            if (spotDataRaw_cursor != null) {
                if (spotDataRaw_cursor.getCount() == 0) {
                    // 到景點API抓景點資訊
                    new TPESpotAPIFetcher(LoginActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    // TODO 放著在背景執行去動UI，結果好像就不了了之，沒有載入成功 哪招QAQ
                    new TWSpotAPIFetcher(LoginActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    helper = new DataBaseHelper(LoginActivity.this);
                    database = helper.getWritableDatabase();
                    Cursor location_cursor = database.query("location",
                            new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
                    if (location_cursor != null) {
                        if (location_cursor.getCount() != 0) {
                            new GetSpotsNSort(LoginActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            globalVariable.SpotDataSorted = null;
                            Log.d("3.9_抓不到位置", "不執行GetSpotsNSort");
                            if (Functions.isMyServiceRunning(LoginActivity.this, LocationService.class)) {
                                Intent intent = new Intent(LoginActivity.this, LocationService.class);
                                stopService(intent);
                            }
                            startService(new Intent(LoginActivity.this, LocationService.class));
                        }
                        location_cursor.close();
                    }
                } else {
                    if (globalVariable.SpotDataSorted.isEmpty()) {
                        // retrieve Location from DB
                        helper = new DataBaseHelper(LoginActivity.this);
                        database = helper.getWritableDatabase();
                        Cursor location_cursor = database.query("location",
                                new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
                        if (location_cursor != null) {
                            if (location_cursor.getCount() != 0) {
                                new GetSpotsNSort(LoginActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                globalVariable.SpotDataSorted = null;
                                Log.d("3.9_抓不到位置", "不執行GetSpotsNSort");
                                if (Functions.isMyServiceRunning(LoginActivity.this, LocationService.class)) {
                                    Intent intent = new Intent(LoginActivity.this, LocationService.class);
                                    stopService(intent);
                                }
                                startService(new Intent(LoginActivity.this, LocationService.class));
                            }
                            location_cursor.close();
                        }

                    }
                }
                spotDataRaw_cursor.close();
            }


        }
        //3.5 Hua
        Intent intent = new Intent(LoginActivity.this, HttpService.class);
        startService(intent);
        //Intent intent = new Intent(LoginActivity.this, HttpService.class);
        //startService(intent);
        /////////// 檢查登入狀態
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

        //11.18 按下textview的動畫
        accountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("11.18", "textView clicked");
                View view = accountText;
                MyAnimation test = new MyAnimation(view, 150, true);
                accountText.startAnimation(test);
                accountEdit.setVisibility(View.VISIBLE);
                accountEdit.setText("請輸入帳號");
                //make the keyboard show
                accountEdit.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        });
        passText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("11.18", "pass clicked");
                View view = passText;
                MyAnimation myAnimation = new MyAnimation(view, 150, true);
                passText.startAnimation(myAnimation);
                passEdit.setVisibility(View.VISIBLE);
                passEdit.setText("請輸入密碼");
                //make the keyboard show
                passEdit.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


            }
        });
        //11.18 按下textview的動畫
        accountEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountEdit.getText().toString().equals("請輸入帳號"))
                    accountEdit.setText("");
            }
        });
        passEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passEdit.getText().toString().equals("請輸入密碼"))
                    passEdit.setText("");
            }
        });
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountEdit.getText().toString().equals("") || !accountEdit.isShown()
                        || passEdit.getText().toString().equals("") || !passEdit.isShown()) {
                    Toast.makeText(LoginActivity.this, "未輸入帳號或密碼", Toast.LENGTH_SHORT).show();
                } else {
//                    Log.d("1/4", "account:" + accountEdit.getText() + "_ \n password:" + passEdit.getText() + "_");
                    login_Data loginData = new login_Data(accountEdit.getText().toString(),
                            passEdit.getText().toString());
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
                            Toast.makeText(LoginActivity.this, "請輸入資料", Toast.LENGTH_SHORT).show();
                        } else {
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
                final Dialog signDialog = new Dialog(LoginActivity.this);
                signDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                signDialog.setContentView(R.layout.dialog_reg);
                Button OK = (Button) signDialog.findViewById(R.id.reg_ok);
                Button cancel = (Button) signDialog.findViewById(R.id.reg_cancel);
                final EditText account = (EditText) signDialog.findViewById(R.id.reg_account);
                final EditText password = (EditText) signDialog.findViewById(R.id.reg_password);
                final EditText name = (EditText) signDialog.findViewById(R.id.reg_name);
                final EditText phone = (EditText) signDialog.findViewById(R.id.reg_phone);
                final EditText email = (EditText) signDialog.findViewById(R.id.reg_email);
                OK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (account.getText().toString().equals("")
                                || password.getText().toString().equals("")
                                || name.getText().toString().equals("")
                                || phone.getText().toString().equals("")
                                || email.getText().toString().equals("")
                                ) {
                            Toast.makeText(LoginActivity.this, "請輸入資料", Toast.LENGTH_SHORT).show();
                        } else {
                            sighUp sighUp = new sighUp(account.getText().toString(),
                                    password.getText().toString(), name.getText().toString(),
                                    phone.getText().toString(), email.getText().toString(), signDialog);
                            sighUp.execute();
                        }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (signDialog.isShowing())
                            signDialog.dismiss();
                    }
                });
                signDialog.setCancelable(false);
                signDialog.show();
                //TODO 測試的帳密
                accountEdit.setText("ljd110@qq.com");
                passEdit.setText("ljd110@qq.com");
                Toast.makeText(LoginActivity.this, "建構中", Toast.LENGTH_SHORT).show();
            }
        });
        //11.18 登入textview
    } //onCreate

    void checkLogin() {
        DataBaseHelper helper = new DataBaseHelper(LoginActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
            //表示登入過了!
//            Toast.makeText(LoginActivity.this, "登入過了!", Toast.LENGTH_SHORT).show();
            Timer a = new Timer();
            //如果正確才會跳到下個畫面
//            a.schedule(new TimerTask() {
//                @Override
//                public void run() {
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

    class sighUp extends AsyncTask<String, Void, Boolean> {

        String account, password, name, phone, email, message;
        Dialog dialog;

        public sighUp(String maccount, String mpassword, String mname, String mphone, String memail,
                      Dialog mdialog) {
            this.account = maccount;
            this.password = mpassword;
            this.name = mname;
            this.phone = mphone;
            this.email = memail;
            this.dialog = mdialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            //{"act":"reg","username":"ljd110@qq.com","password":"ljd110@qq.com",
            // "email":"ljd110@qq.com","mobile":"ljd110@qq.com","nickname":"ljd110@qq.com"}
            try {
                entity.addPart("json", new StringBody("{\"act\":\"reg\",\"username\":\""
                        + account + "\",\"password\":\"" + password
                        + "\",\"email\":\"" + email + "\",\"mobile\":\"" + phone
                        + "\",\"nickname\":\"" + name + "\"}", chars));
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
            if (state != null && state.equals("1"))
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean)
                message = "註冊訊息:" + message;

            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            Timer a = new Timer();
            if (aBoolean)
                a.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                }, 2500);
            super.onPostExecute(aBoolean);
        }
    }

    class findPwd extends AsyncTask<String, Void, Boolean> {
        Dialog mdialog;
        String maccount, memail, message;

        public findPwd(Dialog dialog, String account, String email) {
            mdialog = dialog;
            maccount = account;
            memail = email;
        }

        @Override
        protected void onPreExecute() {
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
            if (state != null && state.equals("1"))
                return true;
            else
                return false;


        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (s)
                message = "錯誤訊息:" + message;

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
            super.onPostExecute(s);
        }
    }


    class login_Data extends AsyncTask<String, Void, String> {
        public String maccount, mpassword, mName, mPhone, mEmail, mAddr, login_result;
        Boolean OK = false;

        login_Data(String account, String password) {
            this.maccount = account;
            this.mpassword = password;
        }

        @Override
        protected void onPreExecute() {
            //Loading Dialog
            mDialog = new ProgressDialog(LoginActivity.this);
            mDialog.setMessage("登入中......");
            mDialog.setCancelable(false);
            if (!mDialog.isShowing()) {
                mDialog.show();
            }

            //Loading Dialog
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

                /*
                * http://zhiyou.lin366.com/api/user/index.aspx
                * {"act":"getinfo","uid":"ljd110@qq.com"}
                * default: (error)state:0 (correct)state:1
                *
                * */
            String total = null;
            //0107

            try {

                HttpClient client9 = new DefaultHttpClient();
                HttpPost post9 = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
                MultipartEntity entity9 = new MultipartEntity();
                Charset chars = Charset.forName("UTF-8");
                //"act":"login","username":"ljd110@qq.com","password":"ljd110@qq.com
                entity9.addPart("json", new StringBody("{\"act\":\"login\",\"username\":\""
                        + maccount + "\",\"password\":\"" + mpassword + "\"}", chars));

                post9.setEntity(entity9);
                HttpResponse resp9 = client9.execute(post9);
                total = EntityUtils.toString(resp9.getEntity());

                //取得登入會員資料
                Log.e("2.26", "msg:" + total);
                String state = null;
                try {
                    state = new JSONObject(total.substring(
                            total.indexOf("{"), total.lastIndexOf("}") + 1)).getString("states");
                } catch (JSONException | NullPointerException e2) {
                    e2.printStackTrace();
                }
                try {
                    login_result = new JSONObject(total.substring(
                            total.indexOf("{"), total.lastIndexOf("}") + 1)).getString("msg");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }

                if (state != null && state.equals("1")) {
                    OK = true;
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
                    MultipartEntity entity = new MultipartEntity();
                    //"act":"login","username":"ljd110@qq.com","password":"ljd110@qq.com
                    entity.addPart("json", new StringBody("{\"act\":\"getinfo\",\"uid\":\""
                            + maccount + "\"}", chars));

                    post.setEntity(entity);
                    HttpResponse resp = client.execute(post);
                    String result = EntityUtils.toString(resp.getEntity());
                    String message = null;
                    try {
                        message = new JSONObject(result.substring(
                                result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        login_result = new JSONObject(result.substring(
                                result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("msg");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (message != null && message.equals("1")) {
                        try {
                            mName = new JSONObject(result.substring(
                                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("nick_name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            mPhone = new JSONObject(result.substring(
                                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("telphone");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            mAddr = new JSONObject(result.substring(
                                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("address");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            mEmail = new JSONObject(result.substring(
                                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("email");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

//                    Log.e("2.26", "getinfo: " + result + "states:" + message);
//                    Log.e("2.26", "name: " + mName);
//                    Log.e("2.26", "phone: " + mPhone);
//                    Log.e("2.26", "Email: " + mEmail);
//                    Log.e("2.26", "Address: " + mAddr);
                } else Log.e("2.26", "state: " + state);


                /*
                * http://zhiyou.lin366.com/api/user/index.aspx
                * default: (error)state:0 (correct)state:1
                *
                * {"act":"getinfo","uid":"ljd110@qq.com"}
                *
                * */


            } catch (UnsupportedEncodingException e) {
//                Log.d("1/7", "UnsupportedEncodingException");
                e.printStackTrace();
            } catch (ClientProtocolException e) {
//                Log.d("1/7", "ClientProtocolException");
                e.printStackTrace();
            } catch (IOException e) {
//                Log.d("1/7", "IOException");
                e.printStackTrace();
            }
            Log.d("2.26", "login result: " + login_result);

            return total;
        }

        @Override
        protected void onPostExecute(String string) {
            mDialog.dismiss();

            /** 新增會員資料 **/
            if (OK) {
                DataBaseHelper helper = new DataBaseHelper(LoginActivity.this);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor member_cursor = database.query("member", new String[]{"account", "password",
                        "name", "phone", "email", "addr"}, null, null, null, null, null);
                if (member_cursor != null && member_cursor.getCount() > 0) {
                    database.delete("member", null, null);
                }
                ContentValues cv = new ContentValues();
                cv.put("account", maccount);
                cv.put("password", mpassword);
                cv.put("name", mName);
                cv.put("phone", mPhone);
                cv.put("email", mEmail);
                cv.put("addr", mAddr);
                long result = database.insert("member", null, cv);
//                Log.d("2.26", "member_insert:" + result);

                if (member_cursor != null)
                    member_cursor.close();
            }


            //等toast跑完再跳到下個activity
            if (login_result == null)
                login_result = "請連接網路！";
            if (!OK)
                login_result = "錯誤:" + login_result;
            else login_result = "成功登入！";
            final Toast toast = Toast.makeText(getApplicationContext()
                    , "=====測試結果=====" + "\n" + login_result, Toast.LENGTH_LONG);
            toast.show();
            //custom time
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 2000);


            Timer a = new Timer();

            if (OK)
                //如果正確才會跳到下個畫面
                a.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, HomepageActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
//                        0
                        1500
                );


            super.onPostExecute(string);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to

                // Loading API
                Intent intent = new Intent(LoginActivity.this, HttpService.class);
                startService(intent);
            } else {
                // Permission was denied or request was cancelled
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                Toast.makeText(LoginActivity.this, "請允許寶島好智遊存取您的位置!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
