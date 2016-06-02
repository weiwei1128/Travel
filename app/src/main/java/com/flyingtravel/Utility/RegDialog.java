package com.flyingtravel.Utility;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.flyingtravel.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by wei on 2016/5/31.
 *
 * 偷吃步 只做到 中國 [省][市]
 *
 *
 */public class RegDialog extends Dialog {
    Context context;
    Tracker tracker;
    Activity activity;
    RegDialog signDialog = this;
    Spinner CountrySpinner, CitySpinner, DistrictSpinner,ChinaDiatrictSpinner;
    String[] country, city, district;
    ArrayAdapter<String> countryAdapter, cityAdapter, districtAdapter;
    EditText addr, email, phone, name, password, account;

    public RegDialog(Context context, Tracker tracker, Activity activity) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.tracker = tracker;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.dialog_reg);

        /********/
        Button OK = (Button) findViewById(R.id.reg_ok);
        Button cancel = (Button) findViewById(R.id.reg_cancel);
        account = (EditText) findViewById(R.id.reg_account);
        password = (EditText) findViewById(R.id.reg_password);
        name = (EditText) findViewById(R.id.reg_name);
        phone = (EditText) findViewById(R.id.reg_phone);
        email = (EditText) findViewById(R.id.reg_email);
        addr = (EditText) findViewById(R.id.reg_addr);
        OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account.getText().toString().equals("")
                        || password.getText().toString().equals("")
                        || name.getText().toString().equals("")
                        || phone.getText().toString().equals("")
                        || email.getText().toString().equals("")
                        || addr.getText().toString().equals("")
                        ) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.InputData_text), Toast.LENGTH_SHORT).show();
                } else {
                    /***GA**/
                    tracker.send(new HitBuilders.EventBuilder().setCategory("註冊")
                            .build());
                    /***GA**/
                    sighUp sighUp = new sighUp(account.getText().toString(),
                            password.getText().toString(), name.getText().toString(),
                            phone.getText().toString(), email.getText().toString(),
                            addr.getText().toString(), signDialog, activity);
                    sighUp.execute();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signDialog.isShowing())
                    signDialog.dismiss();
            }
        });
        this.setCancelable(false);
        /****/
        UI();
    }

    void UI() {
        //TODO china part
        CountrySpinner = (Spinner) findViewById(R.id.spinner_country);
        CitySpinner = (Spinner) findViewById(R.id.spinner_city);
        DistrictSpinner = (Spinner) findViewById(R.id.spinner_district);
//        ChinaDiatrictSpinner = (Spinner)findViewById(R.id.spinner_china);
//        ChinaDiatrictSpinner.setVisibility(View.INVISIBLE);
        //---Country---//
        country = context.getResources().getStringArray(R.array.country);
        countryAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, country);
        CountrySpinner.setAdapter(countryAdapter);
        CountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                addr.setText(CountrySpinner.getSelectedItem().toString() + CitySpinner.getSelectedItem().toString());
                switch (position) {
                    case 0:
                        city = context.getResources().getStringArray(R.array.China);
                        break;
                    case 1:
                        city = context.getResources().getStringArray(R.array.Taiwan);
                        break;
                    default:
                        city = context.getResources().getStringArray(R.array.China);
                        break;
                }
                cityAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, city);
                CitySpinner.setAdapter(cityAdapter);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //---Country---//


        //---City---//
        city = context.getResources().getStringArray(R.array.China);
        cityAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, city);
        CitySpinner.setAdapter(cityAdapter);
        CitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("5.31", "citygetselected");
                addr.setText(CountrySpinner.getSelectedItem().toString() + CitySpinner.getSelectedItem().toString());
                switch (countryAdapter.getPosition(CountrySpinner.getSelectedItem().toString())) {
                    case 0://國家:中國
                        switch (position) {
                            case 0://北京
                                district = context.getResources().getStringArray(R.array.Beijing);
                                break;
                            case 1://天津
                                district = context.getResources().getStringArray(R.array.Tianjin);
                                break;
                            case 2://河北省
                                district = context.getResources().getStringArray(R.array.Hebei);
                                break;
                            case 3://山西省
                                district = context.getResources().getStringArray(R.array.Shanxi);
                                break;
                            case 4://內蒙古自治區
                                district = context.getResources().getStringArray(R.array.Neimenggu);
                                break;
                            case 5://遼寧省
                                district = context.getResources().getStringArray(R.array.Liaoning);
                                break;
                            case 6://吉林省
                                district = context.getResources().getStringArray(R.array.Jilin);
                                break;
                            case 7://黑龍江省
                                district = context.getResources().getStringArray(R.array.Heilongjiang);
                                break;
                            case 8://上海市
                                district = context.getResources().getStringArray(R.array.Shanghai);
                                break;
                            case 9://江蘇省
                                district = context.getResources().getStringArray(R.array.Jiangsu);
                                break;
                            case 10://浙江省
                                district = context.getResources().getStringArray(R.array.Zhejiang);
                                break;
                            case 11://安徽省
                                district = context.getResources().getStringArray(R.array.Anhui);
                                break;
                            case 12://福建省
                                district = context.getResources().getStringArray(R.array.Fujian);
                                break;
                            case 13://江西省
                                district = context.getResources().getStringArray(R.array.Jiangxi);
                                break;
                            case 14://山東省
                                district = context.getResources().getStringArray(R.array.Shandong);
                                break;
                            case 15://河南省
                                district = context.getResources().getStringArray(R.array.Henan);
                                break;
                            case 16://湖北省
                                district = context.getResources().getStringArray(R.array.Hubei);
                                break;
                            case 17://湖南省
                                district = context.getResources().getStringArray(R.array.Hunan);
                                break;
                            case 18://廣東省
                                district = context.getResources().getStringArray(R.array.Guangdong);
                                break;
                            case 19://廣西壯族
                                district = context.getResources().getStringArray(R.array.Guangxi);
                                break;
                            case 20://海南
                                district = context.getResources().getStringArray(R.array.Hainan);
                                break;
                            case 21://重慶市
                                district = context.getResources().getStringArray(R.array.Chungking);
                                break;
                            case 22://四川省
                                district = context.getResources().getStringArray(R.array.Sichuan);
                                break;
                            case 23://貴州
                                district = context.getResources().getStringArray(R.array.Guizhou);
                                break;
                            case 24://雲南省
                                district = context.getResources().getStringArray(R.array.Yunnan);
                                break;
                            case 25://西藏自治區
                                district = context.getResources().getStringArray(R.array.Xizang);
                                break;
                            case 26://陝西
                                district = context.getResources().getStringArray(R.array.Shaanxi);
                                break;
                            case 27://甘肅
                                district = context.getResources().getStringArray(R.array.Gansu);
                                break;
                            case 28://青海省
                                district = context.getResources().getStringArray(R.array.Chinhai);
                                break;
                            case 29://寧夏
                                district = context.getResources().getStringArray(R.array.Ningshia);
                                break;
                            case 30://新疆
                                district = context.getResources().getStringArray(R.array.Xinjiang);
                                break;
                            case 31://香港
                                district = context.getResources().getStringArray(R.array.Hongkong);
                                break;
                            case 32://澳門
                                district = context.getResources().getStringArray(R.array.Aomen);
                                break;
                            default:
                                district = context.getResources().getStringArray(R.array.Beijing);
                                break;
                        }
                        break;
                    case 1://國家:台灣
                        switch (cityAdapter.getPosition(CitySpinner.getSelectedItem().toString())) {
                            case 0://新北市
                                district = context.getResources().getStringArray(R.array.newTaipei);
                                break;
                            case 1://基隆市
                                district = context.getResources().getStringArray(R.array.Keelung);
                                break;
                            case 2://台北市
                                district = context.getResources().getStringArray(R.array.Taipei);
                                break;
                            case 3://桃園縣
                                district = context.getResources().getStringArray(R.array.Taoyuan);
                                break;
                            case 4://新竹縣
                                district = context.getResources().getStringArray(R.array.HsinchuCountry);
                                break;
                            case 5://新竹市
                                district = context.getResources().getStringArray(R.array.HsinchuCity);
                                break;
                            case 6://苗栗縣
                                district = context.getResources().getStringArray(R.array.Miaoli);
                                break;
                            case 7://台中市
                                district = context.getResources().getStringArray(R.array.Taichung);
                                break;
                            case 8://南投縣
                                district = context.getResources().getStringArray(R.array.Nantou);
                                break;
                            case 9://彰化縣
                                district = context.getResources().getStringArray(R.array.Changhua);
                                break;
                            case 10://雲林縣
                                district = context.getResources().getStringArray(R.array.Yunlin);
                                break;
                            case 11://嘉義縣
                                district = context.getResources().getStringArray(R.array.ChiayiCountry);
                                break;
                            case 12://嘉義市
                                district = context.getResources().getStringArray(R.array.ChiayiCity);
                                break;
                            case 13://台南市
                                district = context.getResources().getStringArray(R.array.Tainan);
                                break;
                            case 14://高雄市
                                district = context.getResources().getStringArray(R.array.Kaohsiung);
                                break;
                            case 15://屏東縣
                                district = context.getResources().getStringArray(R.array.Pingtung);
                                break;
                            case 16://宜蘭縣
                                district = context.getResources().getStringArray(R.array.Ilan);
                                break;
                            case 17://花蓮縣
                                district = context.getResources().getStringArray(R.array.Hualien);
                                break;
                            case 18://台東縣
                                district = context.getResources().getStringArray(R.array.Taitung);
                                break;
                            case 19://澎湖縣
                                district = context.getResources().getStringArray(R.array.Penghu);
                                break;
                            case 20://金門縣
                                district = context.getResources().getStringArray(R.array.Kinmen);
                                break;
                            case 21://連江縣
                                district = context.getResources().getStringArray(R.array.Lianjiang);
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        district = context.getResources().getStringArray(R.array.newTaipei);
                        break;
                }

                districtAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, district);
                DistrictSpinner.setAdapter(districtAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //---City---//
        district = context.getResources().getStringArray(R.array.newTaipei);
        districtAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, district);
        DistrictSpinner.setAdapter(districtAdapter);
        DistrictSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                addr.setText(CountrySpinner.getSelectedItem().toString() + CitySpinner.getSelectedItem().toString() + DistrictSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }
}
