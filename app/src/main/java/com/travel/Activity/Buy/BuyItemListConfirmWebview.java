package com.travel.Activity.Buy;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.R;
import com.travel.Utility.Functions;

public class BuyItemListConfirmWebview extends AppCompatActivity {
    LinearLayout backImg;
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
        final ProgressDialog dialog = new ProgressDialog(BuyItemListConfirmWebview.this);
        dialog.setMessage("載入中");
        dialog.setCancelable(false);
        dialog.show();


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
        String myURL = "http://zhiyou.lin366.com/pay.aspx?id=" + id;
        webView.loadUrl(myURL);
    }

    void UI() {
        backImg = (LinearLayout) findViewById(R.id.checkschedulelist_backImg);
        header = (TextView) findViewById(R.id.checkschedulelistHeader);
        webView = (WebView) findViewById(R.id.webView);
        header.setText("付款頁面");
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BuyItemListConfirmWebview.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("AfterPay", true);
                editor.apply();
                Functions.go(false, BuyItemListConfirmWebview.this, BuyItemListConfirmWebview.this,
                        BuyActivity.class, null);
                finish();
            }
        });

    }


}
