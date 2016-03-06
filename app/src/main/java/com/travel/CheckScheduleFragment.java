package com.travel;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.travel.Adapter.CheckAdapter;

/**
 * Created by wei on 2016/1/12.
 */
public class CheckScheduleFragment extends Fragment {

    ListView listView;
    CheckAdapter Checkadapter;
    TextView gonextTxt,nowDayTxt;

    int currentPage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.checkschedule_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.check_listView);
        Checkadapter = new CheckAdapter(getActivity());
        listView.setAdapter(Checkadapter);
        listView.setDividerHeight(0);
        nowDayTxt = (TextView) view.findViewById(R.id.checkschedule_dayTxt);

        return view;
    }
}
