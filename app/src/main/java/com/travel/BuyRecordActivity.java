package com.travel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.travel.Adapter.BuyRecordAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

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

public class BuyRecordActivity extends AppCompatActivity {

    ImageView backImg;
    GridView gridView;
    BuyRecordAdapter adapter;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            Functions.go(true, BuyRecordActivity.this, BuyRecordActivity.this, HomepageActivity.class, null);
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_record_activity);
        backImg = (ImageView) findViewById(R.id.buyrecordlist_backImg);
        gridView = (GridView) findViewById(R.id.buy_record_gridview);
        adapter = new BuyRecordAdapter(BuyRecordActivity.this);
        gridView.setAdapter(adapter);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, BuyRecordActivity.this, BuyRecordActivity.this, HomepageActivity.class, null);
            }
        });
        new getShopRecord().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class getShopRecord extends AsyncTask<String, Void, String> {
        String UserId = "ljd110@qq.com";
        Context context = BuyRecordActivity.this;
//http://zhiyou.lin366.com/api/order/index.aspx

        /**
         * {
         * "act": "list",
         * "type": "",
         * "page": "1",
         * "size": "10",
         * "key": "",
         * "uid": "ljd110@qq.com"
         * }
         */
        @Override
        protected String doInBackground(String... params) {
            String returnMessage=null;

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
                            "order_time", "order_name", "order_phone", "order_email", "order_money", "order_state"},
                    null, null, null, null, null);
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
                        cv.put("order_state", string[6]);
                        long result = database.insert("shoporder", null, cv);
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
                                if (!order_cursor_dul.getString(7).equals(string[6])) {//資料不相同
                                    cv.clear();
                                    cv.put("order_state", string[6]);
                                    long result = database.update("shoporder", cv, "order_id=?", new String[]{string[0]});
                                }
                                order_cursor_dul.moveToNext();
                            }
                        }else {
                            cv.clear();
                            cv.put("order_id", string[0]);
                            cv.put("order_no", string[1]);
                            cv.put("order_time", string[2]);
                            cv.put("order_name", string[3]);
                            cv.put("order_phone", string[4]);
                            cv.put("order_email", string[5]);
                            cv.put("order_state", string[6]);
                            long result = database.insert("shoporder", null, cv);
                        }
                        if(order_cursor_dul!=null)
                            order_cursor_dul.close();
                    }
                }
                order_cursor.close();
            }
            database.close();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null)
                Log.i("3.11", "shoprecord NULL");
            super.onPostExecute(s);
        }
    }
}
