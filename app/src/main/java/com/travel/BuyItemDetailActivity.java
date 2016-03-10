package com.travel;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

import java.util.ArrayList;
import java.util.List;

public class BuyItemDetailActivity extends AppCompatActivity {

    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;

    int ItemPosition = 0;
    TextView ItemName, ItemDetail, ItemHeader;
    ImageView ItemImg, BackImg, AddImg;
    DataBaseHelper helper;
    SQLiteDatabase database;

    //按下返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, BuyItemDetailActivity.this, BuyItemDetailActivity.this, BuyActivity.class, null);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyitem_detail_activity);

        //record which item is clicked
        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            ItemPosition = bundle.getInt("WhichItem");
        }


        ItemName = (TextView) findViewById(R.id.buyitemName_Text);
        ItemDetail = (TextView) findViewById(R.id.buyitemDetail_text);
        ItemHeader = (TextView) findViewById(R.id.buyItemHeader);
        ItemImg = (ImageView) findViewById(R.id.buyitem_Img);
        BackImg = (ImageView) findViewById(R.id.buyitem_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, BuyItemDetailActivity.this, BuyItemDetailActivity.this, BuyActivity.class, null);
            }
        });
        AddImg = (ImageView) findViewById(R.id.buyitemAdd_Img);
        AddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("WhichItem", ItemPosition);
                //=====0308 test popping Dialog
                final Dialog BuyAdd = new Dialog(BuyItemDetailActivity.this);
                BuyAdd.setCancelable(true);
                BuyAdd.requestWindowFeature(Window.FEATURE_NO_TITLE);
                BuyAdd.setCanceledOnTouchOutside(true);
                BuyAdd.setContentView(R.layout.dialog_buyitem);
                LinearLayout linearLayout = (LinearLayout) BuyAdd.findViewById(R.id.dialog_buyitem_layout);
                View view = LayoutInflater.from(BuyItemDetailActivity.this)
                        .inflate(R.layout.buylist_item, null);
                linearLayout.addView(view);
                TextView nameText = (TextView) view.findViewById(R.id.buyitemlist_nameTxt);
                TextView fromText = (TextView) view.findViewById(R.id.buyitemlist_fromTxt);
                final TextView moneyText = (TextView) view.findViewById(R.id.butitemlist_moneyTxt);
                ImageView Img = (ImageView) view.findViewById(R.id.buyitemlist_itemImg);
                ImageView delImg = (ImageView) view.findViewById(R.id.buyitemlist_delImg);
                final TextView numberText = (TextView) view.findViewById(R.id.buyitemlist_numbertext);
                final TextView totalText = (TextView) view.findViewById(R.id.buyitemlist_totalTxt);
                Button addButton = (Button) view.findViewById(R.id.buyitemlist_addbutton);
                Button minusButton = (Button) view.findViewById(R.id.buyitemlist_minusbutton);
                Button okButton = (Button) BuyAdd.findViewById(R.id.dialog_buyitem_OkButton);
                Button cancelButton = (Button) BuyAdd.findViewById(R.id.dialog_buyitem_CancelButton);
                final List<String> goods_id = new ArrayList<>();
                DataBaseHelper helper = new DataBaseHelper(BuyItemDetailActivity.this);
                SQLiteDatabase database = helper.getWritableDatabase();
                //show image init
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
                ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
                        BuyItemDetailActivity.this).build();
                ImageLoader.getInstance().init(configuration);
                //show image init

                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BuyItemDetailActivity.this);
                final Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                        "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);
                if (goods_cursor != null && goods_cursor.getCount() >= ItemPosition) {
                    goods_cursor.moveToPosition(ItemPosition);
                    //name,name,money,img
                    goods_id.clear();
                    goods_id.add(goods_cursor.getString(1));

                    nameText.setText(goods_cursor.getString(2));
                    fromText.setText(goods_cursor.getString(2));

                    moneyText.setText(goods_cursor.getString(4).substring(0, goods_cursor.getString(4).indexOf(".")));
                    numberText.setText("" + sharedPreferences.getInt(goods_cursor.getString(1), 0));
                    Log.d("3.8", "shared:" + sharedPreferences.getInt(goods_cursor.getString(1), 0));
                    loader.displayImage("http://zhiyou.lin366.com/" + goods_cursor.getString(3)
                            , Img, options, listener);
                    goods_cursor.close();
                }
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        numberText.setText((Integer.valueOf(numberText.getText().toString() + "") + 1) + "");
                        totalText.setText(Integer.parseInt(moneyText.getText().toString())
                                * Integer.valueOf(numberText.getText().toString() + "")+"");
                    }
                });
                delImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        numberText.setText("0");
                        totalText.setText(Integer.parseInt(numberText.getText().toString())
                                * Integer.valueOf(numberText.getText().toString() + ""));
                    }
                });
                minusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Integer.valueOf(numberText.getText().toString() + "") > 0) {
                            numberText.setText((Integer.valueOf(numberText.getText().toString() + "") - 1) + "");
                            totalText.setText(Integer.parseInt(moneyText.getText().toString()+"")
                                    * Integer.valueOf(numberText.getText().toString() + "")+"");
                        }
                    }
                });
                final int[] number = {sharedPreferences.getInt("InBuyList", 0)};
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (goods_id.get(0) != null) {
                            number[0]++;
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(goods_id.get(0), Integer.valueOf(numberText.getText().toString() + ""));
                            editor.putInt("InBuyList", number[0]);
//                            Log.d("3.8", "InBuyList number[0]:"+number[0]);
                            editor.putInt("InBuyList"+number[0], ItemPosition);
                            editor.apply();

                        }else Log.d("3.8","null");
                        if (BuyAdd.isShowing())
                            BuyAdd.cancel();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (BuyAdd.isShowing())
                            BuyAdd.cancel();
                    }
                });
                totalText.setText(Integer.valueOf(numberText.getText().toString()+"")
                        * Integer.valueOf(numberText.getText().toString() + "")+"");

                BuyAdd.show();
                //=====0308 test popping Dialog

//                Functions.go(false, BuyItemDetailActivity.this,
//                        BuyItemDetailActivity.this, BuyItemListActivity.class, bundle);
            }
        });


        //show image
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
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
                BuyItemDetailActivity.this).build();
        ImageLoader.getInstance().destroy();
        ImageLoader.getInstance().init(configuration);

        //===各個item的資料=02_24==//
        helper = new DataBaseHelper(BuyItemDetailActivity.this);
        database = helper.getWritableDatabase();
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_addtime"}, null, null, null, null, null);
        //TODO 0224 still need modify!
        if (goods_cursor != null && goods_cursor.getCount() >= ItemPosition) {
            goods_cursor.moveToPosition(ItemPosition);
            ItemDetail.setText(goods_cursor.getString(5));
            ItemName.setText(goods_cursor.getString(2));
            ItemHeader.setText(goods_cursor.getString(2));
            loader.displayImage("http://zhiyou.lin366.com/" + goods_cursor.getString(3)
                    , ItemImg, options, listener);
        }
//        else Log.d("2.24", "not right!!!!" + ItemPosition);
        if (goods_cursor != null)
            goods_cursor.close();


    }


    void IfNeedShop(){

    }
    class checkitem extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }
}
