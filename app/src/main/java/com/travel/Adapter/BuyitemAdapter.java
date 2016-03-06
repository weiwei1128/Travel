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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_content", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null) {
            Log.d("2.24", "getCount:" + goods_cursor.getCount());
            number = goods_cursor.getCount();
            goods_cursor.close();
        }
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
                (Spinner) mview.findViewById(R.id.spinner),
                (Button) mview.findViewById(R.id.button),
                (Button) mview.findViewById(R.id.button2)
        );


        //TODO need modify
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        ImageLoader.getInstance().init(configuration);

        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_content", "goods_addtime"}, null, null, null, null, null);
        final List<String> goods_id = new ArrayList<>();
        if (goods_cursor != null && goods_cursor.getCount() >= position) {
            goods_cursor.moveToPosition(position);
            newcell.cellnameTxt.setText(goods_cursor.getString(2));
            newcell.cellfromTxt.setText(goods_cursor.getString(2));
            goods_id.add(goods_cursor.getString(1));
            loader.displayImage("http://zhiyou.lin366.com/"+goods_cursor.getString(3)
                    , newcell.cellImg, options, listener);
//            Toast.makeText(context, "伴手禮數目:" + goods_cursor.getCount(), Toast.LENGTH_SHORT).show();
        }
        if (goods_cursor != null)
            goods_cursor.close();
        ///1.13
        final List<Integer> spinners = new ArrayList<>();
        for (int i = 0; i <= 25; i++) {
            spinners.add(i);
        }
        final CountAdapter countAdapter = new CountAdapter(context,
                R.layout.support_simple_spinner_dropdown_item, spinners);
        newcell.spinner.setAdapter(countAdapter);
        newcell.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //position =item value
//                Log.e("1/13", "spinner item selected" + position);
                newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString()) * position + "");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(goods_id.get(0), position);
                    editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        newcell.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int spinnerSelect = Integer.valueOf(newcell.spinner.getSelectedItem().toString() + "");
                if (!countAdapter.isLast(spinnerSelect))
                    newcell.spinner.setSelection(countAdapter.getPosition(spinnerSelect) + 1);
            }
        });
        newcell.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int spinnerSelect = Integer.valueOf(newcell.spinner.getSelectedItem().toString() + "");
                if (!countAdapter.isFirst(spinnerSelect))
                    newcell.spinner.setSelection(countAdapter.getPosition(spinnerSelect) - 1);
            }
        });
        ///1.13


        newcell.spinner.setSelection(sharedPreferences.getInt(goods_id.get(0), 0));

        newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString())
                * Integer.valueOf(newcell.spinner.getSelectedItem().toString()) + "");

        //delete chosed item
        newcell.celldelImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newcell.celltotalTxt.setText(0 + "");
                newcell.spinner.setSelection(0);
                if(goods_id.get(0)!=null) {
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
        TextView cellnameTxt, cellfromTxt, cellmoneyTxt, celltotalTxt;
        //1.13
        Spinner spinner;
        Button plus, minus;

        public cell(ImageView itemImg, ImageView itemdelImg, TextView itemnameTxt, TextView itemfromTxt,
                    TextView itemmoneyTxt, TextView itemtotalTxt,
                    Spinner mSpinner, Button mPlus, Button mMinus) {
            this.cellImg = itemImg;
            this.celldelImg = itemdelImg;
            this.cellnameTxt = itemnameTxt;
            this.cellfromTxt = itemfromTxt;
            this.cellmoneyTxt = itemmoneyTxt;
            this.celltotalTxt = itemtotalTxt;
            //1.13
            this.spinner = mSpinner;
            this.plus = mPlus;
            this.minus = mMinus;
        }
    }
}
