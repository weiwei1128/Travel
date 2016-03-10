package com.travel;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.GetSpotsNSort;

/**
 * Created by Tinghua on 2/29/2016.
 */
public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 2000;
    private static final float LOCATION_DISTANCE = 0;

    private Handler handler = new Handler();

    private Context context;
    private GlobalVariable globalVariable;
    private DataBaseHelper helper;
    private SQLiteDatabase database;

    public static final String BROADCAST_ACTION = "com.example.location.status";

    private Boolean isLocationChanged = false;

    @Override
    public void onCreate() {
        Log.d("3.9_", "LocationService: onCreate");
        helper = new DataBaseHelper(getApplicationContext());
        database = helper.getWritableDatabase();
        context = getApplicationContext();
        globalVariable = (GlobalVariable)getApplicationContext();

        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

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
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            isLocationChanged = true;
            Log.d(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            final Double Latitude = mLastLocation.getLatitude();
            final Double Longitude = mLastLocation.getLongitude();
            Log.d("3.9_", "Latitude " + Latitude);
            Log.d("3.9_", "Longitude " + Longitude);

            Cursor location_cursor = database.query("location",
                    new String[]{"CurrentLat", "CurrentLng"}, null, null, null, null, null);
            if (location_cursor != null) {
                if (location_cursor.getCount() == 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("CurrentLat", Latitude);
                    cv.put("CurrentLng", Longitude);
                    long result = database.insert("location", null, cv);
                    Log.d("3.9_新增位置", result + " = DB INSERT " + Latitude + " " + Longitude);
                    if (globalVariable.SpotDataSorted == null) {
                        new GetSpotsNSort(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("CurrentLat", Latitude);
                    cv.put("CurrentLng", Longitude);
                    long result = database.update("location", cv, "_ID=1", null);
                    Log.d("3.9_位置更新", result + " = DB INSERT " + Latitude + " " + Longitude);
                }
                location_cursor.close();
            }

            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra("isLocationChanged", isLocationChanged);
            intent.putExtra("Latitude", Latitude);
            intent.putExtra("Longitude", Longitude);
            sendBroadcast(intent);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isLocationChanged = false;
                    Log.d("3.9_", "isLocationChanged: false");
                }
            }, 1000);
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
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
        super.onDestroy();
    }

    private void initializeLocationManager() {
        Log.i(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
        }
    }
}

