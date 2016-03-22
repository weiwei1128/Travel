package com.travel;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.travel.Adapter.ShopRecordAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.HttpService;
import com.travel.Utility.LoadApiService;
import com.travel.Utility.MyAnimation;
import com.travel.Utility.OrderUpdate;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
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
    //GlobalVariable globalVariable;
    //2.29 Hua

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.login_activity);
        //globalVariable = (GlobalVariable) getApplicationContext();

        Intent intent_LoadApi = new Intent(LoginActivity.this, LoadApiService.class);
        startService(intent_LoadApi);

        Intent intent = new Intent(LoginActivity.this, HttpService.class);
        startService(intent);

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

    public class getShopRecord extends AsyncTask<String, Void, String> {
        String UserId = null;
        Context context;
        ShopRecordAdapter adapter;

        public getShopRecord(ShopRecordAdapter shopRecordAdapter,Context context,String userId) {
            this.adapter = shopRecordAdapter;
            this.context = context;
            this.UserId = userId;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i("3.11", "*************ShopRecord DO IN BACKGROUND");
            String returnMessage = null;
            if(UserId!=null) {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
                MultipartEntity multipartEntity = new MultipartEntity();
                Charset charset = Charset.forName("UTF-8");
                try {
                    multipartEntity.addPart("json", new StringBody("{" +
                            "    \"act\": \"list\"," +
                            "    \"type\": \"\"," +
                            "    \"page\": \"1\"," +
                            "    \"size\": \"100\"," +
                            "    \"key\": \"\"," +
                            "    \"uid\": \"" + UserId + "\"" +
                            "}", charset));
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
                String totalcount = null;
                try {
                    state = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                //如果讀取資料錯誤 不進行之後的動作
                if (state == null || state.equals("0"))
                    return null;

                try {
                    totalcount = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("totalCount");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }

                if (totalcount != null && Integer.valueOf(totalcount) > 100) {
                    HttpClient client2 = new DefaultHttpClient();
                    HttpPost post2 = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
                    MultipartEntity multipartEntity2 = new MultipartEntity();
                    Charset charset2 = Charset.forName("UTF-8");
                    try {
                        multipartEntity2.addPart("json", new StringBody("{" +
                                "    \"act\": \"list\"," +
                                "    \"type\": \"\"," +
                                "    \"page\": \"1\"," +
                                "    \"size\": \"" + totalcount + "\"," +
                                "    \"key\": \"\"," +
                                "    \"uid\": \"" + UserId + "\"" +
                                "}", charset2));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    post2.setEntity(multipartEntity2);
                    HttpResponse response2 = null;
                    getString = null;
                    try {
                        response2 = client2.execute(post2);
                        getString = EntityUtils.toString(response2.getEntity());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        state = new JSONObject(getString.substring(
                                getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (state == null || state.equals("0"))
                        return null;
                    try {
                        totalcount = new JSONObject(getString).getString("totalCount");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //如果總數錯誤就不繼續進行了!!
                if (totalcount == null || Integer.valueOf(totalcount) <= 0)
                    return null;
                //正式處理資料
                String[][] jsonObjects = null;
                JSONArray jsonArray = null;
                jsonObjects = new String[Integer.valueOf(totalcount)][8];
                try {
                    jsonArray = new JSONObject(getString).getJSONArray("list");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //如果資料長度錯誤就不繼續進行了!!
                if (jsonArray == null || jsonArray.length() <= 0)
                    return null;
                for (int i = 0; i < Integer.valueOf(totalcount); i++) {
                    try {
                        jsonObjects[i][0] = jsonArray.getJSONObject(i).getString("id");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][1] = jsonArray.getJSONObject(i).getString("order_no");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][2] = jsonArray.getJSONObject(i).getString("add_time");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][3] = jsonArray.getJSONObject(i).getString("accept_name");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][4] = jsonArray.getJSONObject(i).getString("mobile");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][5] = jsonArray.getJSONObject(i).getString("email");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        String order_amount = jsonArray.getJSONObject(i).getString("order_amount");
                        if (order_amount.contains(".")) {//有小數點!!
                            order_amount = order_amount.substring(0, order_amount.indexOf("."));
                        }
                        jsonObjects[i][6] = order_amount;
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][7] = jsonArray.getJSONObject(i).getString("status");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                DataBaseHelper helper = new DataBaseHelper(context);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_no",
                        "order_time", "order_name", "order_phone", "order_email",
                        "order_money", "order_state"}, null, null, null, null, null);
                if (order_cursor != null) {
                    ContentValues cv = new ContentValues();
                    if (order_cursor.getCount() == 0) {//是新的資料庫 -> 新增資料
                        for (String[] string : jsonObjects) {//會跑[H][]次
                            cv.clear();
                            cv.put("order_id", string[0]);
                            cv.put("order_no", string[1]);
                            cv.put("order_time", string[2]);
                            cv.put("order_name", string[3]);
                            cv.put("order_phone", string[4]);
                            cv.put("order_email", string[5]);
                            cv.put("order_money", string[6]);
                            cv.put("order_state", string[7]);
                            long result = database.insert("shoporder", null, cv);
                            returnMessage = returnMessage + "新的資料庫新增資料:" + string[0] + " result:" + result;
                        }

                    } else { //已經有資料庫了->確認是否有重複資料 ->確認是否要更新狀態 // -> 確認是否有新的資料
                        for (String[] string : jsonObjects) {
                            Cursor order_cursor_dul = database.query("shoporder", new String[]{"order_id", "order_no",
                                            "order_time", "order_name", "order_phone",
                                            "order_email", "order_money", "order_state"},
                                    "order_id=" + string[0], null, null, null, null);
                            if (order_cursor_dul != null && order_cursor_dul.getCount() > 0) {
                                //有重複的資料 ->確認是否更新狀態!
                                order_cursor_dul.moveToFirst();
                                while (order_cursor_dul.isAfterLast()) {
                                    if (!order_cursor_dul.getString(7).equals(string[7])) {//資料不相同
                                        cv.clear();
                                        cv.put("order_state", string[7]);
                                        long result = database.update("shoporder", cv, "order_id=?", new String[]{string[0]});
                                        returnMessage = returnMessage + "新的資料庫更新資料:" + string[0] + " result:" + result;
                                    }
                                    order_cursor_dul.moveToNext();
                                }
                            } else {
                                cv.clear();
                                cv.put("order_id", string[0]);
                                cv.put("order_no", string[1]);
                                cv.put("order_time", string[2]);
                                cv.put("order_name", string[3]);
                                cv.put("order_phone", string[4]);
                                cv.put("order_email", string[5]);
                                cv.put("order_money", string[6]);
                                cv.put("order_state", string[7]);
                                long result = database.insert("shoporder", null, cv);
                                returnMessage = returnMessage + "舊的資料庫新增資料:" + string[0] + " result:" + result;
                            }
                            if (order_cursor_dul != null)
                                order_cursor_dul.close();
                        }
                    }
                    order_cursor.close();
                }
                database.close();
            }
            return returnMessage;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("3.11", "shoprecord on PostExecute:" + s);
            if (s != null)
                adapter.notifyDataSetChanged();
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
            int oldCount = 0;
            /** 新增會員資料 **/
            if (OK) {
                DataBaseHelper helper = new DataBaseHelper(LoginActivity.this);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor member_cursor = database.query("member", new String[]{"account", "password",
                        "name", "phone", "email", "addr"}, null, null, null, null, null);
                Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_no",
                        "order_time", "order_name", "order_phone", "order_email",
                        "order_money", "order_state"}, null, null, null, null, null);

                if(order_cursor!=null)
                    oldCount = order_cursor.getCount();
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
            {
                final int finalOldCount = oldCount;
                a.schedule(new TimerTask() {
                               @Override
                               public void run() {
//                        Intent intent = new Intent();
//                        intent.setClass(LoginActivity.this, HomepageActivity.class);
//                        startActivity(intent);
                                   finish();

                               }
                           },
//                        0
                        1500
                );
            }


            super.onPostExecute(string);
        }
    }
}
