package com.example.stockapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.stockapplication.AppData;
import com.example.stockapplication.R;
import com.example.stockapplication.StockApi;
import com.example.stockapplication.StockApiCallback;
import com.example.stockapplication.StockData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.stockapplication.StockData.generateUuid;

public class MainActivity extends AppCompatActivity{
    StockApi stockApi;
    AppData appData;
    RecyclerView favouriteRecyclerView;
    RecyclerView mostChangedRecyclerView;

    RecyclerAdapter favouriteAdapter;
    RecyclerAdapter mostChangedAdapter;
    BottomNavigationHandler bottomNavigationHandler;

    private Toolbar toolbar;
    public void initBackend(){
        stockApi = new StockApi(this);
        appData = AppData.getAppData();
        StockData temp1 = new StockData("NOK","HKI","NOKIA CORPORATION",5,5.5,true,generateUuid());
        StockData temp123 = new StockData("GME","NDQ","GAMESTOP CORPORATION",25,259,true,generateUuid());
        StockData temp124 = new StockData("GME2","NDQ","GAMESTOP CORPORATION",25,259,true,generateUuid());
        StockData temp125 = new StockData("GME3","NDQ","GAMESTOP CORPORATION",25,259,true,generateUuid());
        StockData temp1252 = new StockData("GME4","NDQ","GAMESTOP CORPORATION",25,259,true,generateUuid());
        StockData temp126 = new StockData("GME5","NDQ","GAMESTOP CORPORATION",25,259,true,generateUuid());
        List<StockData> list = new ArrayList<>();
        list.add(temp1);
        list.add(temp123);
        list.add(temp124);
        list.add(temp125);
        list.add(temp1252);
        list.add(temp126);
        list.add(temp1);
        list.add(temp123);

        StockData temp2 = new StockData("AMC","NDQ","AMERICAN MOVIE CORP",-5,6.9,false,null);
        StockData temp22 = new StockData("TSLA","NDQ","TESLA",20,500,false,null);
        List<StockData> list2 = new ArrayList<>();
        list2.add(temp2);
        list2.add(temp22);
        appData.setMostChanged(list2);
        appData.setFavouriteData(list);
    }
    public void initListViews(){
        // Most changed
        mostChangedAdapter = new RecyclerAdapter(this, appData.getMostChanged(), appData, R.id.mostChangedRecyclerView, new AdapterRefresh() {
            @Override
            public void onFavouriteAdded(int callerId, int position) {
                favouriteAdapter.notifyItemInserted(position);
            }

            @Override
            public void onFavouriteRemoved(int callerId, int position) {
                // update favourites list
                if(position!=-1){
                    favouriteAdapter.notifyItemRemoved(position);
                }

            }

        });
        mostChangedRecyclerView = findViewById(R.id.mostChangedRecyclerView);
        mostChangedRecyclerView.setNestedScrollingEnabled(false);
        mostChangedRecyclerView.setAdapter(mostChangedAdapter);
        mostChangedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Favourites
        favouriteAdapter = new RecyclerAdapter(this, appData.getFavouriteData(), appData, R.id.favouriteRecyclerView, new AdapterRefresh() {
            @Override
            public void onFavouriteAdded(int callerId, int position) {

            }

            @Override
            public void onFavouriteRemoved(int callerId, int position) {
                // update most changed list
                mostChangedAdapter.notifyDataSetChanged();
            }

        });
        favouriteRecyclerView = findViewById(R.id.favouriteRecyclerView);
        favouriteRecyclerView.setNestedScrollingEnabled(false);
        favouriteRecyclerView.setAdapter(favouriteAdapter);
        favouriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        bottomNavigationHandler = new BottomNavigationHandler(this);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.home);
        //AppCompatDelegate.MODE_NIGHT_NO;
        //AppCompatDelegate.MODE_NIGHT_YES;
        //AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);



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