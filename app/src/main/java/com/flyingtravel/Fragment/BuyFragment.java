package com.flyingtravel.Fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.flyingtravel.Activity.Buy.BuyItemDetailActivity;
import com.flyingtravel.Adapter.BuyAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;

public class BuyFragment extends Fragment {

    GridView gridView;
    BuyAdapter adapter;
    int Position = 0;
    Context context;
    Activity activity;


    public BuyFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Position = getArguments().getInt("position");
        context = this.getActivity().getBaseContext();
        activity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buy_fragment, container, false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        adapter = new BuyAdapter(getActivity(), Position);//position 代表頁碼
        gridView.setNumColumns(2);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new itemListener());
        if (adapter.getCount() == 0)
            Toast.makeText(context, "尚無資料!", Toast.LENGTH_SHORT).show();
        return view;
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", (Position - 1) * 10 + position);
            Functions.go(false, activity, context, BuyItemDetailActivity.class, bundle);
        }
    }
}
