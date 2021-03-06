package com.flyingtravel.Fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;

import com.flyingtravel.Activity.Buy.BuyItemDetailActivity;
import com.flyingtravel.Adapter.BuyAdapter;
import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.Goods;

public class BuyFragment extends Fragment {

    GridView gridView;
    BuyAdapter adapter;
    int Position = 0,totalnumber = 0;
    Context context;
    Activity activity;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private SearchView search;


    public BuyFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Position = getArguments().getInt("position");
        context = this.getActivity().getBaseContext();
        activity = this.getActivity();
        totalnumber = getArguments().getInt("total");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buy_fragment, container, false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        adapter = new BuyAdapter(getActivity(), Position, gridView);//position 代表頁碼
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Goods(context, new Functions.TaskCallBack() {
                    @Override
                    public void TaskDone(Boolean OrderNeedUpdate) {
                        if(OrderNeedUpdate)
                            adapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                },totalnumber).execute();
            }
        });

        gridView.setNumColumns(2);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new itemListener());
        if (adapter.getCount() == 0)
            Toast.makeText(context, getContext().getResources().getString(R.string.nofile_text), Toast.LENGTH_SHORT).show();
        search = (SearchView) view.findViewById(R.id.searchView);
        search.setQueryHint(getContext().getResources().getString(R.string.inputkeyword_text));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                    Functions.toast(context,"製作中",1000);
                adapter = new BuyAdapter(getActivity(), Position, gridView);
                gridView.setAdapter(adapter);
                adapter.getFilter().filter(newText);
                //Log.e("4/1_", "搜尋: " + newText.toString());
                return true;
            }
        });
        return view;
    }

    class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//            Log.e("5.26", "whichitem::" + adapter.getIfFilter());
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", (Position - 1) * 10 + position);
            bundle.putString("FilterString", adapter.getFilterString());
            bundle.putInt("FilterStringPosition", position);

            Functions.go(false, activity, context, BuyItemDetailActivity.class, bundle);

            DataBaseHelper helper;
            SQLiteDatabase database;
            helper = DataBaseHelper.getmInstance(context);
            database = helper.getWritableDatabase();

            if (adapter.getIfFilter()) {
                Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id",
                        "goods_title", "goods_url", "goods_money", "goods_content", "goods_click",
                        "goods_addtime"}, "goods_title LIKE ?", new String[]{"%" + adapter.getFilterString() + "%"}, null, null, null);
                if (goods_cursor != null && goods_cursor.getCount() >= position) {
                    goods_cursor.moveToPosition(position);
                    ContentValues cv = new ContentValues();
                    int count = 0;
                    if (goods_cursor.getString(6) != null)
                        count = Integer.parseInt(goods_cursor.getString(6)) + 1;
                    cv.put("goods_click", count + "");
//                Log.d("4.25", "click:" + count);
                    long result = database.update("goods", cv, "goods_id=?", new String[]{goods_cursor.getString(1)});
                }
                if (goods_cursor != null)
                    goods_cursor.close();
            } else {
                Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id",
                        "goods_title", "goods_url", "goods_money", "goods_content", "goods_click",
                        "goods_addtime"}, null, null, null, null, null);

                if (goods_cursor != null && goods_cursor.getCount() >= (Position - 1) * 10 + position) {
                    goods_cursor.moveToPosition((Position - 1) * 10 + position);
                    ContentValues cv = new ContentValues();
                    int count = 0;
                    if (goods_cursor.getString(6) != null)
                        count = Integer.parseInt(goods_cursor.getString(6)) + 1;
                    cv.put("goods_click", count + "");
//                Log.d("4.25", "click:" + count);
                    long result = database.update("goods", cv, "goods_id=?", new String[]{goods_cursor.getString(1)});
                }
                if (goods_cursor != null)
                    goods_cursor.close();
            }
//            adapter.UpdateView((Position - 1) * 10 + position,position);
            adapter.notifyDataSetChanged();
        }
    }
}
