package com.travel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.travel.R;

/**
 * Created by wei on 2016/3/7.
 */
public class CheckScheduleAdapter extends BaseAdapter {
    LayoutInflater layoutInflater;
    int count=0;

    public CheckScheduleAdapter(Context context,int Count){
        layoutInflater = LayoutInflater.from(context);
        this.count = Count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.checkschedule_item,null);
        return view;
    }

    public class item{
        public item(){

        }
    }
}
