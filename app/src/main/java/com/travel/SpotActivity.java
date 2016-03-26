package com.travel;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.travel.Adapter.SpotFragmentPagerAdapter;
import com.travel.Utility.Functions;


public class SpotActivity extends FragmentActivity {

    private ImageView BackImg;

    //3.10
    final int REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_activity);

        BackImg = (ImageView) findViewById(R.id.maps_backImg);
        BackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.go(true, SpotActivity.this, SpotActivity.this, HomepageActivity.class, null);
            }
        });

        // Prompt the user to Enabled GPS
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // API 23 Needs to Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SpotFragmentPagerAdapter(getSupportFragmentManager(), SpotActivity.this));

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            int position = bundle.getInt("position");
            if (position == 1) {
                viewPager.setCurrentItem(1);
                Log.e("3/23_", "viewPager.setCurrentItem: position" + position);
            }
        }
        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // Android 系統返回鍵
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Functions.go(true, SpotActivity.this, SpotActivity.this, HomepageActivity.class, null);
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to

            } else {
                // Permission was denied or request was cancelled
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                Toast.makeText(SpotActivity.this, "請允許寶島好智遊存取您的位置!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

