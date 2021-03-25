package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ChartActivity extends AppCompatActivity {
    BottomNavigationHandler bottomNavigationHandler;
    AppData appData;
    SensorHandler sensorHandler;
    StockApi stockApi;


    public void initBackend() {
        appData = AppData.parseAppDataFromSharedPrefs(this);
        sensorHandler = new SensorHandler(this, null);
        stockApi = new StockApi(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.settings);
    }

    @Override
    public void onStart(){
        super.onStart();
        initBackend();
    }


}