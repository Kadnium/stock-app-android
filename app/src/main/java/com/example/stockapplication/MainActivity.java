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
import android.view.View;
import android.widget.ProgressBar;
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

    public void initBackend(){
        if(stockApi == null){
            stockApi = new StockApi(this);
        }
        if(appData == null){
            appData = AppData.getAppData();
        }
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
        bottomNavigationHandler.initNavigation(R.id.bottomNav, R.id.home);
        // WILL RERUN ONCREATE IF USED
        //AppCompatDelegate.MODE_NIGHT_NO;
        //AppCompatDelegate.MODE_NIGHT_YES;
        //AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        initBackend();
        initListViews();
        updateDailyMovers();
        updateFavourites();



    }


    public void updateDailyMovers(){
        ProgressBar spinner = (ProgressBar) findViewById(R.id.mostChangedProgress);
        //spinner.setVisibility(View.VISIBLE);
        if(appData.getMostChanged().size() <2){
            stockApi.getDailyMovers(1,new StockApiCallback() {
                @Override
                public void onSuccess(List<StockData> response, Context context) {
                    List<StockData> mostChanged = appData.getMostChanged();
                    mostChanged.addAll(response);
                    mostChangedAdapter.notifyDataSetChanged();
                    spinner.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError(VolleyError error,Context context) {
                    Toast.makeText(context,error.networkResponse.toString(),Toast.LENGTH_LONG).show();
                    spinner.setVisibility(View.INVISIBLE);

                }
            });
        }else{
            spinner.setVisibility(View.INVISIBLE);
        }

    }


    public void updateFavourites(){
        List<StockData> userFavourites = appData.getFavouriteData();
        ProgressBar spinner = (ProgressBar) findViewById(R.id.favouriteProgressBar);
        spinner.setVisibility(View.INVISIBLE);

       /* if(!userFavourites.isEmpty()){
            List<String> symbolList = new ArrayList<>();
            for(StockData stock : userFavourites) {
                String symbol = stock.getSymbol();
                symbolList.add(symbol);
            }
            stockApi.getByTickerNames(symbolList, new StockApiCallback() {
                @Override
                public void onSuccess(List<StockData> response, Context context) {
                    spinner.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError(VolleyError error, Context context) {
                    spinner.setVisibility(View.INVISIBLE);
                }
            });
        }else{
            spinner.setVisibility(View.INVISIBLE);
        }*/

    }













}