package com.travel.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.travel.SpotListFragment;
import com.travel.SpotMapFragment;

/**
 * Created by Tinghua on 2016/3/24.
 */
public class SpotFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] {"景點地圖", "景點列表"};
    private Context context;

    public SpotFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fm = null;
        if (position == 0) {
            fm = SpotMapFragment.newInstance();
        }
        else if (position == 1) {
            fm = SpotListFragment.newInstance();
        }
        return fm;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
