package com.travel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.R;

/**
 * Created by wei on 2016/1/30.
 */
public class GoodThingAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater layoutInflater;

    public GoodThingAdapter(Context context) {
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 10;
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
        View view = layoutInflater.inflate(R.layout.goodthing_item, null);
        thing item = new thing(
                (ImageView)view.findViewById(R.id.goodthing_img),
                (TextView)view.findViewById(R.id.goodthing_name_txt),
                (TextView)view.findViewById(R.id.goodthing_addr_txt),
                (TextView)view.findViewById(R.id.goodthing_what_txt)
        );
        item.name.append(position+"");
        return view;
    }

    public class thing {
        ImageView m_img;
        TextView name,addr,what;
        public thing(ImageView imageView, TextView nameTxt,
                     TextView addrTxt, TextView whatTxt) {
            this.m_img = imageView;
            this.name = nameTxt;
            this.addr = addrTxt;
            this.what = whatTxt;
        }
    }
}
