package com.travel.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.travel.TPESpotJson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Tinghua on 3/7/2016.
 */
public class TPESpotAPIFetcher extends AsyncTask<Void, Void, TPESpotJson> {
    public static final String TAG = "TPESpotAPIFetcher";
    public static final String SERVER_URL = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5";
    public static TPESpotJson.PostResult Result;

    Context mcontext;

    public TPESpotAPIFetcher(Context context) {
        this.mcontext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected TPESpotJson doInBackground(Void... params) {
        Log.e("3.9_", "=========TPESpotJson======doInBackground");
        String JsonString = "";
        try {
            //Create an HTTP client
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(SERVER_URL);

            //Perform the request and check the status code
            HttpResponse response = client.execute(get);
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
        TPESpotJson spotJson = null;
        try {
            spotJson = gson.fromJson(JsonString, TPESpotJson.class);
        } catch (Exception ex) {
            Log.e(TAG, "JsonString: Failed to parse JSON due to: " + ex);
        }
        return spotJson;
    }

    protected void onPostExecute(TPESpotJson spotJson) {
        Result = spotJson.getResult();
        DataBaseHelper helper = new DataBaseHelper(mcontext);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor spotDataRaw_cursor = database.query("spotDataRaw", new String[]{"spotId", "spotName", "spotAdd",
                        "spotLat", "spotLng", "picture1", "picture2","picture3",
                        "openTime", "ticketInfo", "infoDetail"},
                null, null, null, null, null);
        Integer ResultsLength = Result.getResults().length;
        if (spotDataRaw_cursor != null && ResultsLength > 0) {
            if (spotDataRaw_cursor.getCount() == 0) {
                for (int i = 0; i < ResultsLength; i++) {
                    ContentValues cv = new ContentValues();
                    cv.put("spotId", i);
                    cv.put("spotName", Result.getResults()[i].getStitle());
                    cv.put("spotAdd", Result.getResults()[i].getAddress());
                    cv.put("spotLat", Double.valueOf(Result.getResults()[i].getLatitude()));
                    cv.put("spotLng", Double.valueOf(Result.getResults()[i].getLongitude()));

                    String ImgString = Result.getResults()[i].getFile();
                    int StringPosition1 = ImgString.indexOf("http", 2);
                    int StringPosition2 = ImgString.indexOf("http", StringPosition1+1);
                    int StringPosition3 = ImgString.indexOf("http", StringPosition2+1);
                    if (StringPosition1 > 0) {
                        String ImgString1 = ImgString.substring(0, StringPosition1);
                        cv.put("picture1", ImgString1);
                        if (StringPosition2 > 0 && StringPosition2 > StringPosition1) {
                            String ImgString2 = ImgString.substring(StringPosition1, StringPosition2);
                            cv.put("picture2", ImgString2);
                            if (StringPosition3 > 0 && StringPosition3 > StringPosition2) {
                                String ImgString3 = ImgString.substring(StringPosition2, StringPosition3);
                                cv.put("picture3", ImgString3);
                            }
                        }
                    }

                    cv.put("openTime", Result.getResults()[i].getMemoTime());
                    cv.put("ticketInfo", "");
                    cv.put("infoDetail", Result.getResults()[i].getXbody());
                    long result = database.insert("spotDataRaw", null, cv);
                    //Log.d("3/8_沒有重複資料", result + " = DB INSERT " + i + " spotName " + Result.getResults()[i].getStitle());
                }
            } else {
                for (int i = 0; i < ResultsLength; i++) {
                    Cursor spotDataRaw_dul = database.query(true, "spotDataRaw", new String[]{"spotId", "spotName", "spotAdd",
                                    "spotLat", "spotLng", "picture1", "picture2","picture3",
                                    "openTime", "ticketInfo", "infoDetail"},
                            "spotName=\"" + Result.getResults()[i].getStitle() + "\"", null, null, null, null, null);
                    if (spotDataRaw_dul != null && spotDataRaw_dul.getCount() != 0) {
                        spotDataRaw_dul.moveToFirst();
                        Log.e("3/8", "有重複的資料!" + i + "title: " + spotDataRaw_dul.getString(1));
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put("spotId", i);
                        cv.put("spotName", Result.getResults()[i].getStitle());
                        cv.put("spotAdd", Result.getResults()[i].getAddress());
                        cv.put("spotLat", Double.valueOf(Result.getResults()[i].getLatitude()));
                        cv.put("spotLng", Double.valueOf(Result.getResults()[i].getLongitude()));

                        String ImgString = Result.getResults()[i].getFile();
                        int StringPosition1 = ImgString.indexOf("http", 2);
                        int StringPosition2 = ImgString.indexOf("http", StringPosition1+1);
                        int StringPosition3 = ImgString.indexOf("http", StringPosition2+1);
                        if (StringPosition1 > 0) {
                            String ImgString1 = ImgString.substring(0, StringPosition1);
                            cv.put("picture1", ImgString1);
                            if (StringPosition2 > 0 && StringPosition2 > StringPosition1) {
                                String ImgString2 = ImgString.substring(StringPosition1, StringPosition2);
                                cv.put("picture2", ImgString2);
                                if (StringPosition3 > 0 && StringPosition3 > StringPosition2) {
                                    String ImgString3 = ImgString.substring(StringPosition2, StringPosition3);
                                    cv.put("picture3", ImgString3);
                                }
                            }
                        }

                        cv.put("openTime", Result.getResults()[i].getMemoTime());
                        cv.put("ticketInfo", "");
                        cv.put("infoDetail", Result.getResults()[i].getXbody());
                        long result = database.insert("spotDataRaw", null, cv);
                        //Log.d("3/8_新增過資料", result + " = DB INSERT " + i + "spotName " + Result.getResults()[i].getStitle());
                    }
                    if(spotDataRaw_dul!=null)
                        spotDataRaw_dul.close();
                }
            }
        }
        if (spotDataRaw_cursor != null)
            spotDataRaw_cursor.close();
        Log.e("3.9_TPESpotAPIFetcher", "DONE");
        super.onPostExecute(spotJson);
    }
}
