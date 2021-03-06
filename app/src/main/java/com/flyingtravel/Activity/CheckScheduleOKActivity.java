package com.flyingtravel.Activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingtravel.Adapter.CheckScheduleFragmentAdapter;
import com.flyingtravel.Fragment.CheckScheduleFragment;
import com.flyingtravel.R;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.GlobalVariable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CheckScheduleOKActivity extends AppCompatActivity {
    String itemid = null;
    LinearLayout backImg, queLayout;
    CheckScheduleFragmentAdapter checkScheduleFragmentAdapter;

    String[][] data;
    String[] summary, address, lat;
    int count = 0;
    List<Fragment> fragments = new ArrayList<>();
    TextView dayText;
    TextView dateText;
    List<String> day = new ArrayList<>();
    List<String> date = new ArrayList<>();
    /**
     * GA
     **/
    public static Tracker tracker;
    //

    @Override
    protected void onResume() {
        super.onResume();
        if (itemid != null) {
            Log.e("5.8", "not null");
            /**GA**/
            tracker.setScreenName("行程查詢內頁-ID:" + itemid);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
            /**GA**/
        }
    }

    /**
     * http://zhiyou.lin366.com/test/diyline.aspx
     * http://zhiyou.lin366.com/api/diy/line.aspx
     * {"act":"show","id":"12"}
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkschedule_okactivity);
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getApplication();
        tracker = globalVariable.getDefaultTracker();
        /**GA**/
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey("order_id")) {
            itemid = bundle.getString("order_id");
            new getScheduleDetail(itemid, new Functions.TaskCallBack() {
                @Override
                public void TaskDone(Boolean OrderNeedUpdate) {
                    methodThatDoesSomethingWhenTaskIsDone(OrderNeedUpdate);
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            setupWebview();
        } else Toast.makeText(CheckScheduleOKActivity.this,
                CheckScheduleOKActivity.this.getResources().getString(R.string.wrongData_text), Toast.LENGTH_SHORT).show();
        dayText = (TextView) findViewById(R.id.checkschedule_dayText);
        dateText = (TextView) findViewById(R.id.checkschedule_dateText);
        queLayout = (LinearLayout) findViewById(R.id.checkschedule_queLayout);
        queLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", 2);
                Functions.go(false, CheckScheduleOKActivity.this, CheckScheduleOKActivity.this, MoreItemActivity.class, bundle);
            }
        });
        backImg = (LinearLayout) findViewById(R.id.checkschedule_backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, CheckScheduleOKActivity.this, CheckScheduleOKActivity.this,
                        CheckScheduleActivity.class, null);
            }
        });

    }

    //    }
    class getScheduleDetail extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progressDialog = new ProgressDialog(CheckScheduleOKActivity.this);
        Functions.TaskCallBack taskCallBack;

        public getScheduleDetail(String uid, Functions.TaskCallBack taskCallBack) {
            this.taskCallBack = taskCallBack;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(CheckScheduleOKActivity.this.getResources().getString(R.string.loading_text));
//            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = false;

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/diy/line.aspx");
            MultipartEntity entity = new MultipartEntity();
            Charset chars = Charset.forName("UTF-8");
            try {
                entity.addPart("json", new StringBody("{\"act\":\"show\",\"id\":\"" + itemid + "\"}", chars));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            post.setEntity(entity);
            HttpResponse resp = null;
            String resultM = null;
            String states = null;
            String message = "";
            try {
                resp = client.execute(post);
                resultM = EntityUtils.toString(resp.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                states = new JSONObject(resultM.substring(
                        resultM.indexOf("{"), resultM.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e2) {
                e2.printStackTrace();
            }
//            Log.i("3.25", "doInBackground" + states);
            if (states == null || states.equals("0"))
                return false;
            else {
                JSONArray jsonArray = null;

                try {
                    jsonArray = new JSONObject(resultM).getJSONArray("jindianlist");
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                if (jsonArray != null && jsonArray.length() > 0) {

                    count = jsonArray.length();
                    data = new String[count][6];
                    summary = new String[count];
                    address = new String[count];
                    lat = new String[count];
//                    Log.e("4.26","jsonlength:"+count);
                    String temp_summary = null, temp_address = null, temp_lat = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            data[i][0] = jsonArray.getJSONObject(i).getString("day");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
//                        Log.e("4.26", "Day" + data[i][0] + "i:" + i);
                        if (i != 0 && data[i][0].equals(data[i - 1][0])) {
                            summary[i - 1] = temp_summary;
                            try {
                                summary[i] = jsonArray.getJSONObject(i).getString("summary");
                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                            }

                            address[i - 1] = temp_address;
                            try {
                                address[i] = jsonArray.getJSONObject(i).getString("address");
                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                            }

                            lat[i - 1] = temp_lat;

                            try {
                                lat[i] = jsonArray.getJSONObject(i).getString("jinwei");
                            } catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
//                        Log.e("4.26","data[i][0]:"+data[i][0]+" i:"+i);
                        try {
                            data[i][1] = jsonArray.getJSONObject(i).getString("date");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            data[i][2] = jsonArray.getJSONObject(i).getString("time");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        try {
                            data[i][3] = jsonArray.getJSONObject(i).getString("summary");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        temp_summary = data[i][3];
                        try {
                            data[i][4] = jsonArray.getJSONObject(i).getString("address");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        temp_address = data[i][4];
                        try {
                            data[i][5] = jsonArray.getJSONObject(i).getString("jinwei");
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }

                        temp_lat = data[i][5];
                    }
                    return true;
                } else return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            taskCallBack.TaskDone(s);
            super.onPostExecute(s);
        }
    }

    private void methodThatDoesSomethingWhenTaskIsDone(Boolean a) {
        if (a) {
            int[] sameday = new int[data.length + 1];

//            Log.d("5.24", "data length:" + data.length + " itemid::" + itemid);
            String date_temp = "";
            for (int u = 0; u < data.length; u++) {
                if (date_temp.equals(data[u][0]))
                    if (sameday[Integer.parseInt(date_temp)] == 0)
                        sameday[Integer.parseInt(date_temp)] = sameday[Integer.parseInt(date_temp)] + 2;
                    else sameday[Integer.parseInt(date_temp)]++;
                date_temp = data[u][0];
            }
//            for (int k = 0; k < data.length; k++)
//                Log.d("5.24", "same day" + k + ":" + sameday[k]);
            Boolean addFragment = false;
            int coundown = 0, get = 0;
            String addDay = "";
            for (int q = 0; q < count; q++) {
                if (sameday[q + 1] != 0) {
//                    Log.w("5.24<<", ">>有兩天以上的行程" + q + " sameday[q+1]:" + sameday[q + 1] + " data:" + data[q][0]);
                    get = sameday[q + 1];
                    CheckScheduleFragment fragment = new CheckScheduleFragment();
                    Bundle bundle = new Bundle();
//                    bundle.putString("scheduleday", data[i][0]);
//                    bundle.putString("scheduledate", data[i][1]);
//                    bundle.putString("scheduletime", data[i][2]);
                    int w = 1;
                    for (int r = 0; r < count; r++) {

                        if (data[r][0].equals((q + 1) + "")) {
                            addDay = addDay + r + "_";
//                            Log.w("5.24<<", q + "是兩天以上的行程：day:" + data[r][0] + "address" + data[r][0] + "第幾個:" + r + "?" + w);
                            if (!day.contains(data[r][0]))
                                day.add(data[r][0]);
                            if (!date.contains(data[r][1]))
                                date.add(data[r][1]);
                            bundle.putString("scheduleday", data[r][0]);
                            bundle.putString("scheduledate", data[r][1]);
                            bundle.putString("scheduletime", data[r][2]);
                            bundle.putString("schedulesummary" + w, data[r][3]);
                            bundle.putString("scheduleaddress" + w, data[r][4]);
                            bundle.putString("scheduleajinwei" + w, data[r][5]);

                            w++;
                        }//是兩天以上的行程

                    }
//                    Log.d("5.24", "schedulecount" + (w - 1));

                    if (itemid != null)
                        bundle.putString("scheduleid", itemid);
                    bundle.putInt("schedulecount", (w - 1));
                    fragment.setArguments(bundle);
                    fragments.add(fragment);
                } else {
                    if (addDay.contains(String.valueOf(q)))
                        Log.w("5.24<<", "這" + q + "已經加過行程" + data[q][0]);
                    else {
//                        Log.w("5.24<<", "這" + q + "天有一個行程或是沒有行程" + data[q][0]);
                        CheckScheduleFragment fragment = new CheckScheduleFragment();
                        Bundle bundle = new Bundle();
                        if (!day.contains(data[q][0]))
                            day.add(data[q][0]);
                        if (!date.contains(data[q][1]))
                            date.add(data[q][1]);
                        bundle.putString("scheduleday", data[q][0]);
                        bundle.putString("scheduledate", data[q][1]);
                        bundle.putString("scheduletime", data[q][2]);
                        bundle.putString("schedulesummary", data[q][3]);
                        if (data[q][4] != null)
                            bundle.putString("schedulejinwei", data[q][4]);
                        else if (data[q][5] != null)
                            bundle.putString("schedulejinwei", data[q][5]);
                        if (itemid != null)
                            bundle.putString("scheduleid", itemid);
                        fragment.setArguments(bundle);
                        fragments.add(fragment);
                    }
                }
            }


//            Log.e("4.26","fragments"+fragments.size());
            ViewPager viewPager = (ViewPager) findViewById(R.id.checkschedule_viewpager);
            checkScheduleFragmentAdapter = new CheckScheduleFragmentAdapter(
                    CheckScheduleOKActivity.this.getSupportFragmentManager(),
                    viewPager, fragments, CheckScheduleOKActivity.this);
            viewPager.setOffscreenPageLimit(1);
            viewPager.setAdapter(checkScheduleFragmentAdapter);
            viewPager.setOnPageChangeListener(new PageListener());
//            if (day != null)
//                Log.e("5.30", day.size() + "=size");
            if (day != null && day.get(0) != null)
                dayText.setText("Day" + day.get(0));
            else
                dayText.setText("Day" + data[0][0]);

            if (date != null && date.get(0) != null)
                dateText.setText(date.get(0));
            else
                dateText.setText(data[0][1]);
            //0601 提醒可向旁邊滑
            if (checkScheduleFragmentAdapter.getCount() > 1)
                Functions.toast(CheckScheduleOKActivity.this, CheckScheduleOKActivity.this.getString(R.string.slide_text), 1000);
        }
    }

    private class PageListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        public void onPageSelected(int position) {
//            Log.e("4.26", "onPageSelected" + position + "--data[position][0]" + data[position][0]);
            if (day != null && day.get(position) != null)
                dayText.setText("Day" + day.get(position));
            else
                dayText.setText("Day" + data[position][0]);

            if (date != null && date.get(position) != null)
                dateText.setText(date.get(position));
            else
                dateText.setText(data[position][1]);
            /*
            pageNo = position + 1;
            if (pageNo == pages)
                nextPage.setVisibility(View.INVISIBLE);
            else nextPage.setVisibility(View.VISIBLE);

            if (pageNo == 1)
                lastPage.setVisibility(View.INVISIBLE);
            else lastPage.setVisibility(View.VISIBLE);

            minus = pageNo - 1;
            String get = String.valueOf(position + 1);
            number.setText(get);
            */
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
//            if (ifWebView && webView.canGoBack())
//                webView.goBack();
//            else
            Functions.go(true, CheckScheduleOKActivity.this, CheckScheduleOKActivity.this,
                    CheckScheduleActivity.class, null);
        return super.onKeyDown(keyCode, event);
    }

}
