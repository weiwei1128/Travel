package com.travel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tinghua on 2016/3/3.
 */
public class RecordMemoAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ViewHolder mViewHolder;

    private Context context;

    public RecordMemoAdapter(Context mcontext) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.record_memo_list,parent,false);
            mViewHolder = new ViewHolder();
            mViewHolder.MemoImg = (ImageView) convertView.findViewById(R.id.MemoImageView);
            mViewHolder.MemoTitle = (TextView) convertView.findViewById(R.id.Title);
            mViewHolder.MemoTimeStamp = (TextView) convertView.findViewById(R.id.TimeStamp);
            mViewHolder.MemoString = (TextView) convertView.findViewById(R.id.MemoString);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.MemoImg.setImageResource(R.drawable.spot1);
        mViewHolder.MemoTitle.setText("台北一日遊");
        SimpleDateFormat DateFormat = new SimpleDateFormat("HH:mm");
        Date date=new Date();
        mViewHolder.MemoTimeStamp.setText(DateFormat.format(date));
        mViewHolder.MemoString.setText("好累，好想睡覺...zZzZ");
        return convertView;
    }

    private static class ViewHolder {
        ImageView MemoImg;
        TextView MemoTitle, MemoTimeStamp, MemoString;
    }
}
