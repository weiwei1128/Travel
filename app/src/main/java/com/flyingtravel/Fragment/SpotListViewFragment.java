package com.flyingtravel.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.flyingtravel.Activity.Spot.SpotDetailActivity;
import com.flyingtravel.Adapter.SpotListAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;

/**
 * Created by Tinghua on 2016/3/25.
 */
public class SpotListViewFragment extends Fragment {

    public static final String TAG = SpotListViewFragment.class.getSimpleName();
    public static final String FRAGMENT_NAME = "FRAGMENT_NAME";
    public static final String PAGE_NO = "PAGE_NO";
    private String mFragmentName;
    private int mPageNo;

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
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new itemListener());

        return view;
    }

    @Override
    public void onDestroyView() {
        System.gc();
        super.onDestroyView();
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
