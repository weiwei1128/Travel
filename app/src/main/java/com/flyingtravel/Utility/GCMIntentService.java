package com.flyingtravel.Utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flyingtravel.HomepageActivity;
import com.flyingtravel.R;
import com.google.android.gcm.GCMBaseIntentService;

/**
 * Created by wei on 2016/5/12.
 */
public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super("241768189228");
    }

    private static void generateNotification(Context context,String message){
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        Notification notification = new Notification(R.drawable.icon_512, message, when);
        //這裡可以設定推播通知的icon
        String title = "Hunger TV";
        //這裡可以設定推播通知的標題
        Intent notificationIntent = new Intent(context, HomepageActivity.class);
        //這裡可以設定當點選推播通知的時候要開起哪支程式
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setContentIntent(intent);
        builder.setWhen(1000);
        Notification notification1 = builder.getNotification();
        notificationManager.notify(0, notification1);
    }
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d("GCM", "RECIEVED A MESSAGE");
        // Get the data from intent and send to notificaion bar
        generateNotification(context, intent.getStringExtra("message"));
    }

    @Override
    protected void onError(Context context, String s) {

    }

    @Override
    protected void onRegistered(Context context, String s) {

    }

    @Override
    protected void onUnregistered(Context context, String s) {

    }
}
