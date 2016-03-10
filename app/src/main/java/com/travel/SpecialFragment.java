package com.travel;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.travel.Adapter.SpecialAdapter;
import com.travel.Utility.Functions;


/**
 * A simple {@link Fragment} subclass.
 */
public class SpecialFragment extends Fragment {
    int page_number = 0;
    Context context;
    Activity activity;
    SpecialAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getActivity().getBaseContext();
        this.activity = this.getActivity();
    }

    public SpecialFragment(Integer pagenumber) {
        this.page_number = pagenumber;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.special_fragment, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        adapter = new SpecialAdapter(getActivity(),page_number);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new itemListener());
        return view;
//        return inflater.inflate(R.layout.special_fragment, container, false);
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", (page_number - 1) * 10 + position);
            Functions.go(false, activity, context, BuyItemDetailActivity.class, bundle);
        }
    }
}
