package com.flyingtravel.Utility;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.flyingtravel.R;

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
import java.util.Timer;

/**
 * Created by wei on 2016/5/31.
 *
 * sighUp
 */
public class sighUp extends AsyncTask<String, Void, Boolean> {

    String account, password, name, phone, email, message, address;
    Dialog dialog;
    Activity activity;

    public sighUp(String maccount, String mpassword, String mname, String mphone, String memail,
                  String maddress,
                  Dialog mdialog,Activity activity) {
        this.account = maccount;
        this.password = mpassword;
        this.name = mname;
        this.phone = mphone;
        this.email = memail;
        this.address = maddress;
        this.dialog = mdialog;
        this.activity = activity;
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
            entity.addPart("json", new StringBody("{\"act\":\"reg\"," +
                    "\"username\":\"" + account + "\"," +
                    "\"password\":\"" + password + "\"," +
                    "\"email\":\"" + email + "\"," +
                    "\"mobile\":\"" + phone + "\",\"nickname\":\"" + name + "\"," + "\"area\":\"" + "\",\"birthday\":\"" + "\",\"city\":\"\",\"amount\":\"1\"}", chars));
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
        } catch (JSONException | NullPointerException e2) {
            e2.printStackTrace();
        }
        try {
            message = new JSONObject(result.substring(
                    result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("msg");
        } catch (JSONException | NullPointerException e2) {
            e2.printStackTrace();
        }
        return state != null && state.equals("1");
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean)
            message = activity.getResources().getString(R.string.regReply_text) + message;

        Toast.makeText(activity.getBaseContext(), message, Toast.LENGTH_SHORT).show();
        Timer a = new Timer();
        if (aBoolean) {
            if (dialog.isShowing())
                dialog.dismiss();

            new login_Data(account, password,activity).execute();
        }
        if (dialog.isShowing())
            dialog.dismiss();
        super.onPostExecute(aBoolean);
    }
}
