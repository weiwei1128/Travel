package com.travel;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.travel.ImageSlide.MainImageFragment;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;
import com.travel.Utility.MyTextview;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    LinearLayout linearLayout, buyLayout, spotLayout, recordLayout, scheduleLayout,
            serviceLayout, goodthingLayout;
    private Fragment contentFragment;
    MainImageFragment homefragment;
    Context context;
    Boolean ifStop = false;


    MyTextview news;
    Bundle getSavedInstanceState;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
//        Log.e("3.22","=========Main onResume");
        if (getNewsBroadcast == null) {
            context.registerReceiver(getNewsBroadcast, new IntentFilter("news"));
            context.registerReceiver(getNewsBroadcast, new IntentFilter("banner"));
        }
        String message = "讀取資料中";
        DataBaseHelper helper = new DataBaseHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor news_cursor = database.query("news", new String[]{"title"}, null, null, null, null, null);
        if (news_cursor != null && news_cursor.getCount() > 0) {
            news_cursor.moveToFirst();
            message = news_cursor.getString(0);
        }
        if (news_cursor != null)
            news_cursor.close();
        news.setText(message);

        super.onResume();
    }

    @Override
    public void onDestroy() {
//        Log.e("3.22","=========Main onDestroy");
        if (getNewsBroadcast != null)
            context.unregisterReceiver(getNewsBroadcast);
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.e("3.22", "=========Main onCreate");
        this.context = getActivity();
        this.getSavedInstanceState = savedInstanceState;
        context.registerReceiver(getNewsBroadcast, new IntentFilter("news"));
        context.registerReceiver(getNewsBroadcast, new IntentFilter("banner"));
        FragmentManager fragmentManager = getChildFragmentManager();
        if (getSavedInstanceState != null) {
            if (fragmentManager.findFragmentByTag(MainImageFragment.ARG_ITEM_ID) != null) {
                homefragment = (MainImageFragment) fragmentManager
                        .findFragmentByTag(MainImageFragment.ARG_ITEM_ID);
                contentFragment = homefragment;
            }
        } else {
            homefragment = new MainImageFragment();
            switchContent(homefragment, MainImageFragment.ARG_ITEM_ID);
        }

    }

    ////ImageSlide
    public void switchContent(Fragment fragment, String tag) {
//        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentManager fragmentManager = getChildFragmentManager();
        while (fragmentManager.popBackStackImmediate())
            ;

        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager
                    .beginTransaction();
            transaction.replace(R.id.content_frame, fragment, tag);
            // Only ProductDetailFragment is added to the back stack.
            if (!(fragment instanceof MainImageFragment)) {
                transaction.addToBackStack(tag);
            }
            transaction.commit();
            contentFragment = fragment;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.e("3.22", "=========Main onCreateView");
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.main_main_layout);
        UI(view);

        news = new MyTextview(context);
        news.setText("讀取資料中");
        news.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        linearLayout.addView(news,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        //little trick
        ((LinearLayout.LayoutParams) news.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
        news.scrollText(20);////開始跑囉
        news.setTextColor(Color.BLACK);
        /////跑馬燈
        return view;
    }


    void UI(View view) {

        //Goodthing
        goodthingLayout = (LinearLayout) view.findViewById(R.id.main_good_layout);
        goodthingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, SpecialActivity.class, null);
            }
        });

        //service
        serviceLayout = (LinearLayout) view.findViewById(R.id.main_service_layout);
        serviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, ServiceActivity.class, null);
            }
        });

        spotLayout = (LinearLayout) view.findViewById(R.id.main_spot_layout);
        spotLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, MapsActivity.class, null);
            }
        });

        recordLayout = (LinearLayout) view.findViewById(R.id.main_record_layout);
        recordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, RecordActivity.class, null);
            }
        });

        buyLayout = (LinearLayout) view.findViewById(R.id.main_buy_layout);
        buyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, BuyActivity.class, null);
            }
        });

        scheduleLayout = (LinearLayout) view.findViewById(R.id.main_schedule_layout);
        scheduleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(false, getActivity(), context, CheckScheduleMainActivity.class, null);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (contentFragment instanceof MainImageFragment) {
            outState.putString("content", MainImageFragment.ARG_ITEM_ID);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        ifStop = true;
    }

    /**
     * 接收下載成功的資料
     **/
    private BroadcastReceiver getNewsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getBooleanExtra("news", false)) {
                    DataBaseHelper helper = new DataBaseHelper(context);
                    SQLiteDatabase database = helper.getWritableDatabase();
                    String message = "讀取資料中";
                    Cursor news_cursor = database.query("news", new String[]{"title"}, null, null, null, null, null);
                    if (news_cursor != null && news_cursor.getCount() > 0) {
                        news_cursor.moveToFirst();
                        message = news_cursor.getString(0);
                    }
                    if (news_cursor != null)
                        news_cursor.close();

                    news.setText(message);
                }
                if (intent.getBooleanExtra("banner", false) && !ifStop) {
                    FragmentManager fragmentManager = getChildFragmentManager();
                    if (getSavedInstanceState != null) {
                        if (fragmentManager.findFragmentByTag(MainImageFragment.ARG_ITEM_ID) != null) {
                            homefragment = (MainImageFragment) fragmentManager
                                    .findFragmentByTag(MainImageFragment.ARG_ITEM_ID);
                            contentFragment = homefragment;
                        }
                    } else {
                        homefragment = new MainImageFragment();
                        switchContent(homefragment, MainImageFragment.ARG_ITEM_ID);
                    }
                }
            }
        }
    };


}
