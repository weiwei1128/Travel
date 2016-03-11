package com.travel;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.travel.Utility.GetSpotsNSort;
import com.travel.Utility.TWSpotAPIFetcher;

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

    private ProgressDialog mDialog = null;

    //3.10
    final int REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        globalVariable = (GlobalVariable) getApplicationContext();

        registerReceiver(broadcastReceiver, new IntentFilter(TWSpotAPIFetcher.BROADCAST_ACTION));

        BitmapDrawable BitmapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.location);
        MarkerIcon = Bitmap.createScaledBitmap(BitmapDraw.getBitmap(), 50, 80, false);

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

        if (!globalVariable.isAPILoaded) {
            Log.e("3/10_", "API is not ready");
            mDialog = new ProgressDialog(MapsActivity.this);
            mDialog.setMessage("景點資料載入中...");
            mDialog.setCancelable(false);
            mDialog.show();
        } else {
            if (globalVariable.MarkerOptionsArray.isEmpty()) {
                Log.e("3/10_", "Marker is not ready");
                mDialog = new ProgressDialog(MapsActivity.this);
                mDialog.setMessage("景點資料載入中...");
                mDialog.setCancelable(false);
                mDialog.show();
                // Get Marker Info
                new GetMarkerInfo(MapsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                if (currentLocation != null) {
                    Log.e("3/10_讀到位置", "事先Sort");
                    if (globalVariable.SpotDataSorted.isEmpty()) {
                        new GetSpotsNSort(MapsActivity.this, currentLocation.getLatitude(),
                                currentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        setUpMapIfNeeded();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates
                    (mGoogleApiClient, this);
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
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
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
            Log.d("3/10_setUpMap","MarkerOption已載入...顯示中");
        }
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates
                        (mGoogleApiClient, mLocationRequest, MapsActivity.this);
            } else {
                handleNewLocation(location);
                if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
                    Log.e("3/10_MapsActivity", "事先Sort");
                    new GetSpotsNSort(MapsActivity.this, currentLocation.getLatitude(),
                            currentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
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
        if (currentLocation != location) {
            handleNewLocation(currentLocation);
        }
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
            // 移動地圖到目前的位置
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title("I am here!").icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            currentMarker.setPosition(latLng);
        }

        DataBaseHelper helper = new DataBaseHelper(MapsActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor location_cursor = database.query("location",
                new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
        if (location_cursor != null) {
            if (location_cursor.getCount() == 0) {
                ContentValues cv = new ContentValues();
                cv.put("CurrentLat", location.getLatitude());
                cv.put("CurrentLng", location.getLongitude());
                long result = database.insert("location", null, cv);
                Log.d("3/10_新增位置", result + " = DB INSERT " + location.getLatitude() + " " + location.getLongitude());

            } else {
                ContentValues cv = new ContentValues();
                cv.put("CurrentLat", location.getLatitude());
                cv.put("CurrentLng", location.getLongitude());
                long result = database.update("location", cv, "_ID=1", null);
                Log.d("3/10_位置更新", result + " = DB INSERT " + location.getLatitude() + " " + location.getLongitude());
            }
            location_cursor.close();
        }
    }

    // Android 系統返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true,MapsActivity.this, MapsActivity.this, HomepageActivity.class, null);
        }
        return false;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Boolean isTWAPILoaded = intent.getBooleanExtra("isTWAPILoaded", false);
                Boolean isTPEAPILoaded = intent.getBooleanExtra("isTPEAPILoaded", false);
                if (isTWAPILoaded && isTPEAPILoaded) {
                    globalVariable.isAPILoaded = true;
                    if (globalVariable.isAPILoaded) {
                        Log.e("3/10_", "Receive Broadcast: APILoaded");
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        if (globalVariable.MarkerOptionsArray.isEmpty()) {
                            // Get Marker Info
                            new GetMarkerInfo(MapsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                        }
                    }
                }

                globalVariable.isAPILoaded = intent.getBooleanExtra("isAPILoaded", false);
                if (globalVariable.isAPILoaded) {
                    Log.e("3/10_", "Receive Broadcast: APILoaded");
                    if (mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    if (globalVariable.MarkerOptionsArray.isEmpty()) {
                        // Get Marker Info
                        new GetMarkerInfo(MapsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    }
                }
            }
        }
    };

    public class GetMarkerInfo extends AsyncTask<Void, Void, ArrayList<MarkerOptions>> {
        public static final String TAG = "GetMarkerInfo";
        Context mcontext;

        public GetMarkerInfo(Context context) {
            this.mcontext = context;
        }

        @Override
        protected void onPreExecute() {
            Log.d("3/10_GetMarkerInfo", "MarkerOption載入中...");
            super.onPreExecute();
        }

        @Override
        protected ArrayList<MarkerOptions> doInBackground(Void... params) {
            ArrayList<MarkerOptions> MarkerOptionsArray = new ArrayList<MarkerOptions>();
            //get Marker Info
            if (globalVariable.isAPILoaded) {
                Integer SpotCount = globalVariable.SpotDataRaw.size();
                for (int i = 0; i < SpotCount; i++) {
                    String Name = globalVariable.SpotDataRaw.get(i).getName();
                    Double Latitude = globalVariable.SpotDataRaw.get(i).getLatitude();
                    Double Longitude = globalVariable.SpotDataRaw.get(i).getLongitude();
                    LatLng latLng = new LatLng(Latitude,Longitude);
                    String OpenTime = globalVariable.SpotDataRaw.get(i).getOpenTime();
                    MarkerOptions markerOpt = new MarkerOptions();
                    markerOpt.position(latLng).title(Name).snippet(OpenTime);

                    MarkerOptionsArray.add(markerOpt);
                }
            } else {
                DataBaseHelper helper = new DataBaseHelper(getBaseContext());
                SQLiteDatabase database = helper.getWritableDatabase();
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
                    spotDataRaw_cursor.close();
                }
            }
            return MarkerOptionsArray;
        }

        protected void onPostExecute(ArrayList<MarkerOptions> markerOptionsArray) {
            if (globalVariable.MarkerOptionsArray.isEmpty()) {
                globalVariable.MarkerOptionsArray = markerOptionsArray;
            }
            for (MarkerOptions markerOptions : globalVariable.MarkerOptionsArray) {
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon));
                    mMap.addMarker(markerOptions);
                }
            mDialog.dismiss();
            super.onPostExecute(markerOptionsArray);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to

            } else {
                // Permission was denied or request was cancelled
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                Toast.makeText(MapsActivity.this, "請允許寶島好智遊存取您的位置!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

