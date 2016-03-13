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
    private static int DISPLACEMENT = 0;       // 0 meters

    private Location CurrentLocation;

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
        mAdapter = new SpotListAdapter(SpotListActivity.this);
        mlistView = (ListView) findViewById(R.id.spotlist_listView);
        mlistView.setOnItemClickListener(new itemListener());

        if (globalVariable.SpotDataSorted.isEmpty()) {
            Log.e("3/10_", "Spot is Sorted");
            progressBar.setVisibility(View.VISIBLE);
            mlistView.setAdapter(null);
            if (CurrentLocation != null) {
                new GetSpotsNSort(SpotListActivity.this, CurrentLocation.getLatitude(),
                        CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            mlistView.setAdapter(mAdapter);
        }

        BackImg = (ImageView) findViewById(R.id.spotlist_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,SpotListActivity.this, SpotListActivity.this, MapsActivity.class, null);
            }
        });

        SpotMapBtn = (Button) findViewById(R.id.spot_map_button);
        SpotMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false,SpotListActivity.this, SpotListActivity.this, MapsActivity.class, null);
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
                if (!mDialog.isShowing()) {
                    Log.d("3.9_景點搜尋", s.toString());
                    mAdapter.getFilter().filter(s.toString());
                }
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
        CurrentLocation = location;
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
/*
    private class GetSortedSpotData extends AsyncTask<Void, Void, ArrayList<SpotData>> {
        private static final String TAG = "GetSortedSpotData";

        public GetSortedSpotData() {

        }

        @Override
        protected void onPreExecute() {
            // Loading ProgressBar
            mlistView.setAdapter(null);
            //Loading Dialog
            mDialog = new ProgressDialog(SpotListActivity.this);
            mDialog.setMessage("資料載入中...");
            mDialog.setCancelable(false);
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected ArrayList<SpotData> doInBackground(Void... param) {
            // get SpotData
            Cursor spotDataSorted_cursor = database.query("spotDataSorted",
                    new String[]{"spotId", "spotName", "spotAdd","spotLat", "spotLng", "picture1",
                            "picture2","picture3", "openTime", "ticketInfo", "infoDetail"},
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
                            Picture1, Picture2, Picture3, OpenTime,TicketInfo, InfoDetail));
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

            return globalVariable.SpotDataSorted;
        }

        protected void onPostExecute(ArrayList<SpotData> SpotData) {
            if (globalVariable.SpotDataSorted == null || globalVariable.SpotDataSorted.isEmpty()) {
                globalVariable.SpotDataSorted = SpotData;
            }
            // Dismiss ProgressBar
            //progressBar.setVisibility(View.INVISIBLE);
            mlistView.setAdapter(mAdapter);
            mDialog.dismiss();
            super.onPostExecute(SpotData);
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

        //帶入距離回傳字串 (距離小於一公里以公尺呈現，距離大於一公里以公里呈現並取小數點兩位)
        private String DistanceText(double distance) {
            if (distance < 1000) return String.valueOf((int) distance) + "m";
            else return new DecimalFormat("#.00").format(distance / 1000) + "km";
        }

    }
*/
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
            Functions.go(true,SpotListActivity.this, SpotListActivity.this, MapsActivity.class, null);
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
