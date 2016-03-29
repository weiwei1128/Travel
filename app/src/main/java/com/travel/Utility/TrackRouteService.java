package com.travel.Utility;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.travel.Fragment.RecordTrackFragment;

public class TrackRouteService extends Service {
    public TrackRouteService() {
    }


    public static final String BROADCAST_ACTION_TIMER = "com.example.tracking.updateprogress";
    public static final String BROADCAST_ACTION = "com.example.trackroute.status";

    private static final String TAG = "TrackRouteService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;

    private Handler handler = new Handler();
    Long start_time = (long) 0;
    Long tempSpent = (long) 0;

    private GlobalVariable globalVariable;

    private Integer RoutesCounter;
    private Integer Track_no;
    private Integer record_status = 0;
    private Boolean isPause = false;

    private String track_title = "";

    @Override
    public void onCreate() {
        Log.d("3/10_", "TrackRouteService: onCreate");
        Log.i(TAG, "onCreate");

        registerReceiver(broadcastReceiver, new IntentFilter(RecordTrackFragment.TRACK_TO_SERVICE));
        registerReceiver(broadcastReceiver_timer, new IntentFilter(RecordTrackFragment.TIMER_TO_SERVICE));
        globalVariable = (GlobalVariable) getApplicationContext();

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
        Log.d("3/10_", "TrackRouteService: onStartCommand");
        if (intent != null) {
            //record_start_boolean = intent.getBooleanExtra("isStart", false);
            start_time = intent.getLongExtra("start", 0);
            record_status = intent.getIntExtra("record_status", 0);
            RoutesCounter = intent.getIntExtra("routesCounter", 1);
            Track_no = intent.getIntExtra("track_no", 1);
            handler.postDelayed(count, 1000);
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
            Log.d("3/10_", "Latitude " + Latitude);
            Log.d("3/10_", "Longitude " + Longitude);

            // 先廣播告知 if status = 0 由receiver寫
            if (!isPause) {
                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra("record_status", record_status);
                intent.putExtra("routesCounter", RoutesCounter);
                intent.putExtra("track_no", Track_no);
                intent.putExtra("track_lat", Latitude);
                intent.putExtra("track_lng", Longitude);
                intent.putExtra("track_start", record_status);
                intent.putExtra("track_title", track_title);
                sendBroadcast(intent);
            }

            if (record_status == 1) {
                TraceOfRoute(Latitude, Longitude);
                Log.d("3/10_", "TraceOfRoute");
            } else if (record_status == 0) {
                Log.e("3/10_", "Call stop TrackRouteService");
                stopSelf();
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
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

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("3/10_", "TrackRouteService: onDestroy");
        handler.removeCallbacks(count);

        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        if (broadcastReceiver_timer != null)
            unregisterReceiver(broadcastReceiver_timer);

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
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
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private Runnable count = new Runnable() {
        @Override
        public void run() {
            Long now = System.currentTimeMillis();
            Long spent = now - start_time + tempSpent;
            //Log.e("3/16", "--------------");
            //Log.e("3/16", "總時間：" + ((spent / 1000) / 60) + "分" + ((spent / 1000) % 60) + "秒");
            if (record_status == 1) {
                //send for UI update
                Intent intent = new Intent(BROADCAST_ACTION_TIMER);
                intent.putExtra("record_status", 1);
                intent.putExtra("spent", spent);
                sendBroadcast(intent);
                handler.postDelayed(count, 1000);
            } else if (record_status == 2) {
                //send for UI update
                Intent intent = new Intent(BROADCAST_ACTION_TIMER);
                intent.putExtra("record_status", 2);
                intent.putExtra("spent", spent);
                sendBroadcast(intent);
                handler.removeCallbacks(count);
            }
        }
    };

    // 紀錄軌跡到DB
    private void TraceOfRoute(Double Latitude, Double Longitude) {
        // if status = 0 由這裡 write to DB
        if (record_status == 1) {
            DataBaseHelper helper = DataBaseHelper.getmInstance(getApplicationContext());
            SQLiteDatabase database = helper.getWritableDatabase();
            Cursor trackRoute_cursor = database.query("trackRoute",
                    new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                            "track_start", "track_title", "track_totaltime", "track_completetime"},
                    null, null, null, null, null);
            if (trackRoute_cursor != null) {

                ContentValues cv = new ContentValues();
                cv.put("routesCounter", RoutesCounter);
                cv.put("track_no", Track_no);
                cv.put("track_lat", Latitude);
                cv.put("track_lng", Longitude);
                cv.put("track_start", 1);
                long result = database.insert("trackRoute", null, cv);
                Log.d("3/10_軌跡紀錄", result + " = DB INSERT RC:" + RoutesCounter
                        + " no:" + Track_no + " 座標 " + Latitude + "," + Longitude
                        + " status " + record_status);
            }
            trackRoute_cursor.close();
        }
    }

    private BroadcastReceiver broadcastReceiver_timer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                record_status = intent.getIntExtra("record_status", 1);
                if (record_status == 1) {
                    RoutesCounter = intent.getIntExtra("routesCounter", RoutesCounter);
                    Track_no = intent.getIntExtra("track_no", Track_no);
                    start_time = intent.getLongExtra("start", 0);
                    tempSpent = intent.getLongExtra("spent", 0);
                    isPause = intent.getBooleanExtra("isPause", false);
                    handler.postDelayed(count, 1000);
                    Log.d("3/18_Service", "BroadcastReceiver: start_time "
                            + start_time + " tempSpent " + tempSpent);
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                record_status = intent.getIntExtra("record_status", 1);
                if (record_status == 1) {
                    RoutesCounter = intent.getIntExtra("routesCounter", RoutesCounter);
                    Track_no = intent.getIntExtra("track_no", Track_no);
                } else if (record_status == 2) {
                    isPause = intent.getBooleanExtra("isPause", false);
                } else {
                    isPause = intent.getBooleanExtra("isPause", false);
                    track_title = intent.getStringExtra("track_title");
                }
            }
        }
    };
}
