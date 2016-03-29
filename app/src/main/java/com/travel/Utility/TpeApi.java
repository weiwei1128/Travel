package com.travel.Utility;

import android.content.Context;
import android.os.AsyncTask;

import com.travel.Activity.Spot.SpotData;

import java.util.ArrayList;

/**
 * Created by Tinghua on 2016/3/23.
 */
public class TpeApi extends AsyncTask<String, Void, ArrayList<SpotData>> {
    public static final String TAG = "TPESpotAPIFetcher";
    public static final String SERVER_URL = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5";
    public static Boolean isTPEAPILoaded = false;

    Context mcontext;
    GlobalVariable globalVariable;

    public TpeApi(Context context) {
        this.mcontext = context;
        globalVariable = (GlobalVariable) mcontext.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<SpotData> doInBackground(String... params) {
        return null;
    }
}
