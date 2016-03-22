package com.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.travel.Adapter.CheckScheduleAdapter;
import com.travel.Utility.Functions;

public class CheckScheduleMainActivity extends AppCompatActivity {
    ImageView backImg;
    ListView listView;
    CheckScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWebview();
        /*
        if (Functions.ifLogin(CheckScheduleMainActivity.this)) {
            setContentView(R.layout.check_schedule_main_activity); //0309
            listView = (ListView) findViewById(R.id.checkschedule_listview); //0309
            adapter = new CheckScheduleAdapter(CheckScheduleMainActivity.this); //0309
            listView.setAdapter(adapter); //0309
        listView.setOnItemClickListener(new itemClickListener()); //0309

        } else {
            setupWebview();
        }
*/

//        setContentView(R.layout.check_schedule_main_activity); //0309
        backImg = (ImageView) findViewById(R.id.checkschedulelist_backImg);
//        listView = (ListView)findViewById(R.id.checkschedule_listview); //0309
//        adapter = new CheckScheduleAdapter(CheckScheduleMainActivity.this); //0309
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, CheckScheduleMainActivity.this, CheckScheduleMainActivity.this,
                        HomepageActivity.class, null);
            }
        });

//        listView.setAdapter(adapter); //0309
//        listView.setOnItemClickListener(new itemClickListener()); //0309
    }

    void setupListview() {

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
            Functions.go(true, CheckScheduleMainActivity.this, CheckScheduleMainActivity.this,
                    HomepageActivity.class, null);
        return false;
    }

    class itemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Functions.go(false, CheckScheduleMainActivity.this,
                    CheckScheduleMainActivity.this, CheckScheduleActivity.class, null);
        }
    }
}
