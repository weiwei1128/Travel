package com.travel;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.travel.Adapter.CheckScheduleAdapter;
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

public class CheckScheduleActivity extends AppCompatActivity {
    ImageView backImg;
    ListView listView;
    CheckScheduleAdapter adapter;
    int itemCount = 0;
    String uid = null;
    int count = 0;
    String[] itemid, itemno, itemdate, itemprice, itemcontent, itemstate;
    LinearLayout putItemLayout;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkschedule_basic);
        backImg = (ImageView) findViewById(R.id.checkschedule_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, CheckScheduleActivity.this, CheckScheduleActivity.this,
                        HomepageActivity.class, null);
            }
        });
        putItemLayout = (LinearLayout) findViewById(R.id.checkschedule_content);
        DataBaseHelper helper = new DataBaseHelper(CheckScheduleActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor member_cursor = database.query("member", new String[]{"account", "password",
                "name", "phone", "email", "addr"}, null, null, null, null, null);
        if (member_cursor != null && member_cursor.getCount() > 0) {
            member_cursor.moveToFirst();
            uid = member_cursor.getString(0);
        }
        if (member_cursor != null)
            member_cursor.close();
        if (uid != null) {
            Log.i("3.25", "uid!=null");
            new getSchedule(uid, new Functions.TaskCallBack() {
                @Override
                public void TaskDone(Boolean OrderNeedUpdate) {
                    methodThatDoesSomethingWhenTaskIsDone(OrderNeedUpdate);
                    Log.i("3.25", "TaskDone" + OrderNeedUpdate);
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            setupWebview();

        }

        //delete below???
        /*
        Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_no",
                "order_time", "order_name", "order_phone", "order_email",
                "order_money", "order_state", "order_schedule"}, "order_schedule=" + "1", null, null, null, null);
        if (order_cursor != null) {
            Log.e("3.23", "count!!" + order_cursor.getCount());
            itemCount = order_cursor.getCount();
            order_cursor.close();
        }

        if (Functions.ifLogin(CheckScheduleActivity.this) && itemCount > 0) {
            setContentView(R.layout.checkschedule_activity); //0309
            listView = (ListView) findViewById(R.id.checkschedule_listview); //0309
            adapter = new CheckScheduleAdapter(CheckScheduleActivity.this, itemCount); //0309
            listView.setAdapter(adapter); //0309
            listView.setOnItemClickListener(new itemClickListener()); //0309

        } else {
            setupWebview();
        }
        */
        //delete upside?


    }

    void setupWebview() {
        Log.i("3.25", "setWebView");
        //WEBVIEW VERSION
        WebView webView = new WebView(CheckScheduleActivity.this);
        putItemLayout.addView(webView);
        String myURL = "http://zhiyou.lin366.com/diy/";
        WebSettings websettings = webView.getSettings();
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(myURL);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            Functions.go(true, CheckScheduleActivity.this, CheckScheduleActivity.this,
                    HomepageActivity.class, null);
        return false;
    }

    class itemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //TODO 0326
            if (adapter.getWebviewId(position) != null) {
                Bundle bundle = new Bundle();
                Log.e("3.25", "getWebVIewID:" + adapter.getWebviewId(position) + " id:" + itemid[position]);
                bundle.putString("order_id", adapter.getWebviewId(position));
                Functions.go(false, CheckScheduleActivity.this,
                        CheckScheduleActivity.this, CheckScheduleOKActivity.class, bundle);
            } else
                Toast.makeText(CheckScheduleActivity.this, "資料錯誤!", Toast.LENGTH_SHORT).show();
        }
    }

    class getSchedule extends AsyncTask<String, Void, Boolean> {
        ProgressDialog dialog = new ProgressDialog(CheckScheduleActivity.this);
        Functions.TaskCallBack taskCallBack;
        String uid;

        public getSchedule(String uid, Functions.TaskCallBack taskCallBack) {
            this.taskCallBack = taskCallBack;
            this.uid = uid;
        }

        @Override
        protected void onPreExecute() {
            Log.e("3.25", "OnPreExecute");
            dialog.setMessage("抓取資料中");
            dialog.setCancelable(false);
            dialog.show();

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            //{"act":"line","uid":"ljd110@qq.com"}
            Log.e("3.25", "doInBackground");
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/order/line.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"line\",\"uid\":\"" + uid + "\"}", chars));
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
            Log.i("3.25", "doInBackground" + states);
            if (states == null || states.equals("0"))
                return false;
            else {
                JSONArray jsonArray = null;

                try {
                    jsonArray = new JSONObject(result).getJSONArray("list");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }

                if (jsonArray != null && jsonArray.length() > 0) {
                    count = jsonArray.length();
                    itemcontent = new String[count];
                    itemdate = new String[count];
                    itemid = new String[count];
                    itemno = new String[count];
                    itemprice = new String[count];
                    itemstate = new String[count];
//                    "id": "30",
//                            "order_no": "B16030310063131",
//                            "img_url": "/upload/201601/18/201601181455470401.jpg",
//                            "add_time": "2016/3/3 10:06:31",
//                            "accept_name": "ljd110@qq.com",
//                            "mobile": "ljd110@qq.com",
//                            "email": "ljd110@qq.com",
//                            "order_amount": "1360.00",
//                            "status": "待確認"
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            itemid[i] = jsonArray.getJSONObject(i).getString("id");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            itemno[i] = jsonArray.getJSONObject(i).getString("order_no");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            itemdate[i] = jsonArray.getJSONObject(i).getString("add_time");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            itemcontent[i] = "姓名：" + jsonArray.getJSONObject(i).getString("accept_name");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            itemcontent[i] = itemcontent[i] + "\n電話：" + jsonArray.getJSONObject(i).getString("mobile");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            String sellprice = jsonArray.getJSONObject(i).getString("order_amount");
                            if (sellprice.contains(".")) {
                                //有小數點!!
                                sellprice = sellprice.substring(0, sellprice.indexOf("."));
                            }
                            itemprice[i] = "$" + sellprice;
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            itemstate[i] = jsonArray.getJSONObject(i).getString("status");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }


                /**
                 * {
                 "states": "1",
                 "uid": "ljd110@qq.com",
                 "list": [{
                 "id": "30",
                 "order_no": "B16030310063131",
                 "img_url": "/upload/201601/18/201601181455470401.jpg",
                 "add_time": "2016/3/3 10:06:31",
                 "accept_name": "ljd110@qq.com",
                 "mobile": "ljd110@qq.com",
                 "email": "ljd110@qq.com",
                 "order_amount": "1360.00",
                 "status": "待確認"
                 }, {
                 "id": "23",
                 "order_no": "B16013110100175",
                 "img_url": "/upload/201601/18/201601181454064253.jpg",
                 "add_time": "2016/1/31 10:10:01",
                 "accept_name": "ljd110@qq.com",
                 "mobile": "ljd110@qq.com",
                 "email": "ljd110@qq.com",
                 "order_amount": "20393.00",
                 "status": "待確認"
                 }, {
                 "id": "22",
                 "order_no": "B16012515185916",
                 "img_url": "/upload/201601/18/201601181454064253.jpg",
                 "add_time": "2016/1/25 15:18:59",
                 "accept_name": "ljd110@qq.com",
                 "mobile": "ljd110@qq.com",
                 "email": "ljd110@qq.com",
                 "order_amount": "988.00",
                 "status": "待確認"
                 }, {
                 "id": "21",
                 "order_no": "B16012515123863",
                 "img_url": "/upload/201601/18/201601181454064253.jpg",
                 "add_time": "2016/1/25 15:12:38",
                 "accept_name": "ljd110@qq.com",
                 "mobile": "ljd110@qq.com",
                 "email": "ljd110@qq.com",
                 "order_amount": "988.00",
                 "status": "待確認"
                 }, {
                 "id": "20",
                 "order_no": "B16012515063823",
                 "img_url": "/upload/201601/18/201601181455470401.jpg",
                 "add_time": "2016/1/25 15:06:38",
                 "accept_name": "ljd110@qq.com",
                 "mobile": "ljd110@qq.com",
                 "email": "ljd110@qq.com",
                 "order_amount": "5376.00",
                 "status": "待確認"
                 }]
                 }
                 *
                 * */


                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            Log.e("3.25", "onPostExecute" + s + " -" + count);
            dialog.dismiss();
            taskCallBack.TaskDone(s);
            super.onPostExecute(s);
        }
    }

    private void methodThatDoesSomethingWhenTaskIsDone(Boolean a) {
        if (a) {
            listView = new ListView(CheckScheduleActivity.this);
            putItemLayout.addView(listView);
            adapter = new CheckScheduleAdapter(CheckScheduleActivity.this, count, itemid, itemno,
                    itemdate, itemprice, itemcontent, itemstate); //0309
            listView.setAdapter(adapter); //0309
            listView.setDivider(new ColorDrawable(0xFFFFFFFF));
            listView.setDividerHeight(10);
            listView.setOnItemClickListener(new itemClickListener()); //0309
        } else setupWebview();
    }

}
