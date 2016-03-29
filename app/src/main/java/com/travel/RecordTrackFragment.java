package com.travel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
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
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordTrackFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecordTrackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordTrackFragment extends Fragment implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = RecordTrackFragment.class.getSimpleName();
    private static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    //private static final String ARG_PARAM2 = "param2";
    private String mFragmentName;
    //private String mParam2;

    private MapView mapView;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 3;       // 5 meters

    private GlobalVariable globalVariable;

    private Location CurrentLocation;

    private LatLng CurrentLatlng;
    private Marker CurrentMarker;

    private Bitmap MarkerIcon;
    private ArrayList<LatLng> TraceRoute;

    private Dialog spotDialog;
    private ScrollView dialog_scrollview;
    private LinearLayout record_start_layout, record_spot_layout;
    private LinearLayout dialog_choose_layout, dialog_confirm_layout, title_layout, content_layout;
    private RelativeLayout dialog_relativeLayout;
    private ImageView record_start_img, record_spot_img;
    private ImageView dialog_img, write, camera, leave;
    private TextView record_start_text, record_spot_text;
    private TextView dialog_header_text, title_textView, title_confirmTextView, content_textView;
    private EditText title_editText, content_editText;

    private Integer RoutesCounter = 1;
    private Integer Track_no = 1;
    private Integer record_status = 0;
    private Long tempSpent = 0L;

    final int REQUEST_CAMERA = 99;
    final int SELECT_FILE = 98;
    final Long[] starttime = new Long[1];
    //----for upload image//
    Bitmap memo_img;
    long inDB = 0;

    public static final String TIMER_TO_SERVICE = "com.example.tracking.restartcount";
    public static final String TRACK_TO_SERVICE = "com.example.tracking.restarttrack";

    private OnFragmentInteractionListener mListener;

    public RecordTrackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragementName Parameter 1.
     * @return A new instance of fragment RecordTrackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordTrackFragment newInstance(String fragementName) {
        RecordTrackFragment fragment = new RecordTrackFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_NAME, fragementName);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragmentName = getArguments().getString(FRAGMENT_NAME);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }

        globalVariable = (GlobalVariable) getActivity().getApplicationContext();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(TrackRouteService.BROADCAST_ACTION));
        getActivity().registerReceiver(broadcastReceiver_timer, new IntentFilter(TrackRouteService.BROADCAST_ACTION_TIMER));

        MarkerIcon = decodeBitmapFromResource(getResources(), R.drawable.location3, 10, 18);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 5 seconds, in milliseconds
                .setFastestInterval(FATEST_INTERVAL) // 1 second, in milliseconds
                .setSmallestDisplacement(DISPLACEMENT);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record_track, container, false);

        mapView = (MapView) rootView.findViewById(R.id.TrackMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();         // needed to get the map to display immediately

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMap = mapView.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        record_start_layout = (LinearLayout) rootView.findViewById(R.id.record_start_layout);
        record_start_img = (ImageView) rootView.findViewById(R.id.record_start_img);
        record_start_text = (TextView) rootView.findViewById(R.id.record_start_text);

        record_spot_layout = (LinearLayout) rootView.findViewById(R.id.record_spot_layout);
        record_spot_img = (ImageView) rootView.findViewById(R.id.record_spot_img);
        record_spot_text = (TextView) rootView.findViewById(R.id.record_spot_text);

        spotDialog = new Dialog(getActivity());
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
        write = (ImageView) spotDialog.findViewById(R.id.dialog_write_img);
        camera = (ImageView) spotDialog.findViewById(R.id.dialog_camera_img);
        leave = (ImageView) spotDialog.findViewById(R.id.dialog_leave_img);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RetrieveRouteFromDB();

        RecordActivity.record_completeImg.setOnClickListener(new View.OnClickListener() {
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
                        RecordActivity.time_text.setVisibility(View.INVISIBLE);
                        RecordActivity.record_completeImg.setVisibility(View.INVISIBLE);

                        record_start_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        record_start_text.setTextColor(Color.parseColor("#555555"));
                        record_start_text.setText("開始紀錄");
                        record_start_img.setImageResource(R.drawable.ic_play_light);
                        record_status = 0;

                        if (Functions.isMyServiceRunning(getActivity(), TrackRouteService.class)) {
                            Intent intent_Trace = new Intent(TRACK_TO_SERVICE);
                            intent_Trace.putExtra("record_status", 0);
                            intent_Trace.putExtra("track_title", title_editText.getText().toString());
                            getActivity().sendBroadcast(intent_Trace);
                        }
                        record_spot_layout.setClickable(false);
                        spotDialog.dismiss();
                    }
                });
                spotDialog.show();
            }
        });

        record_start_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (record_status == 1) {
                    record_start_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    record_start_text.setTextColor(Color.parseColor("#555555"));
                    record_start_text.setText("開始紀錄");
                    record_start_img.setImageResource(R.drawable.ic_play_light);
                    record_status = 2;

                    if (Functions.isMyServiceRunning(getActivity(), TrackRouteService.class)) {
                        Intent intent_Trace = new Intent(TRACK_TO_SERVICE);
                        intent_Trace.putExtra("record_status", 2);
                        getActivity().sendBroadcast(intent_Trace);
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
                    RecordActivity.time_text.setVisibility(View.VISIBLE);
                    RecordActivity.record_completeImg.setVisibility(View.VISIBLE);
                    starttime[0] = System.currentTimeMillis();
                    //----2.4

                    DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
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

                    if (!Functions.isMyServiceRunning(getActivity(), TrackRouteService.class)) {
                        Intent intent_Trace = new Intent(getActivity(), TrackRouteService.class);
                        intent_Trace.putExtra("record_status", 1);
                        intent_Trace.putExtra("start", starttime[0]);
                        intent_Trace.putExtra("routesCounter", RoutesCounter);
                        intent_Trace.putExtra("track_no", Track_no);
                        getActivity().startService(intent_Trace);
                    } else {
                        Intent intent_Trace = new Intent(TIMER_TO_SERVICE);
                        intent_Trace.putExtra("record_status", 1);
                        intent_Trace.putExtra("start", starttime[0]);
                        intent_Trace.putExtra("spent", tempSpent);
                        intent_Trace.putExtra("routesCounter", RoutesCounter);
                        intent_Trace.putExtra("track_no", Track_no);
                        intent_Trace.putExtra("isPause", false);
                        getActivity().sendBroadcast(intent_Trace);
                        tempSpent = 0L;
                    }
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
                        DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
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
                            Log.e("3/23_", "DB insert content" + inDB + " content:"
                                    + content_editText.getText().toString() + " Addtime " + dateString);

                            if (inDB != -1) {
                                if (spotDialog.isShowing()) {
                                    if (content_layout.getVisibility() == content_layout.VISIBLE) {
                                        content_layout.setVisibility(View.INVISIBLE);
                                        content_editText.setText("");
                                    }
                                    spotDialog.dismiss();
                                }
                                Toast.makeText(getActivity(), "心得已上傳！", Toast.LENGTH_SHORT).show();
                            }
                            memo_cursor.close();
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
                Log.e("3/23_", "CAMERA");
                final CharSequence[] items = {"相機", "相簿", "取消"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    }

    @Override
    public void onResume() {
        mapView.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        // 移除Google API用戶端連線
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mapView.onDestroy();
        if (broadcastReceiver_timer != null)
            getActivity().unregisterReceiver(broadcastReceiver_timer);
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
        MarkerIcon.recycle();
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        Log.i(TAG, "Location services connected.");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates
                    (mGoogleApiClient, mLocationRequest, (LocationListener) getActivity());
        } else {
            HandleNewLocation(location);
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
            Toast.makeText(getActivity(), R.string.google_play_service_missing, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (CurrentLocation != location) {
            HandleNewLocation(CurrentLocation);
        }
    }

    private void HandleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        CurrentLocation = location;
        CurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (CurrentMarker == null) {
            // 移動地圖到目前的位置
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatlng, 15));
            CurrentMarker = mMap.addMarker(new MarkerOptions().position(CurrentLatlng).title("I am here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon)));
        } else {
            CurrentMarker.setPosition(CurrentLatlng);
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

        Log.d("3/20_畫出軌跡", "DisplayRoute" + track_latlng.toString());
    }

    // retrieve trackRoute from DB
    private void RetrieveRouteFromDB() {
        DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (resultCode == getActivity().RESULT_OK) {
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
                    ContentResolver cr = getActivity().getContentResolver();
                    try {
                        Log.e("3/23_", "uri:" + uri);
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
            DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
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
                        Log.e("3/23_", "compress " + a);
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
                Log.e("23_", "DB insert img" + inDB + " Addtime " + dateString);

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
                Toast.makeText(getActivity(), "圖片已上傳！", Toast.LENGTH_SHORT).show();
            }
            memo_cursor.close();

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
                        RecordActivity.time_text.setText(((spent / 1000) / 60) + ":" + ((spent / 1000) % 60));
                    else
                        RecordActivity.time_text.setText("00:" + ((spent / 1000) % 60));

                    if (status == 2) {
                        tempSpent = spent;
                    }
                }
                Log.d("3/26", "BroadcastReceiver: " + intent.getLongExtra("spent", 99));
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
                    getActivity().sendBroadcast(intent_Trace);

                    DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
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
                        Log.d("3/20_軌跡紀錄_Pause", result + " = DB INSERT RC:" + routesCounter
                                + " no:" + track_no + " 座標 " + track_lat + "," + track_lng + " status " + status);
                        trackRoute_cursor.close();
                    }
                } else if (status == 0) {
                    String track_title = intent.getStringExtra("track_title");
                    DataBaseHelper helper = DataBaseHelper.getmInstance(getActivity());
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
                        cv.put("track_totaltime", RecordActivity.time_text.getText().toString());
                        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date();
                        String dateString = fmt.format(date);
                        cv.put("track_completetime", dateString);
                        long result = database.insert("trackRoute", null, cv);
                        Log.d("3/20_軌跡紀錄_END", result + " = DB INSERT RC:" + routesCounter
                                + " no:" + track_no + " 座標 " + track_lat + "," + track_lng + ". "
                                + track_title + " TotalTime:" + RecordActivity.time_text.getText().toString() + " status " + status);
                        RecordDiaryFragment.mAdapter.notifyDataSetChanged();
                        Log.e("3/27_", "RecordTrackFragment. notifyDataSetChanged");
                        trackRoute_cursor.close();
                    }
                }
            }
        }
    };

    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.e("3/27_", "Marker size. "+height+","+width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}