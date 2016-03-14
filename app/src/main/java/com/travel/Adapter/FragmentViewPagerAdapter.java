package com.travel.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.R;

import java.util.List;

/**
 * Created by wei on 2016/1/12.
 */
public class FragmentViewPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

    List<Fragment> fragments;
    FragmentManager fragmentManager;
    ViewPager viewPager;
    int currentPageIndex = 0;
    int totalPage = 0;
    int dayNum = 0;
    Context context;

    public FragmentViewPagerAdapter(FragmentManager mfragmentmanager, ViewPager mviewpager,
                                    List<Fragment> mfragments,
                                    Context mcontext, int size, int daysNum) {

        this.fragmentManager = mfragmentmanager;
        this.viewPager = mviewpager;
//        this.viewPager.setAdapter(this);
//        this.viewPager.setOnPageChangeListener(this);
        this.fragments = mfragments;
        this.context = mcontext;
        this.totalPage = size;
        this.dayNum = daysNum;
    }


    @Override
    public int getCount() {
        //1.12
//        return fragments.size();
        if (fragments == null)
            return 0;
        return fragments.size();
        //1.12
    }


    //1.
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        //1.12
        fragments.get(currentPageIndex).onStop();
        if (fragments.get(position).isAdded())
            fragments.get(position).onResume();
        currentPageIndex = position;
        //1.12
        /**
         Log.d("1/12", "currrentPage:" + position);
         TextView textView =(TextView)fragments.get(position).getView().findViewById(R.id.checkschedule_gotimeTxt);
         textView.setText("目前頁數:"+position);
         CheckScheduleFragment.whichPage(position);
         **/
        super.setPrimaryItem(container, position, object);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //1.12
//        super.destroyItem(container, position, object);
        container.removeView(fragments.get(position).getView());
        //1.12
    }


    //  1.12
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //1.12
//        return super.instantiateItem(container, position);
        Fragment m_fragment = fragments.get(position);
        if (!m_fragment.isAdded()) {


            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(m_fragment, m_fragment.getClass().getSimpleName());
            ft.commit();
            fragmentManager.executePendingTransactions();

        }
        if (m_fragment.getView().getParent() == null)
            container.addView(m_fragment.getView());

        Log.d("1/12", "currrentPage:" + position);
        //--------SET UP FRAGMENT INFO--------//
        TextView whichDay = (TextView) m_fragment.getView().findViewById(R.id.checkschedule_dayTxt);
        TextView nowPage = (TextView) m_fragment.getView().findViewById(R.id.checkschedule_nowPageTxt);
        TextView allPage = (TextView) m_fragment.getView().findViewById(R.id.checkschedule_allPageTxt);

        whichDay.setText(dayNum + "");
        nowPage.setText((position + 1) + "");
        allPage.setText(fragments.size() + "");
        //--------SET UP FRAGMENT INFO--------//

        return m_fragment.getView();
        //1.12
    }

    //

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
        //1.12 change false to view==0
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public int getWhichDay() {
        return dayNum;
    }
}
