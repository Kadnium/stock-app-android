package com.example.stockapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    //private static AppData appData;
    private List<StockData> favouriteData = new ArrayList<>();
    private List<StockData> mostChanged = new ArrayList<>();
    private List<StockData> trendingList = new ArrayList<>();
    private List<StockData> searchResults = new ArrayList<>();
    public static final String APP_DATA = "APP_SHARED_PREFS";
    public static final String APP_DATA_JSON = "APP_DATA_JSON";
    public static final String SELECTED_THEME = "SELECTED_THEME";

    public AppData() {

    }


    public List<StockData> getFavouriteData() {
        return favouriteData;
    }

    public List<StockData> getMostChanged() {
        return mostChanged;
    }

    public List<StockData> getTrendingList() {
        return trendingList;
    }

    public List<StockData> getSearchResults() {
        return searchResults;
    }


    public void setFavouriteData(List<StockData> favouriteData) {
        this.favouriteData = favouriteData;
    }


    public void addToFavourites(StockData stock){
        //clone and add uuid
        StockData cloned = new StockData(stock.getSymbol(),stock.getMarket(),stock.getName(),stock.getPercentChange(),stock.getMarketPrice(),stock.isFavourite(),StockData.generateUuid());
        this.favouriteData.add(cloned);
    }
    public int removeFromFavourites(StockData stock){
        int index = getIndex(stock,favouriteData);
        if(index != -1){
            favouriteData.remove(index);
        }
        return index;
    }

    public int getIndex(StockData stock, List<StockData> stockList){
        int index = -1;
        for (int i = 0; i <stockList.size(); i++) {
            StockData s = stockList.get(i);
            if(s.getSymbol().equals(stock.getSymbol())){
                index = i;
                break;
            }
        }
        return index;
    }
    public boolean isStockInFavouriteList(String ticker){
        for(StockData s:favouriteData){
            if(ticker.equals(s.getSymbol())){
                return true;
            }
        }
        return false;
    }
    public int updateFavouriteStatuses(StockData stock,List<StockData> stockList,boolean status){
        // TODO fix
        int index = getIndex(stock,stockList);
        if(index != -1){
            stockList.get(index).setFavourite(status);
        }
        return index;
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

    public int getThemeSetting(int position){
        if(position == 0){
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }else if(position == 1){
            return AppCompatDelegate.MODE_NIGHT_YES;
        }else{
            return AppCompatDelegate.MODE_NIGHT_NO;
        }

    }


    public static AppData parseAppData(Intent intent){
        if(intent == null){
            return null;
        }
        Gson gson = new Gson();
        String s = intent.getStringExtra(APP_DATA_JSON);
        AppData data;
        if(s!= null){
            data = gson.fromJson(s, AppData.class);
        }else{
            data = new AppData();
        }
        return data;
    }

    public static AppData parseAppDataFromBundle(Bundle savedInstanceState){
        Gson gson = new Gson();
        String s = savedInstanceState.getString(APP_DATA_JSON);
        AppData data;
        if(s!= null){
            data = gson.fromJson(s, AppData.class);
        }else{
            data = new AppData();
        }
        return data;
    }
    public static int getThemeFromPrefs(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppData.APP_DATA, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(AppData.SELECTED_THEME, 0);
    }
    public static AppData parseAppDataFromSharedPrefs(Context context){
        SharedPreferences preferences = context.getSharedPreferences(APP_DATA, Context.MODE_PRIVATE);
        String s = preferences.getString(APP_DATA_JSON, "");
        Gson gson = new Gson();
        AppData data;
        if(!s.equalsIgnoreCase("")){
            data = gson.fromJson(s, AppData.class);
        }else{
            data = new AppData();
        }
        return data;

    }

    public static void saveAppDataToSharedPrefs(Context context,AppData appData,boolean clearApiData){
        Gson gson = new Gson();
        if(clearApiData){
            appData.setMostChanged(new ArrayList<>());
            appData.setSearchResults(new ArrayList<>());
            appData.setTrendingList(new ArrayList<>());
        }
        String data = gson.toJson(appData);
        SharedPreferences preferences = context.getSharedPreferences(AppData.APP_DATA,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AppData.APP_DATA_JSON,data);
        editor.apply();
    }

}

