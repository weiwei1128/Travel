package com.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.Utility.Functions;

public class BuyItemListConfirmWebview extends AppCompatActivity {
    ImageView backImg;
    TextView header;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkschedule_main_activity_webview);
        UI();
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey("confirmId")) {
            setWebView(bundle.getString("confirmId"));

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, BuyItemListConfirmWebview.this, BuyItemListConfirmWebview.this, BuyActivity.class, null);
        }

        return false;
    }

    void setWebView(String id) {

        String myURL = "http://zhiyou.lin366.com/pay.aspx?id=" + id;

        WebSettings websettings = webView.getSettings();
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(myURL);
    }

    void UI() {
        backImg = (ImageView) findViewById(R.id.checkschedulelist_backImg);
        header = (TextView) findViewById(R.id.checkschedulelistHeader);
        webView = (WebView) findViewById(R.id.webView);
        header.setText("付款頁面");
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("AfterPay",true);
                Functions.go(false, BuyItemListConfirmWebview.this, BuyItemListConfirmWebview.this,
                        BuyActivity.class, bundle);
                finish();
            }
        });

    }


}
