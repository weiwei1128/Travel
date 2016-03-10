package com.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.travel.Adapter.BuyitemAdapter;
import com.travel.Utility.Functions;

public class BuyItemListActivity extends AppCompatActivity {

    ListView listView;
    BuyitemAdapter adapter;
    ImageView backImg, moreImg;
    LinearLayout confirmLayout;
    int lastItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyitem_list_activity);
        Bundle bundle = this.getIntent().getExtras();
//        if (bundle.containsKey("WhichItem")) {
//            lastItem = bundle.getInt("WhichItem");
//        }
        listView = (ListView) findViewById(R.id.listview);
        backImg = (ImageView) findViewById(R.id.buyitemlist_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("WhichItem", lastItem);
                Functions.go(true,BuyItemListActivity.this, BuyItemListActivity.this,
                        BuyItemDetailActivity.class,
                        null
//                        bundle
                );
            }
        });
        moreImg = (ImageView) findViewById(R.id.buyitemlist_moreImg);
        confirmLayout = (LinearLayout) findViewById(R.id.buyitemlist_listLayout);
        confirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("12/30", "listconfirmLayout CLICKED!!!");
                Bundle bundle = new Bundle();
                bundle.putInt("WhichItem", lastItem);
                Functions.go(false,BuyItemListActivity.this, BuyItemListActivity.this,
                        BuyItemListConfirmActivity.class,null
//                        bundle
                );
            }
        });
        adapter = new BuyitemAdapter(this);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", lastItem);
            Functions.go(true,BuyItemListActivity.this, BuyItemListActivity.this,
                    BuyItemDetailActivity.class,null
//                        bundle
            );
        }
        return false;
    }
}
