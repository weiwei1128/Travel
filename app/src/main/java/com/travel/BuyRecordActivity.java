package com.travel;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.travel.Adapter.BuyRecordAdapter;
import com.travel.Utility.Functions;

public class BuyRecordActivity extends AppCompatActivity {

    ImageView backImg;
    GridView gridView;
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
        gridView = (GridView)findViewById(R.id.buy_record_gridview);
        adapter = new BuyRecordAdapter(BuyRecordActivity.this);
        gridView.setAdapter(adapter);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,BuyRecordActivity.this,BuyRecordActivity.this,HomepageActivity.class,null);
            }
        });

    }

    class getShopRecord extends AsyncTask<String,Void,String>{
//http://zhiyou.lin366.com/api/order/index.aspx
        /**
         * {
         "act": "list",
         "type": "",
         "page": "1",
         "size": "10",
         "key": "",
         "uid": "ljd110@qq.com"
         }
         *
         *
         * */
        @Override
        protected String doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
