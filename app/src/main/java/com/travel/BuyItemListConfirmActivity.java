package com.travel;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

public class BuyItemListConfirmActivity extends AppCompatActivity {
    ImageView backImg, moreImg;
    TextView buylistText, totalText;
    int lastItem = 0;
    CheckBox hotel, add;
    EditText addrEdit;
    DataBaseHelper helper;
    SQLiteDatabase database;
    LinearLayout confrimLayout;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", lastItem);
            Functions.go(true,BuyItemListConfirmActivity.this, BuyItemListConfirmActivity.this,
                    BuyItemListActivity.class, bundle);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyitem_list_confirm_activity);
        //get shop list item
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        helper = new DataBaseHelper(BuyItemListConfirmActivity.this);
        database = helper.getWritableDatabase();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            lastItem = bundle.getInt("WhichItem");
        }
        addrEdit = (EditText) findViewById(R.id.buyitemlist_addrEdit);
        addrEdit.setVisibility(View.INVISIBLE);

        backImg = (ImageView) findViewById(R.id.buyitemlistconfirm_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("WhichItem", lastItem);
                Functions.go(true,BuyItemListConfirmActivity.this, BuyItemListConfirmActivity.this,
                        BuyItemListActivity.class, bundle);
            }
        });
        hotel = (CheckBox) findViewById(R.id.buyitemlist_hotelCheck);
        add = (CheckBox) findViewById(R.id.buyitemlist_addrCheck);
        hotel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addrEdit.setVisibility(View.INVISIBLE);
                    add.setChecked(false);
                }
            }
        });
        add.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hotel.setChecked(false);
                    addrEdit.setVisibility(View.VISIBLE);
                }
            }
        });

        confrimLayout = (LinearLayout)findViewById(R.id.buyitemlistconfirm_confirmLay);
        confrimLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO need modify!
                Toast.makeText(BuyItemListConfirmActivity.this,"建構中!",Toast.LENGTH_SHORT).show();
            }
        });
        moreImg = (ImageView) findViewById(R.id.buyitemlistconfirm_moreImg);
        buylistText = (TextView) findViewById(R.id.buyitemlistconfirm_listText);

        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_content", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null && goods_cursor.getCount() != 0) {
            goods_cursor.moveToFirst();
            while (!goods_cursor.isAfterLast()) {
                Log.d("2.24", "確認sharedPreferences:" + goods_cursor.getString(2) + " 數目： " +
                        sharedPreferences.getInt(goods_cursor.getString(1), 0));
                if (sharedPreferences.getInt(goods_cursor.getString(1), 0) != 0)
                    buylistText.append(goods_cursor.getString(2) + " : "
                            + sharedPreferences.getInt(goods_cursor.getString(1), 0) + " 個 \n");
                goods_cursor.moveToNext();
            }
        }
        if (goods_cursor != null)
            goods_cursor.close();
        totalText = (TextView) findViewById(R.id.buyitemlistconfirm_totalText);
    }
}
