package com.example.stockapplication;

import android.content.Context;

import com.android.volley.VolleyError;

public interface StockApiCallback {
//    void onTrendingApiSuccess(String response);
//    void onSearchApiSuccess(String response);
//    void onDailyGainersSuccess(String response);
//    void onDailyLosersSuccess(String response);
//    void onSingleStockSuccess(String response);
    void onSuccess(String response, Context context);
    void onError(VolleyError error,Context context);
}
