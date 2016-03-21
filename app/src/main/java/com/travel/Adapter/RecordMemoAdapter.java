package com.travel.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.R;
import com.travel.Utility.DataBaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tinghua on 2016/3/3.
 */
public class RecordMemoAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ViewHolder mViewHolder;

    private ImageLoader loader = ImageLoader.getInstance();
    private DisplayImageOptions options;
    private ImageLoadingListener listener;

    private Context context;
    private DataBaseHelper helper;
    private SQLiteDatabase database;

    private Integer RouteConter = 1;

    private Integer mDotsCount;
    private ArrayList<Bitmap> bitmapArrayList = new ArrayList<Bitmap>();
    private List<ArrayList<Bitmap>> listOfBitmapArray = new ArrayList<ArrayList<Bitmap>>();


    public RecordMemoAdapter(Context mcontext) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);

        helper = new DataBaseHelper(mcontext);
        database = helper.getWritableDatabase();

        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageOnLoading(R.drawable.loading2)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory(false)
                .cacheOnDisc(false).build();
        listener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        };
    }

    @Override
    public int getCount() {
        int number = 0;
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                trackRoute_cursor.moveToLast();
                number = trackRoute_cursor.getInt(0);
                Log.e("3/18_", "rs count:" + number);
            }
            trackRoute_cursor.close();
        }
        return number;
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
            mViewHolder.MemoViewPager = (ViewPager) convertView.findViewById(R.id.MemoViewPager);
            //mViewHolder.MemoImg = (ImageView) convertView.findViewById(R.id.MemoImageView);
            mViewHolder.MemoTitle = (TextView) convertView.findViewById(R.id.Title);
            mViewHolder.MemoTotalTime = (TextView) convertView.findViewById(R.id.TimeStamp);
            mViewHolder.MemoString = (TextView) convertView.findViewById(R.id.MemoString);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        //ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        //ImageLoader.getInstance().init(configuration);

        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "track_start=\"0\"", null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                trackRoute_cursor.moveToPosition(trackRoute_cursor.getCount()-position-1);
                mViewHolder.MemoTitle.setText(trackRoute_cursor.getString(5));
                mViewHolder.MemoTotalTime.setText(trackRoute_cursor.getString(6));
                RouteConter = trackRoute_cursor.getInt(0);

                Cursor memo_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                                "memo_content", "memo_img", "memo_latlng", "memo_time"},
                        "memo_routesCounter=\"" + RouteConter + "\" AND memo_content!=\"null\"", null, null, null, null, null);
                if (memo_cursor != null) {
                    if (memo_cursor.getCount() != 0) {
                        memo_cursor.moveToFirst();
                        mViewHolder.MemoString.setText(memo_cursor.getString(2));
                    } else {
                        mViewHolder.MemoString.setText("");
                    }
                    memo_cursor.close();
                }

                Cursor img_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                                "memo_content", "memo_img", "memo_latlng", "memo_time"},
                        "memo_routesCounter=\"" + RouteConter + "\" AND memo_img!=\"null\"", null, null, null, null, null);
                if (img_cursor != null) {
                    int number;
                    if (img_cursor.getCount() != 0) {
                        number = img_cursor.getCount();
                        Log.e("3/18_", "img count:" + number);
                        bitmapArrayList.clear();
                        while (img_cursor.moveToNext()) {
                            Log.e("3/18_", "img: " + img_cursor.getBlob(3));
                            byte[] d = img_cursor.getBlob(3);
                            bitmapArrayList.add(BitmapFactory.decodeByteArray(d, 0, d.length));
                        }
                        Log.e("3/19_", "position: " + position);
                        listOfBitmapArray.add(position, bitmapArrayList);
                    } else {
                        bitmapArrayList.clear();
                        listOfBitmapArray.add(position, bitmapArrayList);
                        Log.e("3/18_", "img_cursor = 0 " + listOfBitmapArray.get(position).toString());
                    }
                    MemoViewPagerAdapter adapter = new MemoViewPagerAdapter(context, position, listOfBitmapArray);
                    mViewHolder.MemoViewPager.setAdapter(adapter);
                    //adapter.notifyDataSetChanged();
                    img_cursor.close();
                } else {
                    Log.e("3/18_", "img_cursor = null");
                }

            }
            trackRoute_cursor.close();
        }

/*
        if (mViewHolder.MemoImg != null)
            mViewHolder.MemoImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
*/

        return convertView;
    }

    private static class ViewHolder {
        //ImageView MemoImg;
        ViewPager MemoViewPager;
        TextView MemoTitle, MemoTotalTime, MemoString;
        //LinearLayout mDotsLayout;
    }
}
