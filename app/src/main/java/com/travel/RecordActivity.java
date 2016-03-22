package com.travel;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;
import com.travel.Utility.TrackRouteService;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordActivity extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static final String TAG = RecordActivity.class.getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 3;       // 3 meters

    private Location currentLocation;
    private Marker currentMarker;

    private Button record_travel_button;
    private Button record_Memo_button;

    private Integer RoutesCounter = 1;
    private Integer Track_no = 1;
    private Integer record_status = 0;
    private Long tempSpent = 0L;

    private Double CurrentLatitude;
    private Double CurrentLongitude;
    private LatLng CurrentLatlng;

    private Bitmap MarkerIcon;
    private ArrayList<LatLng> TraceRoute;

    //3.10
    final int REQUEST_LOCATION = 2;

    //3.13
    final int REQUEST_CAMERA = 99;
    final int SELECT_FILE = 98;

    //====1.28 WEI====new UI //
    private Dialog spotDialog;
    private ScrollView dialog_scrollview;
    private LinearLayout record_start_layout, record_spot_layout;
    private LinearLayout dialog_choose_layout, dialog_confirm_layout, title_layout, content_layout;
    private RelativeLayout dialog_relativeLayout;
    private ImageView BackImg, record_completeImg, record_start_img, record_spot_img, dialog_img;
    private TextView time_text, record_start_text, record_spot_text;
    private TextView dialog_header_text, title_textView, title_confirmTextView, content_textView;
    private EditText title_editText, content_editText;

    final Long[] starttime = new Long[1];
    //----for upload image//
    Bitmap memo_img;
    long inDB = 0;

    public static final String TIMER_TO_SERVICE = "com.example.tracking.restartcount";
    public static final String TRACK_TO_SERVICE = "com.example.tracking.restarttrack";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_activity);

        Log.d("3/4", "RecordActivity: onCreate");

        BackImg = (ImageView) findViewById(R.id.record_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        record_travel_button = (Button) findViewById(R.id.record_travel_button);
        record_travel_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        record_Memo_button = (Button) findViewById(R.id.record_memo_button);
        record_Memo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false,RecordActivity.this, RecordActivity.this, RecordMemoActivity.class, null);
            }
        });

        //====1.28 WEI====//
        record_start_layout = (LinearLayout) findViewById(R.id.record_start_layout);
        record_start_img = (ImageView) findViewById(R.id.record_start_img);
        record_start_text = (TextView) findViewById(R.id.record_start_text);

        record_spot_layout = (LinearLayout) findViewById(R.id.record_spot_layout);
        record_spot_img = (ImageView) findViewById(R.id.record_spot_img);
        record_spot_text = (TextView) findViewById(R.id.record_spot_text);

        time_text = (TextView) findViewById(R.id.record_test_text);
        time_text.setVisibility(View.INVISIBLE);

        record_completeImg = (ImageView) findViewById(R.id.record_completeImg);
        record_completeImg.setVisibility(View.INVISIBLE);

        spotDialog = new Dialog(RecordActivity.this);
        spotDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        spotDialog.setContentView(R.layout.record_memo_dialog);

        dialog_choose_layout = (LinearLayout) spotDialog.findViewById(R.id.dialog_choose_layout);
        dialog_header_text = (TextView) spotDialog.findViewById(R.id.dialog_header_text);
        title_layout = (LinearLayout) spotDialog.findViewById(R.id.title_layout);
        title_textView = (TextView) spotDialog.findViewById(R.id.title_TextView);
        title_editText = (EditText) spotDialog.findViewById(R.id.title_editText);
        title_confirmTextView = (TextView) spotDialog.findViewById(R.id.title_confirmTextView);

        dialog_scrollview = (ScrollView) spotDialog.findViewById(R.id.dialog_scrollview);
        dialog_relativeLayout = (RelativeLayout) spotDialog.findViewById(R.id.dialog_relativeLayout);
        dialog_img = (ImageView) spotDialog.findViewById(R.id.dialog_img);
        content_layout = (LinearLayout) spotDialog.findViewById(R.id.content_layout);
        content_textView = (TextView) spotDialog.findViewById(R.id.content_TextView);
        content_editText = (EditText) spotDialog.findViewById(R.id.content_editText);

        dialog_confirm_layout = (LinearLayout) spotDialog.findViewById(R.id.dialog_confirm_layout);
        ImageView write = (ImageView) spotDialog.findViewById(R.id.dialog_write_img);
        ImageView camera = (ImageView) spotDialog.findViewById(R.id.dialog_camera_img);
        ImageView leave = (ImageView) spotDialog.findViewById(R.id.dialog_leave_img);

        // TODO 完成整段旅程
        record_completeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset內容
                title_editText.setText("");
                // 輸入這趟旅程的標題
                dialog_header_text.setText("請輸入這趟旅程的標題");
                title_layout.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams otelParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                otelParams.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                title_layout.setLayoutParams(otelParams);

                // 隱藏View
                dialog_scrollview.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams otelParams1 = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0);
                otelParams1.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                dialog_scrollview.setLayoutParams(otelParams1);

                dialog_choose_layout.setVisibility(View.INVISIBLE);
                dialog_confirm_layout.setVisibility(View.INVISIBLE);
                // 隱藏View

                // 按下確認
                title_confirmTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Track_no = 1;
                        RoutesCounter++;
                        time_text.setVisibility(View.INVISIBLE);
                        record_completeImg.setVisibility(View.INVISIBLE);

                        record_start_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        record_start_text.setTextColor(Color.parseColor("#555555"));
                        record_start_text.setText("開始紀錄");
                        record_start_img.setImageResource(R.drawable.ic_play_light);
                        record_status = 0;

                        if (Functions.isMyServiceRunning(RecordActivity.this, TrackRouteService.class)) {
                            Intent intent_Trace = new Intent(TRACK_TO_SERVICE);
                            intent_Trace.putExtra("record_status", 0);
                            intent_Trace.putExtra("track_title", title_editText.getText().toString());
                            sendBroadcast(intent_Trace);
                        }
                        spotDialog.dismiss();
                        record_spot_layout.setClickable(false);
                    }
                });
                spotDialog.show();
            }
        });

        //TODO 上傳照片或文字檔
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_img.getVisibility() == dialog_img.VISIBLE) {
                    dialog_img.setImageBitmap(null);
                    dialog_img.setVisibility(View.INVISIBLE);
                }
                dialog_scrollview.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams otelParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 500);
                otelParams.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                dialog_scrollview.setLayoutParams(otelParams);

                dialog_relativeLayout.setVisibility(View.VISIBLE);
                content_layout.setVisibility(View.VISIBLE);

                dialog_choose_layout.setVisibility(View.INVISIBLE);

                dialog_confirm_layout.setVisibility(View.VISIBLE);
                dialog_confirm_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // save content to DB
                        DataBaseHelper helper = new DataBaseHelper(RecordActivity.this);
                        SQLiteDatabase db = helper.getReadableDatabase();
                        Cursor memo_cursor = db.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                                        "memo_content", "memo_img", "memo_latlng", "memo_time"},
                                null, null, null, null, null);
                        if (memo_cursor != null) {
                            ContentValues cv = new ContentValues();
                            cv.put("memo_routesCounter", RoutesCounter);
                            cv.put("memo_trackNo", Track_no);
                            cv.put("memo_content", content_editText.getText().toString());
                            if (CurrentLatlng != null) {
                                cv.put("memo_latlng", CurrentLatlng.toString());
                            }
                            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                            Date date = new Date();
                            String dateString = fmt.format(date);
                            cv.put("memo_time", dateString);
                            inDB = db.insert("travelmemo", null, cv);
                            Log.e("3/13_", "DB insert content" + inDB + " content:"
                                    + content_editText.getText().toString() + " Addtime " + dateString);

                            if (inDB != -1) {
                                if (spotDialog.isShowing()) {
                                    if (content_layout.getVisibility() == content_layout.VISIBLE) {
                                        content_layout.setVisibility(View.INVISIBLE);
                                        content_editText.setText("");
                                    }
                                    spotDialog.dismiss();
                                }
                                Toast.makeText(RecordActivity.this, "心得已上傳！", Toast.LENGTH_SHORT).show();
                            }
                            memo_cursor.close();
                            db.close();
                            helper.close();
                            if (spotDialog.isShowing()) {
                                spotDialog.dismiss();
                            }
                        }
                    }
                });
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("3/13_", "CAMERA");
                final CharSequence[] items = { "相機", "相簿", "取消" };
                AlertDialog.Builder builder = new AlertDialog.Builder(RecordActivity.this);
                builder.setTitle("上傳照片");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("相機")) {
                            Intent intent_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent_camera, REQUEST_CAMERA);
                        } else if (items[item].equals("相簿")) {
                            Intent intent_photo = new Intent(Intent.ACTION_GET_CONTENT);
                            intent_photo.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent_photo, "選擇檔案"),
                                    SELECT_FILE);
                        } else if (items[item].equals("取消")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spotDialog.isShowing()) {
                    if (content_layout.getVisibility() == content_layout.VISIBLE) {
                        content_layout.setVisibility(View.INVISIBLE);
                        content_editText.setText("");
                    }
                    if (memo_img != null) {
                        dialog_img.setImageBitmap(null);
                    }
                    spotDialog.dismiss();
                }
            }
        });

        record_spot_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!spotDialog.isShowing()) {
                    // reset內容
                    dialog_header_text.setText("是否要記錄景點心得或上傳照片？");
                    dialog_img.setImageBitmap(null);
                    content_editText.setText("");

                    // 隱藏View
                    title_layout.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams otelParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    otelParams.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                    title_layout.setLayoutParams(otelParams);

                    dialog_scrollview.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams otelParams1 = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    otelParams1.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
                    dialog_scrollview.setLayoutParams(otelParams1);

                    dialog_choose_layout.setVisibility(View.VISIBLE);
                    dialog_confirm_layout.setVisibility(View.INVISIBLE);
                    // 隱藏View

                    spotDialog.show();
                }
            }
        });
        record_spot_layout.setClickable(false);

        record_start_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (record_status == 1) {
                    record_start_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    record_start_text.setTextColor(Color.parseColor("#555555"));
                    record_start_text.setText("開始紀錄");
                    record_start_img.setImageResource(R.drawable.ic_play_light);
                    record_status = 2;

                    if (Functions.isMyServiceRunning(RecordActivity.this, TrackRouteService.class)) {
                        Intent intent_Trace = new Intent(TRACK_TO_SERVICE);
                        intent_Trace.putExtra("record_status", 2);
                        sendBroadcast(intent_Trace);
                    }
                } else {
                    record_spot_layout.setClickable(true);
                    record_start_layout.setBackgroundColor(Color.parseColor("#5599FF"));
                    record_start_text.setTextColor(Color.parseColor("#FFFFFF"));
                    record_start_text.setText("停止紀錄");
                    record_start_img.performClick();
                    record_start_img.setImageResource(R.drawable.record_selected_pause);
                    record_status = 1;
                    //====1.29
                    time_text.setVisibility(View.VISIBLE);
                    record_completeImg.setVisibility(View.VISIBLE);
                    starttime[0] = System.currentTimeMillis();
                    //----2.4

                    DataBaseHelper helper = new DataBaseHelper(getApplicationContext());
                    SQLiteDatabase database = helper.getWritableDatabase();
                    Cursor trackRoute_cursor = database.query("trackRoute",
                            new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                                    "track_start", "track_title", "track_totaltime", "track_completetime"},
                            null, null, null, null, null);
                    if (trackRoute_cursor != null) {
                        if (trackRoute_cursor.getCount() != 0) {
                            trackRoute_cursor.moveToLast();
                            Integer routesCounter = trackRoute_cursor.getInt(0);
                            if (routesCounter == RoutesCounter) {
                                Track_no++;
                            }
                        }
                        trackRoute_cursor.close();
                    }
                    database.close();
                    helper.close();

                    if (!Functions.isMyServiceRunning(RecordActivity.this, TrackRouteService.class)) {
                        Intent intent_Trace = new Intent(RecordActivity.this, TrackRouteService.class);
                        intent_Trace.putExtra("record_status", 1);
                        intent_Trace.putExtra("start", starttime[0]);
                        intent_Trace.putExtra("routesCounter", RoutesCounter);
                        intent_Trace.putExtra("track_no", Track_no);
                        startService(intent_Trace);
                    } else {
                        Intent intent_Trace = new Intent(TIMER_TO_SERVICE);
                        intent_Trace.putExtra("record_status", 1);
                        intent_Trace.putExtra("start", starttime[0]);
                        intent_Trace.putExtra("spent", tempSpent);
                        intent_Trace.putExtra("routesCounter", RoutesCounter);
                        intent_Trace.putExtra("track_no", Track_no);
                        intent_Trace.putExtra("isPause", false);
                        sendBroadcast(intent_Trace);
                        tempSpent = 0L;
                    }
                }
            }
        });
        //----2.4 WEI----//
        //For doing Update count UI
        registerReceiver(broadcastReceiver_timer, new IntentFilter(TrackRouteService.BROADCAST_ACTION_TIMER));
        registerReceiver(broadcastReceiver, new IntentFilter(TrackRouteService.BROADCAST_ACTION));
        //----2.4 WEI----//

        BitmapDrawable BitmapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.location);
        MarkerIcon = Bitmap.createScaledBitmap(BitmapDraw.getBitmap(), 60, 90, false);

        setUpMapIfNeeded();

        // retrieve trackRoute from DB
        DataBaseHelper helper = new DataBaseHelper(getApplicationContext());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter","track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                Integer routesCounter;
                Integer track_no;
                Double track_lat;
                Double track_lng;
                Integer track_start;
                while (trackRoute_cursor.moveToNext()) {
                    routesCounter = trackRoute_cursor.getInt(0);
                    track_no = trackRoute_cursor.getInt(1);
                    track_lat = trackRoute_cursor.getDouble(2);
                    track_lng = trackRoute_cursor.getDouble(3);
                    //track_start = trackRoute_cursor.getInt(4);
                    // TODO track_start=0:最後一筆 1:還在紀錄
                    LatLng track_latLng = new LatLng(track_lat, track_lng);
                    if (!(routesCounter > 1 && track_no == 1)) {
                        DisplayRoute(track_latLng);
                    }
                }
                trackRoute_cursor.moveToLast();
                track_start = trackRoute_cursor.getInt(4);
                if (track_start == 0) {
                    RoutesCounter = trackRoute_cursor.getInt(0) + 1;
                    Track_no = 1;
                } else {
                    RoutesCounter = trackRoute_cursor.getInt(0);
                    Track_no = trackRoute_cursor.getInt(1);
                }


            }
            trackRoute_cursor.close();
        }
        database.close();
        helper.close();
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        if (broadcastReceiver_timer != null)
            unregisterReceiver(broadcastReceiver_timer);
        MarkerIcon.recycle();
        System.gc();
        Log.d("3/10_", "RecordActivity: onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("3/10_", "RecordActivity: onResume");
        setUpMapIfNeeded();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除位置請求服務
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 移除Google API用戶端連線
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            LoadtoMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    // 載入地圖
    private void LoadtoMap() {
        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);//NETWORK_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // 建立Google API用戶端物件
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        // 建立Location請求物件
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)    // 設定優先讀取高精確度的位置資訊（GPS）
                .setInterval(UPDATE_INTERVAL)        // 5 seconds, in milliseconds
                .setFastestInterval(FATEST_INTERVAL) // 1 second, in milliseconds
                .setSmallestDisplacement(DISPLACEMENT);

        // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_record)).getMap();

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setCompassEnabled(true);       // 顯示指南針
        mMap.getUiSettings().setZoomControlsEnabled(true);  // 顯示縮放控制按鈕
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);         // 設定地圖類型
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon),18);
        //mMap.moveCamera(center);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        Log.i(TAG, "Location services connected.");

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates
                        (mGoogleApiClient, mLocationRequest, RecordActivity.this);
            } else if (currentLocation != location) {
                handleNewLocation(location);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊
        int errorCode = connectionResult.getErrorCode();
        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Location Listener
     */
    @Override
    public void onLocationChanged(Location location) {
        if (currentLocation != location) {
            handleNewLocation(currentLocation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                Log.v(TAG, "Status Changed: Out of Service");
                Toast.makeText(RecordActivity.this, "Status Changed: Out of Service",
                        Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v(TAG, "Status Changed: Temporarily Unavailable");
                Toast.makeText(RecordActivity.this, "Status Changed: Temporarily Unavailable",
                        Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.AVAILABLE:
                Log.v(TAG, "Status Changed: Available");
                Toast.makeText(RecordActivity.this, "Status Changed: Available",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        handleNewLocation(null);
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        currentLocation = location;
        CurrentLatitude = location.getLatitude();
        CurrentLongitude = location.getLongitude();
        CurrentLatlng = new LatLng(CurrentLatitude, CurrentLongitude);

        // 設定目前位置的標記
        if (currentMarker == null) {
            if (mMap == null) {
                LoadtoMap();
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatlng, 14));
            currentMarker = mMap.addMarker(new MarkerOptions().position(CurrentLatlng).title("I am here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            currentMarker.setPosition(CurrentLatlng);
        }
    }

    // 顯示軌跡紀錄
    private void DisplayRoute(LatLng track_latlng) {
        //database.delete("trackRoute", null, null);
        if (TraceRoute == null) {
            TraceRoute = new ArrayList<LatLng>();
        }
        TraceRoute.add(track_latlng);

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : TraceRoute) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.parseColor("#2BB7EC"));

        Polyline line = mMap.addPolyline(polylineOpt);
        line.setWidth(10);

        Log.d("3/10_畫出軌跡", "DisplayRoute" + track_latlng.toString());
    }

    //----2.4----//
    //click for photos
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_CAMERA) {
                    memo_img = (Bitmap) data.getExtras().get("data");
                    //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    //memo_img.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    dialog_img.setImageBitmap(memo_img);

                    /* 將暫存檔儲存在外部儲存空間
                    File destination = new File(Environment.getExternalStorageDirectory(),
                            System.currentTimeMillis() + ".jpg");
                    FileOutputStream fo;
                    try {
                        destination.createNewFile();
                        fo = new FileOutputStream(destination);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    */
                }
                if (requestCode == SELECT_FILE) {
                    Uri uri = data.getData();
                    ContentResolver cr = this.getContentResolver();
                    try {
                        Log.e("3/13_", "uri:" + uri);
                        memo_img = Functions.ScalePic(BitmapFactory.decodeStream(cr.openInputStream(uri)));
                        dialog_img.setImageBitmap(memo_img);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (content_layout.getVisibility() == content_layout.VISIBLE) {
                content_layout.setVisibility(View.INVISIBLE);
            }
            dialog_scrollview.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams otelParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 500);
            otelParams.addRule(RelativeLayout.BELOW, R.id.dialog_header_text);
            dialog_scrollview.setLayoutParams(otelParams);

            dialog_relativeLayout.setVisibility(View.VISIBLE);

            dialog_img.setVisibility(View.VISIBLE);

            dialog_choose_layout.setVisibility(View.INVISIBLE);

            dialog_confirm_layout.setVisibility(View.VISIBLE);
            dialog_confirm_layout.setOnClickListener(ok);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    View.OnClickListener ok = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /**DB**/
            DataBaseHelper helper = new DataBaseHelper(RecordActivity.this);
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor memo_cursor = db.query("travelmemo", new String[]{"memo_routesCounter", "memo_trackNo",
                    "memo_content", "memo_img", "memo_latlng", "memo_time"},
                    null, null, null, null, null);
            if (memo_cursor != null) {
                ContentValues cv = new ContentValues();
                cv.put("memo_routesCounter", RoutesCounter);
                cv.put("memo_trackNo", Track_no);
                if (memo_img != null) {
                    try {
                        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                        Boolean a;
                        a = memo_img.compress(Bitmap.CompressFormat.JPEG, 50, stream2);
                        Log.e("3/13_", "compress " + a);
                        byte[] bytes2 = stream2.toByteArray();
                        cv.put("memo_img", bytes2);
                    } catch (Exception e) {
                        Log.e("EXCEPTION", e.toString());
                    }
                }
                if (CurrentLatlng != null) {
                    cv.put("memo_latlng", CurrentLatlng.toString());
                }
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                Date date = new Date();
                String dateString = fmt.format(date);
                cv.put("memo_time", dateString);
                inDB = db.insert("travelmemo", null, cv);
                Log.e("3/13_", "DB insert img" + inDB + " Addtime " + dateString);

            }
            //2.9 for testing
/*
            if (img_cursor.getCount() > 0) {
                img_cursor.moveToFirst();
                if (img_cursor.getBlob(3) != null) {
                    byte[] d = img_cursor.getBlob(3);
                    Bitmap bmp = BitmapFactory.decodeByteArray(d, 0, d.length);
                    dialog_img.setImageBitmap(bmp);
                }
            }
*/
            if (inDB != -1) {
                if (spotDialog.isShowing()) {
                    if (memo_img != null) {
                        dialog_img.setImageBitmap(null);
                    }
                    spotDialog.dismiss();
                }
                Toast.makeText(RecordActivity.this, "圖片已上傳！", Toast.LENGTH_SHORT).show();
            }
            memo_cursor.close();
            db.close();
            helper.close();
            if (spotDialog.isShowing()) {
                spotDialog.dismiss();
            }
        }
    };

    private BroadcastReceiver broadcastReceiver_timer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Integer status = intent.getIntExtra("record_status", 0);
                Long spent = intent.getLongExtra("spent", 99);
                if (spent != 99) {
                    if (((spent / 1000) / 60) > 0)
                        time_text.setText(((spent / 1000) / 60) + ":" + ((spent / 1000) % 60));
                    else
                        time_text.setText("00:" + ((spent / 1000) % 60));

                    if (status == 2) {
                        tempSpent = spent;
                    }
                }
                Log.d("3/16", "BroadcastReceiver: " + intent.getLongExtra("spent", 99));
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Your UI here..
            if (intent != null) {
                Integer status = intent.getIntExtra("record_status", 0);
                Integer routesCounter = intent.getIntExtra("routesCounter", 1);
                Integer track_no = intent.getIntExtra("track_no", 1);
                Double track_lat = intent.getDoubleExtra("track_lat", 0);
                Double track_lng = intent.getDoubleExtra("track_lng", 0);
                LatLng track_latLng = new LatLng(track_lat, track_lng);
                if (!(routesCounter > 1 && track_no == 1)) {
                    DisplayRoute(track_latLng);
                }

                if (status == 2) {
                    Intent intent_Trace = new Intent(TRACK_TO_SERVICE);
                    intent_Trace.putExtra("record_status", 2);
                    intent_Trace.putExtra("isPause", true);
                    sendBroadcast(intent_Trace);

                    DataBaseHelper helper = new DataBaseHelper(getApplicationContext());
                    SQLiteDatabase database = helper.getWritableDatabase();
                    Cursor trackRoute_cursor = database.query("trackRoute",
                            new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                                    "track_start", "track_title", "track_totaltime", "track_completetime"},
                            null, null, null, null, null);
                    if (trackRoute_cursor != null) {
                        ContentValues cv = new ContentValues();
                        cv.put("routesCounter", routesCounter);
                        cv.put("track_no", track_no);
                        cv.put("track_lat", track_lat);
                        cv.put("track_lng", track_lng);
                        cv.put("track_start", 2);
                        long result = database.insert("trackRoute", null, cv);
                        Log.d("3/10_軌跡紀錄_Pause", result + " = DB INSERT RC:" + routesCounter
                                + " no:" + track_no + " 座標 " + track_lat + "," + track_lng + " status " + status);
                    }
                    database.close();
                    helper.close();
                } else if (status == 0) {
                    String track_title = intent.getStringExtra("track_title");
                    DataBaseHelper helper = new DataBaseHelper(getApplicationContext());
                    SQLiteDatabase database = helper.getWritableDatabase();
                    Cursor trackRoute_cursor = database.query("trackRoute",
                            new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                                    "track_start", "track_title", "track_totaltime", "track_completetime"},
                            null, null, null, null, null);
                    if (trackRoute_cursor != null) {
                        ContentValues cv = new ContentValues();
                        cv.put("routesCounter", routesCounter);
                        cv.put("track_no", track_no);
                        cv.put("track_lat", track_lat);
                        cv.put("track_lng", track_lng);
                        cv.put("track_start", 0);
                        cv.put("track_title", track_title);
                        cv.put("track_totaltime", time_text.getText().toString());
                        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date();
                        String dateString = fmt.format(date);
                        cv.put("track_completetime", dateString);
                        long result = database.insert("trackRoute", null, cv);
                        Log.d("3/10_軌跡紀錄_END", result + " = DB INSERT RC:" + routesCounter
                                + " no:" + track_no + " 座標 " + track_lat + "," + track_lng + ". "
                                + track_title + " TotalTime:" + time_text.getText().toString() + " status " + status);
                    }
                    database.close();
                    helper.close();
                }
            }
        }
    };

    // Android 系統返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, RecordActivity.this, RecordActivity.this, HomepageActivity.class, null);
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to

            } else {
                // Permission was denied or request was cancelled
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                Toast.makeText(RecordActivity.this, "請允許寶島好智遊存取您的位置!", Toast.LENGTH_LONG).show();
            }
        }
    }
}