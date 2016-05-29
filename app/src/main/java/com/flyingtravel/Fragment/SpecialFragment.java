package com.flyingtravel.Fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.flyingtravel.Activity.Special.SpecialDetailActivity;
import com.flyingtravel.Adapter.SpecialAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.Special;


/**
 * A simple {@link Fragment} subclass.
 */
public class SpecialFragment extends Fragment {
    int page_number = 0, totalnumber = 0;
    Context context;
    Activity activity;
    SpecialAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page_number = getArguments().getInt("pagenumber");
        totalnumber = getArguments().getInt("total");
        this.context = this.getActivity().getBaseContext();
        this.activity = this.getActivity();
    }

    public SpecialFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.special_fragment, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.gridView2);
        adapter = new SpecialAdapter(getActivity(), page_number);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new itemListener());
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO 加上taskDONE!!!
                new Special(getActivity(), new Functions.TaskCallBack() {
                    @Override
                    public void TaskDone(Boolean OrderNeedUpdate) {
                        if (OrderNeedUpdate)
                            adapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, totalnumber).execute();
//                Log.d("5.29","onRefresh");
            }
        });
//        Log.e("3.10", "SpecailFragment:" + adapter.getCount() + " page:" + page_number);
        return view;
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", (page_number - 1) * 10 + position);
            Functions.go(false, activity, context, SpecialDetailActivity.class, bundle);
        }
    }
}
