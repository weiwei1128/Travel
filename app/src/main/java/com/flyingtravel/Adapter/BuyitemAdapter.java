package com.flyingtravel.Adapter;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.Locale;

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
    String en, cn;

    public BuyitemAdapter(Context context) {
        this.context = context;
        helper = DataBaseHelper.getmInstance(context);
        database = helper.getWritableDatabase();
        layoutInflater = LayoutInflater.from(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.contains("us"))
            en = sharedPreferences.getString("us", "1");
        if (sharedPreferences.contains("cn"))
            cn = sharedPreferences.getString("cn", "1");
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageOnLoading(R.drawable.empty)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory(false)
                .cacheOnDisk(true).build();
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
        Cursor goodsitem_cursor = database.query("goodsitem", new String[]{"goods_bigid",
                        "goods_itemid", "goods_title", "goods_money", "goods_url"},
                null, null, null, null, null);
        if (goodsitem_cursor != null)
            if (goodsitem_cursor.getCount() > 0)
                while (goodsitem_cursor.moveToNext())
                    if (sharedPreferences.contains(goodsitem_cursor.getString(1)) && (sharedPreferences.getInt(goodsitem_cursor.getString(1), 0) > 0))
                        number++;
//        return sharedPreferences.getInt("InBuyList", 0);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final cell newcell;
        Log.d("5.27", "position::" + position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.buylist_item, null);
            newcell = new cell(
                    (ImageView) convertView.findViewById(R.id.buyitemlist_itemImg),
                    (LinearLayout) convertView.findViewById(R.id.buyitemlist_delImg),
                    (TextView) convertView.findViewById(R.id.buyitemlist_nameTxt),
                    (TextView) convertView.findViewById(R.id.buyitemlist_itemTxt),
                    (TextView) convertView.findViewById(R.id.butitemlist_moneyTxt),
                    (TextView) convertView.findViewById(R.id.buyitemlist_totalTxt),
                    (TextView) convertView.findViewById(R.id.buyitemlist_numbertext),
                    (LinearLayout) convertView.findViewById(R.id.buyitemlist_addbutton),
                    (LinearLayout) convertView.findViewById(R.id.buyitemlist_minusbutton)
            );
            convertView.setTag(newcell);
        } else
            newcell = (cell) convertView.getTag();

        newcell.cellnumberTxt.setText("0");
        //0527//
        String itemiD = null;
        //sharedPreferences.getInt(cartItem[i][0],0)
        Cursor goodsitem_cursor = database.query("goodsitem", new String[]{"goods_bigid",
                        "goods_itemid", "goods_title", "goods_money", "goods_url"},
                null, null, null, null, null);
        int count = -1;
        if (goodsitem_cursor != null) {
            if (goodsitem_cursor.getCount() > 0)

                while (goodsitem_cursor.moveToNext()) {
                    if (sharedPreferences.contains(goodsitem_cursor.getString(1)) && (sharedPreferences.getInt(goodsitem_cursor.getString(1), 0) > 0)) {
                        Cursor goodsbig_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                                        "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"},
                                "goods_id=?", new String[]{goodsitem_cursor.getString(0)}, null, null, null);
                        if (goodsbig_cursor != null) {
                            if (goodsbig_cursor.getCount() > 0) {
                                count++;
                                if (count == position) {
                                    Log.w("5.27", "==");
                                    goodsbig_cursor.moveToFirst();
                                    Log.e("5.27", count + "title:" + goodsbig_cursor.getString(2)
                                            + "[" + goodsitem_cursor.getString(2) + "]::" + sharedPreferences.getInt(goodsitem_cursor.getString(1), 0));
//                                if (position == 0) {
                                    if (goodsbig_cursor.getString(2) != null)
                                        newcell.cellnameTxt.setText(goodsbig_cursor.getString(2));
                                    if (goodsitem_cursor.getString(2) != null)
                                        newcell.cellfromTxt.setText(goodsitem_cursor.getString(2));
                                    if (goodsitem_cursor.getString(3) != null) {
//                                        newcell.cellmoneyTxt.setText(goodsitem_cursor.getString(3));
                                        int ori = Integer.parseInt(goodsitem_cursor.getString(3));
                                        String result = "" + ori;

                                        switch (Locale.getDefault().toString()) {
                                            case "zh_TW":
                                                newcell.cellmoneyTxt.setText(result);
                                                break;

                                            case "zh_CN"://￥
                                                if (cn != null) {
                                                    result = "" + (ori * Double.parseDouble(cn));
                                                    if (result.contains(".")) {
                                                        //有小數點!!
                                                        result = result.substring(0, result.indexOf("."));
                                                    }
                                                }
                                                newcell.cellmoneyTxt.setText(result);
                                                break;

                                            case "en_US":
                                                if (en != null) {
                                                    result = "" + (ori * Double.parseDouble(en));
                                                    if (result.contains(".")) {
                                                        //有小數點!!
                                                        result = result.substring(0, result.indexOf("."));
                                                    }
                                                }
                                                newcell.cellmoneyTxt.setText(result);
                                                break;

                                            default:
                                                newcell.cellmoneyTxt.setText(result);

                                        }
                                    }
                                    newcell.cellnumberTxt.setText(sharedPreferences.getInt(goodsitem_cursor.getString(1), 0) + "");
                                    if (goodsbig_cursor.getString(3).startsWith("http"))
                                        loader.displayImage(goodsbig_cursor.getString(3), newcell.cellImg, options, listener);
                                    else
                                        loader.displayImage("http://zhiyou.lin366.com/" + goodsbig_cursor.getString(3), newcell.cellImg, options, listener);
                                    itemiD = goodsitem_cursor.getString(1);
//                                    Log.e("5.27", "title:" + goodsbig_cursor.getString(2) + "[" + goodsitem_cursor.getString(2) + "]::" + sharedPreferences.getInt(goodsitem_cursor.getString(1), 0));
//                                } else {
//                                    while (count > 0) {
//                                        if (goodsbig_cursor.moveToNext()) {
//                                            newcell.cellnameTxt.setText(goodsbig_cursor.getString(2));
//                                            newcell.cellfromTxt.setText(goodsitem_cursor.getString(2));
//                                            newcell.cellmoneyTxt.setText(goodsitem_cursor.getString(3));
//                                            newcell.cellnumberTxt.setText(sharedPreferences.getInt(goodsitem_cursor.getString(1), 0) + "");
//                                            if (goodsbig_cursor.getString(3).startsWith("http"))
//                                                loader.displayImage(goodsbig_cursor.getString(3), newcell.cellImg, options, listener);
//                                            else
//                                                loader.displayImage("http://zhiyou.lin366.com/" + goodsbig_cursor.getString(3), newcell.cellImg, options, listener);
//                                            itemiD = goodsitem_cursor.getString(1);
//                                            Log.e("5.27", "title:" + goodsbig_cursor.getString(2) + "[" + goodsitem_cursor.getString(2) + "]::" + sharedPreferences.getInt(goodsitem_cursor.getString(1), 0));
//                                            count--;
//                                        }
//                                    }
//                                }
                                }
                            }
                            goodsbig_cursor.close();
                        }
                    }
                }
            goodsitem_cursor.close();
        }


        //TODO need modify
//        int getPosition = position, getitemPosition = 0, BiginCart = 0;
//        Boolean get = false;
//        String BigitemID = null, SmallitemID = null, itemName = null, itemImg = null;
//        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
//                "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);
//        if (goods_cursor != null) {
//            while (goods_cursor.moveToNext()) {
//                if (get) {
        Log.i("3.24", "我在if裡面!!!要離開while了喔");
//                    break;
//                } else {
//                    BiginCart = sharedPreferences.getInt("InBuyListg" + goods_cursor.getString(1), 0);
//                    if (BiginCart > 0) {
//                        for (int k = 0; k < BiginCart; k++) {
//                            String a = sharedPreferences.getString("InBuyListg" + goods_cursor.getString(1) + "id" + (k + 1), null);
//                            if (a != null && getPosition == 0 &&
//                                    sharedPreferences.getInt("InBuyListgC" + goods_cursor.getString(1) + "id" + (k + 1), 0) != 0) {
//                            Log.e("3.24", "這就是我要的!!!!" + getPosition + ".." + position+"~~~~~"+goods_cursor.getString(2)+"單位:"+a);
//                                BigitemID = goods_cursor.getString(1);
//                                itemName = goods_cursor.getString(2);
//                                itemImg = goods_cursor.getString(3);
//                                getitemPosition = k + 1;
//                                SmallitemID = a;
//                                get = true;
//                                break;
//                            } else if (a != null && getPosition != 0 &&
//                                    sharedPreferences.getInt("InBuyListgC" + goods_cursor.getString(1) + "id" + (k + 1), 0) != 0) {
//                                getPosition--;
//                            Log.e("3.24", "這不是我要的!!!!" + getPosition + "!!!.." + position);
//                            }
//                        }
//                    }
//                else {//這個大項目沒有小項目在購物車裡面
//                    Log.e("3.24", "這不是我要的!!!!" + getPosition + "." + position+"///"+goods_cursor.getString(1));
//                }
//                Log.i("3.24","我在while裡面!!!要執行下一輪");
//                }
//            }
//        }
//        if (BigitemID != null && SmallitemID != null) {
//            Cursor goods_cursor_big = database.query("goodsitem", new String[]{"goods_bigid",
//                            "goods_itemid", "goods_title", "goods_money", "goods_url"},
//                    "goods_bigid=? and goods_itemid=?", new String[]{BigitemID, SmallitemID}, null, null, null);
//            if (goods_cursor_big != null && goods_cursor_big.getCount() > 0) {
//                Log.e("3.24","~~~~~~~~~~~~找到你啦!!!");
//                goods_cursor_big.moveToFirst();
//                newcell.cellnameTxt.setText(itemName);
//                newcell.cellfromTxt.setText(goods_cursor_big.getString(2));
//                newcell.cellmoneyTxt.setText(goods_cursor_big.getString(3));
//                newcell.cellnumberTxt.setText(sharedPreferences.getInt("InBuyListgC" + BigitemID + "id" + getitemPosition, 0) + "");
//                if (itemImg.startsWith("http:"))
//                    loader.displayImage(itemImg, newcell.cellImg, options, listener);
//                else
//                    loader.displayImage("http://zhiyou.lin366.com/" + itemImg, newcell.cellImg, options, listener);

//                Log.i("3.24", " bigID " + goods_cursor_big.getString(0));
//                Log.i("3.24", " itemID " + goods_cursor_big.getString(1));
//                Log.i("3.24", " title " + goods_cursor_big.getString(2));
//                Log.i("3.24", " money " + goods_cursor_big.getString(3));
//                goods_cursor_big.close();
//            }
//        }


        /////TODO^^^^^^-----

//        final int[] howmany = {sharedPreferences.getInt("InBuyList", 0)};
//        int itemPosition = sharedPreferences.getInt("InBuyList" + (position + 1), 0);

//        if (goods_cursor != null)
//            goods_cursor.close();


//        final String finalBigitemID = BigitemID;
//        final int finalGetitemPosition = getitemPosition;
//        final int[] finalBiginCart = {BiginCart};
//        final String finalSmallitemID = SmallitemID;
        final String finalItemiD = itemiD;
        newcell.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
//                if (Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") == 0) {
//                    howmany[0]++;
//                    finalBiginCart[0]++;
//                    editor.putInt("InBuyList", howmany[0]);//the total count of the cart
//                    editor.putInt("InBuyListg" + finalBigitemID, finalBiginCart[0]);//這個大項目裡面有幾個小項目在購物車裡
//                }

                newcell.cellnumberTxt.setText((Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + 1) + "");
//                editor.putInt("InBuyListgC" + finalBigitemID + "id" + finalGetitemPosition, Integer.valueOf(newcell.cellnumberTxt.getText().toString()));
                editor.putInt(finalItemiD, Integer.valueOf(newcell.cellnumberTxt.getText().toString()));
                editor.apply();
                newcell.celltotalTxt.setText(Integer.parseInt(newcell.cellmoneyTxt.getText().toString())
                        * Integer.valueOf(newcell.cellnumberTxt.getText().toString() + "") + "");
            }
        });

        newcell.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Integer.valueOf(newcell.cellnumberTxt.getText().toString()) > 1)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    int beforeClickCount = (Integer.valueOf(newcell.cellnumberTxt.getText().toString()));
                    newcell.cellnumberTxt.setText((Integer.valueOf(newcell.cellnumberTxt.getText().toString()) - 1) + "");
//                    if (beforeClickCount - 1 == 0) {
//                        howmany[0]--;
//                        if (finalBiginCart[0] > 0)
//                            finalBiginCart[0]--;
//                        else Log.e("3.24", "~!~!~!~!ERROR!!!!!!!!");
//                        editor.putInt("InBuyList", howmany[0]);//the total count of the cart
//                        editor.putInt("InBuyListg" + finalBigitemID, finalBiginCart[0]);//這個大項目裡面有幾個小項目在購物車裡
//                        editor.putString("InBuyListg" + finalBigitemID + "id" + finalGetitemPosition, finalSmallitemID);//第幾個小項目的id
//                        editor.putInt("InBuyListgC" + finalBigitemID + "id" + finalGetitemPosition,
//                                Integer.valueOf(newcell.cellnumberTxt.getText().toString()));//第幾個小項目的數量
//                        editor.apply();
//
//                    } else {
//                        editor.putInt("InBuyListgC" + finalBigitemID + "id" + finalGetitemPosition, Integer.valueOf(newcell.cellnumberTxt.getText().toString()));
//                        editor.apply();
//                    }
                    editor.putInt(finalItemiD, Integer.valueOf(newcell.cellnumberTxt.getText().toString())).apply();
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
//                howmany[0]--;
//                if (finalBiginCart[0] > 0)
//                    finalBiginCart[0]--;
//                else Log.e("3.24", "~!~!~!~!ERROR!!!!!!!!");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(finalItemiD, Integer.valueOf(newcell.cellnumberTxt.getText().toString()));
//                editor.putInt("InBuyList", howmany[0]);//the total count of the cart
//                editor.putInt("InBuyListg" + finalBigitemID, finalBiginCart[0]);//這個大項目裡面有幾個小項目在購物車裡
//                editor.putString("InBuyListg" + finalBigitemID + "id" + finalGetitemPosition, finalSmallitemID);//第幾個小項目的id
//                editor.putInt("InBuyListgC" + finalBigitemID + "id" + finalGetitemPosition, 0);//第幾個小項目的數量
                editor.apply();
            }
        });

        return convertView;
    }

    public class cell {
        ImageView cellImg;
        TextView cellnameTxt, cellfromTxt, cellmoneyTxt, celltotalTxt, cellnumberTxt;
        LinearLayout plus, minus, celldelImg;

        public cell(ImageView itemImg, LinearLayout itemdelImg, TextView itemnameTxt, TextView itemfromTxt,
                    TextView itemmoneyTxt, TextView itemtotalTxt,
                    TextView itemnumberTxt, LinearLayout mPlus, LinearLayout mMinus) {
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
