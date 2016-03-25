package com.travel;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.travel.Adapter.SpotListAdapter;
import com.travel.R;

/**
 * Created by Tinghua on 2016/3/25.
 */
public class SpotListViewPagerFragment extends Fragment {

    private static final String KEY_POSITION="position";

    public static SpotListAdapter adapter;
    private ListView mlistView;
    int Position = 0;

    public SpotListViewPagerFragment (int position) {
        Position = position;
        Log.e("3/23_", "SpotListViewPagerFragment: position" + Position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spot_listview, container, false);

        mlistView = (ListView) view.findViewById(R.id.spotlist_listView);
        adapter = new SpotListAdapter(getActivity(), Position);
        mlistView.setAdapter(adapter);
        adapter.notifyDataSetInvalidated();
        //mlistView.setOnItemClickListener(new itemListener());

        return view;
    }

}
