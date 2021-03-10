package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    RecyclerView favouriteRecyclerView;
    RecyclerView mostChangedRecyclerView;

    RecyclerAdapter favouriteAdapter;
    RecyclerAdapter mostChangedAdapter;
    public void initBackend(){
        stockApi = new StockApi(this);
        appData = AppData.getAppData();
        StockData temp = new StockData("NOK","HKI","NOKIA CORPORATION",5,5.5);
        StockData temp2 = new StockData("GME","NDQ","GAMESTOP CORPORATION",25,259);
        List<StockData> list = new ArrayList<>();
        list.add(temp);
        list.add(temp2);

        List<StockData> list2 = new ArrayList<>();
        list2.add(temp);
        list2.add(temp2);
        appData.setMostChanged(list2);
        appData.setFavouriteData(list);
    }
    public void initListViews(){
        // Most changed
        mostChangedAdapter = new RecyclerAdapter(this,appData.getMostChanged());
        mostChangedRecyclerView = findViewById(R.id.mostChangedRecyclerView);
        mostChangedRecyclerView.setAdapter(mostChangedAdapter);
        mostChangedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Favourites
        favouriteAdapter = new RecyclerAdapter(this,appData.getFavouriteData());
        favouriteRecyclerView = findViewById(R.id.favouriteRecyclerView);
        favouriteRecyclerView.setAdapter(favouriteAdapter);
        favouriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBackend();
        initListViews();

        //updateDailyGainers();
        //updateDailyLosers();
        //updateFavourites();
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
        List<StockData> userFavourites = appData.getFavouriteData();
        if(!userFavourites.isEmpty()){
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