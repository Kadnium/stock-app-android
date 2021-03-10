package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.VolleyError;

public class MainActivity extends AppCompatActivity{
    StockApi stockApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stockApi = new StockApi(this);
        stockApi.getDailyGainers(5,new StockApiCallback() {
            @Override
            public void onSuccess(String response, Context context) {
                Toast.makeText(context,response,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(VolleyError error,Context context) {
                Toast.makeText(context,error.networkResponse.toString(),Toast.LENGTH_LONG).show();

            }
        });
    }





}