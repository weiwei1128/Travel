package com.travel.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;


/**
 * Created by Tinghua on 2016/3/26.
 */
public class RecordFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private String tabTitles[] = new String[] {"旅遊軌跡", "旅遊日誌"};

    public RecordFragmentPagerAdapter(FragmentManager fm,List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
