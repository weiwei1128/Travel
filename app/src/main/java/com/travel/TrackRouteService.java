package com.travel;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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

    @Override
    public void onCreate()
    {
        Log.d("3/4", "LocationService: onCreate");
        Log.i(TAG, "onCreate");
        Toast.makeText(this, "Service: onCreate", Toast.LENGTH_SHORT).show();
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
        Log.d("3/4", "LocationService: onStartCommand");
        Log.i(TAG, "onStartCommand");
        //Toast.makeText(this, "Service: onStartCommand", Toast.LENGTH_SHORT).show();
/*
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("3/4", "LocationService: Handler: postDelayed");
                Log.d(TAG, "Handler: postDelayed");
            }
        }, 10000);
*/
        super.onStartCommand(intent, flags, startId);
        Log.d("3/4", "LocationService: Handler: START_STICKY");
        Log.d(TAG, "Handler: START_STICKY");
        return START_STICKY;
    }

    private class LocationListener implements android.location.LocationListener
    {
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
            Log.d("3/4", "LocationService: onLocationChanged");
            Log.d(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            final Double Latitude = mLastLocation.getLatitude();
            final Double Longitude = mLastLocation.getLongitude();
            Log.d("3/4", "Latitude " + Latitude);
            Log.d("3/4", "Longitude " + Longitude);

            if (globalVariable.Latitude == null) {
                globalVariable.Latitude = Latitude;
                Log.d("3/4", "globalVariable " + Latitude);
            }
            if (globalVariable.Longitude == null) {
                globalVariable.Longitude = Longitude;
                Log.d("3/4", "globalVariable " + Longitude);
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(BROADCAST_ACTION);
                    intent.putExtra("isLocationChanged", isLocationChanged);
                    intent.putExtra("Latitude", Latitude);
                    intent.putExtra("Longitude", Longitude);
                    sendBroadcast(intent);

                    Log.d("3/4", "isLocationChanged: false");
                    isLocationChanged = false;
                }
            }, 1000);

            Log.d("3/4", "LocationService: Handler: postDelayed");
            Log.d(TAG, "Handler: postDelayed");
        }

        @Override
        public void onProviderDisabled (String provider)
        {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
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
    public void onDestroy()
    {
        Log.d("3/4", "LocationService: onDestroy");
        Log.i(TAG, "onDestroy");
        Toast.makeText(this, "Service: onDestroy", Toast.LENGTH_SHORT).show();
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
}
