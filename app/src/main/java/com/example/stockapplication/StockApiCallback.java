package com.example.stockapplication;

import android.content.Context;

import com.android.volley.VolleyError;

public interface StockApiCallback {
    void onSuccess(String response, Context context);
    void onError(VolleyError error,Context context);
}
