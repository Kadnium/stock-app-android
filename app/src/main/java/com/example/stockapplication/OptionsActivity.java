package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class OptionsActivity extends AppCompatActivity {
    AppData appData;

    public void initBackend(){
        if(appData == null){
            appData = AppData.parseAppDataFromSharedPrefs(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        initBackend();
        BottomNavigationHandler bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.settings);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppData.saveAppDataToSharedPrefs(this,appData,true);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppData.saveAppDataToSharedPrefs(this,appData,false);

    }


}