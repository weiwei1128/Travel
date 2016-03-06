package com.travel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

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
            Functions.go(true,BuyItemDetailActivity.this, BuyItemDetailActivity.this, BuyActivity.class, null);
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
                Functions.go(true,BuyItemDetailActivity.this, BuyItemDetailActivity.this, BuyActivity.class, null);
            }
        });
        AddImg = (ImageView) findViewById(R.id.buyitemAdd_Img);
        AddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("WhichItem", ItemPosition);
                Functions.go(false,BuyItemDetailActivity.this,
                        BuyItemDetailActivity.this, BuyItemListActivity.class, bundle);
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
        ImageLoader.getInstance().init(configuration);

        //===各個item的資料=02_24==//
        helper = new DataBaseHelper(BuyItemDetailActivity.this);
        database = helper.getWritableDatabase();
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_content", "goods_addtime"}, null, null, null, null, null);
        //TODO 0224 still need modify!
        if (goods_cursor != null && goods_cursor.getCount() >= ItemPosition) {
            goods_cursor.moveToPosition(ItemPosition);
            ItemDetail.setText(goods_cursor.getString(4));
            ItemName.setText(goods_cursor.getString(2));
            ItemHeader.setText(goods_cursor.getString(2));
            loader.displayImage("http://zhiyou.lin366.com/"+goods_cursor.getString(3)
                    , ItemImg, options, listener);
        } else Log.d("2.24", "not right!!!!" + ItemPosition);
        if (goods_cursor != null)
            goods_cursor.close();

//        if (ItemImg != null)
//            ItemImg.setScaleType(ImageView.ScaleType.CENTER_CROP);


    }
}
