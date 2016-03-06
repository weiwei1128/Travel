package com.travel;

import android.Manifest;
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
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tinghua on 2016/01/09.
 * Just for test.
 */
public class GsonActivity extends AppCompatActivity {

    public static final String TAG = GsonActivity.class.getSimpleName();

    private LocationManager mLocationManager;               //宣告定位管理控制
    private Criteria criteria;
    private Location mLocation;

    private List<SpotJson> spotJsonList;
    private List<String> SameRegionSpots = new ArrayList<String>();
    private ArrayList<Spot> Spots = new ArrayList<Spot>();   //建立List，屬性為Spot物件

    private String Region;

    TextView GsonText;
    Button GsonButton;

    StringBuilder test = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gson_activity);

        GsonText = (TextView) findViewById(R.id.GsonText);

        LocationServiceInitial();

        GsonButton = (Button) findViewById(R.id.GsonButton);
        GsonButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    LocationListener myListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            mLocation = location;
            HandleNewLocation(mLocation);
            Log.d("TAG", "排序");
            for(Spot mSpot : Spots)
            {
                //for迴圈將距離帶入，判斷距離為Distance function
                //需帶入使用者取得定位後的緯度、經度、景點店家緯度、經度。
                mSpot.setDistance(Distance(mLocation.getLatitude(),mLocation.getLongitude(),
                        mSpot.getLatitude(), mSpot.getLongitude()));
            }

            //依照距離遠近進行List重新排列
            DistanceSort(Spots);

            //印出我的座標-經度緯度
            Log.d("TAG", "我的座標-經度: " + mLocation.getLongitude() + " , 緯度: " + mLocation.getLatitude() );

            //for迴圈，印出景點店家名稱及距離，並依照距離由近至遠排列
            //第一筆為最近的景點店家，最後一筆為最遠的景點店家
            StringBuilder sb = new StringBuilder();
            for(int i = 0 ; i < Spots.size() ; i++ )
            {
                sb.append("地點 : " + Spots.get(i).getName() +
                        " , 距離為 : " + DistanceText(Spots.get(i).getDistance()) + "\n");
                Log.d("TAG", "地點 : " + Spots.get(i).getName() + "  , 距離為 : " + DistanceText(Spots.get(i).getDistance()) );
            }
            GsonText.setText(sb);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

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
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String best = mLocationManager.getBestProvider(criteria, true);

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            final int REQUEST_LOCATION = 2;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            Location location = mLocationManager.getLastKnownLocation(best);//取得上次定位位置
            mLocationManager.requestLocationUpdates(best, 3000, 10, myListener);//每一秒、十公尺偵測一次
            this.mLocation = location;
        }
    }

    private void HandleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        Location newLocation = location;
        Region = Geocoder(newLocation.getLatitude(),newLocation.getLongitude());

        StringBuilder sb = new StringBuilder();
        sb.append("目前最新位置:\n");
        sb.append("緯度:" + newLocation.getLatitude() + "\n");
        sb.append("經度:"+newLocation.getLongitude()+"\n");
        sb.append("Region:"+Region+"\n");

        GsonText.setText(sb);

        PostFetcher fetcher = new PostFetcher();
        fetcher.execute();
    }

    //帶入距離回傳字串 (距離小於一公里以公尺呈現，距離大於一公里以公里呈現並取小數點兩位)
    private String DistanceText(double distance)
    {
        if(distance < 1000 ) return String.valueOf((int)distance) + "m" ;
        else return new DecimalFormat("#.00").format(distance/1000) + "km" ;
    }

    //List排序，依照距離由近開始排列，第一筆為最近，最後一筆為最遠
    private void DistanceSort(ArrayList<Spot> spot)
    {
        Collections.sort(spot, new Comparator<Spot>() {
            @Override
            public int compare(Spot spot1, Spot spot2) {
                return spot1.getDistance() < spot2.getDistance() ? -1 : 1;
            }
        });
    }

    //帶入使用者及景點店家經緯度可計算出距離
    public double Distance(double longitude1, double latitude1, double longitude2,double latitude2)
    {
        double radLatitude1 = latitude1 * Math.PI / 180;
        double radLatitude2 = latitude2 * Math.PI / 180;
        double l = radLatitude1 - radLatitude2;
        double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
                + Math.cos(radLatitude1) * Math.cos(radLatitude2)
                * Math.pow(Math.sin(p / 2), 2)));
        distance = distance * 6378137.0;
        distance = Math.round(distance * 10000) / 10000;

        return distance ;
    }


    public String Geocoder(double Lat, double Lng) {
        Geocoder geocoder = new Geocoder(this, Locale.TAIWAN);
        String gRegion = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(Lat, Lng, 1);
            if (addresses.size() > 0) {
                gRegion = addresses.get(0).getAdminArea();
                GsonText.setText(addresses.get(0).getAdminArea());
            } else {
                GsonText.setText("No Address returned!");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            GsonText.setText("Canont get Address!");
        }
        return gRegion;
    }


    private void handlePostsList(String JsonString) {
        try {

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a");
            Gson gson = gsonBuilder.create();

            // JsonGoods Array 寫法
        /*    List<SpotJson> spotJsonList = new ArrayList<SpotJson>();
            Type spotJsonListType = new TypeToken<List<SpotJson>>() {}.getType();
            spotJsonList = gson.fromJson(reader, spotJsonListType); */

        /*    // JsonGoods Object 寫法
            List<SpotJson> spotJsonList = new ArrayList<SpotJson>();
            SpotJson spotJson = gson.fromJson(JsonString, SpotJson.class);
            spotJsonList.add(spotJson);

            // 輸出
            for(SpotJson spotJsonListOutPut : SpotListActivity.this.spotJsonList) {
                spotJsonListOutPut.getInfos().getInfo()[0].getAdd();
            }    */

            TPESpotJson TPEspotJson = gson.fromJson(JsonString, TPESpotJson.class);
            Integer ResultsLength = TPEspotJson.getResult().getResults().length;
            for (int i = 0; i < ResultsLength; i++) {
                String Name = TPEspotJson.getResult().getResults()[i].getStitle();
                Double Latitude = Double.valueOf(TPEspotJson.getResult().getResults()[i].getLatitude());
                Double Longitude = Double.valueOf(TPEspotJson.getResult().getResults()[i].getLongitude());
                Spots.add(new Spot(i, Name, Latitude, Longitude));
            }

        } catch (Exception ex) {
            Log.e(TAG, "Failed to parse JSON due to: " + ex);
            failedLoadingPosts();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Integer SpotsSize = Spots.size();
                StringBuilder sb = new StringBuilder();
                sb.append("SpotsSize:" + SpotsSize + "\n");
                GsonText.setText(sb);   
                //Toast.makeText(GsonActivity.this, spotJsonList.Infos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void failedLoadingPosts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GsonActivity.this, "Failed to load Posts. Have a look at LogCat.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class PostFetcher extends AsyncTask<Void, Void, String> {

        private static final String TAG = "PostFetcher";
        public static final String SERVER_URL = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5";

        @Override
        protected void onPreExecute() {
            // Create Show ProgressBar
        }

        @Override
        protected String doInBackground(Void... params) {
            String JsonString ="";
            try {
                //Create an HTTP client
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(SERVER_URL);

                //Perform the request and check the status code
                HttpResponse response = client.execute(get);
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();

                    //Read the server response and attempt to parse it as JSON
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        JsonString += line;
                    }
                    content.close();
                } else {
                    Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                    failedLoadingPosts();
                }
            } catch(Exception ex) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
                failedLoadingPosts();
            }
            return JsonString;
        }

        protected void onPostExecute(String JsonString)  {
            // Dismiss ProgressBar
            handlePostsList(JsonString);
        }
    }


    public class Spot
    {
        private Integer Position;        //景點店家編號
        private String Name;        //景點店家名稱
        private double Latitude;    //景點店家緯度
        private double Longitude;   //景點店家經度
        private double Distance;    //景點店家距離

        //建立物件時需帶入景點店家名稱、景點店家緯度、景點店家經度
        public Spot(Integer position, String name , double latitude , double longitude)
        {
            //將資訊帶入類別屬性
            Position = position;
            Name = name ;
            Latitude = latitude ;
            Longitude = longitude ;
        }

        //取得店家編號
        public Integer getPosition()
        {
            return Position;
        }

        //取得店家名稱
        public String getName()
        {
            return Name;
        }

        //取得店家緯度
        public double getLatitude()
        {
            return Latitude;
        }

        //取得店家經度
        public double getLongitude()
        {
            return Longitude;
        }

        //寫入店家距離
        public void setDistance(double distance)
        {
            Distance = distance;
        }

        //取的店家距離
        public double getDistance()
        {
            return Distance;
        }
    }


}
