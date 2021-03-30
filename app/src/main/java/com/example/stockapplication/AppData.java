package com.example.stockapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    private static AppData appData;
    private List<StockData> favouriteData = new ArrayList<>();
    private List<StockData> mostChanged = new ArrayList<>();
    private List<StockData> trendingList = new ArrayList<>();
    private List<StockData> searchResults = new ArrayList<>();
    public static final String APP_DATA = "APP_SHARED_PREFS";
    public static final String APP_DATA_JSON = "APP_DATA_JSON";
    public static final String SELECTED_THEME = "SELECTED_THEME";
    public static final String ACCELOMETER_ENABLED = "ACCELOMETER_ENABLED";
    public static final String LIGHT_SENSOR_ENABLED = "LIGHT_SENSOR_ENABLED";
    private transient StockApi stockApi;
    private transient SensorHandler sensorHandler;
    private transient boolean refreshing = false;
    private boolean lightSensorEnabled = false;
    private boolean accelometerEnabled = false;
    private AppData() {

    }

    public static AppData getInstance(Context context) {
        // TODO If threads are used, might need to be modified to be thread safe
        if (appData == null) {
            appData = AppData.parseAppDataFromSharedPrefs(context);
        }
        return appData;
    }

    public StockApi getStockApi(Context context){
        if(stockApi == null){
            Log.d("STOCKAPI","add");
            stockApi = new StockApi(context);
        }
        return stockApi;
    }
    public SensorHandler getSensorHandler(Context context){
        if(sensorHandler == null){
            Log.d("SENSORHA","add");
            sensorHandler = new SensorHandler(context,accelometerEnabled,lightSensorEnabled);
        }else{
            sensorHandler.updateSensors(accelometerEnabled,lightSensorEnabled);
        }


        return sensorHandler;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
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

    public boolean isLightSensorEnabled() {
        return lightSensorEnabled;
    }

    public void setLightSensorEnabled(boolean lightSensorEnabled) {
        this.lightSensorEnabled = lightSensorEnabled;
    }

    public boolean isAccelometerEnabled() {
        return accelometerEnabled;
    }

    public void setAccelometerEnabled(boolean accelometerEnabled) {
        this.accelometerEnabled = accelometerEnabled;
    }

    public void setFavouriteData(List<StockData> favouriteData) {
        this.favouriteData = favouriteData;
    }


    public void addToFavourites(StockData stock){
        //clone and add uuid
        StockData cloned = new StockData(stock.getSymbol(),stock.getMarket(),stock.getName(),stock.getPercentChange(),stock.getMarketPrice(),stock.isFavourite(),StockData.generateUuid());
        this.favouriteData.add(0,cloned);
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

    public static int getThemeSetting(int position){
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

    public static int getSettingFromPrefs(Context context,String setting){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(AppData.APP_DATA, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(setting, 0);
    }

    public static void setSettingToPrefs(Context context,String setting,int value){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(AppData.APP_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(setting, value);
        editor.apply();
    }

    public static AppData parseAppDataFromSharedPrefs(Context context){
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(APP_DATA, Context.MODE_PRIVATE);
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
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(AppData.APP_DATA,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AppData.APP_DATA_JSON,data);
        editor.apply();
    }

}

