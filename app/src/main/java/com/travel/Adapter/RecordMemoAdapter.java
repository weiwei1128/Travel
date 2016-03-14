package com.travel.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.R;
import com.travel.Utility.DataBaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public RecordMemoAdapter(Context mcontext) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);

        helper = new DataBaseHelper(mcontext);
        database = helper.getWritableDatabase();

        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory()
                .cacheOnDisc().build();
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
        Cursor travelMemo_cursor = database.query("travelmemo", new String[]{"memo_no",
                        "memo_title", "memo_content", "memo_img", "memo_latlng", "memo_time"},
                null, null, null, null, null);
        if (travelMemo_cursor != null) {
            number = travelMemo_cursor.getCount();
            travelMemo_cursor.close();
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
            mViewHolder.MemoImg = (ImageView) convertView.findViewById(R.id.MemoImageView);
            mViewHolder.MemoTitle = (TextView) convertView.findViewById(R.id.Title);
            mViewHolder.MemoTimeStamp = (TextView) convertView.findViewById(R.id.TimeStamp);
            mViewHolder.MemoString = (TextView) convertView.findViewById(R.id.MemoString);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        ImageLoader.getInstance().init(configuration);

        Cursor travelMemo_cursor = database.query("travelmemo", new String[]{"memo_no",
                        "memo_title", "memo_content", "memo_img", "memo_latlng", "memo_time"},
                null, null, null, null, null);
        if (travelMemo_cursor != null && travelMemo_cursor.getCount() > 0) {
            travelMemo_cursor.moveToPosition(position);
            mViewHolder.MemoTitle.setText(travelMemo_cursor.getString(1));
            mViewHolder.MemoString.setText(travelMemo_cursor.getString(2));
            // TODO 資料庫讀Bitmap
            byte[] d = travelMemo_cursor.getBlob(3);
            Bitmap bmp = BitmapFactory.decodeByteArray(d, 0, d.length);
            //dialog_img.setImageBitmap(bmp);
            loader.displayImage(travelMemo_cursor.getString(3), mViewHolder.MemoImg, options, listener);
            mViewHolder.MemoTimeStamp.setText(travelMemo_cursor.getString(5));
            Log.d("3/13_", "img_url:" + travelMemo_cursor.getString(3));
        }

        if (mViewHolder.MemoImg != null)
            mViewHolder.MemoImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (travelMemo_cursor != null)
            travelMemo_cursor.close();

        return convertView;
    }

    private static class ViewHolder {
        ImageView MemoImg;
        TextView MemoTitle, MemoTimeStamp, MemoString;
    }
}
