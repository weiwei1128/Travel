package com.travel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordMemoDetailActivity extends AppCompatActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = RecordMemoDetailActivity.class.getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location currentLocation;
    private Marker currentMarker;

    private ImageLoader loader = ImageLoader.getInstance();
    private DisplayImageOptions options;
    private ImageLoadingListener listener;

    private TextView MemoDetailTitleTextView;
    private ImageView backImg, EnlargeImg;
    private ExpandableHeightGridView gridView;

    private Integer mPosition = 0;
    private Integer RouteConter = 1;

    private DataBaseHelper helper;
    private SQLiteDatabase database;

    private ListView mlistView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_memo_detail);

        helper = new DataBaseHelper(getApplicationContext());
        database = helper.getWritableDatabase();

        backImg = (ImageView) findViewById(R.id.MemoDetail_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, RecordMemoDetailActivity.this, RecordMemoDetailActivity.this,
                        RecordActivity.class, null);
            }
        });

        EnlargeImg = (ImageView) findViewById(R.id.MemoMapEnlarge_img);
        EnlargeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 放大地圖
            }
        });

        MemoDetailTitleTextView = (TextView) findViewById(R.id.MemoDetailTitle);


        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            mPosition = bundle.getInt("WhichItem");
        }

        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "track_start=\"0\"", null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                trackRoute_cursor.moveToPosition(trackRoute_cursor.getCount() - mPosition-1);

                MemoDetailTitleTextView.setText(trackRoute_cursor.getString(5));
                RouteConter = trackRoute_cursor.getInt(0);

                DiaryImgAdapter adapter = new DiaryImgAdapter(getApplicationContext(), RouteConter);
                gridView = (ExpandableHeightGridView)findViewById(R.id.MemoDetail_gridView);
                gridView.setNumColumns(3);
                gridView.setAdapter(adapter);
                gridView.setExpanded(true);

                mlistView = (ListView) findViewById(R.id.MemoContent_listView);
                DiaryContentAdapter mAdapter = new DiaryContentAdapter(getApplicationContext(), RouteConter);
                mlistView.setAdapter(mAdapter);
                setListViewHeightBasedOnChildren(mlistView);
            }
        }

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // 移除位置請求服務
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // 移除Google API用戶端連線
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        System.gc();
        super.onDestroy();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            LoadtoMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {


        //CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon),18);
        //mMap.moveCamera(center);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        Log.i(TAG, "Location services connected.");

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates
                        (mGoogleApiClient, mLocationRequest, RecordMemoDetailActivity.this);
            } else if (currentLocation != location) {
                handleNewLocation(location);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊
        int errorCode = connectionResult.getErrorCode();
        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Location Listener
     */
    @Override
    public void onLocationChanged(Location location) {
        if (currentLocation != location) {
            handleNewLocation(location);
        }
    }

    // 載入地圖
    private void LoadtoMap() {
        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);//NETWORK_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // 建立Google API用戶端物件
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        // 建立Location請求物件
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)    // 設定優先讀取高精確度的位置資訊（GPS）
                .setInterval(3000)             // 設定讀取位置資訊的間隔時間為三秒
                .setFastestInterval(1000);     // 設定讀取位置資訊最快的間隔時間為一秒

        // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_recordMemo)).getMap();

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);         // 設定地圖類型
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        currentLocation = location;
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        // 設定目前位置的標記
        if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("I am here!"));

        } else {
            currentMarker.setPosition(latLng);
        }

        // 移動地圖到目前的位置
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

    }

    private class DiaryImgAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        private ViewHolder mViewHolder;
        private Integer routeCounter = 1;

        public DiaryImgAdapter(Context mcontext, Integer rs) {
            this.context = mcontext;
            inflater = LayoutInflater.from(mcontext);
            routeCounter = rs;
        }

        @Override
        public int getCount() {
            int number = 0;
            Cursor img_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                            "memo_content", "memo_img", "memo_latlng", "memo_time"},
                    "memo_routesCounter=\"" + RouteConter + "\" AND memo_img!=\"null\"", null, null, null, null, null);
            if (img_cursor != null) {
                if (img_cursor.getCount() != 0) {
                    number = img_cursor.getCount();
                }
                img_cursor.close();
            }
            return number;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.memo_detail_grid, parent, false);
                mViewHolder = new ViewHolder();
                mViewHolder.MemoImg = (ImageView) convertView.findViewById(R.id.memoDetailGrid_image);

                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            Cursor img_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                            "memo_content", "memo_img", "memo_latlng", "memo_time"},
                    "memo_routesCounter=\"" + RouteConter + "\" AND memo_img!=\"null\"", null, null, null, null, null);
            if (img_cursor != null) {
                if (img_cursor.getCount() != 0) {
                    List<Map<String, Bitmap>> items = new ArrayList<Map<String, Bitmap>>();
                    while (img_cursor.moveToNext()) {
                        byte[] d = img_cursor.getBlob(3);
                        Bitmap bmp = BitmapFactory.decodeByteArray(d, 0, d.length);
                        Map<String, Bitmap> item = new HashMap<String, Bitmap>();
                        item.put("image", bmp);
                        Log.e("3/27_image: ", bmp.toString());
                        items.add(item);
                    }
                    mViewHolder.MemoImg.setImageBitmap(items.get(position).get("image"));
                }
                img_cursor.close();
            }
            return convertView;
        }

        private class ViewHolder {
            ImageView MemoImg;
        }
    }

    private class DiaryContentAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        private ViewHolder mViewHolder;
        private Integer routeCounter = 1;

        public DiaryContentAdapter(Context mcontext, Integer rs) {
            this.context = mcontext;
            inflater = LayoutInflater.from(mcontext);
            routeCounter = rs;
        }

        @Override
        public int getCount() {
            int number = 0;
            Cursor memo_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                            "memo_content", "memo_img", "memo_latlng", "memo_time"},
                    "memo_routesCounter=\"" + routeCounter + "\" AND memo_content!=\"null\"", null, null, null, null, null);
            if (memo_cursor != null) {
                if (memo_cursor.getCount() != 0) {
                    number = memo_cursor.getCount();
                }
                memo_cursor.close();
            }
            return number;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.memo_detail_content, parent, false);
                mViewHolder = new ViewHolder();
                mViewHolder.MemoString = (TextView) convertView.findViewById(R.id.MemoDetailString);

                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }
            Cursor memo_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                            "memo_content", "memo_img", "memo_latlng", "memo_time"},
                    "memo_routesCounter=\"" + routeCounter + "\" AND memo_content!=\"null\"", null, null, null, null, null);
            if (memo_cursor != null) {
                if (memo_cursor.getCount() != 0) {
                    memo_cursor.moveToPosition(position);
                    mViewHolder.MemoString.setText(memo_cursor.getString(2));
                    Log.e("3/27_memo content: ", memo_cursor.getString(2));
                } else {
                    mViewHolder.MemoString.setText("未上傳景點心得。");
                }
                memo_cursor.close();
            }
            return convertView;
        }

        private class ViewHolder {
            TextView MemoString;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
