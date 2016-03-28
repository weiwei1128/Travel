package com.travel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.travel.Adapter.RecordMemoAdapter;
import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

import java.text.SimpleDateFormat;
import java.util.Date;


public class RecordMemoActivity extends AppCompatActivity {

    ImageView backImg;
    TextView  DateOfLastOne;

    private Button record_travel_button;

    private ListView mlistView;
    private RecordMemoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_memo_activity);

        backImg = (ImageView) findViewById(R.id.recordmemo_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**delete saved file**/
                //TODO 2.11 待修改
                Functions.go(true, RecordMemoActivity.this, RecordMemoActivity.this,
                        RecordActivity.class, null);
            }
        });

        record_travel_button = (Button) findViewById(R.id.record_travel_button);
        record_travel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true,RecordMemoActivity.this, RecordMemoActivity.this,
                        RecordActivity.class, null);
            }
        });

        DateOfLastOne = (TextView) findViewById(R.id.LastOneDate);
        DataBaseHelper helper = DataBaseHelper.getmInstance(getApplicationContext());
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor trackRoute_cursor = database.query("trackRoute",
                new String[]{"routesCounter", "track_no", "track_lat", "track_lng",
                        "track_start", "track_title", "track_totaltime", "track_completetime"},
                "track_start=\"0\"", null, null, null, null, null);
        if (trackRoute_cursor != null) {
            if (trackRoute_cursor.getCount() != 0) {
                trackRoute_cursor.moveToLast();
                String dateString = trackRoute_cursor.getString(7);
                DateOfLastOne.setText(dateString);
            } else {
                SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date=new Date();
                DateOfLastOne.setText(DateFormat.format(date));
            }
            trackRoute_cursor.close();
        }
        database.close();
        helper.close();

        mlistView = (ListView) findViewById(R.id.recordmemo_listView);

        mAdapter = new RecordMemoAdapter(RecordMemoActivity.this);
        mlistView.setAdapter(mAdapter);
        mlistView.setOnItemClickListener(new itemListener());
    }

    private class itemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putInt("WhichItem", position);
            Functions.go(false, RecordMemoActivity.this, RecordMemoActivity.this, RecordMemoDetailActivity.class, bundle);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true,RecordMemoActivity.this, RecordMemoActivity.this, RecordActivity.class, null);
        }
        return false;
    }
}
