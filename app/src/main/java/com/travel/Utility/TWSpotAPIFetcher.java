package com.travel.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.travel.GlobalVariable;
import com.travel.SpotData;
import com.travel.SpotJson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Tinghua on 3/7/2016.
 */
public class TWSpotAPIFetcher extends AsyncTask<Void, Void, SpotJson> {

    public static final String TAG = "TWSpotAPIFetcher";
    public static final String SERVER_URL = "http://data.gov.tw/iisi/logaccess/2205?dataUrl=http://gis.taiwan.net.tw/XMLReleaseALL_public/scenic_spot_C_f.json&ndctype=JSON&ndcnid=7777";
    public static SpotJson.PostInfos Infos;
    public static Boolean isTWAPILoaded = false;

    Context mcontext;
    GlobalVariable globalVariable;

    public static final String BROADCAST_ACTION = "com.example.spotapi.status";

    public TWSpotAPIFetcher(Context context) {
        this.mcontext = context;
        globalVariable = (GlobalVariable) mcontext.getApplicationContext();

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected SpotJson doInBackground(Void... params) {
        Log.e("3/10_", "=========TWSpotAPIFetcher======doInBackground");
        SpotJson spotJson = null;
        try {
            //Create an HTTP client
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(SERVER_URL);

            //Perform the request and check the status code
            HttpResponse response = client.execute(post);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                Log.d("3/10_TWSpotJson", "response from server");

                JsonReader reader = new JsonReader(new InputStreamReader(content, "UTF-8"));
                Log.d("3/10_TWSpotJson", "JsonReader");
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat("M/d/yy hh:mm a");
                Gson gson = gsonBuilder.create();

                try {
                    spotJson = gson.fromJson(reader, SpotJson.class);
                    Log.d("3/10_TWSpotJson", "Json 2 Gson");
                } catch (Exception ex) {
                    Log.e(TAG, "Gson: Failed to parse JSON due to: " + ex);
                }
                content.close();
            } else {
                Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
        }

        Infos = spotJson.getInfos();
        Integer InfoLength = Infos.getInfo().length;
        Log.d("3/10_TWSpotJson", "景點個數: " + InfoLength.toString());
        for (Integer i = 0; i < InfoLength; i++) {
            globalVariable.SpotDataRaw.add(new SpotData(
                    Infos.getInfo()[i].getName(),
                    Double.valueOf(Infos.getInfo()[i].getPy()),
                    Double.valueOf(Infos.getInfo()[i].getPx()),
                    Infos.getInfo()[i].getAdd(),
                    Infos.getInfo()[i].getPicture1(),
                    Infos.getInfo()[i].getPicture2(),
                    Infos.getInfo()[i].getPicture3(),
                    Infos.getInfo()[i].getOpentime(),
                    Infos.getInfo()[i].getTicketinfo(),
                    Infos.getInfo()[i].getToldescribe()));
        }
        isTWAPILoaded = true;
        if (isTWAPILoaded) {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra("isTWAPILoaded", true);
            mcontext.sendBroadcast(intent);
        }
        Log.e("3/10_TWSpotJson", "Loaded to globalVariable");

        Log.e("3/10_", "=========TWSpotJson======Write to DB");
        DataBaseHelper helper = DataBaseHelper.getmInstance(mcontext);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotName", "spotAdd",
                        "spotLat", "spotLng", "picture1", "picture2","picture3",
                        "openTime", "ticketInfo", "infoDetail"},
                null, null, null, null, null);
        //Integer InfoLength = Infos.getInfo().length;
        if (spotDataRaw_cursor != null && InfoLength > 0) {
            if (spotDataRaw_cursor.getCount() == 0) {
                for (Integer i = 0; i < InfoLength; i++) {
                    ContentValues cv = new ContentValues();
                    cv.put("spotName", Infos.getInfo()[i].getName());
                    cv.put("spotAdd", Infos.getInfo()[i].getAdd());
                    cv.put("spotLat", Double.valueOf(Infos.getInfo()[i].getPy()));
                    cv.put("spotLng", Double.valueOf(Infos.getInfo()[i].getPx()));
                    cv.put("picture1", Infos.getInfo()[i].getPicture1());
                    cv.put("picture2", Infos.getInfo()[i].getPicture2());
                    cv.put("picture3", Infos.getInfo()[i].getPicture3());
                    cv.put("openTime", Infos.getInfo()[i].getOpentime());
                    cv.put("ticketInfo", Infos.getInfo()[i].getTicketinfo());
                    cv.put("infoDetail", Infos.getInfo()[i].getToldescribe());
                    long result = database.insert("spotDataRaw", null, cv);
                    Log.d("3/8_沒有重複資料", result + " = DB INSERT " + i + " spotName " + Infos.getInfo()[i].getName());
                }
            } else {
                for (Integer i = 0; i < InfoLength; i++) {
                    Cursor spotDataRaw_dul = database.query(true, "spotDataRaw", new String[]{"spotName", "spotAdd",
                                    "spotLat", "spotLng", "picture1", "picture2","picture3",
                                    "openTime", "ticketInfo", "infoDetail"},
                            "spotName=\"" + Infos.getInfo()[i].getName()+ "\"", null, null, null, null, null);
                    if (spotDataRaw_dul != null && spotDataRaw_dul.getCount() != 0) {
                        spotDataRaw_dul.moveToFirst();
                        Log.e("3/8", "有重複的資料!" + i + "title: " + spotDataRaw_dul.getString(1));
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("spotName", Infos.getInfo()[i].getName());
                        cv.put("spotAdd", Infos.getInfo()[i].getAdd());
                        cv.put("spotLat", Double.valueOf(Infos.getInfo()[i].getPy()));
                        cv.put("spotLng", Double.valueOf(Infos.getInfo()[i].getPx()));
                        cv.put("picture1", Infos.getInfo()[i].getPicture1());
                        cv.put("picture2", Infos.getInfo()[i].getPicture2());
                        cv.put("picture3", Infos.getInfo()[i].getPicture3());
                        cv.put("openTime", Infos.getInfo()[i].getOpentime());
                        cv.put("ticketInfo", Infos.getInfo()[i].getTicketinfo());
                        cv.put("infoDetail", Infos.getInfo()[i].getToldescribe());
                        long result = database.insert("spotDataRaw", null, cv);
                        Log.d("3/8_新增過資料", result + " = DB INSERT " + i + "spotName " + Infos.getInfo()[i].getName());
                    }
                    if(spotDataRaw_dul!=null)
                        spotDataRaw_dul.close();
                }
            }
        }
        if (spotDataRaw_cursor != null) {
            spotDataRaw_cursor.close();
        }
        return spotJson;
    }

    protected void onPostExecute(SpotJson spotJson) {
        if (spotJson != null) {
            Log.e("3/10_TWSpotAPIFetcher", "DONE");
        }
        super.onPostExecute(spotJson);
    }
}
