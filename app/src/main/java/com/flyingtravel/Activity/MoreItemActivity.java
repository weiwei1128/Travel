package com.flyingtravel.Activity;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreItemActivity extends AppCompatActivity {
    int position = 0;
    TextView header;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_fragment);
        LinearLayout content = (LinearLayout) findViewById(R.id.checkschedule_content);
        LinearLayout backLayout = (LinearLayout) findViewById(R.id.linearLayout2);
        header = (TextView) findViewById(R.id.more_text);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, MoreItemActivity.this, getBaseContext(), HomepageActivity.class, null);
            }
        });
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("載入中");
        dialog.show();
        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("position")) {
            position = bundle.getInt("position");
            WebView webView = new WebView(this);
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
            String myURL = null;
            switch (position) {
                case 0://關於我們
                    myURL = "http://zhiyou.lin366.com/help.aspx?tid=84";
                    header.setText("關於我們");
                    break;
                case 1://規劃行程
                    myURL = "http://zhiyou.lin366.com/diy/";
                    header.setText("規劃行程");
                    break;
            }
            webView.loadUrl(myURL);
            content.addView(webView);
        }

    }


}