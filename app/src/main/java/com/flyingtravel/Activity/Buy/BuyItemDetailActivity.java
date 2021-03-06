package com.flyingtravel.Activity.Buy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyingtravel.R;
import com.flyingtravel.Utility.DataBaseHelper;
import com.flyingtravel.Utility.Functions;
import com.flyingtravel.Utility.GlobalVariable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

public class BuyItemDetailActivity extends AppCompatActivity {

    ImageLoader loader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;

    int ItemPosition = 0;
    TextView ItemName, ItemDetail, ItemHeader;
    ImageView ItemImg, AddImg;
    DataBaseHelper helper;
    SQLiteDatabase database;
    String itemID;
    String[][] cartItem;
    LinearLayout addLayout, BackImg, shareLayout;

    //Filter//
    int FilterPostion = 999999;
    String FilterString = null;
    /**
     * GA
     **/
    public static Tracker tracker;

    //按下返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, BuyItemDetailActivity.this, BuyItemDetailActivity.this, BuyActivity.class, null);
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (itemID != null) {
            /**GA**/
            tracker.setScreenName("伴手禮內頁-ID:" + itemID);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
            /**GA**/
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyitem_detail_activity);
        /**GA**/
        GlobalVariable globalVariable = (GlobalVariable) getApplication();
        tracker = globalVariable.getDefaultTracker();
        /**GA**/

        //record which item is clicked
        Bundle bundle = this.getIntent().getExtras();
        if (bundle.containsKey("WhichItem")) {
            ItemPosition = bundle.getInt("WhichItem");
        }
        if (bundle.containsKey("FilterStringPosition"))
            FilterPostion = bundle.getInt("FilterStringPosition");
        if (bundle.containsKey("FilterString"))
            FilterString = bundle.getString("FilterString");
        UI();
        //show image
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageOnLoading(R.drawable.loading2)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory()
                .cacheOnDisk(true).build();
        listener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                ImageView imageView = (ImageView) view.findViewById(R.id.buyitem_Img);
                loader.displayImage(null, imageView, options, listener);

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        };
        getInfo();
    }


    void getInfo() {
        //===各個item的資料=02_24==//
//        Log.e("4.25","!!!!!getIngo!!!!!");
        helper = DataBaseHelper.getmInstance(BuyItemDetailActivity.this);
        database = helper.getWritableDatabase();

        if (FilterPostion != 999999 && FilterString != null) {
            Cursor goods_search_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                    "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, "goods_title LIKE ?", new String[]{"%" + FilterString + "%"}, null, null, null);
            if (goods_search_cursor != null) {
                if (goods_search_cursor.getCount() >= FilterPostion) {
                    goods_search_cursor.moveToPosition(FilterPostion);
                    if (goods_search_cursor.getString(1) != null)
                        itemID = goods_search_cursor.getString(1);
                    if (goods_search_cursor.getString(5) != null)
                        ItemDetail.setText(goods_search_cursor.getString(5));
                    if (goods_search_cursor.getString(2) != null)
                        ItemName.setText(goods_search_cursor.getString(2));
                    if (goods_search_cursor.getString(2) != null)
                        ItemHeader.setText(goods_search_cursor.getString(2));
                    if (goods_search_cursor.getString(3) != null)
                        if (goods_search_cursor.getString(3).startsWith("http://"))
                            loader.displayImage(goods_search_cursor.getString(3)
                                    , ItemImg, options, listener);
                        else
                            loader.displayImage("http://zhiyou.lin366.com/" + goods_search_cursor.getString(3)
                                    , ItemImg, options, listener);
                    new checkitem(new Functions.TaskCallBack() {
                        @Override
                        public void TaskDone(Boolean OrderNeedUpdate) {
                            methodThatDoesSomethingWhenTaskIsDone(OrderNeedUpdate);
                        }
                    }).execute();
                }
                goods_search_cursor.close();

            }
        } else {

            Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id",
                    "goods_title", "goods_url", "goods_money", "goods_content", "goods_click",
                    "goods_addtime"}, null, null, null, null, null);
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
                    if (goods_cursor.getString(3).startsWith("http://"))
                        loader.displayImage(goods_cursor.getString(3)
                                , ItemImg, options, listener);
                    else loader.displayImage("http://zhiyou.lin366.com/" + goods_cursor.getString(3)
                            , ItemImg, options, listener);
                new checkitem(new Functions.TaskCallBack() {
                    @Override
                    public void TaskDone(Boolean OrderNeedUpdate) {
                        methodThatDoesSomethingWhenTaskIsDone(OrderNeedUpdate);
                    }
                }).execute();
            }
            if (goods_cursor != null)
                goods_cursor.close();
        }
    }

    void UI() {
        ItemName = (TextView) findViewById(R.id.buyitemName_Text);
        ItemDetail = (TextView) findViewById(R.id.buyitemDetail_text);
        ItemHeader = (TextView) findViewById(R.id.buyItemHeader);
        ItemImg = (ImageView) findViewById(R.id.buyitem_Img);
        BackImg = (LinearLayout) findViewById(R.id.buyitem_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, BuyItemDetailActivity.this, BuyItemDetailActivity.this, BuyActivity.class, null);
            }
        });
        AddImg = (ImageView) findViewById(R.id.buyitemAdd_Img);
        AddImg.setVisibility(View.INVISIBLE);
        addLayout = (LinearLayout) findViewById(R.id.buyitem_addLayout);
        addLayout.setVisibility(View.INVISIBLE);
        shareLayout = (LinearLayout) findViewById(R.id.buyitem_share);
        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                //drawable -> bitmap
                Bitmap icon = ((BitmapDrawable) ItemImg.getDrawable()).getBitmap();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//                InputStream inputStream = getResources().openRawResource(R.drawable.icon_512);
                byte buf[] = new byte[1024];
                int len = 0;

                String path = Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg";
                File f = new File(path);
                try {
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
//                    while ((len = inputStream.read(buf)) > 0)
//                        fo.write(buf);
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
//                    Log.d("4.18", "error" + e.toString());
                }

                //setting share information
//                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, BuyItemDetailActivity.this.getResources().getString(R.string.title_text));
//                sharingIntent.putExtra(Intent.EXTRA_TEMPLATE, "testtt");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getContext().getResources().getString(R.string.title_text));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://zhiyou.lin366.com/shop/show.aspx?id=" + itemID);
//                sharingIntent.putExtra(Intent.EXTRA_TITLE,BuyItemDetailActivity.this.getResources().getString(R.string.title_text));
//                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                sharingIntent.setType("text/plain");
//                sharingIntent.setType("image/jpeg");
//                sharingIntent.setType("*/*");
//                Log.d("4.18", "path:" + path + " lens: " + len+" bytes"+bytes.size());
//                File file = new File(path);
//                Log.d("4.18", String.valueOf(file.exists()));


//                image/jpeg
                startActivity(Intent.createChooser(sharingIntent, BuyItemDetailActivity.this.getResources().getString(R.string.shareto_text)));
            }
        });
    }

    void setupAddDialog() {


        Bundle bundle = new Bundle();
        bundle.putInt("WhichItem", ItemPosition);
        //------------Dialog
        final Dialog BuyAdd = new Dialog(BuyItemDetailActivity.this);
        BuyAdd.setCancelable(true);
        BuyAdd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BuyAdd.setCanceledOnTouchOutside(true);
        BuyAdd.setContentView(R.layout.dialog_buyitem);

        final TextView totalPrice = (TextView) BuyAdd.findViewById(R.id.dialog_buyitem_totalPrice);
        final int[] totalprice = {0};
        Button okButton = (Button) BuyAdd.findViewById(R.id.dialog_buyitem_OkButton);
        Button cancelButton = (Button) BuyAdd.findViewById(R.id.dialog_buyitem_CancelButton);
        LinearLayout linearLayout = (LinearLayout) BuyAdd.findViewById(R.id.dialog_buyitem_layout);
        //------------Dialog view
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(BuyItemDetailActivity.this);

        final TextView[] nameText = new TextView[cartItem.length],
                fromText = new TextView[cartItem.length],
                moneyText = new TextView[cartItem.length],
                numberText = new TextView[cartItem.length],
                totalText = new TextView[cartItem.length];
        ImageView[] Img = new ImageView[cartItem.length];
        LinearLayout[] addButton = new LinearLayout[cartItem.length], minusButton = new LinearLayout[cartItem.length],
                delImg = new LinearLayout[cartItem.length];
        String itemName = null, itemImg = null, itemId = null;

        DataBaseHelper helper = DataBaseHelper.getmInstance(BuyItemDetailActivity.this);
        SQLiteDatabase database = helper.getWritableDatabase();
        if (FilterPostion != 999999 && FilterString != null) {
            Cursor goods_search_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                    "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, "goods_title LIKE ?", new String[]{"%" + FilterString + "%"}, null, null, null);
            if (goods_search_cursor != null) {
                if (goods_search_cursor.getCount() >= FilterPostion) {
                    goods_search_cursor.moveToPosition(FilterPostion);
                    if (goods_search_cursor.getString(1) != null)
                        itemId = goods_search_cursor.getString(1);
                    if (goods_search_cursor.getString(2) != null)
                        itemName = goods_search_cursor.getString(2);
                    if (goods_search_cursor.getString(3) != null)
                        itemImg = goods_search_cursor.getString(3);
                }
                goods_search_cursor.close();

            }
        } else {
            final Cursor goods_cursor = database.query("goods", new String[]{"totalCount", "goods_id", "goods_title",
                    "goods_url", "goods_money", "goods_content", "goods_click", "goods_addtime"}, null, null, null, null, null);
            if (goods_cursor != null && goods_cursor.getCount() >= ItemPosition) {
                goods_cursor.moveToPosition(ItemPosition);

                if (goods_cursor.getString(1) != null)
                    itemId = goods_cursor.getString(1);
                if (goods_cursor.getString(2) != null)
                    itemName = goods_cursor.getString(2);
                if (goods_cursor.getString(3) != null)
                    itemImg = goods_cursor.getString(3);
            }
            if (goods_cursor != null)
                goods_cursor.close();
        }
        for (int i = 0; i < cartItem.length; i++) {

            View view = LayoutInflater.from(BuyItemDetailActivity.this)
                    .inflate(R.layout.buylist_item, null);
            linearLayout.addView(view);

            nameText[i] = (TextView) view.findViewById(R.id.buyitemlist_nameTxt);
            fromText[i] = (TextView) view.findViewById(R.id.buyitemlist_itemTxt);
            moneyText[i] = (TextView) view.findViewById(R.id.butitemlist_moneyTxt);
            Img[i] = (ImageView) view.findViewById(R.id.buyitemlist_itemImg);
            delImg[i] = (LinearLayout) view.findViewById(R.id.buyitemlist_delImg);
            numberText[i] = (TextView) view.findViewById(R.id.buyitemlist_numbertext);
            totalText[i] = (TextView) view.findViewById(R.id.buyitemlist_totalTxt);
            addButton[i] = (LinearLayout) view.findViewById(R.id.buyitemlist_addbutton);
            minusButton[i] = (LinearLayout) view.findViewById(R.id.buyitemlist_minusbutton);
 /*
  editor.putInt("InBuyList", finalnumber);//the total count of the cart
  editor.putInt("InBuyListg" + finalItemId, smallItemCount);//這個大項目裡面有幾個小項目在購物車裡
  editor.putString("InBuyListg" + finalItemId+"id"+smallItemCount, cartItem[i][0]);//第幾個小項目的id
  editor.putInt("InBuyListgC" + finalItemId+"id"+smallItemCount, Integer.valueOf(numberText[i].getText().toString()));//第幾個小項目的數量
        *int howmany = sharedPreferences.getInt("InBuyList", 0);
        * **/
            //確認大項目裡的小項目有沒有在購物車裡面
//            if (sharedPreferences.getInt("InBuyListg" + itemId, 0) > 0) {//這個大項目有小項目在購物車裡面
//                Log.e("3.24","這個大項目有小項目在購物車裡面"+itemId+"幾個"+sharedPreferences.getInt("InBuyListg"+itemId,0));
//                for (int k = 0; k < sharedPreferences.getInt("InBuyListg" + itemId, 0); k++) {
//                    String a = sharedPreferences.getString("InBuyListg" + itemId + "id" + (k + 1), "NULL");
//                    Log.e("3.24","id:"+a);
//                    if (a.equals(cartItem[i][0]) && sharedPreferences.getInt("InBuyListgC" + itemId + "id" + (k + 1), 0) != 0) {
//                        numberText[i].setText(sharedPreferences.getInt("InBuyListgC" + itemId + "id" + (k + 1), 0) + "");
//                        Log.e("3.24","這個項目有在購物車裡面!!!"+cartItem[i][1]+"有幾個:"
//                                +sharedPreferences.getInt("InBuyListgC" + itemId+"id"+(k+1),0));
//                    }
//                }
//            }
//            if(sharedPreferences.contains(cartItem[i][0])&&(sharedPreferences.getInt(cartItem[i][0],0)>0)){
//                numberText[i].setText(sharedPreferences.getInt(cartItem[i][0],0)+"");
//                Log.e("5.27","cart::::"+itemName+"-"+cartItem[i][1]+": "+sharedPreferences.getInt(cartItem[i][0],0));
//            }else Log.e("5.27","not in cart!"+sharedPreferences.getInt(cartItem[i][0],0));


            nameText[i].setText(itemName);
            fromText[i].setText(cartItem[i][1]);
            String en = "1", cn = "1";
            if (sharedPreferences.contains("us"))
                en = sharedPreferences.getString("us", "1");
            if (sharedPreferences.contains("cn"))
                cn = sharedPreferences.getString("cn", "1");

            int ori = Integer.parseInt(cartItem[i][2]);
            String result = "" + ori;

            switch (Locale.getDefault().toString()) {
                case "zh_TW":
                    moneyText[i].setText(result);
                    break;

                case "zh_CN"://￥
                    if (cn != null) {
                        result = "" + (ori * Double.parseDouble(cn));
                        if (result.contains(".")) {
                            //有小數點!!
                            result = result.substring(0, result.indexOf("."));
                        }
                    }
                    moneyText[i].setText(result);
                    break;

                case "en_US":
                    if (en != null) {
                        result = "" + (ori * Double.parseDouble(en));
                        if (result.contains(".")) {
                            //有小數點!!
                            result = result.substring(0, result.indexOf("."));
                        }
                    }
                    moneyText[i].setText(result);
                    break;

                default:
                    moneyText[i].setText(result);

            }
//            moneyText[i].setText(cartItem[i][2]);
            if (Integer.parseInt(numberText[i].getText().toString()) == 0)
                numberText[i].setText("1");
            totalText[i].setText(Integer.valueOf(moneyText[i].getText().toString())
                    * Integer.valueOf(numberText[i].getText().toString()) + "");
            totalprice[0] += Integer.valueOf(totalText[i].getText().toString());
            totalPrice.setText(totalprice[0] + "");
            if (itemImg.startsWith("http"))
                loader.displayImage(itemImg, Img[i], options, listener);
            else
                loader.displayImage("http://zhiyou.lin366.com/" + itemImg, Img[i], options, listener);

            final int finalI = i;
            addButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    numberText[finalI].setText((Integer.valueOf(numberText[finalI].getText().toString()) + 1) + "");
                    totalText[finalI].setText((Integer.parseInt(moneyText[finalI].getText().toString())
                            * Integer.valueOf(numberText[finalI].getText().toString())) + "");
                    totalprice[0] += (Integer.parseInt(moneyText[finalI].getText().toString()));
                    totalPrice.setText(totalprice[0] + "");
                }
            });
            delImg[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    totalprice[0] = totalprice[0]
                            - (Integer.parseInt(numberText[finalI].getText().toString()) * Integer.parseInt(moneyText[finalI].getText().toString()));
                    numberText[finalI].setText("0");
                    totalText[finalI].setText(Integer.parseInt(numberText[finalI].getText().toString())
                            * Integer.valueOf(numberText[finalI].getText().toString()) + "");
                    totalPrice.setText(totalprice[0] + "");
                }
            });
            minusButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Integer.valueOf(numberText[finalI].getText().toString()) > 1) {
                        numberText[finalI].setText((Integer.valueOf(numberText[finalI].getText().toString()) - 1) + "");
                        totalText[finalI].setText(Integer.parseInt(moneyText[finalI].getText().toString())
                                * Integer.valueOf(numberText[finalI].getText().toString()) + "");
                        totalprice[0] = totalprice[0] - (Integer.parseInt(moneyText[finalI].getText().toString()));
                        totalPrice.setText(totalprice[0] + "");
                    }
                }
            });


        }

        //------------Dialog view


        final String finalItemId = itemId;
        final int[] finalNumber = {sharedPreferences.getInt("InBuyList", 0)};//購物車總數\
        if (sharedPreferences.getBoolean("AfterPay", false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("AfterPay", false);
            editor.apply();
        }
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                ////0527 更新/////
                String[] inCart = null;
                if (sharedPreferences.getString("inCart", null) != null)
                    inCart = sharedPreferences.getString("inCart", null).split(",");
                StringBuilder sb = new StringBuilder();
                for (int s = 0; s < cartItem.length; s++) {
                    sharedPreferences.edit().putInt(cartItem[s][0], Integer.valueOf(numberText[s].getText().toString())).apply();

                    Log.w("5.27", "id:" + cartItem[s][0] + " count:" + Integer.valueOf(numberText[s].getText().toString()));
                }
                ////0527 更新/////


//                int smallItemCount = 0;//大項目裡在購物車裡的小項目總數
//                確認大項目裡的小項目有沒有在購物車裡面
//                HashSet<String> List = new HashSet<String>();
//                Log.d("5.26", "finalItemID:::" + finalItemId + "cartItemlength:" + cartItem.length);
//                int count = sharedPreferences.getInt("InBuyListg" + finalItemId, 0);//這個大項目有幾個小項目在購物車裡
//                Log.e("5.26", "這個大項目有幾個小項目在購物車裡" + count);
//                for (int i = 0; i < cartItem.length; i++) {
//                    if (count > 0) { //這個大項目有小項目在購物車裡面
//                        for (int k = 0; k < count; k++) {
//                            String kthItemInShared = sharedPreferences.getString("InBuyListg" + finalItemId + "id" + (k + 1), "NULL");//shared裡的第K個小項目id
//                            if (Integer.valueOf(numberText[i].getText().toString()) > 0) {
                //                   editor.putString("InBuyListg" + finalItemId + "id" + smallItemCount, cartItem[i][0]);//第幾個小項目的id

//                                if (kthItemInShared.equals(cartItem[i][0])) {//第i個小項目id有在shared裡
//                                    Log.d("5.26", kthItemInShared + "第" + i + "個小項目id有在shared裡 finalItemID:::" + finalItemId + "   cartItem::" + cartItem[i][0] + "-K:" + k);
//                                    editor.putInt("InBuyListgC" + finalItemId + "id" + (k + 1), Integer.valueOf(numberText[i].getText().toString()));
//                                    editor.apply();
//                                    List.add(cartItem[i][0]);
//                                    List.add(cartItem[i][0] + numberText[i].getText().toString());
//                                } else {
//                                    Log.d("5.26", kthItemInShared + "第" + i + "個小項目id沒有在shared裡 finalItemID:::" + finalItemId + "cartItem" + cartItem[i][0] + "-K:" + k);
//                                    smallItemCount++;
//                                    finalNumber[0]++;
//                                    editor.putInt("InBuyList", finalNumber[0]);//the total count of the cart
//                                    editor.putInt("InBuyListg" + finalItemId, smallItemCount);//這個大項目裡面有幾個小項目在購物車裡
//                                    editor.putString("InBuyListg" + finalItemId + "id" + smallItemCount, cartItem[i][0]);//第幾個小項目的id
//                                    editor.putInt("InBuyListgC" + finalItemId + "id" + smallItemCount,
//                                            Integer.valueOf(numberText[i].getText().toString()));//第幾個小項目的數量
//                                    editor.apply();
//                                    List.add(cartItem[i][0]);
//                                    List.add(cartItem[i][0] + numberText[i].getText().toString());
//                                }
//                            } else {
//                                if (kthItemInShared.equals(cartItem[i][0])) {
//                                    smallItemCount--;
//                                    finalNumber[0]--;
//                                    editor.putInt("InBuyList", finalNumber[0]);//the total count of the cart
//                                    editor.putInt("InBuyListg" + finalItemId, smallItemCount);//這個大項目裡面有幾個小項目在購物車裡
//                                    editor.putString("InBuyListg" + finalItemId + "id" + smallItemCount, cartItem[i][0]);//第幾個小項目的id
//                                    editor.putInt("InBuyListgC" + finalItemId + "id" + smallItemCount,
//                                            Integer.valueOf(numberText[i].getText().toString()));//第幾個小項目的數量

//                                    List.add(cartItem[i][0]);
//                                    List.add(cartItem[i][0] + numberText[i].getText().toString());
//                                    editor.apply();
//                                }
//                            }
//                        }
//                    } else {
//                        if (Integer.valueOf(numberText[i].getText().toString()) > 0) {
//                            smallItemCount++;
//                            finalNumber[0]++;
//                            editor.putInt("InBuyList", finalNumber[0]);//the total count of the cart
//                            editor.putInt("InBuyListg" + finalItemId, smallItemCount);//這個大項目裡面有幾個小項目在購物車裡
//                            editor.putString("InBuyListg" + finalItemId + "id" + smallItemCount, cartItem[i][0]);//第幾個小項目的id
//                            editor.putInt("InBuyListgC" + finalItemId + "id" + smallItemCount,
//                                    Integer.valueOf(numberText[i].getText().toString()));//第幾個小項目的數量
//                            List.add(cartItem[i][0]);
//                            List.add(cartItem[i][0] + numberText[i].getText().toString());
//                            editor.apply();
//                        }
//                    }
//                }
//                if (List.size() > 0) {
//                    editor.putStringSet(finalItemId, List);
//                    Log.e("3.24", "inList!!!!");
//                    Log.e("3.24", List.toString());
//                    editor.apply();
//                }
                Functions.toast(BuyItemDetailActivity.this, BuyItemDetailActivity.this.getString(R.string.addok_text), 500);
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


        BuyAdd.show();
    }

    private void methodThatDoesSomethingWhenTaskIsDone(Boolean a) {
        if (a) {
//            Log.e("3.24","確認項目array長度"+cartItem.length);//OK
            addLayout.setVisibility(View.VISIBLE);
            addLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupAddDialog();
                }
            });
        }
    }

    class checkitem extends AsyncTask<String, Void, Boolean> {
        ProgressDialog mDialog;
        Functions.TaskCallBack taskCallBack;

        public checkitem(Functions.TaskCallBack taskCallBack) {
            this.taskCallBack = taskCallBack;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(BuyItemDetailActivity.this);
            mDialog.setMessage(BuyItemDetailActivity.this.getResources().getString(R.string.loading_text));
//            mDialog.setCancelable(false);
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
            super.onPreExecute();
        }

        /**
         * http://zhiyou.lin366.com/api/article/show.aspx
         * {"act":"show","id”:"609","type":"goods"}
         */

        @Override
        protected Boolean doInBackground(String... params) {
//            Log.e("3.9", "=========checkitem======doInBackground" + itemID);
            Boolean result = false;
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                getString = EntityUtils.toString(response.getEntity());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
            String htmlString = null;
            try {
                htmlString = Html.fromHtml(getString).toString();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            String state = null;
            try {
                state = new JSONObject(htmlString.substring(htmlString.indexOf("{"), htmlString.lastIndexOf("}") + 1)).getString("states");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
//            Log.d("3.10", "inBuy Item Detail: states" + state);
            if (state == null || state.equals("0"))
                return false;

            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONObject(getString).getJSONArray("guigelist");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            if (jsonArray != null && jsonArray.length() > 0) {
//                Log.e("3.10", "購物車項目?" + jsonArray + "length:" + jsonArray.length());
                cartItem = new String[jsonArray.length()][4];
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        cartItem[i][0] = jsonArray.getJSONObject(i).getString("id");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        cartItem[i][1] = jsonArray.getJSONObject(i).getString("guige");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        String sellprice = jsonArray.getJSONObject(i).getString("money");
                        if (sellprice.contains(".")) {//有小數點!!
                            sellprice = sellprice.substring(0, sellprice.indexOf("."));
                        }
                        cartItem[i][2] = sellprice;
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        cartItem[i][3] = jsonArray.getJSONObject(i).getString("img_url");
                    } catch (JSONException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                result = true;
                ContentValues cv = new ContentValues();
                Cursor goods_cursor_big = database.query("goodsitem", new String[]{"goods_bigid",
                                "goods_itemid", "goods_title", "goods_money", "goods_url"},
                        "goods_bigid=" + "\"" + itemID + "\"", null, null, null, null);

                for (int i = 0; i < jsonArray.length(); i++) {
                    if (goods_cursor_big != null) {//DB已經有這個大項目了->確認這個小項目在嗎
//                        Log.i("3.24", i + "!!!insertDB! in if");
                        Cursor goods_cursor = database.query("goodsitem", new String[]{"goods_bigid",
                                        "goods_itemid", "goods_title", "goods_money", "goods_url"},
                                "goods_bigid=" + "\"" + itemID + "\"" + " and goods_itemid=" + "\"" + cartItem[i][0] + "\"", null,
                                null, null, null);

                        if (goods_cursor == null || goods_cursor.getCount() <= 0) {//大項目裡面沒有小項目!! ->> 新增
//                            Log.i("3.24", i + "!!!insertDB! in second if" + itemID + "  " + cartItem[i][0] + "=====" + goods_cursor.getCount());
                            cv.clear();
                            cv.put("goods_bigid", itemID);
                            cv.put("goods_itemid", cartItem[i][0]);
                            cv.put("goods_title", cartItem[i][1]);
                            cv.put("goods_money", cartItem[i][2]);
                            cv.put("goods_url", cartItem[i][3]);
                            long result2 = database.insert("goodsitem", null, cv);
                        }
//                        else ////DB已經有這個大項目裡的這個小項目(i)
                        if (goods_cursor != null)
                            goods_cursor.close();
                        goods_cursor_big.close();
                    } else {
                        cv.clear();
                        cv.put("goods_bigid", itemID);
                        cv.put("goods_itemid", cartItem[i][0]);
                        cv.put("goods_title", cartItem[i][1]);
                        cv.put("goods_money", cartItem[i][2]);
                        cv.put("goods_url", cartItem[i][3]);
                        long result2 = database.insert("goodsitem", null, cv);
//                        Log.i("3.24", i + "~~~~~insertDB!" + result2);
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (mDialog.isShowing())
                mDialog.dismiss();
            taskCallBack.TaskDone(s);
            super.onPostExecute(s);
        }
    }
}
