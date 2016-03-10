package com.travel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Adapter.BuyAdapter;
import com.travel.Utility.Functions;

public class BuyFragmentNew extends Fragment {

    GridView gridView;
    BuyAdapter adapter;
    int Position = 0;
    Context context;
    Activity activity;

    public BuyFragmentNew(Integer position) {
        Position = position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getBaseContext();
        activity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buy_fragment_new, container, false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        adapter = new BuyAdapter(getActivity(), Position);//position 代表頁碼
        gridView.setNumColumns(2);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new itemListener());
        Log.d("3.8", "adapter!" + adapter.getCount());
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
