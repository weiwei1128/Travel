package com.flyingtravel.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

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

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        Log.e("3/23_PagerAdapter", "destroyItem");
        container.removeView(fragments.get(position).getView());
    }
}
