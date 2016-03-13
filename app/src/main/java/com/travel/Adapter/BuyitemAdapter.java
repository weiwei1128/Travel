package com.travel.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.R;
import com.travel.Utility.DataBaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wei on 2015/12/30.
 */
public class BuyitemAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    SharedPreferences sharedPreferences;
    DataBaseHelper helper;
    SQLiteDatabase database;
    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;

    public BuyitemAdapter(Context context) {
        this.context = context;
        helper = new DataBaseHelper(context);
        database = helper.getWritableDatabase();
        layoutInflater = LayoutInflater.from(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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
        //TODO need modify
        if (sharedPreferences.getInt("InBuyList", 0) != 0)
            number = sharedPreferences.getInt("InBuyList", 0);
        return number;//wait to edit//
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final cell newcell;
        View mview;
        mview = layoutInflater.inflate(R.layout.buylist_item, null);
        newcell = new cell(
                (ImageView) mview.findViewById(R.id.buyitemlist_itemImg),
                (ImageView) mview.findViewById(R.id.buyitemlist_delImg),
                (TextView) mview.findViewById(R.id.buyitemlist_nameTxt),
                (TextView) mview.findViewById(R.id.buyitemlist_fromTxt),
                (TextView) mview.findViewById(R.id.butitemlist_moneyTxt),
                (TextView) mview.findViewById(R.id.buyitemlist_totalTxt),
                (TextView) mview.findViewById(R.id.buyitemlist_numbertext),
                (Button) mview.findViewById(R.id.buyitemlist_addbutton),
                (Button) mview.findViewById(R.id.buyitemlist_minusbutton)
        );
        newcell.cellnumberTxt.setText("0");

        //TODO need modify

        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);
        final List<String> goods_id = new ArrayList<>();

        int howmany = sharedPreferences.getInt("InBuyList", 0);
        Log.d("3.8", "howmany: " + howmany);
        for (int i = 1; i <= howmany; i++)
            Log.d("3.8", "in Position: " + sharedPreferences.getInt("InBuyList" + i, 0));
        int itemPosition = sharedPreferences.getInt("InBuyList" + (position + 1), 0);
        if (goods_cursor != null && goods_cursor.getCount() >= itemPosition) {
            goods_cursor.moveToPosition(itemPosition);
            Log.d("3.9", "購物車項目名稱" + position + ":" + goods_cursor.getString(2));
            newcell.cellnameTxt.setText(goods_cursor.getString(2));
            newcell.cellfromTxt.setText(goods_cursor.getString(2));
            newcell.cellmoneyTxt.setText(goods_cursor.getString(4));
            newcell.cellnumberTxt.setText(sharedPreferences.getInt(goods_cursor.getString(1), 0)+"");
            goods_id.clear();
            goods_id.add(goods_cursor.getString(1));
            loader.displayImage("http://zhiyou.lin366.com/" + goods_cursor.getString(3)
                    , newcell.cellImg, options, listener);
        }

        if (goods_cursor != null)
            goods_cursor.close();


        newcell.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newcell.cellnumberTxt.setText((Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + 1) + "");
                if (goods_id.get(0) != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(goods_id.get(0), Integer.valueOf(newcell.cellnumberTxt.getText().toString()+""));
                    Log.d("3.9", "id: " + goods_id.get(0) + " BuyItemAdapter goods_id NULL" + Integer.valueOf(newcell.cellnumberTxt.getText().toString() + ""));
                    editor.apply();
                    newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString())
                            * Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + "");
                } else Log.d("3.9","BuyItemAdapter goods_id NULL"+Integer.valueOf(newcell.cellnumberTxt.getText().toString()+""));
            }
        });
        newcell.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Integer.valueOf(newcell.cellnumberTxt.getText().toString()) > 0)) {
                    newcell.cellnumberTxt.setText((Integer.valueOf(newcell.cellnumberTxt.getText().toString()) - 1) + "");
                    if (goods_id.get(0) != null) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(goods_id.get(0), Integer.valueOf(newcell.cellnumberTxt.getText().toString()+""));
                        editor.apply();
                    }
                    newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString())
                            * Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + "");
                }
            }
        });
        ///1.13


        newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString())
                * Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + "");

        //delete chosed item
        newcell.celldelImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newcell.celltotalTxt.setText("0");
                newcell.cellnumberTxt.setText("0");
                if (goods_id.get(0) != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(goods_id.get(0), 0);
                    editor.apply();
                }
            }
        });

        return mview;
    }

    public class cell {
        ImageView cellImg, celldelImg;
        TextView cellnameTxt, cellfromTxt, cellmoneyTxt, celltotalTxt, cellnumberTxt;
        Button plus, minus;

        public cell(ImageView itemImg, ImageView itemdelImg, TextView itemnameTxt, TextView itemfromTxt,
                    TextView itemmoneyTxt, TextView itemtotalTxt,
                    TextView itemnumberTxt, Button mPlus, Button mMinus) {
            this.cellImg = itemImg;
            this.celldelImg = itemdelImg;
            this.cellnameTxt = itemnameTxt;
            this.cellfromTxt = itemfromTxt;
            this.cellmoneyTxt = itemmoneyTxt;
            this.celltotalTxt = itemtotalTxt;
            this.cellnumberTxt = itemnumberTxt;
            this.plus = mPlus;
            this.minus = mMinus;
        }
    }
}
