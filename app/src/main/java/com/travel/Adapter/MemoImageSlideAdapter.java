package com.travel.Adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.travel.Utility.DataBaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tinghua on 2016/3/18.
 */
public class MemoImageSlideAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;

    //private Integer RouteConter = 1;
    private DataBaseHelper helper;
    private SQLiteDatabase database;

    private Integer mLocation;
    private ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
    private List<ArrayList<Bitmap>> listOfBitmapArray = new ArrayList<ArrayList<Bitmap>>();

    public MemoImageSlideAdapter(Context mcontext, Integer location, List<ArrayList<Bitmap>> bitmaps) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);

        mLocation = location;
        listOfBitmapArray = bitmaps;
        //bitmapArray = bitmaps;
        //listOfBitmapArray.add(location, bitmapArray);

        helper = new DataBaseHelper(mcontext);
        database = helper.getWritableDatabase();
    }

    @Override
    public int getCount() {
        return listOfBitmapArray.get(mLocation).size();
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
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(listOfBitmapArray.get(mLocation).get(position));
        imageView.setLayoutParams(new Gallery.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        if (imageView != null)
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        return imageView;
    }
}
