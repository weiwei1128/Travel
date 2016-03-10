package com.travel.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wei on 2016/1/4.
 * <p/>
 * ======伴手禮=====
 * //0219__not RUN yet!
 * //0223_context problem may SOLVED 0225
 **/
public class JsonGoods extends AsyncTask<String, String, Map<String, String[][]>> {

    Context mcontext;
    //0218
    Boolean ifOK = false;
    int Count = 0;


    public JsonGoods(Context context) {
        this.mcontext = context;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Map<String, String[][]> doInBackground(String... params) {
        Log.e("3.9", "=========JsonGoods======doInBackground");

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
            } catch (JSONException e) {
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
                    //TODO 要把小數點去掉
                    jsonObjects[i][6] = jsonArray.getJSONObject(i).getString("sell_price");
                    //sell_price
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            ifOK = true;
            Count = Integer.parseInt(totalcount);
        }


        Map<String, String[][]> fromnet = new HashMap<>();
        fromnet.put("item", jsonObjects);
        return fromnet;
    }

    @Override
    protected void onPostExecute(Map<String, String[][]> stringStringMap) {
        String[][] jsonObjects = stringStringMap.get("item");
        if (ifOK && Count != 0) {
            DataBaseHelper helper = new DataBaseHelper(mcontext);
            SQLiteDatabase database = helper.getWritableDatabase();
            database.beginTransaction();
            Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id",
                            "goods_title", "goods_url","goods_money", "goods_content","goods_click", "goods_addtime"},
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
                                            "goods_title", "goods_url","goods_money", "goods_content","goods_click", "goods_addtime"},
                                    "goods_id=" + jsonObjects[i][4], null, null, null, null, null);
                            if (goods_dul != null && goods_dul.getCount() > 0) {
                                //有重複的資料
                                //TODO 要更新click資料
                                goods_dul.moveToFirst();
//                                Log.e("2.25", "有重複的資料!" + goods_dul.getString(1) + "title: " + goods_dul.getString(2));
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
                            if(goods_dul!=null)
                                goods_dul.close();
                        }
                    }

                }
            }
//            else
//                Log.d("2.19", "something NULL!" + jsonObjects + " :jsonObjects");
            if (goods_cursor != null)
                goods_cursor.close();
            database.endTransaction();
            database.close();
        }
        super.onPostExecute(stringStringMap);
    }
}
