package com.flyingtravel.Adapter;

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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by wei on 2015/11/11.
 */
public class BuyAdapter extends BaseAdapter implements Filterable {
    private ImageLoader loader = ImageLoader.getInstance();
    private DisplayImageOptions options;
    private ImageLoadingListener listener;
    private DataBaseHelper helper;
    private SQLiteDatabase database;
    private Context context;
    private LayoutInflater inflater;
    int pageNO = 0;
    private GridView gridView;
    ////0526 伴手禮搜尋
    private BuyFilter mFilter = new BuyFilter();
    ArrayList<String> buyData;
    private Boolean ifFilter = false;
    private String filterString;
    ArrayList<String> buyDataID;

    public BuyAdapter(Context mcontext, Integer index, GridView gridView) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);
        this.gridView = gridView;

        pageNO = index;
        helper = DataBaseHelper.getmInstance(context);
        database = helper.getWritableDatabase();

        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageOnLoading(R.drawable.loading2)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory(false)
                .cacheOnDisk(true).build();
        listener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {


            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                ImageView imageView = (ImageView) view.findViewById(R.id.buy_thingImg);
                loader.displayImage(null, imageView, options, listener);

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
        if (ifFilter && buyData != null)
            return buyData.size();
        else {
            int number = 0;
            Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                    "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);
            if (goods_cursor != null) {
                number = goods_cursor.getCount();
                goods_cursor.close();
            }
            if ((number % 10 > 0)) {
                if (number / 10 + 1 == pageNO) {
                    number = number % 10;
                } else number = 10;
            } else
                number = 10;
            return number;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.d("5.26", "filter ??" + ifFilter);
        cell mcell;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.buy_item, null);
            mcell = new cell(
                    (ImageView) convertView.findViewById(R.id.buy_thingImg),
                    (TextView) convertView.findViewById(R.id.buy_thingText),
                    (TextView) convertView.findViewById(R.id.buything_clickText),
                    (TextView) convertView.findViewById(R.id.buy_thingMoney)
            );
            convertView.setTag(mcell);
        } else
            mcell = (cell) convertView.getTag();
        if (ifFilter) {
//            Log.d("5.26", "filter String::" + filterString + "position::" + position);
            Cursor goods_search_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                    "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, "goods_title LIKE ?", new String[]{"%" + filterString + "%"}, null, null, null);
            if (goods_search_cursor != null) {
                if (goods_search_cursor.getCount() != 0)
                    goods_search_cursor.moveToPosition(position);
//                    while ((goods_search_cursor.moveToNext())) {
//                        Log.d("5.26", "GET VIEW goods search::" + goods_search_cursor.getString(2));
                if (goods_search_cursor.getString(2) != null)
                    mcell.buyText.setText(goods_search_cursor.getString(2));
                else mcell.buyText.setText(R.string.wrongData_text);
                if (goods_search_cursor.getString(4) != null)
                    mcell.moneyText.setText("$" + goods_search_cursor.getString(4));

//            if (!(mcell.clickText.getText().toString().substring(3).startsWith("0") &&
//                    mcell.clickText.getText().toString().endsWith("0")))//避免出現:00
                if (goods_search_cursor.getString(6) != null) {
//                    Log.d("4.25", "BuyAdapter:" + goods_cursor.getString(6));
                    String click = context.getResources().getString(R.string.buyClick_text);
                    mcell.clickText.setText(click + goods_search_cursor.getString(6));
                }

                if (goods_search_cursor.getString(3) != null)
                    if (goods_search_cursor.getString(3).startsWith("http"))
                        loader.displayImage(goods_search_cursor.getString(3)
                                , mcell.buyImg, options, listener);
                    else
                        loader.displayImage("http://zhiyou.lin366.com/" + goods_search_cursor.getString(3)
                                , mcell.buyImg, options, listener);
//                    }
            }

        } else {

            Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                    "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);

            if (goods_cursor != null && goods_cursor.getCount() >= (pageNO - 1) * 10 + position) {
                goods_cursor.moveToPosition((pageNO - 1) * 10 + position);
                if (goods_cursor.getString(2) != null)
                    mcell.buyText.setText(goods_cursor.getString(2));
                else mcell.buyText.setText(R.string.wrongData_text);
                if (goods_cursor.getString(4) != null)
                    mcell.moneyText.setText("$" + goods_cursor.getString(4));

//            if (!(mcell.clickText.getText().toString().substring(3).startsWith("0") &&
//                    mcell.clickText.getText().toString().endsWith("0")))//避免出現:00
                if (goods_cursor.getString(6) != null) {
//                    Log.d("4.25", "BuyAdapter:" + goods_cursor.getString(6));
                    String click = context.getResources().getString(R.string.buyClick_text);
                    mcell.clickText.setText(click + goods_cursor.getString(6));
                }

                if (goods_cursor.getString(3) != null)
                    if (goods_cursor.getString(3).startsWith("http"))
                        loader.displayImage(goods_cursor.getString(3)
                                , mcell.buyImg, options, listener);
                    else
                        loader.displayImage("http://zhiyou.lin366.com/" + goods_cursor.getString(3)
                                , mcell.buyImg, options, listener);

            }

            if (goods_cursor != null)
                goods_cursor.close();
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new BuyFilter();
        return mFilter;
    }

    private class BuyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            if (constraint == "" || constraint.length() == 0) {
                // No filter implemented we return all the list
//                results.values = mSpotsData;
//                results.count = mSpotsData.size();
                //Log.e("4/1_", "沒打字時 results.count: " + results.count);
            } else {
//                Log.d("5.26","filter item position start:"+(pageNO - 1) * 10 +"~~"+(pageNO - 1) * 10 + 9);
                Cursor goods_search_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                        "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, "goods_title LIKE ?", new String[]{"%" + constraint.toString() + "%"}, null, null, null);
                Cursor goods_all_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                        "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);

                if (goods_search_cursor != null) {
                    if (goods_search_cursor.getCount() != 0) {
                        ifFilter = true;
                        filterString = constraint.toString();
                        buyData = new ArrayList<>();
                        Boolean gonext = true;
//                        Log.d("5.26", "goods search::" + goods_search_cursor.getCount());
                        while ((goods_search_cursor.moveToNext())) {
                            if (!gonext)
                                break;
                            while (goods_all_cursor.moveToNext()) {
//                                Log.d("5.26", "goods search:id:" + goods_search_cursor.getString(1)+"all::"+goods_all_cursor.getString(1));

                                if (goods_all_cursor.getString(1).equals(goods_search_cursor.getString(1)))
                                    if (goods_all_cursor.getPosition() > ((pageNO - 1) * 10 + 9)) {
//                                        Log.e("5.26", "不是這一頁的!!!!!");
                                        gonext = false;
                                        break;
                                    } else {
                                        buyData.add(goods_search_cursor.getString(1));
//                                        Log.e("5.26", "是這一頁的!!!!!");
                                    }
                            }
                            goods_all_cursor.moveToFirst();

//                            Log.d("5.26", "goods search::" + goods_search_cursor.getString(2));
                        }

                    }
                }
//                for (SpotData spotData : mSpotsData) {
//                    if (spotData.getName().toUpperCase().contains(constraint.toString().toUpperCase()))//.startsWith(constraint.toString().toUpperCase()))
//                        FilteredSpots.add(spotData);
//                }
//                results.values = FilteredSpots;
//                results.count = FilteredSpots.size();
                //Log.e("4/1_", "有打字時 results.count: " + results.count);
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
//            Log.d("5.26", "filter!" + constraint);

            // Now we have to inform the adapter about the new list filtered
            if (ifFilter) {
//                Toast.makeText(context, "無此景點！", Toast.LENGTH_LONG).show();
//                notifyDataSetInvalidated();
//                Log.e("4/1_", "沒東西時 results.count: " + results.count);
            } else {
//                mFilteredSpots = (ArrayList<SpotData>) results.values;
                notifyDataSetChanged();

                //Log.e("4/1_", "有東西時 results.count: " + results.count);
            }
        }
    }


    public class cell {
        ImageView buyImg;
        TextView buyText, clickText, moneyText;

        public cell(ImageView buy_img, TextView buy_text, TextView click_Text, TextView money_Text) {
            this.buyImg = buy_img;
            this.buyText = buy_text;
            this.clickText = click_Text;
            this.moneyText = money_Text;
        }

    }


    /**
     * Update certain view
     * [BAD] whole adapter wont update
     *
     * @param ItemIndex item position in DB
     * @param position  item position in GridView
     */
    public void UpdateView(int ItemIndex, int position) {
        int visiblePosition = gridView.getFirstVisiblePosition();
//        Log.e("4.25", "UpdateView" + ItemIndex + "visiblePosition" + visiblePosition + "position" + position);
        View view = gridView.getChildAt(position - visiblePosition);
        cell mcell = new cell(
                (ImageView) view.findViewById(R.id.buy_thingImg),
                (TextView) view.findViewById(R.id.buy_thingText),
                (TextView) view.findViewById(R.id.buything_clickText),
                (TextView) view.findViewById(R.id.buy_thingMoney)
        );
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);

        if (goods_cursor != null && goods_cursor.getCount() >= ItemIndex) {
            goods_cursor.moveToPosition(ItemIndex);
            if (goods_cursor.getString(6) != null) {
//                Log.e("4.25", "2222 BuyAdapter:" + goods_cursor.getString(6));
                String click = context.getResources().getString(R.string.buyClick_text);
                mcell.clickText.setText(click + goods_cursor.getString(6));
            }
//            else {
//                Log.e("4.25", "2222 BuyAdapter: NULL");
//            }

        }
//        else
//            Log.e("4.25","UpdateView NULL!");
//        Log.e("4.25", "UpdateView" + mcell.buyText.getText());
        if (goods_cursor != null)
            goods_cursor.close();

    }


    public Boolean getIfFilter() {
        return ifFilter;
    }

    public String getFilterString() {
        return filterString;
    }


}
