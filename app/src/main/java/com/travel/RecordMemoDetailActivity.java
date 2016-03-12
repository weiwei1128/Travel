package com.travel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
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
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener {

    public static final String TAG = RecordMemoDetailActivity.class.getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location currentLocation;
    private Marker currentMarker;

    private ImageLoader loader = ImageLoader.getInstance();
    private DisplayImageOptions options;
    private ImageLoadingListener listener;

    private TextView MemoDetailTitleTextView, MemoDetailTextView;
    private ImageView backImg, EnlargeImg;
    private ExpandableHeightGridView gridView;

    private Integer mPosition;
    private DataBaseHelper helper;
    private SQLiteDatabase database;

    private String[] image_url;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_memo_detail);

        backImg = (ImageView) findViewById(R.id.MemoDetail_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, RecordMemoDetailActivity.this, RecordMemoDetailActivity.this,
                        RecordMemoActivity.class, null);
            }
        });

        EnlargeImg = (ImageView) findViewById(R.id.MemoMapEnlarge_img);
        EnlargeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 放大地圖
            }
        });

        MemoDetailTextView = (TextView) findViewById(R.id.MemoDetailString);
        MemoDetailTitleTextView = (TextView) findViewById(R.id.MemoDetailTitle);


        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            mPosition = bundle.getInt("WhichItem");
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

        ImageLoaderConfiguration configuration =
                new ImageLoaderConfiguration.Builder(RecordMemoDetailActivity.this).build();
        ImageLoader.getInstance().destroy();
        ImageLoader.getInstance().init(configuration);

        Cursor travelMemo_cursor = database.query("travelMemo", new String[]{"totalCount", "id",
                "title", "url","zhaiyao", "click", "addtime"}, null, null, null, null, null);
        if (travelMemo_cursor != null && travelMemo_cursor.getCount() > 0) {
            travelMemo_cursor.moveToPosition(mPosition);
            MemoDetailTitleTextView.setText(travelMemo_cursor.getString(2));
            MemoDetailTextView.setText(travelMemo_cursor.getString(4));
        }

        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < image_url.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("image", image_url[i]);
            items.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(this,
                items, R.layout.memo_detail_grid, new String[]{"image"},
                new int[]{R.id.memoDetailGrid_image});

        gridView = (ExpandableHeightGridView)findViewById(R.id.MemoDetail_gridView);
        gridView.setNumColumns(3);
        gridView.setAdapter(adapter);
        gridView.setExpanded(true);

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

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);                    // 顯示定位按鈕
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);         // 設定地圖類型

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

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
