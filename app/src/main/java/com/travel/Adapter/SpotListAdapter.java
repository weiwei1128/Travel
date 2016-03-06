package com.travel.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.R;
import com.travel.SpotJson;
import com.travel.SpotListActivity;
import com.travel.TPESpotJson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tinghua on 2015/11/26.
 * Updated by Tinghua on 2016/01/11.
 */
public class SpotListAdapter extends BaseAdapter {
    private static final String TAG = SpotListActivity.class.getSimpleName();

    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;

    private LayoutInflater inflater;
    private ViewHolder mViewHolder;

    private SpotJson.PostInfos mInfos;
    private SpotJson.PostInfos.PostInfo mInfo;

    private TPESpotJson.PostResult mResult;
    private TPESpotJson.PostResult.PostResults mResults;

    private ArrayList<SpotListActivity.Spot> mSpots;
    private List<SpotListActivity.Spot> mSpotsList = null;

    private String mRegion;
    private Context context;

    public SpotListAdapter(Context mcontext, ArrayList<SpotListActivity.Spot> Spots,
                           SpotJson.PostInfos Infos, TPESpotJson.PostResult Result,
                           String Region) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);
        mResult = Result;
        mInfos = Infos;
        mSpots = Spots;
        mRegion = Region;
        this.mSpotsList = new ArrayList<SpotListActivity.Spot>();
        mSpotsList = Spots;

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
        return mSpotsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSpotsList.get(position).getPosition();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<SpotListActivity.Spot> Spots, SpotJson.PostInfos Infos)
    {
        // TODO Auto-generated method stub
        this.mSpotsList = Spots;
        this.mInfos = Infos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spot_list,parent,false);
            mViewHolder = new ViewHolder();
            mViewHolder.SpotImg = (ImageView) convertView.findViewById(R.id.SpotImg);
            mViewHolder.SpotName = (TextView) convertView.findViewById(R.id.SpotName);
            mViewHolder.SpotAddress = (TextView) convertView.findViewById(R.id.SpotAddress);
            mViewHolder.SpotDistance = (TextView) convertView.findViewById(R.id.SpotDistance);
            mViewHolder.SpotHour = (TextView) convertView.findViewById(R.id.SpotHour);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        //ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).build();
        //ImageLoader.getInstance().init(configuration);

        if (mRegion.equals("台北市")) {
            mResults = mResult.results[mSpotsList.get(position).getPosition()];

            String ImgString = mResults.getFile();
            int StringPosition = ImgString.indexOf("http", 2);
            if (StringPosition > 0) {
                ImgString = ImgString.substring(0, StringPosition);
            }

            // 404 Not Found loader will crash Q_Q
            loader.displayImage(ImgString, mViewHolder.SpotImg, options, listener);
            mViewHolder.SpotName.setText(mResults.getStitle());
            mViewHolder.SpotAddress.setText(mResults.getAddress());
            mViewHolder.SpotDistance.setText(DistanceText(mSpotsList.get(position).getDistance()));
            mViewHolder.SpotHour.setText("開放時間："+mResults.getMemoTime());

        } else {
            mInfo = mInfos.Info[mSpotsList.get(position).getPosition()];

            // 404 Not Found loader will crash Q_Q
            loader.displayImage(mInfo.getPicture1(), mViewHolder.SpotImg, options, listener);
            mViewHolder.SpotName.setText(mInfo.getName());
            mViewHolder.SpotAddress.setText(mInfo.getAdd());
            mViewHolder.SpotDistance.setText(DistanceText(mSpotsList.get(position).getDistance()));
            mViewHolder.SpotHour.setText("開放時間："+mInfo.getOpentime());
        }

        return convertView;
    }

    // 景點距離單位轉換
    private String DistanceText(double distance)
    {
        if(distance < 1000 ) return String.valueOf((int)distance) + "m" ;
        else return new DecimalFormat("#.00").format(distance/1000) + "km" ;
    }

    private static class ViewHolder {
        ImageView SpotImg;
        TextView SpotName, SpotAddress, SpotDistance, SpotHour;
    }

    // Filter Class
    public void filter(String SearchEditText) {
        SearchEditText = SearchEditText.toLowerCase(Locale.getDefault());
        mSpotsList.clear();
        if (SearchEditText.length() == 0) {
            mSpotsList.addAll(mSpots);
        } else {
            for (SpotListActivity.Spot Sp : mSpots) {
                if (Sp.getName().toLowerCase(Locale.getDefault()).contains(SearchEditText)) {
                    mSpotsList.add(Sp);
                }
            }
        }
        setData(mSpotsList, mInfos);
        notifyDataSetChanged();
    }

}
