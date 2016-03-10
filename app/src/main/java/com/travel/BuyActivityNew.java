package com.travel;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Adapter.BuyFragmentViewPagerAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

import java.util.ArrayList;
import java.util.List;

public class BuyActivityNew extends AppCompatActivity {
    ViewPager viewPager;
    List<Fragment> fragments = new ArrayList<>();
    BuyFragmentViewPagerAdapter adapter;
    DataBaseHelper helper;
    SQLiteDatabase database;
    ImageView backImg, ListImg;
    //0307
    LinearLayout linearLayout;
    int count = 0;
    public ArrayList<TextView> textViews = new ArrayList<>();

    @Override
    protected void onResume() {
        //if need to display button
//        Log.d("3.9", "BuyActivityNew onResume");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int howmany = sharedPreferences.getInt("InBuyList", 0);
        if (howmany > 0) {
            ListImg.setVisibility(View.VISIBLE);
            ListImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Functions.go(false, BuyActivityNew.this,
                            BuyActivityNew.this, BuyItemListActivity.class, null);
                }
            });
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.e("3.10", "buyActivity onStart!");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_activity_new);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        linearLayout = (LinearLayout) findViewById(R.id.buy_textlayout);
        backImg = (ImageView) findViewById(R.id.buy_backImg);
        ListImg = (ImageView) findViewById(R.id.buy_listImg);
        ListImg.setVisibility(View.INVISIBLE);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, BuyActivityNew.this, BuyActivityNew.this, HomepageActivity.class, null);
            }
        });

        int howmany = sharedPreferences.getInt("InBuyList", 0);
        helper = new DataBaseHelper(BuyActivityNew.this);
        database = helper.getWritableDatabase();
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_addtime"}, null, null, null, null, null);


        if (howmany > 0) {
            ListImg.setVisibility(View.VISIBLE);
            ListImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Functions.go(false, BuyActivityNew.this,
                            BuyActivityNew.this, BuyItemListActivity.class, null);
                }
            });
        }

        if (goods_cursor != null) {
            count = goods_cursor.getCount();
            goods_cursor.close();
        }
//        Log.d("3.7", "" + count);
        int pages = 0;
        if (count % 10 > 0)
            pages = (count / 10) + 1;
        else pages = (count / 10);

        viewPager = (ViewPager) findViewById(R.id.buy_viewpager);
        //fragment(i) -> i代表第幾頁
        for (int i = 0; i < pages; i++) {
            fragments.add(new BuyFragmentNew(i + 1));
            TextView number = new TextView(this);
            number.setText(i + 1 + "  ");
            number.setTextColor(getResources().getColor(R.color.black));
            if (i == 0)
                number.setTextColor(getResources().getColor(R.color.peach));
            textViews.add(number);
            linearLayout.addView(number);
        }


        adapter = new BuyFragmentViewPagerAdapter(this.getSupportFragmentManager(), viewPager,
                fragments, BuyActivityNew.this, 4, 1);

        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new PageListener());
        if (adapter.getCount() == 0)
            Toast.makeText(BuyActivityNew.this, "尚無資料!", Toast.LENGTH_SHORT).show();
        Log.e("3.8", "currentItem:" + viewPager.getCurrentItem() + "" + adapter.getCurrentPosition());
    }

    private class PageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
//            Log.e("3.8", "**********onPageSelected" + position);
            for (int i = 0; i < textViews.size(); i++)
                textViews.get(i).setTextColor(Color.parseColor("#000000"));
            textViews.get(position).setTextColor(Color.parseColor("#FF0088"));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            Functions.go(true, BuyActivityNew.this, BuyActivityNew.this, HomepageActivity.class, null);
        return false;
    }
}
