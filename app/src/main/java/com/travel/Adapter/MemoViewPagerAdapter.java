package com.travel.Adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.travel.R;
import com.travel.Utility.DataBaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tinghua on 2016/3/18.
 */
public class MemoViewPagerAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater inflater;

    private DataBaseHelper helper;
    private SQLiteDatabase database;

    private Integer mCount;
    private Integer mLocation;
    private List<ArrayList<Bitmap>> listOfBitmapArray = new ArrayList<ArrayList<Bitmap>>();

    public MemoViewPagerAdapter(Context mcontext, Integer location, List<ArrayList<Bitmap>> bitmaps) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);

        if (mLocation != location) {
            notifyDataSetChanged();
            mLocation = location;
        }
        listOfBitmapArray = bitmaps;
        mCount = listOfBitmapArray.get(mLocation).size();

        helper = new DataBaseHelper(mcontext);
        database = helper.getWritableDatabase();
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = inflater.inflate(R.layout.record_memo_item, container, false);
        ImageView MemoImage = (ImageView) itemView.findViewById(R.id.imageView);
        if (listOfBitmapArray.get(mLocation).isEmpty()) {
            MemoImage.setImageResource(R.drawable.empty);
            Log.e("3/18_", listOfBitmapArray.get(mLocation).toString());
        } else {
            MemoImage.setImageBitmap(listOfBitmapArray.get(mLocation).get(position));
        }

        container.addView(itemView);
        return itemView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
