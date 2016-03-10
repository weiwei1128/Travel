package com.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.travel.Adapter.BuyAdapter;
import com.travel.Utility.Functions;

public class BuyActivity extends AppCompatActivity {
    GridView gridView;
    BuyAdapter adapter;
    ImageView BackImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_activity);
        gridView = (GridView) findViewById(R.id.buy_gridView);
        BackImg = (ImageView) findViewById(R.id.buy_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,BuyActivity.this, BuyActivity.this, HomepageActivity.class, null);
            }
        });
        adapter = new BuyAdapter(BuyActivity.this,1);
        gridView.setNumColumns(2);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new itemListener());

        if(adapter.getCount()==0)
            Toast.makeText(BuyActivity.this,"尚無資料!",Toast.LENGTH_SHORT).show();
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", position);
            Functions.go(false,BuyActivity.this, BuyActivity.this, BuyItemDetailActivity.class, bundle);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true,BuyActivity.this, BuyActivity.this, HomepageActivity.class, null);
        }

        return false;
    }
}
