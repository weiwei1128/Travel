package com.travel.Utility;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.travel.GlobalVariable;
import com.travel.SpotData;

/**
 * Created by Hua on 2016/3/11.
 * //要記得註冊service
 * //
 */
public class LoadApiService extends Service {
    Context context;
    GlobalVariable globalVariable;

    public LoadApiService() {
    }

    @Override
    public void onCreate() {
        context = this.getBaseContext();
        globalVariable = (GlobalVariable) context.getApplicationContext();
        registerReceiver(broadcastReceiver, new IntentFilter(TWSpotAPIFetcher.BROADCAST_ACTION));
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("3/10_", "LoadApiService onDestroy");
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("3/10_", "LoadApiService onStartCommand");

        //利用 executeOnExecutor 確切執行非同步作業
        DataBaseHelper helper = new DataBaseHelper(getBaseContext());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotId", "spotName", "spotAdd",
                        "spotLat", "spotLng", "picture1", "picture2", "picture3",
                        "openTime", "ticketInfo", "infoDetail"},
                null, null, null, null, null);
        if (spotDataRaw_cursor != null) {
            TPESpotAPIFetcher tpeApi = new TPESpotAPIFetcher(context);
            TWSpotAPIFetcher twApi = new TWSpotAPIFetcher(context);
            if (spotDataRaw_cursor.getCount() == 0 || spotDataRaw_cursor.getCount() < 300) {
                // 到景點API抓景點資訊
                // TODO TW API放著在背景執行去動UI，結果好像就不了了之，沒有載入成功 哪招QAQ
                Log.e("3/10_", "*****Download API*****");
                if(tpeApi.getStatus() == AsyncTask.Status.PENDING) {
                    tpeApi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                if(twApi.getStatus() == AsyncTask.Status.PENDING) {
                    twApi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                //new TPESpotAPIFetcher(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                //new TWSpotAPIFetcher(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (spotDataRaw_cursor.getCount() > 300 && spotDataRaw_cursor.getCount() < 4600) {
                if(twApi.getStatus() == AsyncTask.Status.PENDING) {
                    twApi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } else if (spotDataRaw_cursor.getCount() > 4600) {
                if (globalVariable.SpotDataRaw.size() < 4600) {
                    globalVariable.SpotDataRaw.clear();
                    Log.e("3/10_", "API load to GlobalVariable");
                    while (spotDataRaw_cursor.moveToNext()) {
                        String Id = spotDataRaw_cursor.getString(0);
                        String Name = spotDataRaw_cursor.getString(1);
                        String Add = spotDataRaw_cursor.getString(2);
                        Double Latitude = spotDataRaw_cursor.getDouble(3);
                        Double Longitude = spotDataRaw_cursor.getDouble(4);
                        String Picture1 = spotDataRaw_cursor.getString(5);
                        String Picture2 = spotDataRaw_cursor.getString(6);
                        String Picture3 = spotDataRaw_cursor.getString(7);
                        String OpenTime = spotDataRaw_cursor.getString(8);
                        String TicketInfo = spotDataRaw_cursor.getString(9);
                        String InfoDetail = spotDataRaw_cursor.getString(10);
                        globalVariable.SpotDataRaw.add(new SpotData(Id, Name, Latitude, Longitude, Add,
                                Picture1, Picture2, Picture3, OpenTime,TicketInfo, InfoDetail));
                    }
                    globalVariable.isAPILoaded = true;
                    if (globalVariable.isAPILoaded) {
                        Log.e("3/10_", "API is Loaded Broadcast");
                        Intent APILoaded = new Intent(TWSpotAPIFetcher.BROADCAST_ACTION);
                        APILoaded.putExtra("isAPILoaded", true);
                        sendBroadcast(APILoaded);
                        Log.e("3/10_", "Call StopLoadApiService");
                        stopSelf();
                    }
                } else if (TWSpotAPIFetcher.isTWAPILoaded && TPESpotAPIFetcher.isTPEAPILoaded) {
                    Log.e("3/10_", "API is Loaded Broadcast");
                    Intent APILoaded = new Intent(TWSpotAPIFetcher.BROADCAST_ACTION);
                    APILoaded.putExtra("isAPILoaded", true);
                    sendBroadcast(APILoaded);
                    Log.e("3/10_", "Call StopLoadApiService");
                    stopSelf();
                }
            }
            spotDataRaw_cursor.close();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Boolean isTWAPILoaded = intent.getBooleanExtra("isTWAPILoaded", false);
                Boolean isTPEAPILoaded = intent.getBooleanExtra("isTPEAPILoaded", false);
                if (isTWAPILoaded && isTPEAPILoaded) {
                    globalVariable.isAPILoaded = true;
                    if (globalVariable.isAPILoaded) {
                        Log.e("3/10_", "API is Loaded Broadcast");
                        Intent APILoaded = new Intent(TWSpotAPIFetcher.BROADCAST_ACTION);
                        APILoaded.putExtra("isAPILoaded", true);
                        sendBroadcast(APILoaded);
                        Log.e("3/10_", "Call StopLoadApiService");
                        stopSelf();
                    }
                }
            }
        }
    };
}
