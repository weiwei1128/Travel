package com.travel;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Utility.DataBaseHelper;
import com.travel.Utility.Functions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class BuyItemListConfirmActivity extends AppCompatActivity {
    ImageView backImg;
    TextView buylistText, totalText;
    DataBaseHelper helper;
    SQLiteDatabase database;
    LinearLayout confrimLayout;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, BuyItemListConfirmActivity.this, BuyItemListConfirmActivity.this,
                    BuyItemListActivity.class, null);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyitem_list_confirm_activity);
        //get shop list item
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        helper = new DataBaseHelper(BuyItemListConfirmActivity.this);
        database = helper.getWritableDatabase();
        buylistText = (TextView) findViewById(R.id.buyitemlistconfirm_listText);
        backImg = (ImageView) findViewById(R.id.buyitemlistconfirm_backImg);
        confrimLayout = (LinearLayout) findViewById(R.id.buyitemlistconfirm_confirmLay);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, BuyItemListConfirmActivity.this, BuyItemListConfirmActivity.this,
                        BuyItemListActivity.class, null);
            }
        });

        confrimLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO need modify!
                Toast.makeText(BuyItemListConfirmActivity.this, "建構中!", Toast.LENGTH_SHORT).show();
            }
        });

        int totalnumber = 0, getitemPosition = 0, BiginCart = 0, totalmoney = 0;
        String BigitemID = null, SmallitemID = null, itemName = null;
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null && goods_cursor.getCount() != 0) {
            while (goods_cursor.moveToNext()) {
                BiginCart = sharedPreferences.getInt("InBuyListg" + goods_cursor.getString(1), 0);
                if (BiginCart > 0) {
                    for (int k = 0; k < BiginCart; k++) {
                        String a = sharedPreferences.getString("InBuyListg" + goods_cursor.getString(1) + "id" + (k + 1), null);
                        int smallItemCount = sharedPreferences.getInt("InBuyListgC" + goods_cursor.getString(1) + "id" + (k + 1), 0);
                        if (a != null && smallItemCount != 0) {

                            BigitemID = goods_cursor.getString(1);
                            getitemPosition = k + 1;
                            SmallitemID = a;

                            if (BigitemID != null && SmallitemID != null) {
                                Cursor goods_cursor_big = database.query("goodsitem", new String[]{"goods_bigid",
                                                "goods_itemid", "goods_title", "goods_money", "goods_url"},
                                        "goods_bigid=? and goods_itemid=?", new String[]{BigitemID, SmallitemID}, null, null, null);
                                goods_cursor_big.moveToFirst();
                                int money = Integer.valueOf(goods_cursor_big.getString(3)) * smallItemCount;
                                totalmoney = totalmoney + money;
                                buylistText.append(goods_cursor.getString(2) + " " + goods_cursor_big.getString(2) + " : "
                                        + smallItemCount + " 個 " + " $" + money + "\n");
                            }
                        }
                    }
                }
//                else {//這個大項目沒有小項目在購物車裡面
//                    Log.e("3.24", "這不是我要的!!!!" + getPosition + "." + position+"///"+goods_cursor.getString(1));
//                }
//                Log.i("3.24","我在while裡面!!!要執行下一輪");

            }
        }

        //////////^^^^^
        if (goods_cursor != null)
            goods_cursor.close();
        totalText = (TextView) findViewById(R.id.buyitemlistconfirm_totalText);
        totalText.setText(totalmoney + "");
    }


    /**
     * http://zhiyou.lin366.com/api/order/index.aspx
     * {"act":"add","uid":"ljd110@qq.com","name":"name","tel":"tel",
     * "email":"email","content":"content","express":"1","payment":"3",
     * "sname":"sname","stel":"stel","semail":"semail","sstate":"sstate",
     * "scity":"scity","saddress":"saddress","carlist":[{"gid":"123","num":"1"},
     * {"gid":"123","num":"2"}]}
     * <p/>
     * <p/>
     * 回傳資料
     * {"states":"1","msg":"加入成功","id":"45"}
     */
    class SendOrder extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/order/index.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json",
                        new StringBody("{\"act\":\"add\"," +
                                "\"uid\":\"" + "!!!username!!!" + "\",\"name\":\"" + "!!!username!!!"
                                + "\",\"tel\":\"" + "!!!tel!!!" + "\",\"email\":\"" + "!!!email!!!"
                                + "\",\"content\":\"" + "!!!content!!!" + "\",\"express\":\"" + "!!!數字!!!"
                                + "\",\"payment\":\"" + "!!!數字!!!" + "\",\"sname\":\"" + "!!!sname!!!"
                                + "\",\"stel\":\"" + "!!!stel!!!" + "\",\"semail\":\"" + "!!!semail!!!"
                                + "\",\"sstate\":\"" + "!!!s!tate!!!" + "\",\"scity\":\"" + "!!!scity!!!"
                                + "\",\"saddress\":\"" + "!!!saddress!!!" + "\",\"carlist\":[{\"gid\":\""
                                + "!!!數字!!" + "\",\"num\":\"" + "!!!數字!!!" + "\"},{\"gid\":\"" + "!!!數字!!"
                                + "\",\"num\":\"" + "!!!數字!!" + "\"}]}", charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(multipartEntity);
            HttpResponse response = null;
            String getString = null;
            try {
                response = client.execute(post);
            } catch (IOException e) {
                Log.e("3.10", e.toString());
            }
            try {
                getString = EntityUtils.toString(response.getEntity());
            } catch (IOException | NullPointerException e) {
                Log.e("3.10", e.toString() + "error");
            }
            String state = null;
            String totalcount = null;
            try {
                state = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


}
