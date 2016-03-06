package com.travel;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.Adapter.FragmentViewPagerAdapter;
import com.travel.Utility.Functions;

import java.util.ArrayList;
import java.util.List;

public class CheckScheduleActivity extends AppCompatActivity {

    //slidePager
    ArrayList<String> Titles;
    ViewPager viewPager;
    List<Fragment> fragments = new ArrayList<Fragment>();
    FragmentViewPagerAdapter adapter;
    //slidePager

    ImageView backImg,moreImg;
    TextView gonextTxt,golastTxt;

    //next Day test
    int day=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkschedule_activity);
        moreImg = (ImageView)findViewById(R.id.checkschedule_moreImg);
        moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //-----UNKNOWN-------//
            }
        });

        //TODO golast and gonext

        golastTxt = (TextView)findViewById(R.id.checkschedule_lastdayTxt);
        golastTxt.setVisibility(View.INVISIBLE);
        golastTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (day > 1) {
                    List<Fragment> newFragments = new ArrayList<Fragment>();
                    for (int i = 0; i < 6; i++) {
                        newFragments.add(new CheckScheduleFragment());
                    }
                    day = day - 1;

                    FragmentViewPagerAdapter newAdapter = new FragmentViewPagerAdapter(
                            CheckScheduleActivity.this.getSupportFragmentManager()
                            , viewPager, newFragments, CheckScheduleActivity.this, 5, day
                    );
                    viewPager.setAdapter(newAdapter);
                }
                if (day == 1)
                    golastTxt.setVisibility(View.INVISIBLE);
            }
        });

        gonextTxt = (TextView)findViewById(R.id.checkschedule_nextdayTxt);
        gonextTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (viewPager.getCurrentItem() != viewPager.getAdapter().getCount() - 1)
//                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

                List<Fragment> newFragments = new ArrayList<Fragment>();
                for (int i = 0; i < 6; i++) {
                    newFragments.add(new CheckScheduleFragment());
                }

                day = day + 1;

                FragmentViewPagerAdapter newAdapter = new FragmentViewPagerAdapter(
                        CheckScheduleActivity.this.getSupportFragmentManager()
                        , viewPager, newFragments, CheckScheduleActivity.this, 5, day
                );
                viewPager.setAdapter(newAdapter);
                if (day > 1)
                    golastTxt.setVisibility(View.VISIBLE);
            }
        });

        backImg = (ImageView)findViewById(R.id.checkschedule_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,CheckScheduleActivity.this, CheckScheduleActivity.this,
                        HomepageActivity.class, null);
            }
        });
        viewPager = (ViewPager) findViewById(R.id.check_viewPager);
        slidePage();
    }
//
    void slidePage() {

        fragments.add(new CheckScheduleFragment());
        fragments.add(new CheckScheduleFragment());
        fragments.add(new CheckScheduleFragment());

        adapter = new FragmentViewPagerAdapter(this.getSupportFragmentManager()
                ,viewPager,fragments,CheckScheduleActivity.this,3,day);

        viewPager.setAdapter(adapter);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
            Functions.go(true,CheckScheduleActivity.this, CheckScheduleActivity.this,
                    HomepageActivity.class, null);
        return super.onKeyDown(keyCode, event);
    }
}
