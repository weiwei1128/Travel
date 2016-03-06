package com.travel.Utility;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by wei on 2016/2/23.
 * //not run yet
 * //bind in Login
 */
public class HttpService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        JsonGoods a = new JsonGoods();
//        a.execute();
        return super.onStartCommand(intent, flags, startId);
    }
}
