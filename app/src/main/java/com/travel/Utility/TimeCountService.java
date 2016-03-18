package com.travel.Utility;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


/**
 * Created by wei on 2016/2/4.
 */
public class TimeCountService extends Service {

    private Handler handler = new Handler() {

    };

    Long start_time = (long) 0;
    //send for UI update
    public static final String BROADCAST_ACTION = "com.example.tracking.updateprogress";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable count = new Runnable() {

        @Override
        public void run() {
            Long now = System.currentTimeMillis();
            Long spent = now - start_time;
            Log.e("3/16", "--------------");
            Log.e("3/16", "總時間：" + ((spent / 1000) / 60) + "分" + ((spent / 1000) % 60) + "秒");
            //send for UI update
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra("spent", spent);
            sendBroadcast(intent);
            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public void onDestroy() {
        handler.removeCallbacks(count);
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if (intent != null)
            start_time = intent.getLongExtra("start", 0);
        handler.postDelayed(count, 1000);
        super.onStart(intent, startId);
    }

}
