package com.flyingtravel.Utility;

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

    //GlobalVariable globalVariable;
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
//        Log.d("3.7", "Service onStartCommand");

        //利用 executeOnExecutor 確切執行非同步作業
        new Banner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new Goods(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new News().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new Special(context, new Functions.TaskCallBack() {
            @Override
            public void TaskDone(Boolean OrderNeedUpdate) {

            }
        }, 0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new ExchangeRate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return 1;
    }

    @Override
    public void onCreate() {
        context = this.getBaseContext();
        //globalVariable = (GlobalVariable) context.getApplicationContext();
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    /**
     * 各種JSON
     **/

    private class ExchangeRate extends AsyncTask<String, Void, Boolean> {
        String us, cn;

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            //http://zhiyou.lin366.com/api/feedback/index.aspx
            //http://zhiyou.lin366.com/api/feedback/content.aspx
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/feedback/content.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"content\"}", chars));
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
            String message = null;
            try {
                message = new JSONObject(result.substring(
                        result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
            if (message == null || !message.equals("1"))
                return false;
            else {
                try {
                    us = new JSONObject(result.substring(
                            result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("us");
                } catch (JSONException | NullPointerException e2) {
                    e2.printStackTrace();
                }

                try {
                    cn = new JSONObject(result.substring(
                            result.indexOf("{"), result.lastIndexOf("}") + 1)).getString("cn");
                } catch (JSONException | NullPointerException e2) {
                    e2.printStackTrace();
                }
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
                sharedPreferences.edit().putString("us", us).apply();
                sharedPreferences.edit().putString("cn", cn).apply();

            }
            super.onPostExecute(result);
        }
    }

    private class Banner extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {
//            Log.e("3.9", "=========Banner======doInBackground");

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

//            Log.d("3.7", "BannerService result:" + result);
            if (jsonArray != null) {
//                Log.d("3.7", "BannerService result:" + jsonArray.length());
                DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor cursor = database.query("banner", new String[]{"img_url", "link", "bannerid"}, null, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0)
                    database.delete("banner", null, null);
                if (cursor != null)
                    cursor.close();
                ContentValues contentValues = new ContentValues();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("count", jsonArray.length());
                final int anInt = jsonArray.length();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        editor.putString("img" + i,
                                "http://zhiyou.lin366.com" + jsonArray.getJSONObject(i).getString("img_url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //link_url"
                    try {
                        contentValues.clear();
                        contentValues.put("img_url", "http://zhiyou.lin366.com" + jsonArray.getJSONObject(i).getString("img_url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("link", jsonArray.getJSONObject(i).getString("link_url"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //id
                    try {
                        contentValues.put("bannerid", jsonArray.getJSONObject(i).getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    database.insert("banner", null, contentValues);
                }
                editor.apply();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            Intent intent = new Intent("banner");
            intent.putExtra("banner", true);
            sendBroadcast(intent);
            super.onPostExecute(s);

        }
    }

    private class News extends AsyncTask<String, Void, Void> {

        //{"act":"top","type":"tophot","size":"10"}
        //http://zhiyou.lin366.com/api/news/index.aspx
        int count = 0;
        String link[];

        @Override
        protected Void doInBackground(String... params) {
//            Log.e("3.9", "=========News======doInBackground");

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
            String message[] = new String[0];

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
            if (states == null || states.equals("0"))
                return null;
            else {
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONObject(result).getJSONArray("list");
//                    Log.e("3.9", jsonArray.length() + ":jsonArray長度"); //3.9 OK
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (jsonArray != null) {
                    count = jsonArray.length();
                    message = new String[count];
                    link = new String[count];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            message[i] = jsonArray.getJSONObject(i).getString("title");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            link[i] = jsonArray.getJSONObject(i).getString("link_url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
//                message="";//test
//                Log.e("3.10","news: "+message); //3.10 OK
                DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor news_cursor = database.query("news", new String[]{"title", "link"}, null, null, null, null, null);
                if (news_cursor != null && message != null && message.length > 0) {
                    ContentValues cv = new ContentValues();
//                Log.e("3.10","news cursor count: "+news_cursor.getCount());
                    news_cursor.moveToFirst();
                    if (news_cursor.getCount() == 0) {
                        for (int i = 0; i < count; i++) {
                            cv.clear();
                            cv.put("title", message[i]);
                            cv.put("link", link[i]);
                            long result2 = database.insert("news", null, cv);
                        }

                    Log.e("3.10","news insert DB result: "+result);
                    } else //資料不相同 -> 更新
                        for (int i = 0; i < count; i++) {
                            news_cursor.moveToPosition(i);
                            if (!news_cursor.getString(0).equals(message[i])) {
                                cv.clear();
                                cv.put("title", message[i]);
                                cv.put("link", link[i]);
                                //.update("special_activity", cv, "special_id=?", new String[]{jsonObjects[i][0]});
                                long result2 = database.update("news", cv, "title=?", new String[]{news_cursor.getString(i)});
                            }
                        }
                    Log.e("3.10","news update DB result: "+result);

                    news_cursor.close();

                    Intent intent = new Intent("news");
                    intent.putExtra("news", true);
                    context.sendBroadcast(intent);
                }
            }

            return null;
        }
    }


    private class Goods extends AsyncTask<String, String, Void> {

        Context mcontext;
        //0218
        Boolean ifOK = false;
        int Count = 0;


        public Goods(Context context) {
            this.mcontext = context;
        }


        @Override
        protected Void doInBackground(String... params) {
//        Log.e("3.9", "=========Goods======doInBackground");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/article/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json", new StringBody("{\"act\":\"list\",\"type\":\"goods\",\"page\":\"1\",\"size\":\"10\",\"key\":\"\",\"tid\":\"\"}", charset));
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
        /* //測試!
        if (getString != null) {
            Log.d("2.23", "getString: " + getString);
            Log.d("2.25", "getStringLength:" + getString.length());
        } else
            Log.d("2.23", "getString NULL");
            */
            try {
                state = new JSONObject(getString.substring(
                        getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");

            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            try {

                totalcount = new JSONObject(getString).getString("totalCount");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }


            if (totalcount != null && !totalcount.equals("10")) {
//            Log.d("2.25", "updated" + totalcount);
                HttpClient client2 = new DefaultHttpClient();
                HttpPost post2 = new HttpPost("http://zhiyou.lin366.com/api/article/index.aspx");
                MultipartEntity multipartEntity2 = new MultipartEntity();
                Charset charset2 = Charset.forName("UTF-8");
                try {
                    multipartEntity2.addPart("json",
                            new StringBody("{\"act\":\"list\",\"type\":\"goods\",\"page\":\"1\",\"size\":\" "
                                    + totalcount + "\",\"key\":\"\",\"tid\":\"\"}", charset2));
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
                    totalcount = new JSONObject(getString).getString("totalCount");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                //BOTH states and totalCount should be upgraded
//            Log.e("2.25", "Updated: " + getString.length());
            }
            String[][] jsonObjects = null;
            if (state != null && state.equals("1") && totalcount != null) {
                JSONArray jsonArray = null;
                jsonObjects = new String[Integer.valueOf(totalcount)][7];
                try {
                    jsonArray = new JSONObject(getString).getJSONArray("list");
//                Log.e("2.25", jsonArray.length() + ":jsonArray長度");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < Integer.valueOf(totalcount); i++) {
                    try {
                        jsonObjects[i][0] = jsonArray.getJSONObject(i).getString("title");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][1] = jsonArray.getJSONObject(i).getString("img_url");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][2] = jsonArray.getJSONObject(i).getString("zhaiyao");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][3] = jsonArray.getJSONObject(i).getString("add_time");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][4] = jsonArray.getJSONObject(i).getString("id");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        jsonObjects[i][5] = jsonArray.getJSONObject(i).getString("click");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        String sellprice = jsonArray.getJSONObject(i).getString("sell_price");
                        if (sellprice.contains(".")) {
                            //有小數點!!
                            sellprice = sellprice.substring(0, sellprice.indexOf("."));
                        }
//                        Log.e("3.10","special_activity 去除小數點前: "+jsonArray.getJSONObject(i).getString("sell_price")+"後: "+sellprice);
                        jsonObjects[i][6] = sellprice;

//                    jsonObjects[i][6] = jsonArray.getJSONObject(i).getString("sell_price");
                        //sell_price
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                ifOK = true;
                Count = Integer.parseInt(totalcount);
            } else return null;
            //===start writing to DB==//
            if (ifOK && Count != 0) {
                DataBaseHelper helper = DataBaseHelper.getmInstance(context);
                SQLiteDatabase database = helper.getWritableDatabase();
//            database.beginTransaction();
                Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id",
                                "goods_title", "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"},
                        null, null, null, null, null);
                if (goods_cursor != null && jsonObjects != null) {
                    if (goods_cursor.getCount() == 0) //如果還沒新增過資料->直接新增!
                        for (int i = 0; i < Count; i++) {
                            //其中一項資料不得為NULL
                            if (jsonObjects[i][0] != null && jsonObjects[i][1] != null
                                    && jsonObjects[i][2] != null && jsonObjects[i][3] != null
                                    && jsonObjects[i][4] != null && jsonObjects[i][5] != null
                                    && jsonObjects[i][6] != null) {
                                ContentValues cv = new ContentValues();
                                cv.put("goods_title", jsonObjects[i][0]);
                                cv.put("goods_url", jsonObjects[i][1]);
                                cv.put("goods_content", jsonObjects[i][2]);
                                cv.put("goods_addtime", jsonObjects[i][3]);
                                cv.put("goods_id", jsonObjects[i][4]);
                                cv.put("goods_click", jsonObjects[i][5]);
                                cv.put("goods_money", jsonObjects[i][6]);
                                long result = database.insert("goods", null, cv);
//                            Log.d("2.19＿沒有重複資料", result + " = DB INSERT" + i + "title " + jsonObjects[i][0]);
                            }
                        }
                    else { //資料庫已經有資料了!
                        for (int i = 0; i < Count; i++) {
                            if (jsonObjects[i][0] != null && jsonObjects[i][1] != null
                                    && jsonObjects[i][2] != null && jsonObjects[i][3] != null
                                    && jsonObjects[i][4] != null && jsonObjects[i][5] != null
                                    && jsonObjects[i][6] != null) {
                                Cursor goods_dul = database.query(true, "goods", new String[]{"totalCount", "goods_id",
                                                "goods_title", "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"},
                                        "goods_id=" + jsonObjects[i][4], null, null, null, null, null);
                                if (goods_dul != null && goods_dul.getCount() > 0) {
                                    //TODO 要更新click資料？
                                    goods_dul.moveToFirst();
                                } else {
                                    ContentValues cv = new ContentValues();
                                    cv.put("goods_title", jsonObjects[i][0]);
                                    cv.put("goods_url", jsonObjects[i][1]);
                                    cv.put("goods_content", jsonObjects[i][2]);
                                    cv.put("goods_addtime", jsonObjects[i][3]);
                                    cv.put("goods_id", jsonObjects[i][4]);
                                    cv.put("goods_click", jsonObjects[i][5]);
                                    cv.put("goods_money", jsonObjects[i][6]);
                                    long result = database.insert("goods", null, cv);
//                                Log.d("2.25_新增過資料", result + " = DB INSERT" + i + "title " + jsonObjects[i][0]);
                                }
                                if (goods_dul != null)
                                    goods_dul.close();
                            }
                        }

                    }
                }
//            else
//                Log.d("2.19", "something NULL!" + jsonObjects + " :jsonObjects");
                if (goods_cursor != null)
                    goods_cursor.close();
            }
            return null;
        }
    }

}



