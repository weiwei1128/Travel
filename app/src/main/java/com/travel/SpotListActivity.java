package com.travel;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.travel.Adapter.SpotListAdapter;
import com.travel.Utility.Functions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SpotListActivity extends Activity {

    private static final String TAG = SpotListActivity.class.getSimpleName();

    private LocationManager mLocationManager;               //宣告定位管理控制
    private Criteria mCriteria;
    private Location mLocation = null;
    private String mBestProvider = LocationManager.NETWORK_PROVIDER;//LocationManager.GPS_PROVIDER;

    public static SpotJson.PostInfos Infos;
    public static TPESpotJson.PostResult Result;
    private List<String> SameRegionSpots = new ArrayList<String>();
    private ArrayList<Spot> Spots = new ArrayList<Spot>();   //建立List，屬性為Spot物件

    public String Region;
    private String JsonString = null;
    private String JsonString_TPE = null;

    EditText SearchEditText;
    ImageView BackImg, SearchImg;
    ProgressBar progressBar;
    Button SpotMapBtn, SpotListBtn;

    private ListView mlistView;
    private SpotListAdapter mAdapter;

    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_list_activity);

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

        SearchEditText = (EditText) findViewById(R.id.spotlist_searchEditText);
        SearchEditText.addTextChangedListener(new TextWatcher() {
            //TODO 2.2 在list還沒跑出來之前打字會發生error

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(progressBar.getVisibility()==View.INVISIBLE) {
                    Log.e("2.3","SearchEditText_Change"+s.toString());
                    // Call back the Adapter with current character to Filter
                    mAdapter.filter(s.toString());
                    //adapter1.getFilter().filter(s.toString());
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

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mlistView = (ListView) findViewById(R.id.spotlist_listView);

        // Restore data
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("LastLocation")) {
                // Restore value of members from saved state
                mLocation = savedInstanceState.getParcelable("LastLocation");
                Log.d(TAG, "Restore Location");
                Toast.makeText(SpotListActivity.this, "Restore Location", Toast.LENGTH_SHORT).show();
            } else if (savedInstanceState.keySet().contains("LastJsonString")) {
                // Restore value of members from saved state
                JsonString = savedInstanceState.getString("LastJsonString");
                Gson(JsonString);
                Log.d(TAG, "Restore Infos");
                Toast.makeText(SpotListActivity.this, "Restore Infos", Toast.LENGTH_SHORT).show();
            } else if (savedInstanceState.keySet().contains("LastSpots")) {
                // Restore value of members from saved state
                Spots = savedInstanceState.getParcelableArrayList("LastSpots");
                Log.d(TAG, "Restore Spots");
                Toast.makeText(SpotListActivity.this, "Restore Spots", Toast.LENGTH_SHORT).show();
            }

            if (Spots != null && (Infos != null || Result != null)) {
                mAdapter = new SpotListAdapter(SpotListActivity.this, Spots, Infos, Result, Region);
                mlistView.setAdapter(mAdapter);
                mlistView.setOnItemClickListener(new itemListener());
                Log.d(TAG, "Restore SpotList");
            }
        }
        Log.d(TAG, "onCreate");

        if (mLocation == null) {
            progressBar.setVisibility(View.VISIBLE);
            LocationServiceInitial();
            Log.d(TAG, "LocationServiceInitial: 定位中");
            Toast.makeText(SpotListActivity.this, "LocationServiceInitial: 定位中", Toast.LENGTH_SHORT).show();
        } else {
            // API 23 Needs to Check Permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Check Permissions Now
                final int REQUEST_LOCATION = 2;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            } else {
                Location location = mLocationManager.getLastKnownLocation(mBestProvider);//取得上次定位位置
                if (location != null && location != mLocation) {
                    mLocation = location;
                    HandleNewLocation(mLocation);
                } else if (location == null) {
                    mLocationManager.requestLocationUpdates(mBestProvider, 5000, 5, myListener);//每1秒、5公尺偵測一次
                }
            }
        }
    }

    LocationListener myListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            HandleNewLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            if (mLocation == null) {
                mLocationManager.requestLocationUpdates(mBestProvider, 60000, 5, myListener);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

    /*    // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            mLocationManager.removeUpdates(myListener);
        }   */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            mLocationManager.removeUpdates(myListener);
        }
    }

    private void LocationServiceInitial() {
        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mCriteria = new Criteria();
        //mBestProvider = mLocationManager.getBestProvider(mCriteria, true);

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            mLocationManager.requestLocationUpdates(mBestProvider, 60000, 5, myListener);    //每1分、5公尺偵測一次
        }
    }

    private void HandleNewLocation(Location location) {
        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            if (location == null) {
                Log.d(TAG, "Location is NULL");
                Toast.makeText(SpotListActivity.this, "HandleNewLocation: Location is NULL", Toast.LENGTH_SHORT).show();
                mLocationManager.requestLocationUpdates(mBestProvider, 60000, 5, myListener);//每1分、5公尺偵測一次
            }
        }
        mLocation = location;
        if (mLocation != null) {
            Log.d(TAG, mLocation.toString());

            // 依經緯度判斷所在縣市
            Region = "";
            Region = Geocoder(mLocation.getLatitude(), mLocation.getLongitude());
            Log.d(TAG, Region);

            // 到景點API抓相同縣市的景點資訊
            PostFetcher fetcher = new PostFetcher();
            fetcher.execute();
        }
    }

    //帶入距離回傳字串 (距離小於一公里以公尺呈現，距離大於一公里以公里呈現並取小數點兩位)
    private String DistanceText(double distance) {
        if (distance < 1000) return String.valueOf((int) distance) + "m";
        else return new DecimalFormat("#.00").format(distance / 1000) + "km";
    }

    //List排序，依照距離由近開始排列，第一筆為最近，最後一筆為最遠
    private void DistanceSort(ArrayList<Spot> spot) {
        Collections.sort(spot, new Comparator<Spot>() {
            @Override
            public int compare(Spot spot1, Spot spot2) {
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

    public String Geocoder(double Lat, double Lng) {
        Geocoder geocoder = new Geocoder(this, Locale.TAIWAN);
        String gRegion = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(Lat, Lng, 1);
            if (addresses.size() > 0) {
                gRegion = addresses.get(0).getAdminArea();
            } else {
                Log.d("TAG", "No Address returned!");
                Toast.makeText(SpotListActivity.this, "Geocoder: No Address returned!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("TAG", "Can't get Address!");
            Toast.makeText(SpotListActivity.this, "Geocoder: Can't get Address!", Toast.LENGTH_SHORT).show();
        }
        return gRegion;
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
            Toast.makeText(SpotListActivity.this, "TPE_API Failed to load Posts.", Toast.LENGTH_SHORT).show();
        }
        try {
            if (jsonString.equals(JsonString)){
                SpotJson spotJson = gson.fromJson(jsonString, SpotJson.class);
                Infos = spotJson.getInfos();
            }
        } catch (Exception ex) {
            Log.e(TAG, "JsonString: Failed to parse JSON due to: " + ex);
            Toast.makeText(SpotListActivity.this, "TW_API Failed to load Posts.", Toast.LENGTH_SHORT).show();
        }
    }

    // retrieve Spots
    private void SpotsSort() {
        Log.e(TAG, "2/3_SPOTLIST_LOCATION: " + mLocation);
        if (Region.equals("台北市")) {
            Integer ResultsLength = Result.getResults().length;
            Spots.clear();
            for (int i = 0; i < ResultsLength; i++) {
                String Name = Result.getResults()[i].getStitle();
                Double Latitude = Double.valueOf(Result.getResults()[i].getLatitude());
                Double Longitude = Double.valueOf(Result.getResults()[i].getLongitude());
                Spots.add(new Spot(i, Name, Latitude, Longitude));
            }
        } else {
            Integer InfoLength = Infos.getInfo().length;
            SameRegionSpots.clear();
            for (int i = 0; i < InfoLength; i++) {
                String RegionSpot = Infos.getInfo()[i].getRegion();
                if (RegionSpot.equals(Region)) {
                    SameRegionSpots.add(String.valueOf(i));
                }
            }

            Integer RegionSpotsSize = SameRegionSpots.size();
            Spots.clear();
            for (int j = 0; j < RegionSpotsSize; j++) {
                Integer position = Integer.parseInt(SameRegionSpots.get(j));
                String Name = Infos.getInfo()[position].getName();
                Double Latitude = Double.valueOf(Infos.getInfo()[position].getPy());
                Double Longitude = Double.valueOf(Infos.getInfo()[position].getPx());
                Spots.add(new Spot(position, Name, Latitude, Longitude));
            }
        }

        Log.d(TAG, "排序");
        for (Spot mSpot : Spots) {
            //for迴圈將距離帶入，判斷距離為Distance function
            //需帶入使用者取得定位後的緯度、經度、景點店家緯度、經度。
            mSpot.setDistance(Distance(mLocation.getLatitude(), mLocation.getLongitude(),
                    mSpot.getLatitude(), mSpot.getLongitude()));
        }

        //依照距離遠近進行List重新排列
        DistanceSort(Spots);

        //印出我的座標-經度緯度
        Log.d("TAG", "我的座標-經度: " + mLocation.getLongitude() +
                " , 緯度: " + mLocation.getLatitude());
        //for迴圈，印出景點店家名稱及距離，並依照距離由近至遠排列
        //第一筆為最近的景點店家，最後一筆為最遠的景點店家
        for (int i = 0; i < Spots.size(); i++) {
            Log.d("TAG", "地點: " + Spots.get(i).getName() + " " +
                    ", 距離為: " + DistanceText(Spots.get(i).getDistance()));
        }
    }

    private void handlePostsList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Spots.size() == 0) {
                    Toast.makeText(SpotListActivity.this, "附近沒有景點", Toast.LENGTH_SHORT).show();
                }

                mAdapter = new SpotListAdapter(SpotListActivity.this, Spots, Infos, Result, Region);
                mlistView.setAdapter(mAdapter);
                mlistView.setOnItemClickListener(new itemListener());
            }
        /*        for(SpotJson spotJsonList : SpotListActivity.this.spotJsonList) {
                    spotJsonList.getInfos().getInfo();
                    //GsonText.setText(spotJsonList.getInfos().getInfo()[0].getAdd());
                    //Toast.makeText(GsonActivity.this, spotJsonList.Infos, Toast.LENGTH_SHORT).show();
                }*/

        });
    }

    private void failedLoadingPosts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SpotListActivity.this, "Failed to get GlobalVariable.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class PostFetcher extends AsyncTask<Void, Void, String> {
        private static final String TAG = "PostFetcher";

        @Override
        protected void onPreExecute() {
            // Loading ProgressBar
            mlistView.setAdapter(null);
//            progressBar.setVisibility(View.VISIBLE);
            //2.2 wei move to another place
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... param) {

            GlobalVariable globalVariable = (GlobalVariable)getApplicationContext();
            JsonString = globalVariable.JsonString;
            if (!JsonString.equals("null")) {
                Gson(JsonString);
                Log.d(TAG, "JsonString");
                //Toast.makeText(this, "JsonString", Toast.LENGTH_SHORT).show();
            } else
                failedLoadingPosts();

            JsonString_TPE = globalVariable.JsonString_TPE;
            if (!JsonString_TPE.equals("null")) {
                Gson(JsonString_TPE);
                Log.d(TAG, "JsonString_TPE");
                //Toast.makeText(this, "JsonString_TPE", Toast.LENGTH_SHORT).show();
            } else
                failedLoadingPosts();

            SpotsSort();

            return null;
        }

        protected void onPostExecute(String JsonString) {
            // Dismiss ProgressBar
            handlePostsList();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", Spots.get(position).getPosition());
            bundle.putString("Region", Region);
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
        outState.putParcelable("LastLocation", mLocation);
        outState.putParcelableArrayList("LastSpots", Spots);
        outState.putString("LastJsonString", JsonString);

        Log.d(TAG, "onSaveInstanceState");

        super.onSaveInstanceState(outState);
    }

    public class Spot implements Parcelable {
        private Integer Position;        //景點店家編號
        private String Name;        //景點店家名稱
        private double Latitude;    //景點店家緯度
        private double Longitude;   //景點店家經度
        private double Distance;    //景點店家距離

        //建立物件時需帶入景點店家名稱、景點店家緯度、景點店家經度
        public Spot(Integer position, String name, double latitude, double longitude) {
            //將資訊帶入類別屬性
            Position = position;
            Name = name;
            Latitude = latitude;
            Longitude = longitude;
        }

        protected Spot(Parcel in) {
            Name = in.readString();
            Latitude = in.readDouble();
            Longitude = in.readDouble();
            Distance = in.readDouble();
        }

        public final Creator<Spot> CREATOR = new Creator<Spot>() {
            @Override
            public Spot createFromParcel(Parcel in) {
                return new Spot(in);
            }

            @Override
            public Spot[] newArray(int size) {
                return new Spot[size];
            }
        };

        //取得店家編號
        public Integer getPosition() {
            return Position;
        }

        //取得店家名稱
        public String getName() {
            return Name;
        }

        //取得店家緯度
        public double getLatitude() {
            return Latitude;
        }

        //取得店家經度
        public double getLongitude() {
            return Longitude;
        }

        //寫入店家距離
        public void setDistance(double distance) {
            Distance = distance;
        }

        //取的店家距離
        public double getDistance() {
            return Distance;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(Name);
            dest.writeDouble(Latitude);
            dest.writeDouble(Longitude);
            dest.writeDouble(Distance);
        }
    }

}
