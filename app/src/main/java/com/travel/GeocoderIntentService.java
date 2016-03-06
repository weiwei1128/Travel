package com.travel;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.util.List;
import java.util.Locale;

/**
 * Created by Tinghua on 2016/1/12.
 */
public class GeocoderIntentService extends IntentService {

    private static final String TAG = "GeocoderIntentService";
    private String errorMessage = "";

    public GeocoderIntentService() {
        super("GeocoderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("test", "Geocoder: onHandleIntent:");

        Geocoder geocoder = new Geocoder(this, Locale.TAIWAN);
        List<Address> addresses = null;
        String gRegion = "";
    /*    try {
            addresses = geocoder.getFromLocation(Lat, Lng, 1);

        } catch (IOException ioException) {
            errorMessage = "Can't get Address!";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "Invalid Latitude or Longitude Used";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() + ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }*/

        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No Address returned!";
            }
    //        deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage, null);
        } else {
            gRegion = addresses.get(0).getAdminArea();
        }
    }
/*
    private void deliverResultToReceiver(int resultCode, String message, Address address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RESULT_ADDRESS, address);
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }*/
}
