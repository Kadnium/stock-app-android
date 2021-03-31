package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    StockApi stockApi;
    AppData appData;
    BottomNavigationHandler bottomNavigationHandler;
    SensorHandler sensorHandler;
    boolean themeChanged = false;

    public void initBackend(){
        appData = AppData.getInstance(this);
        stockApi = appData.getStockApi(this);
        sensorHandler = appData.getSensorHandler(this);// new SensorHandler(this);
        bottomNavigationHandler = new BottomNavigationHandler(this);//appData.getBottomNavigationHandler(this,R.id.home);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do theme checking first because will destroy activity if changed
        int theme = AppData.getThemeSetting(AppData.getSettingFromPrefs(this,AppData.SELECTED_THEME));
        if(AppCompatDelegate.getDefaultNightMode() != theme){
            AppCompatDelegate.setDefaultNightMode(theme);
            recreate();
            themeChanged = true;
            return;
        }
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

    }
    private void setBottomNavigationItemSelected(){
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if(currentFragment instanceof MainFragment || currentFragment instanceof ChartFragment) {
            bottomNavigationHandler.setSelectedItem(R.id.home);
        }else if (currentFragment instanceof SearchFragment) {
            bottomNavigationHandler.setSelectedItem(R.id.search);
        }else if (currentFragment instanceof OptionsFragment) {
            bottomNavigationHandler.setSelectedItem(R.id.settings);
        }
    }

    private int getCurrentFragmentsNavId(){
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if(currentFragment instanceof MainFragment) {
            return R.id.home;
        }else if (currentFragment instanceof SearchFragment) {
            return R.id.search;
        }else if (currentFragment instanceof OptionsFragment) {
            return R.id.settings;
        }
        return -1;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setBottomNavigationItemSelected();

        }



    @Override
    public void onStart() {
        super.onStart();
        if (!themeChanged) {
            initBackend();
            FragmentManager fragmentManager = getSupportFragmentManager();
            // If theme is changed from settings or returned from desktop
            // Without this, app will not redirect back to old fragment
            int id = R.id.home;
            if (fragmentManager.getBackStackEntryCount() == 0) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, MainFragment.newInstance()).commit();
               /* // Get the current backstack entry (settings fragment in this case)
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                Fragment fragment = fragmentManager.getFragments().get(0);
                // get latest fragment and redirect
                fragmentTransaction.replace(R.id.fragmentContainer, fragment).commit();
                setBottomNavigationItemSelected();*/
            }else{
                id = getCurrentFragmentsNavId();
            }
            bottomNavigationHandler.initNavigation(R.id.bottomNav, id);


        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When app is closed
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        // Activity change
        if(appData != null){
            AppData.saveAppDataToSharedPrefs(this,appData,false);
        }
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }

    @Override
    public void finish() {
        super.finish();
        // Override back button default animation

        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }




}

