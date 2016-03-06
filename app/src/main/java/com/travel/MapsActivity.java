package com.travel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.travel.Utility.Functions;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location currentLocation;
    private Marker currentMarker;

    public static final String TAG = MapsActivity.class.getSimpleName();

    public static SpotJson.PostInfos Infos;
    public static TPESpotJson.PostResult Result;
    //public Bundle bundle = new Bundle();
    private String JsonString = null;
    private String JsonString_TPE = null;

    private ArrayList<MarkerOptions> MarkerOptionsArray = new ArrayList<MarkerOptions>();
    private BitmapDescriptor MarkerIcon;

    private ImageView BackImg;
    //LinearLayout SpotMapLayout,SpotListLayout;
    private Button SpotMapBtn, SpotListBtn;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        GlobalVariable globalVariable = (GlobalVariable)getApplicationContext();
        JsonString = globalVariable.JsonString;
        if (!JsonString.equals("null")) {
            Gson(JsonString);
            Toast.makeText(this, "JsonString", Toast.LENGTH_SHORT).show();
        }

        JsonString_TPE = globalVariable.JsonString_TPE;
        if (!JsonString_TPE.equals("null")) {
            Gson(JsonString_TPE);
            Toast.makeText(this, "JsonString_TPE", Toast.LENGTH_SHORT).show();
        }

        MarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.location);

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
                Log.e("2.3","Map->Spot_location"+currentLocation);
                Functions.go(false,MapsActivity.this, MapsActivity.this, SpotListActivity.class, null);
            }
        });

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates
                    (mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
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

        if (MarkerOptionsArray != null)
            for (MarkerOptions markerOptions : MarkerOptionsArray) {
                mMap.addMarker(markerOptions);
                //marker.setVisible(false);
                //marker.remove(); <-- works too!
            }
        /*
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(22.997219, 120.202415)).title("赤崁樓"));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(23.001564, 120.160676)).title("安平古堡"));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(23.010687, 120.199797)).title("花園夜市"));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(22.934781, 120.226067)).title("奇美博物館"));
        */
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
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng).title("I am here!"));
        } else {
            currentMarker.setPosition(latLng);
        }

        // 移動地圖到目前的位置
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

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

        //get Marker Info
        Integer ResultsLength = Result.getResults().length;
        for (int i = 0; i < ResultsLength; i++) {
            String Name = Result.getResults()[i].getStitle();
            String OpenTime = Result.getResults()[i].getMemoTime();
            Double Latitude = Double.valueOf(Result.getResults()[i].getLatitude());
            Double Longitude = Double.valueOf(Result.getResults()[i].getLongitude());
            LatLng latLng = new LatLng(Latitude,Longitude);
            MarkerOptions markerOpt = new MarkerOptions();
            markerOpt.position(latLng).title(Name).snippet(OpenTime).icon(MarkerIcon);

            MarkerOptionsArray.add(markerOpt);
        }

        Integer InfoLength = Infos.getInfo().length;
        for (int i = 0; i < InfoLength; i++) {
            String Name = Infos.getInfo()[i].getName();
            String OpenTime = Infos.getInfo()[i].getOpentime();
            Double Latitude = Double.valueOf(Infos.getInfo()[i].getPy());
            Double Longitude = Double.valueOf(Infos.getInfo()[i].getPx());
            LatLng latLng = new LatLng(Latitude,Longitude);
            MarkerOptions markerOpt = new MarkerOptions();
            markerOpt.position(latLng).title(Name).snippet(OpenTime).icon(MarkerIcon);

            MarkerOptionsArray.add(markerOpt);
        }
    }

    // retrieve SpotJson
    private void Gson(String jsonString) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        Gson gson = gsonBuilder.create();
        try {
            if (jsonString.equals(JsonString_TPE)) {
                TPESpotJson tpespotJson = gson.fromJson(jsonString, TPESpotJson.class);
                Result = tpespotJson.getResult();
            }
        } catch (Exception ex) {
            Log.e(TAG, "JsonString_TPE: Failed to parse JSON due to: " + ex);
            Toast.makeText(MapsActivity.this, "TPE_API Failed to load Posts.", Toast.LENGTH_SHORT).show();
            }
        try {
            if (jsonString.equals(JsonString)){
                SpotJson spotJson = gson.fromJson(jsonString, SpotJson.class);
                Infos = spotJson.getInfos();
            }
        } catch (Exception ex) {
            Log.e(TAG, "JsonString: Failed to parse JSON due to: " + ex);
            Toast.makeText(MapsActivity.this, "TW_API Failed to load Posts.", Toast.LENGTH_SHORT).show();
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

}

