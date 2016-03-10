package com.travel;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Adapter.SpecialAdapter;
import com.travel.Adapter.SpecialFragmentViewPagerAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SpecialActivity extends AppCompatActivity {
    ImageView backImg;
    LinearLayout textLayout;
    int FragmentNumber=0;
    int PageNo=0;
    ViewPager viewPager;
    List<Fragment> fragments = new ArrayList<>();
    List<TextView> NoText = new ArrayList<>();
    SpecialFragmentViewPagerAdapter specialFragmentViewPagerAdapter;
    DataBaseHelper helper;
    SQLiteDatabase database;
    FragmentManager fragmentManager;

    @Override
    protected void onStart() {
        Log.e("3.10","Special onStart");
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("3.10", "Special onCreate");
        setContentView(R.layout.special_activity_new);
        backImg = (ImageView) findViewById(R.id.special_backImg);
        textLayout = (LinearLayout)findViewById(R.id.special_textlayout);
        viewPager = (ViewPager)findViewById(R.id.special_viewpager);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                Functions.go(true, SpecialActivity.this, SpecialActivity.this, HomepageActivity.class, null);
            }
        });
        helper = new DataBaseHelper(SpecialActivity.this);
        database = helper.getReadableDatabase();
//        database.beginTransaction();
//        Log.e("3.10", "Special beforeDB");
        Cursor special = database.query("special_activity", new String[]{"special_id", "title", "img", "content", "price", "click"},
                null, null, null, null, null);

        if(special!=null){
            FragmentNumber = special.getCount();
            special.close();
//            Log.e("3.10", "specialNumber:" + FragmentNumber);
//            database.endTransaction();
//            database.close();
        }
        fragmentManager = this.getSupportFragmentManager();


//        Log.e("3.10","specialNumber:"+FragmentNumber);
//        new UI().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        /*
        if(FragmentNumber%10>0)
            PageNo = (FragmentNumber/10)+1;
        else PageNo = FragmentNumber/10;
        for(int i=0;i<PageNo;i++){
            fragments.add(new SpecialFragment((i+1)));
            TextView number = new TextView(this);
            number.setText(i + 1 + "  ");
            number.setTextColor(getResources().getColor(R.color.black));
            if (i == 0)
                number.setTextColor(getResources().getColor(R.color.peach));
            NoText.add(number);
            textLayout.addView(number);
        }
        specialFragmentViewPagerAdapter = new SpecialFragmentViewPagerAdapter(this.getSupportFragmentManager(),
                viewPager,fragments,this);
        viewPager.setAdapter(specialFragmentViewPagerAdapter);
        viewPager.setOnPageChangeListener(new PageListener());
        if(specialFragmentViewPagerAdapter.getCount()==0)
            Toast.makeText(this,"尚無資料！",Toast.LENGTH_SHORT).show();
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
                NoText.get(i).setTextColor(Color.parseColor("#000000"));
            NoText.get(position).setTextColor(Color.parseColor("#FF0088"));
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
