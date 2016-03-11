package com.travel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.travel.Adapter.ShopRecordAdapter;
import com.travel.Adapter.ShopRecordItemAdapter;
import com.travel.Utility.Functions;

public class ShopRecordItemActivity extends AppCompatActivity {
    int ItemPosition;
    ImageView backImg;
    GridView gridView;
    ShopRecordItemAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoprecord_item_activity);
        backImg = (ImageView)findViewById(R.id.shoprecorditem_backImg);
        gridView = (GridView)findViewById(R.id.shoprecorditem_gridview);
        gridView.setClickable(false);
        adapter = new ShopRecordItemAdapter();
        gridView.setAdapter(adapter);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            ItemPosition = bundle.getInt("WhichItem");
        }

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,ShopRecordItemActivity.this,ShopRecordItemActivity.this,
                        ShopRecordActivity.class,null);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
            Functions.go(true,ShopRecordItemActivity.this,ShopRecordItemActivity.this,
                    ShopRecordActivity.class,null);
        return false;
    }
}
