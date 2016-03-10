package com.travel;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location currentLocation;
    private Marker currentMarker;

    private Bitmap MarkerIcon;
    private GlobalVariable globalVariable;

    private ImageView BackImg;
    private Button SpotMapBtn, SpotListBtn;

    private ProgressDialog mDialog;

    private DataBaseHelper helper;
    private SQLiteDatabase database;
    private Double Latitude;
    private Double Longitude;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        helper = new DataBaseHelper(getApplicationContext());
        database = helper.getWritableDatabase();
        // retrieve Location from DB
        Cursor location_cursor = database.query("location",
                new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
        if (location_cursor != null) {
            if (location_cursor.getCount() != 0) {
                while (location_cursor.moveToNext()) {
                    Latitude = location_cursor.getDouble(0);
                    Longitude = location_cursor.getDouble(1);
                }
                latLng = new LatLng(Latitude, Longitude);
                Log.d("3.9_抓取位置", Latitude.toString() + Longitude.toString());
            }
            location_cursor.close();
        }

        BitmapDrawable BitmapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.location);
        MarkerIcon = Bitmap.createScaledBitmap(BitmapDraw.getBitmap(), 60, 90, false);

        globalVariable = (GlobalVariable) getApplicationContext();
        if (globalVariable.MarkerOptionsArray.isEmpty()) {
            // Get Marker Info
            GetMarkerInfo getMarkerInfo = new GetMarkerInfo(MapsActivity.this);
            getMarkerInfo.execute();
        }

        BackImg = (ImageView) findViewById(R.id.maps_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,MapsActivity.this, MapsActivity.this, HomepageActivity.class, null);
            }
        });

        SpotMapBtn = (Button) findViewById(R.id.spot_map_button);
        SpotMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent();
//                intent.setClass(MapsActivity.this, MapsActivity.class);
//                startActivity(intent);
//                finish();
            }
        });

        SpotListBtn = (Button) findViewById(R.id.spot_list_button);
        SpotListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("2.3", "Map->Spot_location" + currentLocation);
                Functions.go(false, MapsActivity.this, MapsActivity.this, SpotListActivity.class, null);
            }
        });
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates
                    (mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        // 移除Google API用戶端連線
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MarkerIcon.recycle();
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

        if (!globalVariable.MarkerOptionsArray.isEmpty()) {
            for (MarkerOptions markerOptions : globalVariable.MarkerOptionsArray) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon));
                mMap.addMarker(markerOptions);
            }
            Log.d("3.9_setUpMap","MarkerOption已載入...顯示中");
        }
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
                        (mGoogleApiClient, mLocationRequest, MapsActivity.this);
            } else {
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
            Toast.makeText(this, R.string.google_play_service_missing,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Location Listener
     */
    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        currentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (currentMarker == null) {
            if (mMap == null) {
                LoadtoMap();
            }
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title("I am here!").icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            currentMarker.setPosition(latLng);
        }

        // 移動地圖到目前的位置
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void LoadtoMap() {
        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)        // 5 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map)).getMap();
        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    // Android 系統返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true,MapsActivity.this, MapsActivity.this, HomepageActivity.class, null);
        }
        return false;
    }

    public class GetMarkerInfo extends AsyncTask<Void, Void, ArrayList<MarkerOptions>> {
        public static final String TAG = "GetMarkerInfo";
        Context mcontext;
        ArrayList<MarkerOptions> MarkerOptionsArray = new ArrayList<MarkerOptions>();

        public GetMarkerInfo(Context context) {
            this.mcontext = context;
        }

        @Override
        protected void onPreExecute() {
            Log.d("3.9_GetMarkerInfo","MarkerOption載入中...");
            //Loading Dialog
            mDialog = new ProgressDialog(MapsActivity.this);
            mDialog.setMessage("載入中......");
            mDialog.setCancelable(false);
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected ArrayList<MarkerOptions> doInBackground(Void... params) {
            //get Marker Info
            Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotId", "spotName", "spotAdd",
                            "spotLat", "spotLng", "picture1", "picture2","picture3",
                            "openTime", "ticketInfo", "infoDetail"},
                    null, null, null, null, null);
            if (spotDataRaw_cursor != null) {
                while (spotDataRaw_cursor.moveToNext()) {
                    String Name = spotDataRaw_cursor.getString(1);
                    Double Latitude = spotDataRaw_cursor.getDouble(3);
                    Double Longitude = spotDataRaw_cursor.getDouble(4);
                    LatLng latLng = new LatLng(Latitude,Longitude);
                    String OpenTime = spotDataRaw_cursor.getString(8);
                    MarkerOptions markerOpt = new MarkerOptions();
                    markerOpt.position(latLng).title(Name).snippet(OpenTime);

                    MarkerOptionsArray.add(markerOpt);
                }
            }
            if (spotDataRaw_cursor != null)
                spotDataRaw_cursor.close();
            return MarkerOptionsArray;
        }

        protected void onPostExecute(ArrayList<MarkerOptions> markerOptionsArray) {
            if (MarkerOptionsArray.isEmpty()) {
                MarkerOptionsArray = markerOptionsArray;
            } else {
                globalVariable.MarkerOptionsArray = markerOptionsArray;
                for (MarkerOptions markerOptions : globalVariable.MarkerOptionsArray) {
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon));
                    mMap.addMarker(markerOptions);
                }
            }
            mDialog.dismiss();
            super.onPostExecute(markerOptionsArray);
        }
    }
}

