package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.stockapplication.AppData;
import com.example.stockapplication.R;
import com.example.stockapplication.StockApi;
import com.example.stockapplication.StockApiCallback;
import com.example.stockapplication.StockData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    StockApi stockApi;
    AppData appData;

    public void init(){
        stockApi = new StockApi(this);
        appData = AppData.getAppData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        updateDailyGainers();
        updateDailyLosers();
    }
    public void updateDailyGainers(){
        stockApi.getDailyGainers(1,new StockApiCallback() {
            @Override
            public void onSuccess(String response, Context context) {
                Toast.makeText(context,response,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(VolleyError error,Context context) {
                Toast.makeText(context,error.networkResponse.toString(),Toast.LENGTH_LONG).show();

            }
        });
    }
    public void updateDailyLosers(){
        stockApi.getDailyLosers(1,new StockApiCallback() {
            @Override
            public void onSuccess(String response, Context context) {
                Toast.makeText(context,response,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(VolleyError error,Context context) {
                Toast.makeText(context,error.networkResponse.toString(),Toast.LENGTH_LONG).show();

            }
        });
    }

    public void updateFavourites(){
        List<StockData> userFavourites =  appData.getStockData();
        if(!userFavourites.isEmpty() ){
            List<String> symbolList = new ArrayList<>();
            for(StockData stock : userFavourites) {
                String symbol = stock.getSymbol();
                symbolList.add(symbol);
            }
            stockApi.getByTickerNames(symbolList, new StockApiCallback() {
                @Override
                public void onSuccess(String response, Context context) {

                }

                @Override
                public void onError(VolleyError error, Context context) {

                }
            });
        }


    }










}