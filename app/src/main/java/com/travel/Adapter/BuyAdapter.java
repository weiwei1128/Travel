package com.travel.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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

/**
 * Created by wei on 2015/11/11.
 */
public class BuyAdapter extends BaseAdapter {
    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;
    DataBaseHelper helper;
    SQLiteDatabase database;

    private Context context;
    private LayoutInflater inflater;

    public BuyAdapter(Context mcontext) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);

        helper = new DataBaseHelper(context);
        database = helper.getWritableDatabase();

        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
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
        //TODO need modify!
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_content", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null) {
            Log.d("2.24", "getCount:" + goods_cursor.getCount());
            number = goods_cursor.getCount();
            goods_cursor.close();
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

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        cell mcell;
        View mview;
        mview = inflater.inflate(R.layout.buy_item, null);
        mcell = new cell(
                (ImageView) mview.findViewById(R.id.buy_thingImg),
                (TextView) mview.findViewById(R.id.buy_thingText)
        );

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        ImageLoader.getInstance().init(configuration);

        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_content", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null && goods_cursor.getCount() >= position) {
            goods_cursor.moveToPosition(position);
            mcell.buyText.setText(goods_cursor.getString(2));
            //http://zhiyou.lin366.com/
            loader.displayImage("http://zhiyou.lin366.com/"+goods_cursor.getString(3)
                    , mcell.buyImg, options, listener);
        }
        if (mcell.buyImg != null)
            mcell.buyImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if(mcell.buyText.getText()==null)
            mcell.buyText.setText("資料錯誤");

        if (goods_cursor != null)
            goods_cursor.close();


        return mview;
    }

    public class cell {
        ImageView buyImg;
        TextView buyText;

        public cell(ImageView buy_img, TextView buy_text) {
            this.buyImg = buy_img;
            this.buyText = buy_text;
        }

    }
}
