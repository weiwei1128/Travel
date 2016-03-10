package com.travel.Utility;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.travel.GlobalVariable;

import org.apache.http.HttpResponse;
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

/**
 * Created by wei on 2016/2/23.
 * //要記得註冊service
 */
public class HttpService extends Service {
    Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("3.7", "Service onStartCommand");

        //利用 executeOnExecutor 確切執行非同步作業
        new Banner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new JsonGoods(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new News().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return 1;
    }

    @Override
    public void onCreate() {
        context = this.getBaseContext();
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    private class Banner extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {
            Log.e("3.9", "=========Banner======doInBackground");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/adv/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"top\",\"type\":\"1\",\"size\":\"20\"}}", chars));
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
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONObject(result).getJSONArray("list");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }

            Log.d("3.7", "BannerService result:" + result);
            if (jsonArray != null) {
                Log.d("3.7", "BannerService result:" + jsonArray.length());
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("count", jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        editor.putString("img" + i,
                                "http://zhiyou.lin366.com" + jsonArray.getJSONObject(i).getString("img_url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                editor.apply();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }
    private class News extends AsyncTask<String,Void,String>{

        //{"act":"top","type":"tophot","size":"10"}
        //http://zhiyou.lin366.com/api/news/index.aspx

        @Override
        protected String doInBackground(String... params) {
            Log.e("3.9", "=========News======doInBackground");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/news/index.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"top\",\"type\":\"tophot\",\"size\":\"100\"}", chars));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            post.setEntity(entity);
            HttpResponse resp = null;
            String result = null;
            String states = null;
            String message=null;
            try {
                resp = client.execute(post);
                result = EntityUtils.toString(resp.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                states = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
//            Log.e("3.10","result:"+result); //OK
            if(states==null||states.equals("0"))
                return null;
            else{
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONObject(result).getJSONArray("list");
//                    Log.e("3.9", jsonArray.length() + ":jsonArray長度"); //OK
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return message;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


}
