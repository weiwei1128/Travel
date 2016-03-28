package com.travel.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.travel.SpotListFragment;
import com.travel.SpotMapFragment;

import java.util.List;

/**
 * Created by Tinghua on 2016/3/24.
 */
public class SpotFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private String tabTitles[] = new String[] {"景點地圖", "景點列表"};

    public SpotFragmentPagerAdapter(FragmentManager fm,List<Fragment> fragments) {
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
