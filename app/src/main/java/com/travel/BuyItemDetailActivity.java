package com.travel;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
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
import java.util.ArrayList;
import java.util.List;

public class BuyItemDetailActivity extends AppCompatActivity {

    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;

    int ItemPosition = 0;
    TextView ItemName, ItemDetail, ItemHeader;
    ImageView ItemImg, BackImg, AddImg;
    DataBaseHelper helper;
    SQLiteDatabase database;
    String itemID;

    //按下返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, BuyItemDetailActivity.this, BuyItemDetailActivity.this, BuyActivity.class, null);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyitem_detail_activity);

        //record which item is clicked
        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            ItemPosition = bundle.getInt("WhichItem");
        }


        ItemName = (TextView) findViewById(R.id.buyitemName_Text);
        ItemDetail = (TextView) findViewById(R.id.buyitemDetail_text);
        ItemHeader = (TextView) findViewById(R.id.buyItemHeader);
        ItemImg = (ImageView) findViewById(R.id.buyitem_Img);
        BackImg = (ImageView) findViewById(R.id.buyitem_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, BuyItemDetailActivity.this, BuyItemDetailActivity.this, BuyActivity.class, null);
            }
        });
        AddImg = (ImageView) findViewById(R.id.buyitemAdd_Img);
        AddImg.setVisibility(View.INVISIBLE);
        AddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupAddDialog();
            }
        });


        //show image
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageOnLoading(R.drawable.loading2)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory()
                .cacheOnDisc().build();
        listener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        };

        //===各個item的資料=02_24==//
        helper = new DataBaseHelper(BuyItemDetailActivity.this);
        database = helper.getWritableDatabase();
        Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null && goods_cursor.getCount() >= ItemPosition) {
            goods_cursor.moveToPosition(ItemPosition);
            if (goods_cursor.getString(1) != null)
                itemID = goods_cursor.getString(1);
            if (goods_cursor.getString(5) != null)
                ItemDetail.setText(goods_cursor.getString(5));
            if (goods_cursor.getString(2) != null)
                ItemName.setText(goods_cursor.getString(2));
            if (goods_cursor.getString(2) != null)
                ItemHeader.setText(goods_cursor.getString(2));
            if (goods_cursor.getString(3) != null)
                loader.displayImage("http://zhiyou.lin366.com/" + goods_cursor.getString(3)
                        , ItemImg, options, listener);
            Log.e("3.10", "id:" + itemID);
            new checkitem().execute();
        }

//        else Log.d("2.24", "not right!!!!" + ItemPosition);
        if (goods_cursor != null)
            goods_cursor.close();
        if (database.isOpen())
            database.close();


    }

    void setupAddDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt("WhichItem", ItemPosition);
        //=====0308 test popping Dialog
        final Dialog BuyAdd = new Dialog(BuyItemDetailActivity.this);
        BuyAdd.setCancelable(true);
        BuyAdd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BuyAdd.setCanceledOnTouchOutside(true);
        BuyAdd.setContentView(R.layout.dialog_buyitem);
        LinearLayout linearLayout = (LinearLayout) BuyAdd.findViewById(R.id.dialog_buyitem_layout);
        View view = LayoutInflater.from(BuyItemDetailActivity.this)
                .inflate(R.layout.buylist_item, null);
        linearLayout.addView(view);
        TextView nameText = (TextView) view.findViewById(R.id.buyitemlist_nameTxt);
        TextView fromText = (TextView) view.findViewById(R.id.buyitemlist_fromTxt);
        final TextView moneyText = (TextView) view.findViewById(R.id.butitemlist_moneyTxt);
        ImageView Img = (ImageView) view.findViewById(R.id.buyitemlist_itemImg);
        ImageView delImg = (ImageView) view.findViewById(R.id.buyitemlist_delImg);
        final TextView numberText = (TextView) view.findViewById(R.id.buyitemlist_numbertext);
        final TextView totalText = (TextView) view.findViewById(R.id.buyitemlist_totalTxt);
        Button addButton = (Button) view.findViewById(R.id.buyitemlist_addbutton);
        Button minusButton = (Button) view.findViewById(R.id.buyitemlist_minusbutton);
        Button okButton = (Button) BuyAdd.findViewById(R.id.dialog_buyitem_OkButton);
        Button cancelButton = (Button) BuyAdd.findViewById(R.id.dialog_buyitem_CancelButton);
        final List<String> goods_id = new ArrayList<>();
        DataBaseHelper helper = new DataBaseHelper(BuyItemDetailActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BuyItemDetailActivity.this);
        final Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);
        if (goods_cursor != null && goods_cursor.getCount() >= ItemPosition) {
            goods_cursor.moveToPosition(ItemPosition);
            //name,name,money,img
            goods_id.clear();
            goods_id.add(goods_cursor.getString(1));

            nameText.setText(goods_cursor.getString(2));
            fromText.setText(goods_cursor.getString(2));

            moneyText.setText(goods_cursor.getString(4));
            numberText.setText("" + sharedPreferences.getInt(goods_cursor.getString(1), 0));

            Log.d("3.8", "shared:" + sharedPreferences.getInt(goods_cursor.getString(1), 0));

            loader.displayImage("http://zhiyou.lin366.com/" + goods_cursor.getString(3)
                    , Img, options, listener);
            goods_cursor.close();
        }
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberText.setText((Integer.valueOf(numberText.getText().toString() + "") + 1) + "");
                totalText.setText((Integer.parseInt(moneyText.getText().toString())
                        * Integer.valueOf(numberText.getText().toString()) + ""));
            }
        });
        delImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberText.setText("0");
                totalText.setText(Integer.parseInt(numberText.getText().toString())
                        * Integer.valueOf(numberText.getText().toString() + ""));
            }
        });
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.valueOf(numberText.getText().toString() + "") > 0) {
                    numberText.setText((Integer.valueOf(numberText.getText().toString() + "") - 1) + "");
                    totalText.setText(Integer.parseInt(moneyText.getText().toString() + "")
                            * Integer.valueOf(numberText.getText().toString() + "") + "");
                }
            }
        });
        final int[] number = {sharedPreferences.getInt("InBuyList", 0)};
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goods_id.get(0) != null) {
                    number[0]++;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(goods_id.get(0), Integer.valueOf(numberText.getText().toString() + ""));
                    editor.putInt("InBuyList", number[0]);
//                            Log.d("3.8", "InBuyList number[0]:"+number[0]);
                    editor.putInt("InBuyList" + number[0], ItemPosition);
                    editor.apply();

                } else Log.d("3.8", "null");
                if (BuyAdd.isShowing())
                    BuyAdd.cancel();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuyAdd.isShowing())
                    BuyAdd.cancel();
            }
        });
        totalText.setText(Integer.valueOf(numberText.getText().toString())
                * Integer.valueOf(numberText.getText().toString()) + "");

        BuyAdd.show();
    }

    class checkitem extends AsyncTask<String, Void, String> {
        /**
         * http://zhiyou.lin366.com/api/article/show.aspx
         * {"act":"show","id”:"609","type":"goods"}
         */
        @Override
        protected String doInBackground(String... params) {
            Log.e("3.9", "=========checkitem======doInBackground" + itemID);
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://zhiyou.lin366.com/api/article/show.aspx");
            MultipartEntity multipartEntity = new MultipartEntity();
            Charset charset = Charset.forName("UTF-8");
            try {
                multipartEntity.addPart("json", new StringBody("{\"act\":\"show\",\"id\":\"" + itemID + "\",\"type\":\"goods\"}", charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(multipartEntity);
            HttpResponse response = null;
            String getString = null;
            try {
                response = client.execute(post);
                Log.d("3.10", "購物車項目? 1getString: " + response);
            } catch (IOException e) {
                Log.e("3.10", e.toString());
            }
            Log.d("3.10", "購物車項目: " + response.getEntity().toString());

            try {
                Log.d("3.10", "購物車項目? 2getString: start");
                getString = EntityUtils.toString(response.getEntity());
                Log.d("3.10", "購物車項目? 2getString: " + getString);
            } catch (IOException e) {
                Log.e("3.10", "error!!!!!");
                Log.e("3.10", e.toString() + "error");
            }
            Log.e("3.10", "購物車項目? getString: " + getString);


            if (getString.contains("guigelist"))
                Log.d("3.11", "contain!!!");

            String state = null;
            String totalcount = null;
            try {
                state = new JSONObject(getString.substring(getString.indexOf("{"), getString.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            Log.d("3.10", "inBuy Item Detail: states" + state);
            /*
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONObject(getString).getJSONArray("guigelist");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("3.10","購物車項目?"+jsonArray+"getString: "+getString);
            */
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
