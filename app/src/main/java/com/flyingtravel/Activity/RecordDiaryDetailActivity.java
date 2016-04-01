package com.flyingtravel.Activity;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.View.ExpandableHeightGridView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordDiaryDetailActivity extends AppCompatActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = RecordDiaryDetailActivity.class.getSimpleName();

    private MapView mapView;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 3;       // 5 meters

    //private GlobalVariable globalVariable;

    private Location CurrentLocation;

    private LatLng CurrentLatlng;
    private Marker CurrentMarker;

    private Bitmap MarkerIcon;
    private ArrayList<LatLng> TraceRoute;

    private FrameLayout HeaderLayout, MapLayout;
    private LinearLayout ContentLayout;
    private ImageView backImg, EnlargeImg, ReduceImg;
    private TextView DiaryDetailTitleTextView;
    private ExpandableHeightGridView gridView;
    private ListView mlistView;

    private Integer mPosition = 0;
    private Integer RoutesCounter = 1;
    //private Integer Track_no = 1;

    private DataBaseHelper helper;
    private SQLiteDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_diary_detail_activity);

        helper = DataBaseHelper.getmInstance(getApplicationContext());
        database = helper.getWritableDatabase();

        backImg = (ImageView) findViewById(R.id.DiaryDetail_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, RecordDiaryDetailActivity.this, RecordDiaryDetailActivity.this,
                        RecordActivity.class, null);
            }
        });

        HeaderLayout = (FrameLayout) findViewById(R.id.DiaryDetailHeader_Layout);
        MapLayout = (FrameLayout) findViewById(R.id.DiaryDetailMap_Layout);
        ContentLayout = (LinearLayout) findViewById(R.id.DiaryDetailContent_Layout);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 5 seconds, in milliseconds
                .setFastestInterval(FATEST_INTERVAL) // 1 second, in milliseconds
                .setSmallestDisplacement(DISPLACEMENT);

        mapView = (MapView) findViewById(R.id.DiaryDetailMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();         // needed to get the map to display immediately

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMap = mapView.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        //mMap.setMyLocationEnabled(false);
        //mMap.getUiSettings().setCompassEnabled(true);
        //mMap.getUiSettings().setZoomControlsEnabled(false);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);

        MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);

        EnlargeImg = (ImageView) findViewById(R.id.DiaryMapEnlarge_img);
        EnlargeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 放大地圖
                MapLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                ContentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0));


                EnlargeImg.setVisibility(View.INVISIBLE);
                ReduceImg.setVisibility(View.VISIBLE);

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                SetMapBounds();
            }
        });

        ReduceImg = (ImageView) findViewById(R.id.DiaryMapReduce_img);
        ReduceImg.setVisibility(View.INVISIBLE);
        ReduceImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 放大地圖
                MapLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, getPx(300)));

                ContentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                ReduceImg.setVisibility(View.INVISIBLE);
                EnlargeImg.setVisibility(View.VISIBLE);

                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setAllGesturesEnabled(false);
                SetMapBounds();
            }
        });

        DiaryDetailTitleTextView = (TextView) findViewById(R.id.DiaryDetailTitle);

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
                trackRoute_cursor.moveToPosition(trackRoute_cursor.getCount() - mPosition - 1);

                DiaryDetailTitleTextView.setText(trackRoute_cursor.getString(5));
                RoutesCounter = trackRoute_cursor.getInt(0);

                RetrieveRouteFromDB();

                DiaryImgAdapter adapter = new DiaryImgAdapter(getApplicationContext(), RoutesCounter);
                gridView = (ExpandableHeightGridView)findViewById(R.id.DiaryDetailImg_gridView);
                gridView.setNumColumns(3);
                gridView.setAdapter(adapter);
                gridView.setExpanded(true);

                DiaryTxtAdapter mAdapter = new DiaryTxtAdapter(getApplicationContext(), RoutesCounter);
                mlistView = (ListView) findViewById(R.id.DiaryDetailTxt_listView);
                mlistView.setAdapter(mAdapter);
                setListViewHeightBasedOnChildren(mlistView);
            }
            trackRoute_cursor.close();
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        // 移除Google API用戶端連線
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        MarkerIcon.recycle();
        System.gc();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        Log.i(TAG, "Location services connected.");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates
                    (mGoogleApiClient, mLocationRequest, (LocationListener) this);
        } else {
            //HandleNewLocation(location);
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

    @Override
    public void onLocationChanged(Location location) {
        if (CurrentLocation != location) {
            //HandleNewLocation(CurrentLocation);
        }
    }

    private void HandleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        CurrentLocation = location;
        CurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (CurrentMarker == null) {
            // 移動地圖到目前的位置
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatlng, 15));
            CurrentMarker = mMap.addMarker(new MarkerOptions().position(CurrentLatlng).title("I am here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            CurrentMarker.setPosition(CurrentLatlng);
        }

    }

    // 顯示軌跡紀錄
    private void DisplayRoute(LatLng track_latlng) {
        //database.delete("trackRoute", null, null);
        if (TraceRoute == null) {
            TraceRoute = new ArrayList<LatLng>();
        }
        TraceRoute.add(track_latlng);

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : TraceRoute) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.parseColor("#2BB7EC"));

        Polyline line = mMap.addPolyline(polylineOpt);
        line.setWidth(10);

        Log.d("3/20_畫出軌跡", "DisplayRoute" + track_latlng.toString());
    }

    // retrieve trackRoute from DB
    private void RetrieveRouteFromDB() {
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "routesCounter=\"" + RoutesCounter + "\"", null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                //track_start = 0:該Route最後一筆(停止)，1:記錄中(開始)，2:該Track最後一筆(暫停)
                Boolean DontDisplay = false;
                while (trackRoute_cursor.moveToNext()) {
                    if (DontDisplay) {
                        DontDisplay = false;
                        continue;
                    } else {
                        Double track_lat = trackRoute_cursor.getDouble(2);
                        Double track_lng = trackRoute_cursor.getDouble(3);
                        Integer track_start = trackRoute_cursor.getInt(4);
                        LatLng track_latLng = new LatLng(track_lat, track_lng);
                        DisplayRoute(track_latLng);

                        if (track_start == 0 || track_start == 2) {
                            DontDisplay = true;
                        }
                    }
                }

                trackRoute_cursor.moveToFirst();
                Double start_lat = trackRoute_cursor.getDouble(2);
                Double start_lng = trackRoute_cursor.getDouble(3);
                LatLng start_latLng = new LatLng(start_lat, start_lng);
                mMap.addMarker(new MarkerOptions().position(start_latLng).title("start")
                        .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
                trackRoute_cursor.moveToLast();
                Double end_lat = trackRoute_cursor.getDouble(2);
                Double end_lng = trackRoute_cursor.getDouble(3);
                LatLng end_latLng = new LatLng(end_lat, end_lng);
                mMap.addMarker(new MarkerOptions().position(end_latLng).title("end")
                        .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));

            }
            trackRoute_cursor.close();
        }
        SetMapBounds();
    }

    private void SetMapBounds() {
        Double north_lat = 0.0;
        Double south_lat = 0.0;
        Double east_lng = 0.0;
        Double west_lng = 0.0;
        Cursor lat_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "routesCounter=\"" + RoutesCounter + "\"", null, null, null, "track_lat DESC", null);
        if (lat_cursor != null) {
            if (lat_cursor.getCount() != 0) {
                lat_cursor.moveToFirst();
                north_lat = lat_cursor.getDouble(2);
                lat_cursor.moveToLast();
                south_lat = lat_cursor.getDouble(2);
            }
            lat_cursor.close();
        }

        Cursor lng_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "routesCounter=\"" + RoutesCounter + "\"", null, null, null, "track_lng DESC", null);
        if (lng_cursor != null) {
            if (lng_cursor.getCount() != 0) {
                lng_cursor.moveToFirst();
                east_lng = lng_cursor.getDouble(3);
                lat_cursor.moveToLast();
                west_lng = lng_cursor.getDouble(3);
            }
            lng_cursor.close();
        }
        LatLng Northeast_latLng = new LatLng(north_lat, east_lng);
        LatLng Southwest_latLng = new LatLng(south_lat, west_lng);
        LatLngBounds bounds = new LatLngBounds(Southwest_latLng, Northeast_latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 15));
        //LatLng mid_latLng = new LatLng((start_lat+end_lat)/2, (start_lng+end_lng)/2);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mid_latLng, 15));
    }

    public int getPx(int dimensionDp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dimensionDp * density + 0.5f);
    }

    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.e("3/27_", "Marker size. " + height + "," + width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // Android 系統返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, RecordDiaryDetailActivity.this, RecordDiaryDetailActivity.this, RecordActivity.class, null);
        }
        return false;
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
                    "memo_routesCounter=\"" + routeCounter + "\" AND memo_img!=\"null\"", null, null, null, null, null);
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
                convertView = inflater.inflate(R.layout.diary_detail_grid, parent, false);
                mViewHolder = new ViewHolder();
                mViewHolder.MemoImg = (ImageView) convertView.findViewById(R.id.memoDetailGrid_image);

                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            Cursor img_cursor = database.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                            "memo_content", "memo_img", "memo_latlng", "memo_time"},
                    "memo_routesCounter=\"" + routeCounter + "\" AND memo_img!=\"null\"", null, null, null, null, null);
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

    private class DiaryTxtAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        private ViewHolder mViewHolder;
        private Integer routeCounter = 1;

        public DiaryTxtAdapter(Context mcontext, Integer rs) {
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
                convertView = inflater.inflate(R.layout.diary_detail_content, parent, false);
                mViewHolder = new ViewHolder();
                mViewHolder.DiaryString = (TextView) convertView.findViewById(R.id.DiaryDetailString);

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
                    mViewHolder.DiaryString.setText(memo_cursor.getString(2));
                    Log.e("3/27_memo content: ", memo_cursor.getString(2));
                } else {
                    mViewHolder.DiaryString.setText("未上傳景點心得。");
                }
                memo_cursor.close();
            }
            return convertView;
        }

        private class ViewHolder {
            TextView DiaryString;
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
