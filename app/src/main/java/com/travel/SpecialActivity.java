package com.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.travel.Adapter.SpecialAdapter;
import com.travel.Utility.Functions;

public class SpecialActivity extends AppCompatActivity {
    ImageView backImg, moreImg;
    ListView listView;
    SpecialAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.special_activity);
        backImg = (ImageView) findViewById(R.id.goodthing_backImg);
        listView = (ListView) findViewById(R.id.goodthing_listView);

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                Functions.go(true,SpecialActivity.this, SpecialActivity.this, HomepageActivity.class, null);
            }
        });

        adapter = new SpecialAdapter(SpecialActivity.this);
        listView.setAdapter(adapter);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true,SpecialActivity.this, SpecialActivity.this, HomepageActivity.class, null);
        }

        return false;
    }
}
