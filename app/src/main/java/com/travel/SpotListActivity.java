package com.travel;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.travel.Adapter.SpotListAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;
import com.travel.Utility.GetSpotsNSort;

public class SpotListActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = SpotListActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    EditText SearchEditText;
    ImageView BackImg, SearchImg;

    ProgressBar progressBar;
    Button SpotMapBtn, SpotListBtn;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 5;       // 5 meters

    private Location currentLocation;

    private Double Latitude;
    private Double Longitude;

    private ListView mlistView;
    private SpotListAdapter mAdapter;

    private ProgressDialog mDialog;

    private DataBaseHelper helper;
    private SQLiteDatabase database;
    private GlobalVariable globalVariable;

    //3.10
    final int REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_list_activity);

        registerReceiver(broadcastReceiver, new IntentFilter(GetSpotsNSort.BROADCAST_ACTION));

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        globalVariable = (GlobalVariable) getApplicationContext();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //mAdapter = new SpotListAdapter(SpotListActivity.this);
        mlistView = (ListView) findViewById(R.id.spotlist_listView);
        mlistView.setOnItemClickListener(new itemListener());

        if (globalVariable.SpotDataSorted.isEmpty()) {
            Log.e("3/10_", "Spot is Sorted");
            progressBar.setVisibility(View.VISIBLE);
            mlistView.setAdapter(null);
            if (currentLocation != null) {
                new GetSpotsNSort(SpotListActivity.this, currentLocation.getLatitude(),
                        currentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            mlistView.setAdapter(mAdapter);
        }

        BackImg = (ImageView) findViewById(R.id.spotlist_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,SpotListActivity.this, SpotListActivity.this, SpotActivity.class, null);
            }
        });

        SpotMapBtn = (Button) findViewById(R.id.spot_map_button);
        SpotMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false,SpotListActivity.this, SpotListActivity.this, SpotActivity.class, null);
            }
        });

        SpotListBtn = (Button) findViewById(R.id.spot_list_button);
        SpotListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //progressBar.setVisibility(View.VISIBLE);
/*        if (globalVariable.SpotDataSorted == null || globalVariable.SpotDataSorted.isEmpty()) {
            GetSortedSpotData getSortedSpotData = new GetSortedSpotData();
            getSortedSpotData.execute();
            Log.d("3.9", "資料載入中...");
            Toast.makeText(SpotListActivity.this, "資料載入中...", Toast.LENGTH_SHORT).show();
        } else {
            //progressBar.setVisibility(View.INVISIBLE);
            mlistView.setAdapter(mAdapter);
            if(mAdapter.getCount() == 0) {
                Toast.makeText(SpotListActivity.this, "尚無資料!", Toast.LENGTH_SHORT).show();
            }
        }
*/
        SearchEditText = (EditText) findViewById(R.id.spotlist_searchEditText);
        SearchEditText.addTextChangedListener(new TextWatcher() {
            //TODO 2.2 在list還沒跑出來之前打字會發生error
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("3.9_景點搜尋", s.toString());
                mAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        SearchImg = (ImageView) findViewById(R.id.spotlist_searchImg);
        SearchImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "此裝置沒有支援Google Play Services", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 5 seconds, in milliseconds
                .setFastestInterval(FATEST_INTERVAL) // 1 second, in milliseconds
                .setSmallestDisplacement(DISPLACEMENT);
    }


    private void HandleNewLocation(Location location) {
        currentLocation = location;
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
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
        super.onDestroy();
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
                        (mGoogleApiClient, mLocationRequest, (LocationListener) SpotListActivity.this);
            } else {
                HandleNewLocation(location);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Boolean isSpotSorted = intent.getBooleanExtra("isSpotSorted", false);
                if (isSpotSorted) {
                    Log.e("3/10_景點排序完畢", "Receive Broadcast");
                    progressBar.setVisibility(View.INVISIBLE);
                    mlistView.setAdapter(mAdapter);
                }
            }
        }
    };

    private class itemListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", position);
            Functions.go(false,SpotListActivity.this, SpotListActivity.this, SpotDetailActivity.class, bundle);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true,SpotListActivity.this, SpotListActivity.this, SpotActivity.class, null);
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
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
                Toast.makeText(SpotListActivity.this, "請允許寶島好智遊存取您的位置!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
