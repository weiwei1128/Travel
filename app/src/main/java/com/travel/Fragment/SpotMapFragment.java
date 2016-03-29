package com.travel.Fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.travel.Utility.GlobalVariable;
import com.travel.R;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.GetSpotsNSort;
import com.travel.Utility.LoadApiService;

import java.util.ArrayList;

public class SpotMapFragment extends Fragment implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = SpotMapFragment.class.getSimpleName();
    private static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    //private static final String ARG_PARAM2 = "param2";
    private String mFragmentName;
    //private String mParam2;

    private MapView mapView;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 3;       // 5 meters

    private GlobalVariable globalVariable;

    private ProgressDialog mProgressDialog;

    private Location CurrentLocation;
    private Marker CurrentMarker;

    private Bitmap MarkerIcon;

    public SpotMapFragment() {
        // Required empty public constructor
    }

    public static SpotMapFragment newInstance(String fragementName) {
        SpotMapFragment fragment = new SpotMapFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_NAME, fragementName);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragmentName = getArguments().getString(FRAGMENT_NAME);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
        globalVariable = (GlobalVariable) getActivity().getApplicationContext();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(LoadApiService.BROADCAST_ACTION));

        mProgressDialog = new ProgressDialog(getActivity());
        MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 5 seconds, in milliseconds
                .setFastestInterval(FATEST_INTERVAL) // 1 second, in milliseconds
                .setSmallestDisplacement(DISPLACEMENT);

        if (!globalVariable.isAPILoaded) {
            Log.e("3/23_", "API is not ready");
            mProgressDialog.setMessage("景點資料龐大，載入中...");
            mProgressDialog.show();
        } else {
            if (globalVariable.MarkerOptionsArray.isEmpty()) {
                Log.e("3/23_", "Marker is not ready");
                mProgressDialog.setMessage("景點Marker載入中...");
                mProgressDialog.show();
                // Get Marker Info
                new GetMarkerInfo(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                if (CurrentLocation != null) {
                    Log.e("3/23_讀到位置", "事先Sort");
                    if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
                        new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                                CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spot_map, container, false);

        mapView = (MapView) rootView.findViewById(R.id.SpotMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();         // needed to get the map to display immediately

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMap = mapView.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        return rootView;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        if (!globalVariable.MarkerOptionsArray.isEmpty()) {
            if(MarkerIcon == null)
                MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location, 10, 18);
            for (MarkerOptions markerOptions : globalVariable.MarkerOptionsArray) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon));
                mMap.addMarker(markerOptions);
            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates (mGoogleApiClient, this);
        }
        super.onPause();
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
    public void onDestroyView() {
        mapView.onDestroy();
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
        MarkerIcon.recycle();
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
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
            if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
                Log.e("3/23_MapsActivity", "事先Sort");
                new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                        CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            Toast.makeText(getActivity(), R.string.google_play_service_missing, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (CurrentLocation != location) {
            HandleNewLocation(CurrentLocation);
        }
    }

    private void HandleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        CurrentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (CurrentMarker == null) {
            // 移動地圖到目前的位置
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            CurrentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("I am here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            CurrentMarker.setPosition(latLng);
        }

        DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
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
        helper.close();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                globalVariable.isAPILoaded = intent.getBooleanExtra("isAPILoaded", false);
                if (globalVariable.isAPILoaded) {
                    Log.e("3/23_", "Receive Broadcast: APILoaded");
                    if (globalVariable.MarkerOptionsArray.isEmpty()) {
                        // Get Marker Info
                        mProgressDialog.setMessage("景點Marker載入中...");
                        new GetMarkerInfo(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
            }
        }
    };

    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public class GetMarkerInfo extends AsyncTask<Void, Void, ArrayList<MarkerOptions>> {
        public static final String TAG = "GetMarkerInfo";
        Context mcontext;

        public GetMarkerInfo(Context context) {
            this.mcontext = context;
        }

        @Override
        protected void onPreExecute() {
            Log.d("3/23_GetMarkerInfo", "MarkerOption載入中...");
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
                DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
                SQLiteDatabase database = helper.getWritableDatabase();
                Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotName", "spotAdd",
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
                database.close();
                helper.close();
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
            mProgressDialog.dismiss();
            if (CurrentLocation != null) {
                Log.e("3/23_讀到位置", "事先Sort");
                if (globalVariable.isAPILoaded && globalVariable.SpotDataSorted.isEmpty()) {
                    new GetSpotsNSort(getActivity(), CurrentLocation.getLatitude(),
                            CurrentLocation.getLongitude()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            super.onPostExecute(markerOptionsArray);
        }
    }
}
