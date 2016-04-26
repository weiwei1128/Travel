package com.flyingtravel.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flyingtravel.R;

public class CheckScheduleFragment extends Fragment {
    String[] data = new String[5];
    ListView listView;
    int count = 0;
    String[] getsummary;

    public CheckScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("4.26", "-------onCreate");
        data[0] = getArguments().getString("scheduleday");
        data[1] = getArguments().getString("scheduledate");
        data[2] = getArguments().getString("scheduletime");


        if(getArguments().containsKey("schedulecount")) {
            count = getArguments().getInt("schedulecount");
            getsummary = new String[count];
            for (int i=0;i<count;i++){
                if(getArguments().containsKey("schedulesummary"+i)) {
                    getsummary[i] = getArguments().getString("schedulesummary" + i);
                    Log.d("4.26", "!!!!!!!!!" + getArguments().getString("schedulesummary" + i));
                }
            }

        }else
            data[3] = getArguments().getString("schedulesummary");
        Log.d("4.26","-------"+getArguments().getString("schedulesummary"));
        data[4] = getArguments().getString("schedulejinwei");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.checkschedule_frament, container, false);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.checkschedule_layout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(50, 20, 30, 0);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        if(count>0)
            for(int i=0;i<count;i++){
                if(getsummary[i]!=null)
                {
                    TextView textView = new TextView(getActivity());
                    textView.setText(getsummary[i]);
                    textView.setTextSize(15);
                    textView.setTextColor(getResources().getColor(R.color.black));
                    linearLayout.addView(textView,layoutParams);
                }
            }
        else{
            TextView textView = new TextView(getActivity());
            textView.setText(data[3]);
            textView.setTextSize(15);
            textView.setTextColor(getResources().getColor(R.color.black));
            linearLayout.addView(textView,layoutParams);
        }
        return view;
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
