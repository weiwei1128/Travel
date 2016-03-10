package com.travel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.travel.Adapter.BuyRecordAdapter;
import com.travel.Utility.Functions;

public class BuyRecordActivity extends AppCompatActivity {

    ImageView backImg;
    ListView listView;
    BuyRecordAdapter adapter;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
            Functions.go(true,BuyRecordActivity.this,BuyRecordActivity.this,HomepageActivity.class,null);
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_record_activity);
        backImg = (ImageView)findViewById(R.id.buyrecordlist_backImg);
        listView = (ListView)findViewById(R.id.buyrecordlist_listview);
        adapter = new BuyRecordAdapter(BuyRecordActivity.this);
        listView.setAdapter(adapter);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,BuyRecordActivity.this,BuyRecordActivity.this,HomepageActivity.class,null);
            }
        });
    }
}
