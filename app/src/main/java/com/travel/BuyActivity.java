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

public class BuyActivity extends AppCompatActivity {
    ViewPager viewPager;
    List<Fragment> fragments = new ArrayList<>();
    BuyFragmentViewPagerAdapter adapter;
    DataBaseHelper helper;
    SQLiteDatabase database;
    ImageView ListImg;
    LinearLayout backImg;
    int count = 0, pageNo = 1, pages = 0, minus = pageNo - 1;
    TextView number, lastPage, nextPage;
    //http://www.anbon.tw/travel/good_cover.png


    @Override
    protected void onResume() {
        //if need to display button
//        Log.d("3.9", "BuyActivity onResume");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int howmany = sharedPreferences.getInt("InBuyList", 0);
        if (howmany > 0) {
            ListImg.setVisibility(View.VISIBLE);
            ListImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Functions.go(false, BuyActivity.this,
                            BuyActivity.this, BuyItemListActivity.class, null);
                }
            });
        } else {
            ListImg.setVisibility(View.INVISIBLE);
        }
        super.onResume();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_activity);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        UI();


        helper = DataBaseHelper.getmInstance(BuyActivity.this);
        database = helper.getWritableDatabase();
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null) {
            count = goods_cursor.getCount();
            goods_cursor.close();
        }
        int howmany = sharedPreferences.getInt("InBuyList", 0);
        if (howmany > 0) {
            ListImg.setVisibility(View.VISIBLE);
            ListImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Functions.go(false, BuyActivity.this,
                            BuyActivity.this, BuyItemListActivity.class, null);
                }
            });
        } else
            ListImg.setVisibility(View.INVISIBLE);


//        Log.d("3.7", "" + count);

        if (count % 10 > 0)
            pages = (count / 10) + 1;
        else pages = (count / 10);


        //fragment(i) -> i代表第幾頁
        LinearLayout layout = (LinearLayout) findViewById(R.id.buy_textLayout);
        TextView textView = new TextView(this);
        textView.setText("/" + pages);
        textView.setTextColor((Color.parseColor("#000000")));
        number = new TextView(this);
        number.setText("1");
        number.setTextColor((Color.parseColor("#FF0088")));
        layout.addView(number);
        layout.addView(textView);

        for (int i = 0; i < pages; i++) {
            fragments.add(new BuyFragment(i + 1));
        }


        adapter = new BuyFragmentViewPagerAdapter(this.getSupportFragmentManager(), viewPager,
                fragments, BuyActivity.this);

        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new PageListener());
        if (adapter.getCount() == 0)
            Toast.makeText(BuyActivity.this, "尚無資料!", Toast.LENGTH_SHORT).show();
        //        Log.e("3.8", "currentItem:" + viewPager.getCurrentItem() + "" + adapter.getCurrentPosition());
    }


    void UI() {
        lastPage = (TextView) findViewById(R.id.lastpage_text);
        lastPage.setVisibility(View.INVISIBLE);
        nextPage = (TextView) findViewById(R.id.nextpage_text);
        backImg = (LinearLayout) findViewById(R.id.buy_backImg);
        ListImg = (ImageView) findViewById(R.id.buy_listImg);
        viewPager = (ViewPager) findViewById(R.id.buy_viewpager);
        ListImg.setVisibility(View.INVISIBLE);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, BuyActivity.this, BuyActivity.this, HomepageActivity.class, null);
            }
        });
        lastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNo - 2);
            }
        });
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNo);

            }
        });
    }

    private class PageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
            pageNo = position + 1;
            if (pageNo == pages)
                nextPage.setVisibility(View.INVISIBLE);
            else nextPage.setVisibility(View.VISIBLE);

            if (pageNo == 1)
                lastPage.setVisibility(View.INVISIBLE);
            else lastPage.setVisibility(View.VISIBLE);

            minus = pageNo - 1;
            String get = String.valueOf(position + 1);
            number.setText(get);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            Functions.go(true, BuyActivity.this, BuyActivity.this, HomepageActivity.class, null);
        return false;
    }
}
