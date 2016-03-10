package com.travel.Adapter;

/**
 * Created by wei on 2016/3/7.
 */

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {

    private ArrayList<View> Views;
    private ArrayList<String> Titles;

    public ViewPagerAdapter(ArrayList<View> Views, ArrayList<String> Titles) {
        this.Titles = Titles;
        this.Views = Views;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView(Views.get(position));
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//		 System.out.println("ViewPagerAdapter_setPrimaryItem"+position);
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles.get(position);
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ((ViewPager) container).addView(Views.get(position), 0);
        return Views.get(position);
    }

    @Override
    public int getCount() {
        if (Views != null)
            return Views.size();
        return 0;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

}

