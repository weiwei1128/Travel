package com.travel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.travel.Adapter.SpotListAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;
import com.travel.Utility.GetSpotsNSort;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SpotListActivity extends Activity {

    private static final String TAG = SpotListActivity.class.getSimpleName();

    EditText SearchEditText;
    ImageView BackImg, SearchImg;

    //ProgressBar progressBar;
    Button SpotMapBtn, SpotListBtn;

    private Double Latitude;
    private Double Longitude;

    private ListView mlistView;
    private SpotListAdapter mAdapter;

    private ProgressDialog mDialog;

    private DataBaseHelper helper;
    private SQLiteDatabase database;
    private GlobalVariable globalVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_list_activity);

        globalVariable = (GlobalVariable) getApplicationContext();

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
                    Log.d("3.9_抓取位置", Latitude.toString() + Longitude.toString());
                }
            }
            location_cursor.close();
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

        mAdapter = new SpotListAdapter(SpotListActivity.this);
        mlistView = (ListView) findViewById(R.id.spotlist_listView);
        mlistView.setOnItemClickListener(new itemListener());

        //progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //progressBar.setVisibility(View.VISIBLE);
        if (globalVariable.SpotDataSorted == null || globalVariable.SpotDataSorted.isEmpty()) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void HandleNewLocation(Double lat, Double lng) {

    }

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


}
