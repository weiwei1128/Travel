package com.travel.Utility;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RelativeLayout;

import com.travel.HomepageActivity;
import com.travel.MemberActivity;

import java.io.ByteArrayOutputStream;

import static android.content.Context.CONTEXT_RESTRICTED;

/**
 * Created by wei on 2016/1/30.
 */
public class Functions {
    public static void go(Boolean isBack, Activity activity, Context context, Class goclass, Bundle bundle) {

        if (isBack)
            activity.finish();
        else {
            Intent intent = new Intent();
            intent.setClass(context, goclass);
            if (bundle != null)
                intent.putExtras(bundle);
            activity.startActivity(intent);
//            if(activity.getLocalClassName().equals("HomepageActivity")&&(goclass==MemberActivity.class))
//                activity.finish();
//            if(activity.getLocalClassName().equals("MemberActivity")&&(goclass==HomepageActivity.class))
//                activity.finish();



        }
    }

    public static boolean isMyServiceRunning(Activity activity, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //----2.4 WEI----//
    // check if picture size too big
    public static Bitmap ScalePic(Bitmap bitmap) {
        Bitmap GetImage = null;
        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();
        int l = bitmap.getWidth();
        int i = bitmap.getHeight();
        while ((int) l > 500 || (int) i > 500) {
            l = (int) (l * 0.9);
            i = (int) (i * 0.9);
        }
        int newWidth = l;
        int newHeight = i;

        float scaleWidth = ((float) newWidth) / oldWidth;
        float scaleHeight = ((float) newHeight) / oldHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        GetImage = Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight,
                matrix, true);

        return GetImage;
    }

    public static RelativeLayout.LayoutParams RecordMemoItem() {

        int width = 0, height = 0;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);

        return layoutParams;
    }

    public static String getImageUri(Context inContext, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return (Uri.parse(path)).toString();
    }


}
