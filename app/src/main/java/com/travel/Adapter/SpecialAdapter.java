package com.travel.Adapter;

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
 * Created by wei on 2016/1/30.
 */
public class SpecialAdapter extends BaseAdapter {
    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;
    DataBaseHelper helper;
    SQLiteDatabase database;
    Context mContext;
    LayoutInflater layoutInflater;
    int page_no;

    public SpecialAdapter(Context context, Integer pageNo) {
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
        this.page_no = pageNo;
        helper = new DataBaseHelper(context);
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
        Cursor special = database.query("special_activity", new String[]{"special_id", "title", "img", "content", "price", "click"},
                null, null, null, null, null);
        if (special != null) {
            number = special.getCount();
            special.close();
        }
        if ((number % 10 > 0))
            if (number / 10 + 1 == page_no) {
                number = number % 10;
            } else number = 10;
        Log.e("3.10", "special:" + number);
        return number;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        thing item;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.special_item, null);
            item = new thing(
                    (ImageView) convertView.findViewById(R.id.special_img),
                    (TextView) convertView.findViewById(R.id.special_name_text),
                    (TextView) convertView.findViewById(R.id.special_price_text)
            );
            convertView.setTag(item);
        } else
            item = (thing) convertView.getTag();
            /*
        View view = layoutInflater.inflate(R.layout.special_item, null);
        item = new thing(
                (ImageView) view.findViewById(R.id.special_img),
                (TextView) view.findViewById(R.id.special_name_text),
                (TextView) view.findViewById(R.id.special_price_text)
        );
*/
//        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(mContext).build();
//        ImageLoader.getInstance().clearMemoryCache();
//        ImageLoader.getInstance().init(configuration);

        Cursor special = database.query("special_activity", new String[]{"special_id",
                        "title", "img", "content", "price", "click"},
                null, null, null, null, null);
        if (special != null && special.getCount() >= ((page_no - 1) * 10 + position)) {
            special.moveToPosition((page_no - 1) * 10 + position);
            if (special.getString(1) != null)
                item.name.setText(special.getString(1));
            if (special.getString(4) != null)
                item.what.setText("價格: "+special.getString(4));
            if (special.getString(2) != null)
                if (special.getString(2).startsWith("http:"))
                    loader.displayImage(special.getString(2)
                            , item.m_img, options, listener);
                else loader.displayImage("http://zhiyou.lin366.com/" + special.getString(2)
                        , item.m_img, options, listener);
        }
        if (special != null)
            special.close();


        return convertView;
//        return view;
    }

    public class thing {
        ImageView m_img;
        TextView name, what;

        public thing(ImageView imageView, TextView nameTxt, TextView whatTxt) {
            this.m_img = imageView;
            this.name = nameTxt;
            this.what = whatTxt;
        }
    }
}
