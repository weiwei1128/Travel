package com.travel.Utility;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.travel.GlobalVariable;

import java.util.ArrayList;

public class TrackRouteService extends Service {

    public TrackRouteService() {
    }

    private static final String TAG = "TrackRouteService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;

    private Handler handler = new Handler();

    private GlobalVariable globalVariable;

    public static final String BROADCAST_ACTION = "com.example.trackroute.status";

    private Boolean isLocationChanged = false;

    private ArrayList<LatLng> TraceRoute;
    private Boolean record_start_boolean;
    private Integer RoutesCounter;
    private Integer Track_no;

    @Override
    public void onCreate() {
        Log.d("3.9_", "TrackRouteService: onCreate");
        Log.i(TAG, "onCreate");

        globalVariable = (GlobalVariable)getApplicationContext();

        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("3.9_", "TrackRouteService: onStartCommand");
        if (intent != null) {
            record_start_boolean = intent.getBooleanExtra("isStart", false);
            RoutesCounter = intent.getIntExtra("routesCounter", 1);
            Track_no = intent.getIntExtra("track_no", 1);
            Log.d("3.9_", "isStart: " + record_start_boolean);
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            final Double Latitude = mLastLocation.getLatitude();
            final Double Longitude = mLastLocation.getLongitude();
            Log.d("3.9_", "Latitude " + Latitude);
            Log.d("3.9_", "Longitude " + Longitude);

            // 加上軌跡
            if (record_start_boolean) {
                TraceOfRoute(Latitude, Longitude);
                Log.d("3.9_", "TraceOfRoute");
            }

        }

        @Override
        public void onProviderDisabled (String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("3.9_", "TrackRouteService: onDestroy");
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.i(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    // 紀錄軌跡
    private void TraceOfRoute(Double Latitude, Double Longitude) {
        DataBaseHelper helper = new DataBaseHelper(getApplicationContext());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng", "track_start"},
                null, null, null, null, null);
        if (trackRoute_cursor != null) {
            ContentValues cv = new ContentValues();
            cv.put("routesCounter", RoutesCounter);
            cv.put("track_no", Track_no);
            cv.put("track_lat", Latitude);
            cv.put("track_lng", Longitude);
            if (record_start_boolean) {
                cv.put("track_start", 1);
            } else {
                cv.put("track_start", 0);
            }
            long result = database.insert("trackRoute", null, cv);
            Log.d("3.9_軌跡紀錄", result + " = DB INSERT RC:" + RoutesCounter
                    + " no:" + Track_no + " 座標 " + Latitude + "," + Longitude);
            trackRoute_cursor.close();
        }

        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("isStart", record_start_boolean);
        intent.putExtra("routesCounter", RoutesCounter);
        intent.putExtra("track_no", Track_no);
        intent.putExtra("track_lat", Latitude);
        intent.putExtra("track_lng", Longitude);
        sendBroadcast(intent);
    }
}
