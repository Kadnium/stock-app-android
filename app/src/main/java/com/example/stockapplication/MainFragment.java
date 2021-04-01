package com.example.stockapplication;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainFragment extends Fragment {



    StockApi stockApi;
    AppData appData;

    RecyclerView favouriteRecyclerView;
    RecyclerView mostChangedRecyclerView;

    RecyclerAdapter favouriteAdapter;
    RecyclerAdapter mostChangedAdapter;


    SwipeRefreshLayout swipeRefreshLayout;
    SensorHandler sensorHandler;
    View fragmentView;

    public void initBackend(){
        appData = AppData.getInstance(getContext());
        stockApi = appData.getStockApi(getContext());
        sensorHandler = appData.getSensorHandler(getContext());
        sensorHandler.setOnShakeCallback(() -> {
            appData.setRefreshing(true);
            updateDailyMovers(()-> updateFavourites(()->appData.setRefreshing(false)));
        });

    }
    public MainFragment() {
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        swipeRefreshLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            appData.setRefreshing(true);
            updateDailyMovers(() -> updateFavourites(() ->{
                swipeRefreshLayout.setRefreshing(false);
                appData.setRefreshing(false);
            }));
        });

        // Inflate the layout for this fragment
        return fragmentView;
    }

    private RecyclerView setRecyclerSettings(int viewId, RecyclerAdapter adapter){
        RecyclerView view = fragmentView.findViewById(viewId);
        view.setNestedScrollingEnabled(false);
        view.setAdapter(adapter);
        //view.suppressLayout(true);
        view.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    public void initListViews(){
        // Most changed
        mostChangedAdapter = new RecyclerAdapter(getContext(), appData.getMostChanged(), appData, R.id.mostChangedRecyclerView, new AdapterRefresh() {
            @Override
            public void onFavouriteAddClicked(StockData stock) {
                // Add to favourites and update favouriteAdapter
                appData.addToFavourites(stock);
                favouriteAdapter.notifyItemInserted(0);
                // In other activity - no need to update adapter
                appData.updateFavouriteStatuses(stock,appData.getTrendingList(),true);
            }

            @Override
            public void onFavouriteRemoveClicked(StockData stock) {
                // Remove from favourites and update favouriteAdapter
                int favouriteIndex = appData.removeFromFavourites(stock);
                if(favouriteIndex!=-1){
                    favouriteAdapter.notifyItemRemoved(favouriteIndex);
                }
                // In other activity - no need to update adapter
                appData.updateFavouriteStatuses(stock, appData.getTrendingList(),false);

            }

        });
        mostChangedRecyclerView = setRecyclerSettings(R.id.mostChangedRecyclerView,mostChangedAdapter);

        // Favourites
        favouriteAdapter = new RecyclerAdapter(getContext(), appData.getFavouriteData(), appData, R.id.favouriteRecyclerView, new AdapterRefresh() {
            @Override
            public void onFavouriteAddClicked(StockData stock) {
                // Not used
            }

            @Override
            public void onFavouriteRemoveClicked(StockData stock) {
                // update most changed list
                List<StockData> tempMostCHanged = appData.getMostChanged();
                int favouriteIndex = appData.updateFavouriteStatuses(stock,tempMostCHanged,false);
                if(favouriteIndex != -1){
                    mostChangedAdapter.notifyItemChanged(favouriteIndex);
                }
                // In other activity - no need to update adapter
                appData.updateFavouriteStatuses(stock,appData.getTrendingList(),false);
            }

        });
        favouriteRecyclerView = setRecyclerSettings(R.id.favouriteRecyclerView,favouriteAdapter);


    }



    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        initListViews();
        updateDailyMovers(null);
        ProgressBar spinner = fragmentView.findViewById(R.id.favouriteProgressBar);
        spinner.setVisibility(View.INVISIBLE);

    }



    public void updateDailyMovers(HelperCallback cb){
        ProgressBar spinner = fragmentView.findViewById(R.id.mostChangedProgress);
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
                public void onError(VolleyError error, Context context) {
                    Toast.makeText(context,error.networkResponse.toString(),Toast.LENGTH_LONG).show();
                    spinner.setVisibility(View.INVISIBLE);
                    finishCallback(cb);


                }
            });
        }else{
            finishCallback(null);
            spinner.setVisibility(View.INVISIBLE);
        }

    }

    private void finishCallback(HelperCallback cb){
        if(cb != null){
            cb.onComplete();
        }
    }
    public void updateFavourites(HelperCallback cb){
        List<StockData> userFavourites = appData.getFavouriteData();
        ProgressBar spinner = fragmentView.findViewById(R.id.favouriteProgressBar);
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
    public void onPause() {
        super.onPause();
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }








}