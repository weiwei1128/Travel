package com.flyingtravel.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;

public class CheckScheduleOKActivity extends AppCompatActivity {
    String itemid;
    LinearLayout backImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkschedule_okactivity);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey("order_id")) {
            itemid = bundle.getString("order_id");
            setupWebview();
        } else Toast.makeText(CheckScheduleOKActivity.this,
                CheckScheduleOKActivity.this.getResources().getString(R.string.wrongData_text), Toast.LENGTH_SHORT).show();

        backImg = (LinearLayout) findViewById(R.id.checkschedule_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, CheckScheduleOKActivity.this, CheckScheduleOKActivity.this,
                        CheckScheduleActivity.class, null);
            }
        });
    }

    void setupWebview() {
        //WEBVIEW VERSION
        final ProgressDialog dialog = new ProgressDialog(CheckScheduleOKActivity.this);
        dialog.setMessage(CheckScheduleOKActivity.this.getResources().getString(R.string.loading_text));
        dialog.setCancelable(false);
        dialog.show();

        WebView webView = (WebView) findViewById(R.id.checkschedule_webview);
        String myURL = "http://zhiyou.lin366.com/guihua.aspx?id=" + itemid;

        WebSettings websettings = webView.getSettings();
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dialog.dismiss();
            }
        });
        webView.loadUrl(myURL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            Functions.go(true, CheckScheduleOKActivity.this, CheckScheduleOKActivity.this,
                    CheckScheduleActivity.class, null);
        return super.onKeyDown(keyCode, event);
    }
}
