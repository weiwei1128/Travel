package com.travel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Adapter.SpecialFragmentViewPagerAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.FlowLayout;
import com.travel.Utility.Functions;

import java.util.ArrayList;
import java.util.List;

public class SpecialActivity extends AppCompatActivity {
    ImageView backImg;
    FlowLayout flowLayout;
    int FragmentNumber = 0;
    int PageNo = 0;
    ViewPager viewPager;
    List<Fragment> fragments = new ArrayList<>();
    List<TextView> NoText = new ArrayList<>();
    SpecialFragmentViewPagerAdapter specialFragmentViewPagerAdapter;
    DataBaseHelper helper;
    SQLiteDatabase database;
    FragmentManager fragmentManager;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.special_activity_new);
        backImg = (ImageView) findViewById(R.id.special_backImg);
        flowLayout = (FlowLayout) findViewById(R.id.special_flowlayout);
        viewPager = (ViewPager) findViewById(R.id.special_viewpager);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, SpecialActivity.this, SpecialActivity.this, HomepageActivity.class, null);
            }
        });
        helper = new DataBaseHelper(SpecialActivity.this);
        database = helper.getReadableDatabase();
        Cursor special = database.query("special_activity", new String[]{"special_id", "title", "img", "content", "price", "click"},
                null, null, null, null, null);
        if (special != null) {
            FragmentNumber = special.getCount();
            special.close();
        }
        fragmentManager = this.getSupportFragmentManager();

        if (FragmentNumber % 10 > 0)
            PageNo = (FragmentNumber / 10) + 1;
        else PageNo = FragmentNumber / 10;

        for (int i = 0; i < PageNo; i++) {
            fragments.add(new SpecialFragment((i + 1)));
            TextView number = new TextView(this);
            number.setText(i + 1 + "  ");
            number.setTextColor(getResources().getColor(R.color.black));
            if (i == 0) {
                number.setTextColor(getResources().getColor(R.color.peach));
            }
            NoText.add(number);
            flowLayout.addView(number);
        }
        specialFragmentViewPagerAdapter = new SpecialFragmentViewPagerAdapter(this.getSupportFragmentManager(),
                viewPager, fragments, this);
        viewPager.setAdapter(specialFragmentViewPagerAdapter);
        viewPager.setOnPageChangeListener(new PageListener());
        if (specialFragmentViewPagerAdapter.getCount() == 0)
            Toast.makeText(this, "尚無資料！", Toast.LENGTH_SHORT).show();
//*/
//
    }


    private class PageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
//            Log.e("3.8", "**********onPageSelected" + position);
            for (int i = 0; i < NoText.size(); i++)
                NoText.get(i).setTextColor(getResources().getColor(R.color.black));
            NoText.get(position).setTextColor(getResources().getColor(R.color.peach));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, SpecialActivity.this, SpecialActivity.this, HomepageActivity.class, null);
        }

        return false;
    }
}
