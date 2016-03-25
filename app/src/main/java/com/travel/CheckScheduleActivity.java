package com.travel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.ListView;
import android.widget.Toast;

import com.travel.Adapter.CheckScheduleAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

public class CheckScheduleActivity extends AppCompatActivity {
    ImageView backImg;
    ListView listView;
    CheckScheduleAdapter adapter;
    int itemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setupWebview();
        DataBaseHelper helper = new DataBaseHelper(CheckScheduleActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
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


        backImg = (ImageView) findViewById(R.id.checkschedulelist_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, CheckScheduleActivity.this, CheckScheduleActivity.this,
                        HomepageActivity.class, null);
            }
        });
    }

    void setupWebview() {
        //WEBVIEW VERSION
        setContentView(R.layout.checkschedule_main_activity_webview);
        WebView webView = (WebView) findViewById(R.id.webView);
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
            String orderid = null;
            DataBaseHelper helper = new DataBaseHelper(CheckScheduleActivity.this);
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_no",
                    "order_time", "order_name", "order_phone", "order_email",
                    "order_money", "order_state", "order_schedule"}, "order_schedule=" + "1", null, null, null, null);
            if (order_cursor != null && order_cursor.getCount() >= position) {
                Log.e("3.23", "count!!" + order_cursor.getCount());
                itemCount = order_cursor.getCount();
                order_cursor.moveToPosition(position);
                if (order_cursor.getString(0) != null)
                    orderid = order_cursor.getString(0);
                order_cursor.close();
            }
            if (orderid != null) {
                Bundle bundle = new Bundle();
                bundle.putString("order_id", orderid);
                Functions.go(false, CheckScheduleActivity.this,
                        CheckScheduleActivity.this, CheckScheduleOKActivity.class, bundle);
            } else
                Toast.makeText(CheckScheduleActivity.this, "資料錯誤!", Toast.LENGTH_SHORT).show();
        }
    }
}
