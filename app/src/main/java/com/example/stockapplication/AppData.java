package com.example.stockapplication;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    private static AppData appData;

    public static final String APP_DATA = "APP_SHARED_PREFS";
    public static final String APP_DATA_JSON = "APP_DATA_JSON";
    public static final String SELECTED_THEME = "SELECTED_THEME";
    public static final String MAIN_FRAGMENT = "MAIN_FRAGMENT";
    public static final String SEARCH_FRAGMENT = "SEARCH_FRAGMENT";
    public static final String OPTIONS_FRAGMENT = "OPTIONS_FRAGMENT";
    public static final String CHART_FRAGMENT = "CHART_FRAGMENT";
    // Store stock data in these lists
    private List<StockData> favouriteData = new ArrayList<>();
    private List<StockData> mostChanged = new ArrayList<>();
    private List<StockData> trendingList = new ArrayList<>();
    private List<StockData> searchResults = new ArrayList<>();
    // Manage state of sensors
    private boolean lightSensorEnabled = false;
    private boolean accelometerEnabled = false;
    // Helper classes for activities
    // Transient variables prevent saving to json when serializing
    private transient StockApi stockApi;
    private transient SensorHandler sensorHandler;
    private transient boolean refreshing = false;



    private AppData() {
    }

    /**
     * Gets AppData
     * @param context current context
     * @return Singleton instance of AppData
     */
    public static AppData getInstance(Context context) {
       // Singleton might not be thread safe
        if (appData == null) {
            // If appdata doesn't exist, get saved data from shared preferences
            appData = AppData.parseAppDataFromSharedPrefs(context);
        }
        return appData;
    }

    /**
     * Get current StockApi instance
     * @param context current context
     * @return StockApi instance
     */
    public StockApi getStockApi(Context context){
        if(stockApi == null){
            stockApi = new StockApi(context);
        }
        return stockApi;
    }

    /**
     * Get current SensorHandler instance,
     * will refresh sensor statuses every time this is called
     * @param context Current context
     * @return SensorHandler instance
     */
    public SensorHandler getSensorHandler(Context context){
        if(sensorHandler == null){
            sensorHandler = new SensorHandler(context,accelometerEnabled,lightSensorEnabled);
        }else{
            sensorHandler.updateSensors(accelometerEnabled,lightSensorEnabled);
            sensorHandler.setContext(context);
        }


        return sensorHandler;
    }




    /**
     * Add stock to favourites, will not keep reference to given instance
     * @param stock Stock to get the data from
     */
    public void addToFavourites(StockData stock){
        // Clone and add uuid
        StockData cloned = new StockData(stock.getSymbol(),stock.getMarket(),stock.getName(),stock.getPercentChange(),stock.getMarketPrice(),stock.isFavourite(),StockData.generateUuid());
        this.favouriteData.add(0,cloned);
    }

    /**
     * Remove stock from favourites
     * @param stock Stock to remove
     * @return If removed, will return remove index, else -1
     */
    public int removeFromFavourites(StockData stock){
        int index = getIndex(stock,favouriteData);
        if(index != -1){
            favouriteData.remove(index);
        }
        return index;
    }

    /**
     * Find index of given stock from given list
     * @param stock Stock to find
     * @param stockList List to find from
     * @return Index of stock, -1 if not found
     */
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

    /**
     * Checks if given ticker exists in favourite list
     * @param ticker Ticker of the stock, for example NOK
     * @return Stock exists or not
     */
    public boolean isStockInFavouriteList(String ticker){
        for(StockData s:favouriteData){
            if(ticker.equals(s.getSymbol())){
                return true;
            }
        }
        return false;
    }

    /**
     * Updates favourite status from given list for given stock
     * @param stock Stock to update
     * @param stockList Stocklist to update
     * @param status New favourite status
     * @return Returns index of updated stock, if not found returns -1
     */
    public int updateFavouriteStatuses(StockData stock,List<StockData> stockList,boolean status){
        int index = getIndex(stock,stockList);
        if(index != -1){
            stockList.get(index).setFavourite(status);
        }
        return index;
    }


    /**
     * Parses theme setting for settings page dropdown item positions
     * @param position Drop down menu position
     * @return returns matching theme
     */
    public static int getThemeSetting(int position){
        if(position == 0){
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }else if(position == 1){
            return AppCompatDelegate.MODE_NIGHT_YES;
        }else{
            return AppCompatDelegate.MODE_NIGHT_NO;
        }

    }

    /**
     *
     * @param context Current context
     * @param setting Key to find
     * @return Returns value from sharedpreferences by given key, default value 0
     */

    public static int getSettingFromPrefs(Context context,String setting){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(AppData.APP_DATA, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(setting, 0);
    }

    /**
     *
     * @param context Current context
     * @param setting Setting name
     * @param value Integer value to save
     */
    public static void setSettingToPrefs(Context context,String setting,int value){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(AppData.APP_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(setting, value);
        editor.apply();
    }
    /**
     * Method for parsing saved app data from shared preferences,
     * used when app is launched
     * @param context current context
     **/
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

    /**
     * Method for saving current AppData instance to shared preferences to persist data after restart
     * @param context current context
     * @param appData AppData instance to save
     * @param clearApiData If true, will leave only favourite data to AppData when saving
     */
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


    /**  SETTERS AND GETTERS FOR APPDATA VARIABLES **/

    public boolean isAccelometerEnabled() {
        return accelometerEnabled;
    }

    public boolean isRefreshing() {
        return refreshing;
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


    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public void setLightSensorEnabled(boolean lightSensorEnabled) {
        this.lightSensorEnabled = lightSensorEnabled;
    }
    public void setAccelometerEnabled(boolean accelometerEnabled) {
        this.accelometerEnabled = accelometerEnabled;
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
    public void setFavouriteData(List<StockData> favouriteData) {
        this.favouriteData = favouriteData;
    }


}

