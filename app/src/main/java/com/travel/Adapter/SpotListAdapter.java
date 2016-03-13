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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.GlobalVariable;
import com.travel.R;
import com.travel.SpotData;
import com.travel.SpotListActivity;
import com.travel.Utility.DataBaseHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Tinghua on 2015/11/26.
 * Updated by Tinghua on 2016/03/08.
 */
public class SpotListAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = SpotListActivity.class.getSimpleName();

    private ImageLoader loader = ImageLoader.getInstance();
    private DisplayImageOptions options;
    private ImageLoadingListener listener;

    private LayoutInflater inflater;
    private ViewHolder mViewHolder;

    private Context context;
    private Double Latitude;
    private Double Longitude;

    private GlobalVariable globalVariable;
    private ArrayList<SpotData> mFilteredSpots;

    private SpotFilter mFilter = new SpotFilter();

    public SpotListAdapter(Context mcontext) {
        this.context = mcontext;
        inflater = LayoutInflater.from(mcontext);

        DataBaseHelper helper = new DataBaseHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        globalVariable = (GlobalVariable) context.getApplicationContext();
        if (globalVariable.SpotDataSorted.isEmpty()) {
            // retrieve Location from DB
            Cursor location_cursor = database.query("location",
                    new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
            if (location_cursor != null) {
                if (location_cursor.getCount() != 0) {
                    while (location_cursor.moveToNext()) {
                        Latitude = location_cursor.getDouble(0);
                        Longitude = location_cursor.getDouble(1);
                        Log.d("3/8_抓取位置", Latitude.toString() + Longitude.toString());
                    }
                }
                location_cursor.close();
            }
            // get SpotData
            Cursor spotDataSorted_cursor = database.query("spotDataSorted",
                    new String[]{"spotId", "spotName", "spotAdd", "spotLat", "spotLng", "picture1",
                            "picture2", "picture3", "openTime", "ticketInfo", "infoDetail"},
                    null, null, null, null, null);
            if (spotDataSorted_cursor != null) {
                while (spotDataSorted_cursor.moveToNext()) {
                    String Id = spotDataSorted_cursor.getString(0);
                    String Name = spotDataSorted_cursor.getString(1);
                    String Add = spotDataSorted_cursor.getString(2);
                    Double Latitude = spotDataSorted_cursor.getDouble(3);
                    Double Longitude = spotDataSorted_cursor.getDouble(4);
                    String Picture1 = spotDataSorted_cursor.getString(5);
                    String Picture2 = spotDataSorted_cursor.getString(6);
                    String Picture3 = spotDataSorted_cursor.getString(7);
                    String OpenTime = spotDataSorted_cursor.getString(8);
                    String TicketInfo = spotDataSorted_cursor.getString(9);
                    String InfoDetail = spotDataSorted_cursor.getString(10);
                    globalVariable.SpotDataSorted.add(new SpotData(Id, Name, Latitude, Longitude, Add,
                            Picture1, Picture2, Picture3, OpenTime, TicketInfo, InfoDetail));
                }
                spotDataSorted_cursor.close();
            }

            //Log.d(TAG, "排序");
            for (SpotData mSpot : globalVariable.SpotDataSorted) {
                //for迴圈將距離帶入，判斷距離為Distance function
                //需帶入使用者取得定位後的緯度、經度、景點店家緯度、經度。
                mSpot.setDistance(Distance(Latitude, Longitude,
                        mSpot.getLatitude(), mSpot.getLongitude()));
            }

            //依照距離遠近進行List重新排列
            DistanceSort(globalVariable.SpotDataSorted);

            mFilteredSpots = new ArrayList<SpotData>();
            mFilteredSpots = globalVariable.SpotDataSorted;
        } else {
            mFilteredSpots = new ArrayList<SpotData>();
            mFilteredSpots = globalVariable.SpotDataSorted;
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
    }

    @Override
    public int getCount() {
        return mFilteredSpots.size();
    }

    @Override
    public Object getItem(int position) {
        return mFilteredSpots.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spot_list, parent, false);
            mViewHolder = new ViewHolder();
            mViewHolder.SpotImg = (ImageView) convertView.findViewById(R.id.SpotImg);
            mViewHolder.SpotName = (TextView) convertView.findViewById(R.id.SpotName);
            mViewHolder.SpotAddress = (TextView) convertView.findViewById(R.id.SpotAddress);
            mViewHolder.SpotDistance = (TextView) convertView.findViewById(R.id.SpotDistance);
            mViewHolder.SpotOpenTime = (TextView) convertView.findViewById(R.id.SpotOpenTime);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        ImageLoaderConfiguration configuration =
                new ImageLoaderConfiguration.Builder(context).build();
        ImageLoader.getInstance().init(configuration);

        String ImgString = mFilteredSpots.get(position).getPicture1();
        loader.displayImage(ImgString, mViewHolder.SpotImg, options, listener);
        mViewHolder.SpotName.setText(mFilteredSpots.get(position).getName());
        mViewHolder.SpotAddress.setText(mFilteredSpots.get(position).getAdd());
        mViewHolder.SpotDistance.setText(DistanceText(mFilteredSpots.get(position).getDistance()));
        mViewHolder.SpotOpenTime.setText("開放時間：" + mFilteredSpots.get(position).getOpenTime());

        return convertView;
    }

    private static class ViewHolder {
        ImageView SpotImg;
        TextView SpotName, SpotAddress, SpotDistance, SpotOpenTime;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new SpotFilter();
        return mFilter;
    }

    private class SpotFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            if (constraint == "" || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = globalVariable.SpotDataSorted;
                results.count = globalVariable.SpotDataSorted.size();
            } else {
                ArrayList<SpotData> FilteredSpots = new ArrayList<SpotData>();

                for (SpotData spotData : globalVariable.SpotDataSorted) {
                    if (spotData.getName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
                        FilteredSpots.add(spotData);
                }
                results.values = FilteredSpots;
                results.count = FilteredSpots.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                mFilteredSpots = (ArrayList<SpotData>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    //帶入距離回傳字串 (距離小於一公里以公尺呈現，距離大於一公里以公里呈現並取小數點兩位)
    private String DistanceText(double distance) {
        if (distance < 1000) return String.valueOf((int) distance) + "m";
        else return new DecimalFormat("#.00").format(distance / 1000) + "km";
    }

    //List排序，依照距離由近開始排列，第一筆為最近，最後一筆為最遠
    private void DistanceSort(ArrayList<SpotData> spot) {
        Collections.sort(spot, new Comparator<SpotData>() {
            @Override
            public int compare(SpotData spot1, SpotData spot2) {
                return spot1.getDistance() < spot2.getDistance() ? -1 : 1;
            }
        });
    }

    //帶入使用者及景點店家經緯度可計算出距離
    public double Distance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double radLatitude1 = latitude1 * Math.PI / 180;
        double radLatitude2 = latitude2 * Math.PI / 180;
        double l = radLatitude1 - radLatitude2;
        double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
                + Math.cos(radLatitude1) * Math.cos(radLatitude2)
                * Math.pow(Math.sin(p / 2), 2)));
        distance = distance * 6378137.0;
        distance = Math.round(distance * 10000) / 10000;

        return distance;
    }
}
