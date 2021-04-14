package com.example.stockapplication.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainFragment extends Fragment {
    private StockApi stockApi;
    private AppData appData;

    private RecyclerAdapter favouriteAdapter;
    private RecyclerAdapter mostChangedAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private SensorHandler sensorHandler;
    private View fragmentView;

    public void initBackend(){
        appData = AppData.getInstance(getContext());
        stockApi = appData.getStockApi(getContext());
        sensorHandler = appData.getSensorHandler(getContext());
        sensorHandler.setOnShakeCallback(()->refreshData(true));
        refreshData(false);

    }

    /**
     * Refreshs data, will only refresh infoboxes on first run,
     * others every time
     * @param manualRefresh If called from user action - true
     */
    private void refreshData(boolean manualRefresh){
        appData.setRefreshing(true);
        if(manualRefresh){
            FragmentManager m = getFragmentManager();
            if(m != null){
                MainInfoFragment fragment = (MainInfoFragment) getFragmentManager().findFragmentById(R.id.infoBoxLayout);
                if(fragment != null){
                    fragment.updateInfoBoxes(()->{});
                }else{
                    addInfoLayout();
                }
            }
            updateDailyMovers(()-> updateFavourites(()-> {
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
                appData.setRefreshing(false);
            }));
        }else{
            addInfoLayout();
            updateDailyMovers(()-> updateFavourites(()->{
                appData.setRefreshing(false);
            }));
        }


    }
    public MainFragment() {

    }

    /**
     * Adds info layout which is on top of lists
     */
    public void addInfoLayout(){
        FragmentTransaction fragmentTransaction= Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.infoBoxLayout,MainInfoFragment.newInstance());
        fragmentTransaction.commit();
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
            refreshData(true);

        });

        // Inflate the layout for this fragment
        return fragmentView;
    }

    /**
     * Helper method for setting recycler view settings
     * @param viewId Recyclerview id
     * @param adapter Created recycleradapter
     * @return Returns recyclerview
     */
    private RecyclerView setRecyclerSettings(int viewId, RecyclerAdapter adapter){
        RecyclerView view = fragmentView.findViewById(viewId);
        view.setNestedScrollingEnabled(false);
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    /**
     * Inits recycler views
     */
    public void initListViews(){
        // Most changed
        mostChangedAdapter = new RecyclerAdapter(getContext(), appData.getMostChanged(), appData, R.id.mostChangedRecyclerView, R.layout.stock_row, new AdapterRefresh() {
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
        setRecyclerSettings(R.id.mostChangedRecyclerView,mostChangedAdapter);

        // Favourites
        favouriteAdapter = new RecyclerAdapter(getContext(), appData.getFavouriteData(), appData, R.id.favouriteRecyclerView,R.layout.stock_row, new AdapterRefresh() {
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
        setRecyclerSettings(R.id.favouriteRecyclerView,favouriteAdapter);


    }



    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        initListViews();
        ProgressBar spinner = fragmentView.findViewById(R.id.favouriteProgressBar);
        spinner.setVisibility(View.INVISIBLE);

    }


    /**
     * Updates daily movers
     * @param cb After finish callback
     */
    public void updateDailyMovers(HelperCallback cb){
        ProgressBar spinner = fragmentView.findViewById(R.id.mostChangedProgress);
        spinner.setVisibility(View.VISIBLE);
        if(appData.getMostChanged().size() <2 || cb != null){
            stockApi.getDailyMovers(AppData.MOST_CHANGED_QUERY_COUNT,new StockApiCallback() {
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
                    Toast.makeText(context,getString(R.string.error_msg),Toast.LENGTH_LONG).show();
                    spinner.setVisibility(View.INVISIBLE);
                    finishCallback(cb);


                }
            });
        }else{
            finishCallback(null);
            spinner.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Helper method for running callbacks
     * @param cb HelperCallback instance
     */
    private void finishCallback(HelperCallback cb){
        if(cb != null){
            cb.onComplete();
        }
    }

    /**
     * Updates favourites
     * @param cb After finish callback
     */
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
                    Toast.makeText(context,getString(R.string.error_msg),Toast.LENGTH_LONG).show();
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