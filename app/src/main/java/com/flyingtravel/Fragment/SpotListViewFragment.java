package com.flyingtravel.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.flyingtravel.Activity.Spot.SpotDetailActivity;
import com.flyingtravel.Adapter.SpotListAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.TrackRouteService;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Tinghua on 2016/3/25.
 */
public class SpotListViewFragment extends Fragment {

    public static final String TAG = SpotListViewFragment.class.getSimpleName();
    public static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    public static final String PAGE_NO = "PAGE_NO";
    private String mFragmentName;
    private int mPageNo;

    public static FrameLayout spotList_searchLayout;
    private EditText SearchEditText;
    private ImageView SearchImg;

    public static SpotListAdapter adapter;
    private ListView mlistView;

    public SpotListViewFragment () {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragementName Parameter 1.
     * @param pageNo Parameter 2.
     * @return A new instance of fragment RecordDiaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpotListViewFragment newInstance(String fragementName, int pageNo) {
        SpotListViewFragment fragment = new SpotListViewFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_NAME, fragementName);
        args.putInt(PAGE_NO, pageNo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFragmentName = getArguments().getString(FRAGMENT_NAME);
            mPageNo = getArguments().getInt(PAGE_NO);
        }
        Log.e("3/27_", "SpotListViewFragment. onCreate pageNo" + mPageNo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spot_listview, container, false);

        mlistView = (ListView) view.findViewById(R.id.spotlist_listView);
        adapter = new SpotListAdapter(getActivity(), mPageNo);

        spotList_searchLayout = (FrameLayout) view.findViewById(R.id.spotList_searchLayout);
        SearchImg = (ImageView) view.findViewById(R.id.spotlist_searchImg);
        SearchEditText = (EditText) view.findViewById(R.id.spotlist_searchEditText);
        SearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("4/1_景點搜尋", s.toString());
                //adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new itemListener());
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.e("3/23_SpotListView", "onDestroyView");
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        Log.e("3/23_SpotListView", "onLowMemory");
        System.gc();
        super.onLowMemory();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //you are visible to user now - so set whatever you need
            Log.e("3/23_SpotListView", "setUserVisibleHint: Visible");
        }
        else {
            //you are no longer visible to the user so cleanup whatever you need
            Log.e("3/23_SpotListView", "setUserVisibleHint: not Visible");
            System.gc();
        }
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", (mPageNo - 1) * 10 + position);
            Functions.go(false, getActivity(), getContext(), SpotDetailActivity.class, bundle);
        }
    }
}
