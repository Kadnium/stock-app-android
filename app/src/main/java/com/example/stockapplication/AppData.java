package com.example.stockapplication;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    private static AppData appData;
    private List<StockData> stockData = new ArrayList<>();

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

    public List<StockData> getStockData() {
        return new ArrayList<StockData>(stockData);
    }

    public void setStockData(List<StockData> stockData) {
        this.stockData = stockData;
    }
    //add methods here for insert, delete, search etc......
}

