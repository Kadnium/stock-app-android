package com.example.stockapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

public class BottomNavigationHandler {
    Context context;
    BottomNavigationView bottomNavigationView;
    AppData appData;
    int navId;
    public BottomNavigationHandler(Context ctx,AppData appData) {
        this.context = ctx;
        this.appData = appData;
    }
    public void setIntentData(Intent intent){
        Gson gson = new Gson();
        String data = gson.toJson(appData);
        intent.putExtra("appData",data);
    }

    public void initNavigation(int viewId,int navId){

        if (!(context instanceof AppCompatActivity)) {
            return;
        }
        this.navId = navId;
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.getSupportActionBar().hide();
        bottomNavigationView = activity.findViewById(viewId);
        bottomNavigationView.setSelectedItemId(navId);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == navId) {
                    return true;
                }else if (id == R.id.home){
                    Intent intent = new Intent(context.getApplicationContext(),MainActivity.class);
                    //setIntentData(intent);
                    context.startActivity(intent);
                    activity.overridePendingTransition(0,0);
                    return true;
                }else if(id == R.id.search){
                    Intent intent = new Intent(context.getApplicationContext(),SearchActivity.class);
                    //setIntentData(intent);
                    context.startActivity(intent);
                    activity.overridePendingTransition(0,0);
                    return true;
                }else if (id == R.id.settings){
                    Intent intent = new Intent(context.getApplicationContext(),OptionsActivity.class);
                    //setIntentData(intent);
                    context.startActivity(intent);
                    activity.overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });

    }

    public void setSelectedItem(int navId){
        if(bottomNavigationView != null){
            bottomNavigationView.setSelectedItemId(navId);
        }

    }

    public void refresh(){
        if(bottomNavigationView != null){
            bottomNavigationView.setSelectedItemId(this.navId);
        }
    }
}
