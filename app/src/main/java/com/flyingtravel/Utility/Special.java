package com.flyingtravel.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

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
 * Created by wei on 2016/5/29.
 */
public class Special extends AsyncTask<String, Void, Boolean> {
    /*
    * {"act":"list","type":"jindian","page":"1","size":"10","key":"","tid":""}**/
    Context context;
    Functions.TaskCallBack callBack;
    int oldcount;

    public Special(Context context, Functions.TaskCallBack callBack, int oldcount) {
        this.context = context;
        this.callBack = callBack;
        this.oldcount = oldcount;
    }

    @Override
    protected Boolean doInBackground(String... params) {
//            Log.e("3.9", "=========Special======doInBackground");
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/article/index.aspx");
        MultipartEntity multipartEntity = new MultipartEntity();
        Charset charset = Charset.forName("UTF-8");
        try {
            multipartEntity.addPart("json", new StringBody("{\"act\":\"list\",\"type\":\"jindian\",\"page\":\"1\",\"size\":\"9999\",\"key\":\"\",\"tid\":\"\"}", charset));
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
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        if (state == null || state.equals("0"))
            return false;
        try {
            totalcount = new JSONObject(getString).getString("totalCount");
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        //如果總數錯誤就不繼續進行了!!
        if (totalcount == null || Integer.valueOf(totalcount) <= 0)
            return false;
        if (oldcount >= Integer.parseInt(totalcount))
            return false;
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
//                    Log.e("3.10","price**title"+jsonObjects[i][1]);
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
                String sellprice = jsonArray.getJSONObject(i).getString("sell_price");
                if (sellprice.contains(".")) {
                    //有小數點!!
                    sellprice = sellprice.substring(0, sellprice.indexOf("."));
                }
                jsonObjects[i][5] = sellprice;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }

        }


//                Log.e("3.10", "special_activity item size:" + jsonObjects.length);
            DataBaseHelper helper = DataBaseHelper.getmInstance(context);
            SQLiteDatabase database = helper.getWritableDatabase();
//                database.beginTransaction();
            Cursor special = database.query("special_activity", new String[]{"special_id", "title", "img", "content", "price", "click"},
                    null, null, null, null, null);
            if (special != null) {
//                    Log.e("4.19","special != null"+jsonObjects.length);

                if (special.getCount() == 0) //如果還沒新增過資料->直接新增!
                    for (int i = 0; i < jsonObjects.length; i++) {
                        ContentValues cv = new ContentValues();
                        cv.put("special_id", jsonObjects[i][0]);
                        cv.put("title", jsonObjects[i][1]);
                        cv.put("img", jsonObjects[i][2]);
                        cv.put("content", jsonObjects[i][3]);
                        cv.put("price", jsonObjects[i][5]);
                        cv.put("click", jsonObjects[i][4]);
                        long result = database.insert("special_activity", null, cv);
//                            Log.e("4.19", "3 price:" + jsonObjects[i][5] + " title" + jsonObjects[i][1]);
//                            Log.d("3.10", "special_activity: " + result + " = DB INSERT" + i + "title " + jsonObjects[i][1]);
                    }
                else { //資料庫已經有資料了!
                    for (int i = 0; i < jsonObjects.length; i++) {
                        Cursor special_dul = database.query(true, "special_activity", new String[]{"special_id",
                                        "title", "img", "content", "price", "click"},
                                "special_id=" + jsonObjects[i][0], null, null, null, null, null);
                        if (special_dul != null && special_dul.getCount() > 0) {
                            //有重複的資料
                            special_dul.moveToFirst();
                            ContentValues cv = new ContentValues();
                            //若資料不一樣 則更新 ! (besides ID)
                            if (!special_dul.getString(1).equals(jsonObjects[i][1]))
                                cv.put("title", jsonObjects[i][1]);
                            if (!special_dul.getString(2).equals(jsonObjects[i][2]))
                                cv.put("img", jsonObjects[i][2]);
                            if (!special_dul.getString(3).equals(jsonObjects[i][3]))
                                cv.put("content", jsonObjects[i][3]);
                            if (!special_dul.getString(4).equals(jsonObjects[i][5]))
                                cv.put("price", jsonObjects[i][5]);
                            if (!special_dul.getString(5).equals(jsonObjects[i][4]))
                                cv.put("click", jsonObjects[i][4]);
                            if (!special_dul.getString(1).equals(jsonObjects[i][1]) ||
                                    !special_dul.getString(2).equals(jsonObjects[i][2]) ||
                                    !special_dul.getString(3).equals(jsonObjects[i][3]) ||
                                    !special_dul.getString(4).equals(jsonObjects[i][5]) ||
                                    !special_dul.getString(5).equals(jsonObjects[i][4])) {
                                long result = database.update("special_activity", cv, "special_id=?", new String[]{jsonObjects[i][0]});
//                                    Log.e("3.10", "special_activity updated: " + result + " title: " + jsonObjects[i][1]+" price "+jsonObjects[i][5]);
//                                    Log.e("4.19", "3 price:" + jsonObjects[i][5]+" title"+jsonObjects[i][1]);
                            }
//                                else Log.e("4.19", "3 price:" + jsonObjects[i][5]+" title"+jsonObjects[i][1]);
                        } else {
                            //資料庫存在 但資料不存在
                            ContentValues cv = new ContentValues();
                            cv.put("special_id", jsonObjects[i][0]);
                            cv.put("title", jsonObjects[i][1]);
                            cv.put("img", jsonObjects[i][2]);
                            cv.put("content", jsonObjects[i][3]);
                            cv.put("price", jsonObjects[i][5]);
                            cv.put("click", jsonObjects[i][4]);
                            long result = database.insert("special_activity", null, cv);
//                                Log.e("4.19", "3 price:" + jsonObjects[i][5]+" title"+jsonObjects[i][1]);
//                                Log.d("3.10", "special_activity insert: " + result + " = DB INSERT" + i + "title " + jsonObjects[i][1]);
                        }
                        if (special_dul != null)
                            special_dul.close();

                }
                special.close();
            }
            return true;
        } else
            return false;
    }

    @Override
    protected void onPostExecute(Boolean s) {
        callBack.TaskDone(s);
        super.onPostExecute(s);
    }
}

