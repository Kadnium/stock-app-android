package com.example.stockapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;

import com.example.stockapplication.datahelpers.AppData;
import com.example.stockapplication.datahelpers.BottomNavigationHandler;
import com.example.stockapplication.fragments.ChartFragment;
import com.example.stockapplication.fragments.MainFragment;
import com.example.stockapplication.fragments.OptionsFragment;
import com.example.stockapplication.R;
import com.example.stockapplication.fragments.SearchFragment;
import com.example.stockapplication.datahelpers.SensorHandler;

import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    private AppData appData;
    private BottomNavigationHandler bottomNavigationHandler;
    private SensorHandler sensorHandler;
    private boolean themeChanged = false;

    public void initBackend(){
        appData = AppData.getInstance(this);
        sensorHandler = appData.getSensorHandler(this);
        bottomNavigationHandler = new BottomNavigationHandler(this);
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
        Objects.requireNonNull(getSupportActionBar()).hide();

    }

    /**
     * Handle bottom navigation selections after back button press
     */
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

    /**
     * Get navigation item id's based on current visible view
     * @return Navigation item id
     */
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
            int id = R.id.home;
            // If theme is changed from settings or returned from desktop
            // Check if there are backstack, if not, start mainFragment
            // and init navigation with home selected
            if (fragmentManager.getBackStackEntryCount() == 0) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, MainFragment.newInstance()).commit();
            }else{
                // If backstack, get current visible fragment and init bottom navigation with it
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
            appData.setFirstUpdate(true);
            AppData.saveAppDataToSharedPrefs(this,appData,false);
        }
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }





}

