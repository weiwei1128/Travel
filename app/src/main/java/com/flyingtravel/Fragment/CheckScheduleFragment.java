package com.flyingtravel.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.flyingtravel.Adapter.CheckScheduleNavAdapter;
import com.flyingtravel.R;
import com.flyingtravel.ScheduleMapsActivity;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.GlobalVariable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class CheckScheduleFragment extends Fragment {
    String[] data = new String[5], getsummary, getaddress;
    int count = 0;
    Context context;
    Activity activity;
    ListView gridView;
    CheckScheduleNavAdapter adapter;
    LinearLayout showLayout;
    String itemid = null;
    /*GA*/
    public static Tracker tracker;

    public CheckScheduleFragment() {
        // Required empty public constructor
    }

    //05020502!!!!
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.e("4.26", "-------onCreate");
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getActivity().getApplication();
        tracker = globalVariable.getDefaultTracker();
        /**GA**/
        data[0] = getArguments().getString("scheduleday");
        data[1] = getArguments().getString("scheduledate");
        data[2] = getArguments().getString("scheduletime");

        if (getArguments().containsKey("schedulecount")) {
            count = getArguments().getInt("schedulecount");
            getsummary = new String[count];
            getaddress = new String[count];
            Log.e("5.16","count::"+count);
            for (int i = 0; i < count; i++) {
                if (getArguments().containsKey("schedulesummary" + (i+1))) {
                    getsummary[i] = getArguments().getString("schedulesummary" + (i+1));
                    getaddress[i] = getArguments().getString("scheduleaddress" + (i+1));
                    Log.d("4.26", i+"!!!!!!!!!" + getArguments().getString("schedulesummary" + (i+1)));
                }
            }

        } else
            data[3] = getArguments().getString("schedulesummary");
//        Log.d("4.26","-------"+getArguments().getString("schedulesummary"));
        if (getArguments().containsKey("schedulejinwei"))
            data[4] = getArguments().getString("schedulejinwei");
        if (getArguments().containsKey("scheduleid"))
            itemid = getArguments().getString("scheduleid");
        context = getActivity().getBaseContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.checkschedule_frament, container, false);
        showLayout = (LinearLayout) view.findViewById(R.id.checkschedule_allLayout);
        //瀏覽導航
        showLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                int putcount = 1;
                if (count > 0)
                    putcount = count;
                bundle.putInt("count", putcount);
                if (count != 0)
                    bundle.putStringArray("address", getaddress);
                else bundle.putStringArray("address", new String[]{data[4]});
//                Log.w("5.24","count:"+count+" getaddress"+getaddress.length);
                tracker.send(new HitBuilders.EventBuilder().setCategory("行程查詢-行程模擬-ID:"+itemid)
//                .setAction("click")
//                .setLabel("submit")
                        .build());
                Functions.go(false, getActivity(), context, ScheduleMapsActivity.class, bundle);
            }
        });
        //瀏覽導航
        gridView = (ListView) view.findViewById(R.id.schedule_gridview);
        if (count != 0)
            adapter = new CheckScheduleNavAdapter(context, count, getsummary, getaddress);
        else {
            String[] tdata = {data[3]};
            String[] add4 = {data[4]};
            adapter = new CheckScheduleNavAdapter(context, 1, tdata, add4);
        }
        gridView.setAdapter(adapter);
        gridView.setDividerHeight(10);
        gridView.setOnItemClickListener(new itemListener());
        return view;
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /***GA**/
            tracker.send(new HitBuilders.EventBuilder().setCategory("行程查詢-單點導航-ID:" + itemid)
//                .setAction("click")
//                .setLabel("submit")
                    .build());
            /***GA**/
//            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "saddr="
//                        + "111" + "," + "222" + "&daddr=" + "333" + "," + "444"));
//                intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
            String addr = null;
            if (count == 0)
                addr = data[4];
            else addr = getaddress[position];
//            Log.e("4.30","itemClick:"+addr);
            if (addr != null) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + addr));
//            Log.d("4.26","-------!!!!"+data[4]);
                startActivity(intent);
            } else Toast.makeText(context,
                    context.getResources().getString(R.string.wrongData_text), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
