package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {
    BottomNavigationHandler bottomNavigationHandler;
    AppData appData;
    SensorHandler sensorHandler;
    StockApi stockApi;


    private void initBackend() {
        appData = AppData.parseAppDataFromSharedPrefs(this);
        sensorHandler = new SensorHandler(this, null);
        stockApi = new StockApi(this);
    }

    private void initChart(){
        Intent intent = getIntent();
        String ticker = intent.getStringExtra("Ticker");
        stockApi.getChart(ticker, StockApi.DAILY_RANGE, new StockApiCallback() {
            @Override
            public void onSuccess(List<StockData> response, Context context) {
                List<StockData> respon = new ArrayList<>();
            }

            @Override
            public void onError(VolleyError error, Context context) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        getSupportActionBar().hide();
        bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav, -1);


    }

    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        initChart();
    }


}