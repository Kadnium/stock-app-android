package com.example.stockapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.stockapplication.AppData;
import com.example.stockapplication.R;
import com.example.stockapplication.StockApi;
import com.example.stockapplication.StockApiCallback;
import com.example.stockapplication.StockData;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    SwipeRefreshLayout swipeRefreshLayout;




    public void initBackend(){
        if(stockApi == null){
            stockApi = new StockApi(this);
        }
        if(appData == null){
            appData = AppData.parseAppDataFromSharedPrefs(this);
            //int themeSetting =  appData.getThemeSetting(appData.getSelectedThemeOption());
            //int temp = AppCompatDelegate.getDefaultNightMode();
            int theme = appData.getThemeSetting(AppData.getThemeFromPrefs(this));
            //AppCompatDelegate.setDefaultNightMode(theme);
            if(AppCompatDelegate.getDefaultNightMode() != theme){
                AppCompatDelegate.setDefaultNightMode(theme);
            }
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
                appData.updateFavouriteStatuses(stock,appData.getTrendingList(),true);
            }

            @Override
            public void onFavouriteRemoveClicked(int position, StockData stock) {
                // Remove from favourites and update favouriteAdapter
                int favouriteIndex = appData.removeFromFavourites(stock);
                if(favouriteIndex!=-1){
                    favouriteAdapter.notifyItemRemoved(favouriteIndex);
                }
               // update trending list
                appData.updateFavouriteStatuses(stock, appData.getTrendingList(),false);

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
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),false);
                mostChangedAdapter.notifyDataSetChanged();
                appData.updateFavouriteStatuses(stock,appData.getTrendingList(),false);
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
        initBackend();
        bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav, R.id.home);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){
                updateDailyMovers(() -> {
                    updateFavourites(() -> swipeRefreshLayout.setRefreshing(false));
                });


            }
        });

        initListViews();
        updateDailyMovers(null);
        ProgressBar spinner = (ProgressBar) findViewById(R.id.favouriteProgressBar);
        spinner.setVisibility(View.INVISIBLE);
        //updateFavourites(null);


    }


    public void updateDailyMovers(LoadCallback cb){
        ProgressBar spinner = (ProgressBar) findViewById(R.id.mostChangedProgress);
        spinner.setVisibility(View.VISIBLE);
        if(appData.getMostChanged().size() <2 || cb != null){
            stockApi.getDailyMovers(1,new StockApiCallback() {
                @Override
                public void onSuccess(List<StockData> response, Context context) {
                    List<StockData> mostChanged = appData.getMostChanged();
                    mostChanged.clear();
                    mostChanged.addAll(response);
                    mostChangedAdapter.notifyDataSetChanged();
                    spinner.setVisibility(View.INVISIBLE);
                    finishCallback(cb);

                }

                @Override
                public void onError(VolleyError error,Context context) {
                    Toast.makeText(context,error.networkResponse.toString(),Toast.LENGTH_LONG).show();
                    spinner.setVisibility(View.INVISIBLE);
                    finishCallback(cb);


                }
            });
        }else{
            finishCallback(cb);
            spinner.setVisibility(View.INVISIBLE);
        }

    }

    private void finishCallback(LoadCallback cb){
        if(cb != null){
            cb.onComplete();
        }
    }
    public void updateFavourites(LoadCallback cb){
        List<StockData> userFavourites = appData.getFavouriteData();
        ProgressBar spinner = (ProgressBar) findViewById(R.id.favouriteProgressBar);
        spinner.setVisibility(View.VISIBLE);
        if(!userFavourites.isEmpty()){
            List<String> symbolList = new ArrayList<>();
            for(StockData stock : userFavourites) {
                String symbol = stock.getSymbol();
                symbolList.add(symbol);
            }
            stockApi.getByTickerNames(symbolList, new StockApiCallback() {
                @Override
                public void onSuccess(List<StockData> response, Context context) {
                    //Calendar calendar = Calendar.getInstance();
                    //Date currentTime = Calendar.getInstance().getTime();
                    //SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    //String currentDateandTime = sdf.format(new Date());


                    //TextView t = findViewById(R.id.favUpdated);
                    //t.setText(currentDateandTime);
                    //calendar.getTime();
                    for(StockData stock:response){
                        int index = appData.getIndex(stock,userFavourites);
                        StockData favStock = userFavourites.get(index);
                        favStock.setMarketPrice(stock.getMarketPrice());
                        favStock.setPercentChange(stock.getPercentChange());
                        favouriteAdapter.notifyItemChanged(index);

                    }
                    finishCallback(cb);

                    spinner.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError(VolleyError error, Context context) {
                    spinner.setVisibility(View.INVISIBLE);
                    finishCallback(cb);
                }
            });
        }else{
            finishCallback(cb);
            spinner.setVisibility(View.INVISIBLE);
        }

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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }

    @Override
    public void onResume() {
        super.onResume();
        bottomNavigationHandler.refresh();

    }

}