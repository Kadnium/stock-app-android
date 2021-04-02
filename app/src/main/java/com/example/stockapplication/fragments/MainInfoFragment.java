package com.example.stockapplication.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.stockapplication.datahelpers.AppData;
import com.example.stockapplication.R;
import com.example.stockapplication.datahelpers.RecyclerAdapter;
import com.example.stockapplication.datahelpers.StockApi;
import com.example.stockapplication.datahelpers.StockData;
import com.example.stockapplication.interfaces.HelperCallback;
import com.example.stockapplication.interfaces.StockApiCallback;

import java.util.List;


public class MainInfoFragment extends Fragment {
    private AppData appData;
    private StockApi stockApi;
    private View fragmentView;
    private RecyclerAdapter infoAdapter;
    private RecyclerView infoRecyclerView;
    public MainInfoFragment() {
        // Required empty public constructor
    }

    public void initBackend(){
        appData = AppData.getInstance(getContext());
        stockApi = appData.getStockApi(getContext());
        initListViews();
        updateInfoBoxes(()->{});


    }

    public static MainInfoFragment newInstance() {
        return new MainInfoFragment();
    }


    /**
     * Helper method for setting recycler view settings
     * @param adapter Created recycleradapter
     * @return Returns recyclerview
     */
    private RecyclerView setRecyclerSettings(RecyclerAdapter adapter){
        RecyclerView view = fragmentView.findViewById(R.id.infoRecyclerView);
        view.setNestedScrollingEnabled(true);
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        return view;
    }
    /**
     * Inits recycler views
     */
    public void initListViews(){
        // Most changedContext ctx, List<StockData> stockList,AppData data,int viewId,int layoutToInflate, AdapterRefresh refresh
        infoAdapter = new RecyclerAdapter(getContext(), appData.getInfoData(), appData,-1,R.layout.info_box,null);
        infoRecyclerView = setRecyclerSettings(infoAdapter);
    }

    /**
     * Updates info boxes
     * @param cb After finish callback
     */
    public void updateInfoBoxes(HelperCallback cb){
        ProgressBar spinner = fragmentView.findViewById(R.id.infoProgress);
        spinner.setVisibility(View.VISIBLE);
        if(cb != null){
            stockApi.getFrontPageSymbols(new StockApiCallback() {
                @Override
                public void onSuccess(List<StockData> response, Context context) {
                    List<StockData> infoData = appData.getInfoData();
                    infoData.clear();
                    infoData.addAll(response);
                    infoAdapter.notifyDataSetChanged();
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

    /**
     * Helper method for running callbacks
     * @param cb HelperCallback instance
     */
    private void finishCallback(HelperCallback cb){
        if(cb != null){
            cb.onComplete();
        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_main_info, container, false);
        initBackend();
        return fragmentView;
    }
}