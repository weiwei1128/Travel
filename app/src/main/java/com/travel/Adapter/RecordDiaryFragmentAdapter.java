package com.travel.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.travel.R;
import com.travel.Utility.DataBaseHelper;

/**
 * Created by Tinghua on 3/27/2016.
 */
public class RecordDiaryFragmentAdapter extends BaseAdapter implements ViewPagerEx.OnPageChangeListener {

    private LayoutInflater inflater;
    private ViewHolder mViewHolder;

    private Context context;
    private DataBaseHelper helper;
    private SQLiteDatabase database;

    private Integer RouteConter = 1;

    public RecordDiaryFragmentAdapter(Context mcontext) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);

        helper = DataBaseHelper.getmInstance(context);
        database = helper.getWritableDatabase();
    }

    @Override
    public int getCount() {
        int number = 0;
        Cursor RouteCount_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "track_start=\"0\"", null, null, null, null, null);
        if (RouteCount_cursor != null) {
            if (RouteCount_cursor.getCount() != 0) {
                RouteCount_cursor.moveToLast();
                number = RouteCount_cursor.getInt(0);
            }
            RouteCount_cursor.close();
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_record_diarylist, parent, false);
            mViewHolder = new ViewHolder();
            mViewHolder.ImageSlider = (SliderLayout) convertView.findViewById(R.id.slider);
            mViewHolder.pagerIndicator = (PagerIndicator) convertView.findViewById(R.id.custom_indicator);
            mViewHolder.MemoTitle = (TextView) convertView.findViewById(R.id.Title);
            mViewHolder.MemoTotalTime = (TextView) convertView.findViewById(R.id.TimeStamp);
            mViewHolder.MemoString = (TextView) convertView.findViewById(R.id.MemoString);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "track_start=\"0\"", null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                trackRoute_cursor.moveToPosition(trackRoute_cursor.getCount() - position-1);
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
                    mViewHolder.ImageSlider.removeAllSliders();
                    if (img_cursor.getCount() != 0) {
                        number = img_cursor.getCount();
                        Log.e("3/28_", "img count:" + number);
                        while (img_cursor.moveToNext()) {
                            Log.e("3/28_", "img: " + img_cursor.getBlob(3));
                            byte[] d = img_cursor.getBlob(3);
                            DefaultSliderView sliderView = new DefaultSliderView(context);
                            sliderView.image(BitmapFactory.decodeByteArray(d, 0, d.length))
                                    .setScaleType(BaseSliderView.ScaleType.Fit);
                            mViewHolder.ImageSlider.addSlider(sliderView);
                        }
                        Log.e("3/29_", "position: " + position);
                        if (img_cursor.getCount() > 1) {
                            mViewHolder.ImageSlider.setCustomIndicator(mViewHolder.pagerIndicator);
                            mViewHolder.ImageSlider.addOnPageChangeListener(this);
                            Log.e("3/29_", "set pagerIndicator ");
                        }
                    } else {
                        DefaultSliderView sliderView = new DefaultSliderView(context);
                        sliderView.image(R.drawable.empty)
                                .setScaleType(BaseSliderView.ScaleType.Fit);
                        mViewHolder.ImageSlider.addSlider(sliderView);
                        Log.e("3/28_", "img_cursor = 0 ");
                    }
                    mViewHolder.ImageSlider.stopAutoCycle();
                    img_cursor.close();
                }
            }
            trackRoute_cursor.close();
        }

        return convertView;
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

    private static class ViewHolder {
        SliderLayout ImageSlider;
        PagerIndicator pagerIndicator;
        TextView MemoTitle, MemoTotalTime, MemoString;
    }
}