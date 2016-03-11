package com.travel.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.R;
import com.travel.Utility.DataBaseHelper;

/**
 * Created by wei on 2016/3/7.
 */
public class BuyRecordAdapter extends BaseAdapter {
    LayoutInflater layoutInflater;
    Context m_context;
    DataBaseHelper helper;
    SQLiteDatabase database;
    public BuyRecordAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
        this.m_context = context;
        helper = new DataBaseHelper(context);
        database = helper.getWritableDatabase();
    }
    @Override
    public int getCount() {
        int number=0;

        Cursor order_cursor = database.query("shoporder", new String[]{"order_id", "order_no",
                        "order_time", "order_name", "order_phone", "order_email", "order_money", "order_state"},
                null, null, null, null, null);
        if(order_cursor!=null){
            number = order_cursor.getCount();
            order_cursor.close();
        }
        return number;
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
        item item ;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.special_item, null);
            item = new item();
            convertView.setTag(item);
        } else
            item = (item) convertView.getTag();



        return convertView;
    }

    public class item{
        item(){

        }
    }
}
