package com.travel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.travel.Adapter.SpotListFragmentViewPagerAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.GetSpotsNSort;
import com.travel.Utility.SpotListViewPagerFragment;

import java.util.ArrayList;
import java.util.List;

public class SpotListFragment extends Fragment implements
        //LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "SpotListFragment";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 3;       // 5 meters

    private Location CurrentLocation;

    int count = 0, pageNo = 1, pages = 0, minus = pageNo-1;
    private TextView number, lastPage, nextPage;
    private LinearLayout spotList_pageLayout, spotList_textLayout;

    private EditText SearchEditText;
    private ImageView SearchImg;
    private FrameLayout spotList_searchLayout;

    private ViewPager viewPager;
    private ProgressBar progressBar;
    private SpotListFragmentViewPagerAdapter adapter;

    private List<Fragment> fragments = new ArrayList<>();

    private DataBaseHelper helper;
    private SQLiteDatabase database;
    private GlobalVariable globalVariable;


    public SpotListFragment() {
    }

    public static SpotListFragment newInstance() {
        SpotListFragment fragment = new SpotListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mPage = getArguments().getInt(ARG_PAGE);
        }
        globalVariable = (GlobalVariable) getActivity().getApplicationContext();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(GetSpotsNSort.BROADCAST_ACTION));

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 5 seconds, in milliseconds
                .setFastestInterval(FATEST_INTERVAL) // 1 second, in milliseconds
                .setSmallestDisplacement(DISPLACEMENT);

        if (globalVariable.SpotDataSorted.isEmpty()) {
            if (CurrentLocation != null) {
                Log.e("3/23_SpotListFragment", "start sort");
                new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                        CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spot_list, container, false);

        SearchEditText = (EditText) view.findViewById(R.id.spotlist_searchEditText);
        SearchEditText.addTextChangedListener(new TextWatcher() {
            //TODO 2.2 在list還沒跑出來之前打字會發生error
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("3.9_景點搜尋", s.toString());
                SpotListViewPagerFragment.adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        SearchImg = (ImageView) view.findViewById(R.id.spotlist_searchImg);
        SearchImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        spotList_searchLayout = (FrameLayout) view.findViewById(R.id.spotList_searchLayout);
        spotList_pageLayout = (LinearLayout) view.findViewById(R.id.spotList_pageLayout);
        spotList_textLayout = (LinearLayout) view.findViewById(R.id.spotList_textLayout);

        lastPage = (TextView) view.findViewById(R.id.lastpage_text);
        lastPage.setVisibility(View.INVISIBLE);
        lastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNo - 2);
            }
        });
        nextPage = (TextView) view.findViewById(R.id.nextpage_text);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(pageNo);

            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        viewPager = (ViewPager) view.findViewById(R.id.spotList_viewpager);

        if (globalVariable.SpotDataSorted.isEmpty()) {
            Log.e("3/23_SpotListFragment", "no sort");
            progressBar.setVisibility(View.VISIBLE);
            spotList_searchLayout.setVisibility(View.INVISIBLE);
            spotList_pageLayout.setVisibility(View.INVISIBLE);
            //viewPager.setAdapter(null);

        } else {
            count = globalVariable.SpotDataSorted.size();
            if (count % 10 > 0) {
                pages = (count / 10) + 1;
            } else {
                pages = (count / 10);
            }

            //fragment(i) -> i代表第幾頁
            TextView textView = new TextView(getContext());
            textView.setText("/" + pages);
            textView.setTextColor((Color.parseColor("#000000")));
            number = new TextView(getContext());
            number.setText("1");
            number.setTextColor((Color.parseColor("#FF0088")));
            spotList_textLayout.addView(number);
            spotList_textLayout.addView(textView);

            for (int i = 0; i < pages; i++) {
                fragments.add(new SpotListViewPagerFragment(i + 1));
            }
            viewPager.setAdapter(new SpotListFragmentViewPagerAdapter(getChildFragmentManager(), viewPager,
                    fragments, getActivity()));
            viewPager.setOnPageChangeListener(new PageListener());
        }

        return view;
    }

    private class PageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
            pageNo = position + 1;
            if (pageNo == pages)
                nextPage.setVisibility(View.INVISIBLE);
            else nextPage.setVisibility(View.VISIBLE);
            if (pageNo == 1)
                lastPage.setVisibility(View.INVISIBLE);
            else lastPage.setVisibility(View.VISIBLE);
            minus = pageNo-1;
            String get = String.valueOf(position + 1);
            number.setText(get);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public void onResume() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onResume();
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
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
        System.gc();
        super.onDestroy();
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
                    (mGoogleApiClient, mLocationRequest, (LocationListener) getActivity());
        } else {
            HandleNewLocation(location);
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
            Toast.makeText(getActivity(), R.string.google_play_service_missing, Toast.LENGTH_LONG).show();
        }
    }

/*
    @Override
    public void onLocationChanged(Location location) {
        if (CurrentLocation != location) {
            HandleNewLocation(CurrentLocation);
        }
    }
*/
    private void HandleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        CurrentLocation = location;
/*        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (CurrentMarker == null) {
            // 移動地圖到目前的位置
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            CurrentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("I am here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            CurrentMarker.setPosition(latLng);
        }

        DataBaseHelper helper = new DataBaseHelper(getActivity());
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
        database.close();
        helper.close();*/
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Boolean isSpotSorted = intent.getBooleanExtra("isSpoted", false);
                if (isSpotSorted) {
                    Log.e("3/23_景點排序完畢", "Receive Broadcast");

                    count = globalVariable.SpotDataSorted.size();
                    if (count % 10 > 0) {
                        pages = (count / 10) + 1;
                    } else {
                        pages = (count / 10);
                    }

                    //fragment(i) -> i代表第幾頁
                    TextView textView = new TextView(getContext());
                    textView.setText("/" + pages);
                    textView.setTextColor((Color.parseColor("#000000")));
                    number = new TextView(getContext());
                    number.setText("1");
                    number.setTextColor((Color.parseColor("#FF0088")));
                    spotList_textLayout.addView(number);
                    spotList_textLayout.addView(textView);

                    for (int i = 0; i < pages; i++) {
                        fragments.add(new SpotListViewPagerFragment(i + 1));
                    }
                    adapter = new SpotListFragmentViewPagerAdapter(getChildFragmentManager(), viewPager,
                            fragments, getActivity());
                    viewPager.setAdapter(adapter);
                    viewPager.setOnPageChangeListener(new PageListener());
                    adapter.notifyDataSetChanged();

                    spotList_pageLayout.setVisibility(View.VISIBLE);
                    spotList_searchLayout.setVisibility(View.VISIBLE);

                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    };
}
