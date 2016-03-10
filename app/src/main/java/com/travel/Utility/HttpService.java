package com.travel.Utility;

import android.app.Service;
import android.content.ContentValues;
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
 * //0310 -> NEWS OK
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
        new Special().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
            String message = "";
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
//                    Log.e("3.9", jsonArray.length() + ":jsonArray長度"); //3.9 OK
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(jsonArray!=null)
                    for(int i=0;i<jsonArray.length();i++){
                        try {
                            message = message+jsonArray.getJSONObject(i).getString("title")+"    ";
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
//                message="";//test
//                Log.e("3.10","news: "+message); //3.10 OK
                return message;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            DataBaseHelper helper = new DataBaseHelper(context);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor news_cursor = database.query("news", new String[]{"title"}, null, null, null, null, null);
            if(news_cursor!=null && !s.equals("")){
//                Log.e("3.10","news cursor count: "+news_cursor.getCount());
                news_cursor.moveToFirst();
                if(news_cursor.getCount()==0) {
                    ContentValues cv = new ContentValues();
                    cv.put("title", s);
                    long result = database.insert("news", null, cv);
//                    Log.e("3.10","news insert DB result: "+result);
                }else if(!news_cursor.getString(0).equals(s)){ //資料不相同 -> 更新
                    ContentValues cv = new ContentValues();
                    cv.put("title", s);
                    long result = database.update("news", cv, null, null);
//                    Log.e("3.10","news update DB result: "+result);
                }
                news_cursor.close();
            }
//            else Log.e("3.10","news: cursor=NULL? message:"+s);
            super.onPostExecute(s);
        }
    }
    private class Special extends AsyncTask<String,Void,String>{
        /*
        * {"act":"list","type":"jindian","page":"1","size":"10","key":"","tid":""}**/

        @Override
        protected String doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/article/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json", new StringBody("{\"act\":\"list\",\"type\":\"jindian\",\"page\":\"1\",\"size\":\"1000\",\"key\":\"\",\"tid\":\"\"}", charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(multipartEntity);
            HttpResponse response = null;
            String getString = null;
            try {
                response = client.execute(post);
                getString = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String state = null;
            String totalcount = null;
            try {
                state = new JSONObject(getString.substring(
                        getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
                totalcount = new JSONObject(getString).getString("totalCount");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            /*
            *{
        "id": "618",
        "title": "雲林-北港春生活博物館",
        "img_url": "http://www.abic.com.tw/photoDB/post/1429406074.jpg",
        "zhaiyao": "北港春生活博物館，位於雲林，是一個以木工為特色的博物館，是由超過70年的木工傢具店「盛椿木業」所轉型成立的，裡面的木工文化別有一番特色。館區最特別的就是有一個捷克藝術家海大海的進駐，使得館區內中西文化交錯，呈現一個兼容並蓄的藝文空間。裡面除了有木工文物的展示…",
        "click": "0",
        "add_time": "2016/3/9 17:49:57",
        "sell_price": "100.00"
    }
            * */


            String[][] jsonObjects = null;
            if (state != null && state.equals("1") && totalcount != null) {
                JSONArray jsonArray = null;
                jsonObjects = new String[Integer.valueOf(totalcount)][6];
                try {
                    jsonArray = new JSONObject(getString).getJSONArray("list");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < Integer.valueOf(totalcount); i++) {
                    try {
                        jsonObjects[i][0] = jsonArray.getJSONObject(i).getString("id");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][1] = jsonArray.getJSONObject(i).getString("title");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][2] = jsonArray.getJSONObject(i).getString("img_url");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][3] = jsonArray.getJSONObject(i).getString("zhaiyao");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][4] = jsonArray.getJSONObject(i).getString("click");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        //TODO 要把小數點去掉
                        //goods_cursor.getString(4).substring(0, goods_cursor.getString(4).indexOf("."))
                        String sellprice = jsonArray.getJSONObject(i).getString("sell_price");
                        if(sellprice.contains(".")){
                            //有小數點!!
                            sellprice = sellprice.substring(0,sellprice.indexOf("."));
                        }
                        Log.e("3.10","special 去除小數點前: "+jsonArray.getJSONObject(i).getString("sell_price")+"後: "+sellprice);
                        jsonObjects[i][5] = sellprice;
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}
