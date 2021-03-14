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
            public void onFavouriteAddClicked(int position, StockData stock) {
                // Add to favourites and update favouriteAdapter
                appData.addToFavourites(stock);
                favouriteAdapter.notifyItemInserted(appData.getFavouriteData().size()-1);
                // update trending list
                appData.updateFavouriteStatuses(stock.getSymbol(),appData.getTrendingList(),true);
            }

            @Override
            public void onFavouriteRemoveClicked(int position, StockData stock) {
                // Remove from favourites and update favouriteAdapter
                int favouriteIndex = appData.removeFromFavourites(stock);
                if(favouriteIndex!=-1){
                    favouriteAdapter.notifyItemRemoved(favouriteIndex);
                }
               // update trending list
                appData.updateFavouriteStatuses(stock.getSymbol(), appData.getTrendingList(),false);

            }

        });
        mostChangedRecyclerView = findViewById(R.id.mostChangedRecyclerView);
        mostChangedRecyclerView.setNestedScrollingEnabled(false);
        mostChangedRecyclerView.setAdapter(mostChangedAdapter);
        mostChangedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Favourites
        favouriteAdapter = new RecyclerAdapter(this, appData.getFavouriteData(), appData, R.id.favouriteRecyclerView, new AdapterRefresh() {
            @Override
            public void onFavouriteAddClicked(int position, StockData stock) {
                // Not used
            }

            @Override
            public void onFavouriteRemoveClicked(int position, StockData stock) {
                // update most changed list
                appData.updateFavouriteStatuses(stock.getSymbol(),appData.getMostChanged(),false);
                mostChangedAdapter.notifyDataSetChanged();
                appData.updateFavouriteStatuses(stock.getSymbol(),appData.getTrendingList(),false);
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