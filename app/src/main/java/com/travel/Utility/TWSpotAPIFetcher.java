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
import com.travel.LocationService;
import com.travel.SpotJson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Tinghua on 3/7/2016.
 */
public class TWSpotAPIFetcher extends AsyncTask<Void, Void, SpotJson> {

    public static final String TAG = "TWSpotAPIFetcher";
    public static final String SERVER_URL = "http://data.gov.tw/iisi/logaccess/2205?dataUrl=http://gis.taiwan.net.tw/XMLReleaseALL_public/scenic_spot_C_f.json&ndctype=JSON&ndcnid=7777";
    public static SpotJson.PostInfos Infos;

    Context mcontext;

    public TWSpotAPIFetcher(Context context) {
        this.mcontext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected SpotJson doInBackground(Void... params) {
        Log.e("3.9_", "=========TWSpotAPIFetcher======doInBackground");
        String JsonString = "";
        try {
            //Create an HTTP client
            HttpClient client = new DefaultHttpClient();
            HttpPost post;
            post = new HttpPost(SERVER_URL);

            //Perform the request and check the status code
            HttpResponse response = client.execute(post);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

                //Read the server response and attempt to parse it as JSON
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                String line;
                while ((line = reader.readLine()) != null) {
                    JsonString += line;
                }
                content.close();
            } else {
                Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        Gson gson = gsonBuilder.create();
        SpotJson spotJson = null;
        try {
            spotJson = gson.fromJson(JsonString, SpotJson.class);
        } catch (Exception ex) {
            Log.e(TAG, "JsonString: Failed to parse JSON due to: " + ex);
        }
        return spotJson;
    }

    protected void onPostExecute(SpotJson spotJson) {
        Infos = spotJson.getInfos();
        DataBaseHelper helper = new DataBaseHelper(mcontext);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotId", "spotName", "spotAdd",
                        "spotLat", "spotLng", "picture1", "picture2","picture3",
                        "openTime", "ticketInfo", "infoDetail"},
                null, null, null, null, null);
        Integer InfoLength = Infos.getInfo().length;
        if (spotDataRaw_cursor != null && InfoLength > 0) {
            if (spotDataRaw_cursor.getCount() == 0) {
                for (int i = 0; i < InfoLength; i++) {
                    ContentValues cv = new ContentValues();
                    cv.put("spotId", i);
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
                    //Log.d("3/8_沒有重複資料", result + " = DB INSERT " + i + " spotName " + Infos.getInfo()[i].getName());
                }
            } else {
                for (int i = 0; i < InfoLength; i++) {
                    Cursor spotDataRaw_dul = database.query(true, "spotDataRaw", new String[]{"spotId", "spotName", "spotAdd",
                                    "spotLat", "spotLng", "picture1", "picture2","picture3",
                                    "openTime", "ticketInfo", "infoDetail"},
                            "spotName=\"" + Infos.getInfo()[i].getName()+ "\"", null, null, null, null, null);
                    if (spotDataRaw_dul != null && spotDataRaw_dul.getCount() != 0) {
                        spotDataRaw_dul.moveToFirst();
                        //Log.e("3/8", "有重複的資料!" + i + "title: " + spotDataRaw_dul.getString(1));
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
                        //Log.d("3/8_新增過資料", result + " = DB INSERT " + i + "spotName " + Infos.getInfo()[i].getName());
                    }
                    if(spotDataRaw_dul!=null)
                        spotDataRaw_dul.close();
                }
            }
        }
        if (spotDataRaw_cursor != null) {
            spotDataRaw_cursor.close();
        }
        Log.e("3.9_TWSpotAPIFetcher", "DONE");
        super.onPostExecute(spotJson);
    }
}
