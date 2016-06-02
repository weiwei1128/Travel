package com.flyingtravel.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.flyingtravel.R;

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

/**
 * Created by wei on 2016/5/31.
 */
public class login_Data extends AsyncTask<String, Void, String> {
    public String maccount, mpassword, mName, mPhone, mEmail, mAddr,mtype, login_result,channel_id;
    Boolean OK = false;
    Activity activity;
    ProgressDialog mDialog;
    public login_Data(String account, String password,Activity activity) {
        this.maccount = account;
        this.mpassword = password;
        this.activity = activity;

    }

    @Override
    protected void onPreExecute() {
        //Loading Dialog
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(activity.getResources().getString(R.string.logining_text));
        mDialog.setCancelable(false);
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        channel_id = sharedPreferences.getString("channel_id",null);
        Log.e("5.24","get Channelid:"+channel_id);
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
        if(channel_id==null)
            channel_id ="12345";
        //0107

        try {

            HttpClient client9 = new DefaultHttpClient();
            HttpPost post9 = new HttpPost("http://zhiyou.lin366.com/api/user/index.aspx");
            MultipartEntity entity9 = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            //"act":"login","username":"ljd110@qq.com","password":"ljd110@qq.com
            entity9.addPart("json", new StringBody("{\"act\":\"login\",\"username\":\""
                    + maccount + "\",\"password\":\"" + mpassword
                    + "\",\"channel_id\":\"" + channel_id + "\"}", chars));

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
                                result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("mobile");
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

                    try {
                        mtype = new JSONObject(result.substring(
                                result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("type");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

//                    Log.e("2.26", "getinfo: " + result + "states:" + message);
//                    Log.e("2.26", "name: " + mName);
//                    Log.e("2.26", "phone: " + mPhone);
//                    Log.e("2.26", "Email: " + mEmail);
//                    Log.e("2.26", "Address: " + mAddr);
//                    Log.e("2.26", "type: " + mtype);
            }
//                else Log.e("2.26", "state: " + state);


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
//            Log.d("2.26", "login result: " + login_result);

        return total;
    }

    @Override
    protected void onPostExecute(String string) {

        /** 新增會員資料 **/
        if (OK) {

            DataBaseHelper helper = DataBaseHelper.getmInstance(activity);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor member_cursor = database.query("member", new String[]{"account", "password",
                    "name", "phone", "email", "addr","type"}, null, null, null, null, null);

            if (member_cursor != null && member_cursor.getCount() > 0) {
                database.delete("member", null, null);
            }
            if (member_cursor != null)
                member_cursor.close();

            ContentValues cv = new ContentValues();
            cv.put("account", maccount);
            cv.put("password", mpassword);
            cv.put("name", mName);
            cv.put("phone", mPhone);
            cv.put("email", mEmail);
            cv.put("addr", mAddr);
            cv.put("type", mtype);
            long result = database.insert("member", null, cv);
            Log.d("2.26", "member_insert:" + result);


        }


        //等toast跑完再跳到下個activity
        if (login_result == null)
            login_result = activity.getResources().getString(R.string.nonet_text);
        if (!OK)
            login_result = activity.getResources().getString(R.string.errorReply_text) + login_result;
        else login_result = activity.getResources().getString(R.string.loginok_text);
        final Toast toast = Toast.makeText(activity.getBaseContext(),
//                    "=====測試結果=====" + "\n" +
                login_result, Toast.LENGTH_LONG);
        toast.show();
        //custom time
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 1000);
        Timer a = new Timer();
        a.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.finish();
            }
        }, 1500);
        mDialog.dismiss();

        super.onPostExecute(string);
    }
}
