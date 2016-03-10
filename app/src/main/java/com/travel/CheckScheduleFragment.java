package com.travel;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("3.7","ItemClick!!!! "+view.getId()+"  /// "+position);
            }
        });

        return view;
    }
}
