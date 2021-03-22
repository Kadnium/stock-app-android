package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SearchActivity extends AppCompatActivity {
    TextInputEditText searchField;
    AppData appData;
    StockApi stockApi;
    RecyclerAdapter searchResultAdapter;
    RecyclerView searchRecyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerAdapter trendingRecyclerAdapter;
    RecyclerView trendingRecyclerView;
    BottomNavigationHandler bottomNavigationHandler;
    SensorHandler sensorHandler;

    public void initBackend(){
        stockApi = new StockApi(this);
        appData = AppData.parseAppDataFromSharedPrefs(this);
        sensorHandler = new SensorHandler(this, new HelperCallback() {
                @Override
                public void onComplete() {
                }
        });




    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.search);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){
                setTrendingData(() -> {
                   swipeRefreshLayout.setRefreshing(false);
                });


            }
        });
        initSearchField();
    }

    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        bottomNavigationHandler.refresh();
        initListViews();
        setTrendingData(null);
        clearSearchResults();


    }



    private RecyclerView setRecyclerSettings( int viewId, RecyclerAdapter adapter){
        RecyclerView view = findViewById(viewId);
        view.setNestedScrollingEnabled(false);
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(this));
        return view;
    }
    public void initListViews(){
        searchResultAdapter = new RecyclerAdapter(this, appData.getSearchResults(), appData, R.id.searchRecyclerView, new AdapterRefresh() {
            @Override
            public void onFavouriteAddClicked(int position, StockData stock) {
                // search most changed list and set as a favourite
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),true);
                int index = appData.updateFavouriteStatuses(stock,appData.getTrendingList(),true);
                if(index != -1){
                    trendingRecyclerAdapter.notifyItemChanged(index);
                }
                appData.addToFavourites(stock);
            }

            @Override
            public void onFavouriteRemoveClicked(int position, StockData stock) {
                // update most changed
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),false);
                int index = appData.updateFavouriteStatuses(stock,appData.getTrendingList(),false);
                if(index != -1){
                    trendingRecyclerAdapter.notifyItemChanged(index);
                }
                appData.removeFromFavourites(stock);

            }

        });
        searchRecyclerView = setRecyclerSettings(R.id.searchRecyclerView,searchResultAdapter);

        trendingRecyclerAdapter = new RecyclerAdapter(this, appData.getTrendingList(), appData, R.id.searchRecyclerView, new AdapterRefresh() {
            @Override
            public void onFavouriteAddClicked(int position, StockData stock) {
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),true);
                int index = appData.updateFavouriteStatuses(stock,appData.getSearchResults(),true);
                if(index != -1){
                    searchResultAdapter.notifyItemChanged(index);
                }
                appData.addToFavourites(stock);
            }

            @Override
            public void onFavouriteRemoveClicked(int position, StockData stock) {
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),false);
                int index = appData.updateFavouriteStatuses(stock,appData.getSearchResults(),false);
                if(index != -1){
                    searchResultAdapter.notifyItemChanged(index);
                }
                appData.removeFromFavourites(stock);
            }
        });
        trendingRecyclerView = setRecyclerSettings(R.id.trendingRecyclerView,trendingRecyclerAdapter);


    }
    private List<StockData> clearSearchResults(){
        List<StockData> searchResults = appData.getSearchResults();
        if(searchResults.size()>0){
            searchResults.clear();
        }
        return searchResults;
    }

    private void initSearchField(){
        searchField = findViewById(R.id.searchInput);
        ProgressBar searchSpinner = (ProgressBar) findViewById(R.id.searchSpinner);
        searchSpinner.setVisibility(View.INVISIBLE);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Search if user has inputted two characters,
                // if field is emptied, clear search results
                if(s.length() >=2){
                    searchSpinner.setVisibility(View.VISIBLE);
                    stockApi.getSearchResults(s.toString(), 5, new StockApiCallback() {
                        @Override
                        public void onSuccess(List<StockData> response, Context context) {
                            List<StockData> searchResults = clearSearchResults();
                            searchResults.addAll(response);

                            searchResultAdapter.notifyDataSetChanged();
                            searchSpinner.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError(VolleyError error, Context context) {

                        }
                    });
                }else if(s.length() == 0){
                    clearSearchResults();
                    searchResultAdapter.notifyDataSetChanged();
                }

                }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
    }
    private void finishCallback(HelperCallback cb){
        if(cb != null){
            cb.onComplete();
        }
    }
    private void setTrendingData(HelperCallback cb){
        ProgressBar trendingSpinner = (ProgressBar) findViewById(R.id.trendingSpinner);
        List<StockData> trendingList = appData.getTrendingList();
        trendingSpinner.setVisibility(View.VISIBLE);
        if(trendingList.size() == 0 || cb != null){
            stockApi.getTrending(5, new StockApiCallback() {
                @Override
                public void onSuccess(List<StockData> response, Context context) {
                    List<StockData> trending = appData.getTrendingList();
                    if(trending.size()>0){
                        trending.clear();
                    }
                    trending.addAll(response);
                    trendingRecyclerAdapter.notifyDataSetChanged();
                    trendingSpinner.setVisibility(View.INVISIBLE);
                    finishCallback(cb);

                }

                @Override
                public void onError(VolleyError error, Context context) {
                    trendingSpinner.setVisibility(View.INVISIBLE);
                    finishCallback(cb);
                }
            });
        }else{
            trendingSpinner.setVisibility(View.INVISIBLE);
            finishCallback(cb);
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
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }


    }
    @Override
    public void finish() {
        super.finish();
      //  AppData.saveAppDataToSharedPrefs(this,appData,false);
        // override back button default animation
        overridePendingTransition(0,0);
    }


}