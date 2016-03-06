package com.travel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.Utility.Functions;

public class SpotDetailActivity extends AppCompatActivity {

    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;

    Integer mSpotPosition;
    String mRegion;
    TextView SpotName, SpotOpenTime, SpotAddress, SpotTicketInfo, SpotDetail;
    ImageView SpotImg,BackImg;

    private SpotJson.PostInfos.PostInfo[] mInfo;
    private TPESpotJson.PostResult.PostResults[] mResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_detail_activity);

        SpotImg = (ImageView) findViewById(R.id.spotdetail_Img);
        SpotName = (TextView) findViewById(R.id.spotdetailName_Text);
        SpotOpenTime = (TextView) findViewById(R.id.opentime_Text);
        SpotAddress = (TextView) findViewById(R.id.address_Text);
        SpotTicketInfo = (TextView) findViewById(R.id.ticketinfo_Text);
        SpotDetail = (TextView) findViewById(R.id.spotdetail_Text);

        BackImg = (ImageView) findViewById(R.id.spotdetail_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, SpotDetailActivity.this, SpotDetailActivity.this, SpotListActivity.class, null);
            }
        });
        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("Region")) {
            mRegion = bundle.getString("Region");
        }

        if (mRegion.equals("台北市")) {
            if (bundle.containsKey("WhichItem")) {
                mSpotPosition = bundle.getInt("WhichItem");
                mResults = SpotListActivity.Result.getResults();
            }
        } else {
            if (bundle.containsKey("WhichItem")) {
                mSpotPosition = bundle.getInt("WhichItem");
                mInfo = SpotListActivity.Infos.getInfo();
            }
        }

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

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration
                .Builder(SpotDetailActivity.this).build();
        ImageLoader.getInstance().init(configuration);

        if (mRegion.equals("台北市")) {
            String ImgString = mResults[mSpotPosition].getFile();
            int StringPosition = ImgString.indexOf("http", 2);
            if (StringPosition > 0) {
                ImgString = ImgString.substring(0, StringPosition);
            }
            loader.displayImage(ImgString, SpotImg, options, listener);
            SpotName.setText(mResults[mSpotPosition].getStitle());

            if (mResults[mSpotPosition].getMemoTime().equals("")) {
                SpotOpenTime.setText("1. 開放時間：無");
            } else {
                SpotOpenTime.setText("1. 開放時間：" + mResults[mSpotPosition].getMemoTime());
            }

            if (mResults[mSpotPosition].getAddress().equals("")) {
                SpotAddress.setText("2. 地址：無");
            } else {
                SpotAddress.setText("2. 地址：" + mResults[mSpotPosition].getAddress());
            }

            SpotTicketInfo.setText("門票資訊：無");
/*
            if (mResults[mSpotPosition].getTicketinfo().equals("")) {
                SpotTicketInfo.setText("門票資訊：無");
            } else {
                SpotTicketInfo.setText("門票資訊：" + mResults[mSpotPosition].getTicketinfo());
            }
*/
            SpotDetail.setText(mResults[mSpotPosition].getXbody());
        } else {
            loader.displayImage(mInfo[mSpotPosition].getPicture1(), SpotImg, options, listener);
            SpotName.setText(mInfo[mSpotPosition].getName());

            if (mInfo[mSpotPosition].getOpentime().equals("")) {
                SpotOpenTime.setText("1. 開放時間：無");
            } else {
                SpotOpenTime.setText("1. 開放時間：" + mInfo[mSpotPosition].getOpentime());
            }

            if (mInfo[mSpotPosition].getAdd().equals("")) {
                SpotAddress.setText("2. 地址：無");
            } else {
                SpotAddress.setText("2. 地址：" + mInfo[mSpotPosition].getAdd());
            }

            if (mInfo[mSpotPosition].getTicketinfo().equals("")) {
                SpotTicketInfo.setText("門票資訊：無");
            } else {
                SpotTicketInfo.setText("門票資訊：" + mInfo[mSpotPosition].getTicketinfo());
            }

            SpotDetail.setText(mInfo[mSpotPosition].getToldescribe());
        }
        if(SpotImg!=null)
            SpotImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            Functions.go(true, SpotDetailActivity.this, SpotDetailActivity.this, SpotListActivity.class, null);
        }
        return false;
    }
}
