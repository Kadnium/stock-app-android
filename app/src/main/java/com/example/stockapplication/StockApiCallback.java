package com.example.stockapplication;

import android.content.Context;

import com.android.volley.VolleyError;

import java.util.List;

public interface StockApiCallback {
    void onSuccess(List<StockData> response, Context context);
    void onError(VolleyError error,Context context);
}
