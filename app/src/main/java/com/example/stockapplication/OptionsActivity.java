package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

public class OptionsActivity extends AppCompatActivity {
    AppData appData;

    public void initBackend(Bundle savedInstanceState){
        if(appData == null){
            if(savedInstanceState != null){
                appData = AppData.parseAppDataFromBundle(savedInstanceState);
            }else{
                appData = AppData.parseAppData(getIntent());
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        initBackend(savedInstanceState);
        BottomNavigationHandler bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.settings);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Gson gson = new Gson();
        String data = gson.toJson(appData);
        outState.putString("appData",data);
        super.onSaveInstanceState(outState);
    }


}