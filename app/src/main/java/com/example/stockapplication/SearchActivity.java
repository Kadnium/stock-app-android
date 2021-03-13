package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.example.stockapplication.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.function.UnaryOperator;

public class SearchActivity extends AppCompatActivity {
    TextInputEditText searchField;
    AppData appData;
    StockApi stockApi;
    RecyclerAdapter searchResultAdapter;
    RecyclerView searchRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        BottomNavigationHandler bottomNavigationHandler = new BottomNavigationHandler(this);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.search);

        if(appData == null){
            appData = AppData.getAppData();
        }

        if(stockApi == null){
            stockApi = new StockApi(this);
        }

        initListViews();
        initSearchField();



    }

    public void initListViews(){
        searchResultAdapter = new RecyclerAdapter(this, appData.getSearchResults(), appData, R.id.searchRecyclerView, new AdapterRefresh() {
            @Override
            public void onFavouriteAdded(int callerId, int position) {

            }

            @Override
            public void onFavouriteRemoved(int callerId, int position) {


            }

        });
        searchRecyclerView = findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setNestedScrollingEnabled(false);
        searchRecyclerView.setAdapter(searchResultAdapter);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void initSearchField(){
        searchField = findViewById(R.id.searchInput);
        ProgressBar searchSpinner = (ProgressBar) findViewById(R.id.searchSpinner);
        searchSpinner.setVisibility(View.INVISIBLE);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() >=2){
                    searchSpinner.setVisibility(View.VISIBLE);
                    stockApi.getSearchResults(s.toString(), 5, new StockApiCallback() {
                        @Override
                        public void onSuccess(List<StockData> response, Context context) {
                            List<StockData> searchResults = appData.getSearchResults();
                            if(searchResults.size()>0){
                                searchResults.clear();
                            }
                            searchResults.addAll(response);

                            searchResultAdapter.notifyDataSetChanged();
                            searchSpinner.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError(VolleyError error, Context context) {

                        }
                    });
                }

                }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });


    }
}