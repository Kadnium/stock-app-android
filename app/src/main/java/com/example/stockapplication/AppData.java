package com.example.stockapplication;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    private static AppData appData;
    private List<StockData> favouriteData = new ArrayList<>();
    private List<StockData> mostChanged = new ArrayList<>();
    private List<StockData> trendingList = new ArrayList<>();
    private List<StockData> searchResults = new ArrayList<>();
    private AppData() {
        if(appData != null) {
            throw new RuntimeException("Use getAppData method to use this class!");
        }
    }

    public static AppData getAppData() {
       // TODO If threads are used, might need to be modified to be thread safe
        if (appData == null) {
            appData = new AppData();

        }
        return appData;
    }

    public List<StockData> getFavouriteData() {
        return new ArrayList<StockData>(favouriteData);
    }

    public List<StockData> getMostChanged() {
        return new ArrayList<StockData>(mostChanged);
    }

    public List<StockData> getTrendingList() {
        return new ArrayList<StockData>(trendingList);
    }

    public List<StockData> getSearchResults() {
        return new ArrayList<StockData>(searchResults);
    }


    public void setFavouriteData(List<StockData> favouriteData) {
        this.favouriteData =  new ArrayList<>(favouriteData);
    }

    public void setMostChanged(List<StockData> mostChanged) {
        this.mostChanged = mostChanged;
    }

    public void setTrendingList(List<StockData> trendingList) {
        this.trendingList = trendingList;
    }

    public void setSearchResults(List<StockData> searchResults) {
        this.searchResults = searchResults;
    }

    //add methods here for insert, delete, search etc......
}

