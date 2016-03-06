package com.travel.Adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wei on 2016/1/13.
 */
public class CountAdapter extends ArrayAdapter<Integer> {
    List<Integer> itemList = new ArrayList<>();

    public CountAdapter(Context context, int resource, List<Integer> objects) {
        super(context, resource, objects);
        this.itemList = objects;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Integer getItem(int position) {
        return super.getItem(position);
    }

    public boolean isFirst(int position){
        if(position==0)
            return true;
        else
            return false;
    }

    public boolean isLast(int position){
        position++;
        if(position==itemList.size())
            return true;
        else
            return false;
    }
}
