package com.example.stockapplication.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.stockapplication.datahelpers.AppData;
import com.example.stockapplication.R;
import com.example.stockapplication.datahelpers.RecyclerAdapter;
import com.example.stockapplication.datahelpers.SensorHandler;
import com.example.stockapplication.datahelpers.StockApi;
import com.example.stockapplication.datahelpers.StockData;
import com.example.stockapplication.interfaces.AdapterRefresh;
import com.example.stockapplication.interfaces.HelperCallback;
import com.example.stockapplication.interfaces.StockApiCallback;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Objects;

public class SearchFragment extends Fragment {
    private AppData appData;
    private StockApi stockApi;
    private RecyclerAdapter searchResultAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerAdapter trendingRecyclerAdapter;
    private View fragmentView;
    SensorHandler sensorHandler;
    public void initBackend(){
        appData = AppData.getInstance(getContext());
        stockApi = appData.getStockApi(getContext());
        sensorHandler = appData.getSensorHandler(getContext());

        sensorHandler.setOnShakeCallback(()->{
            appData.setRefreshing(true);
            setTrendingData(()->appData.setRefreshing(false));
        });

        swipeRefreshLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            appData.setRefreshing(true);
            setTrendingData(() -> {
                swipeRefreshLayout.setRefreshing(false);
                appData.setRefreshing(false);
            });


        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_search, container, false);

        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        // Inflate the layout for this fragment
        return fragmentView;
    }



    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        initSearchField();
        initListViews();
        setTrendingData(()->{});
        clearSearchResults();


    }


    /**
     * Helper for setting recycler settings
     * @param viewId Id of recycler
     * @param adapter Recycleradapter instance
     * @return RecyclerAdapter
     */
    private RecyclerView setRecyclerSettings(int viewId, RecyclerAdapter adapter){
        RecyclerView view = fragmentView.findViewById(viewId);
        view.setNestedScrollingEnabled(false);
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    /**
     * Inits recyclerviewrs
     */
    public void initListViews(){
        searchResultAdapter = new RecyclerAdapter(getContext(), appData.getSearchResults(), appData, R.id.searchRecyclerView, R.layout.stock_row, new AdapterRefresh() {
            @Override
            public void onFavouriteAddClicked(StockData stock) {
                // Search most changed list and set as a favourite
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),true);
                int index = appData.updateFavouriteStatuses(stock,appData.getTrendingList(),true);
                if(index != -1){
                    trendingRecyclerAdapter.notifyItemChanged(index);
                }
                appData.addToFavourites(stock);
            }

            @Override
            public void onFavouriteRemoveClicked(StockData stock) {
                // update most changed
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),false);
                int index = appData.updateFavouriteStatuses(stock,appData.getTrendingList(),false);
                if(index != -1){
                    trendingRecyclerAdapter.notifyItemChanged(index);
                }
                appData.removeFromFavourites(stock);

            }

        });
        setRecyclerSettings(R.id.searchRecyclerView,searchResultAdapter);

        trendingRecyclerAdapter = new RecyclerAdapter(getContext(), appData.getTrendingList(), appData, R.id.searchRecyclerView,R.layout.stock_row, new AdapterRefresh() {
            @Override
            public void onFavouriteAddClicked(StockData stock) {
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),true);
                int index = appData.updateFavouriteStatuses(stock,appData.getSearchResults(),true);
                if(index != -1){
                    searchResultAdapter.notifyItemChanged(index);
                }
                appData.addToFavourites(stock);
            }

            @Override
            public void onFavouriteRemoveClicked(StockData stock) {
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),false);
                int index = appData.updateFavouriteStatuses(stock,appData.getSearchResults(),false);
                if(index != -1){
                    searchResultAdapter.notifyItemChanged(index);
                }
                appData.removeFromFavourites(stock);
            }
        });
        setRecyclerSettings(R.id.trendingRecyclerView,trendingRecyclerAdapter);


    }

    /**
     * Clear searchresults from appData
     * @return empty searchresults list
     */
    private List<StockData> clearSearchResults(){
        List<StockData> searchResults = appData.getSearchResults();
        if(searchResults.size()>0){
            searchResults.clear();
        }
        return searchResults;
    }

    /**
     * Inits search field
     */
    private void initSearchField(){
        TextInputEditText searchField = fragmentView.findViewById(R.id.searchInput);
        ProgressBar searchSpinner = fragmentView.findViewById(R.id.searchSpinner);
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
                    stockApi.getSearchResults(s.toString(), AppData.SEARCH_QUERY_COUNT, new StockApiCallback() {
                        @Override
                        public void onSuccess(List<StockData> response, Context context) {
                            List<StockData> searchResults = clearSearchResults();
                            searchResults.addAll(response);

                            searchResultAdapter.notifyDataSetChanged();
                            searchSpinner.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError(VolleyError error, Context context) {
                            Toast.makeText(context,getString(R.string.error_msg),Toast.LENGTH_LONG).show();
                            searchSpinner.setVisibility(View.INVISIBLE);
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

    /**
     * Helper for finishing callbacks
     * @param cb HelperCallback instance
     */
    private void finishCallback(HelperCallback cb){
        if(cb != null){
            cb.onComplete();
        }
    }

    /**
     * Finds and sets trending data
     * @param cb
     */
    private void setTrendingData(HelperCallback cb){
        ProgressBar trendingSpinner = fragmentView.findViewById(R.id.trendingSpinner);
        List<StockData> trendingList = appData.getTrendingList();
        trendingSpinner.setVisibility(View.VISIBLE);
        if(trendingList.size() == 0 || cb != null){
            stockApi.getTrending(AppData.TRENDING_QUERY_COUNT, new StockApiCallback() {
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
            finishCallback(null);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }


    public SearchFragment() {

    }


    public static SearchFragment newInstance() {
        return new SearchFragment();

    }



}