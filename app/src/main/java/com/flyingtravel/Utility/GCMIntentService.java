package com.flyingtravel.Utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushMessageReceiver;
import com.flyingtravel.Activity.LoginActivity;
import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.google.android.gcm.GCMBaseIntentService;

import java.util.List;

/**
 * Created by wei on 2016/5/12.
 */
public class GCMIntentService extends PushMessageReceiver{

    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        Log.e("5.23","======"+errorCode+" user id: "+userId+" appid:"+appid+" channelid:"+channelId+" request id:"+requestId);
        updateContent(context,channelId);
    }

    @Override
    public void onUnbind(Context context, int i, String s) {

    }

    @Override
    public void onSetTags(Context context, int i, List<String> list, List<String> list1, String s) {

    }

    @Override
    public void onDelTags(Context context, int i, List<String> list, List<String> list1, String s) {

    }

    @Override
    public void onListTags(Context context, int i, List<String> list, String s) {

    }

    @Override
    public void onMessage(Context context, String s, String s1) {

    }

    @Override
    public void onNotificationClicked(Context context, String s, String s1, String s2) {

    }

    @Override
    public void onNotificationArrived(Context context, String s, String s1, String s2) {

    }
    private void updateContent(Context context, String content) {
        Log.e("5.23", "updateContent");
        //String logText = "" + Utils.logStringCache;

//        if (!logText.equals("")) {
//            logText += "\n";
//        }

//        SimpleDateFormat sDateFormat = new SimpleDateFormat("HH-mm-ss");
//        logText += sDateFormat.format(new Date()) + ": ";
//        logText += content;

        //Utils.logStringCache = logText;

//        Intent intent = new Intent();
//        intent.putExtra("result", content);
//        intent.setClass(context.getApplicationContext(), LoginActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.getApplicationContext().startActivity(intent);
    }
}
