package com.example.stockapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHandler {
    Context context;
    public BottomNavigationHandler(Context ctx) {
        this.context = ctx;
    }


    public void initNavigation(int viewId,int navId){

        if (!(context instanceof AppCompatActivity)) {
            return;
        }
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.getSupportActionBar().hide();
        BottomNavigationView bottomNavigationView = activity.findViewById(viewId);
        bottomNavigationView.setSelectedItemId(navId);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == navId) {
                    return true;
                }else if (id == R.id.home){
                    context.startActivity(new Intent(context.getApplicationContext(),MainActivity.class));
                    activity.overridePendingTransition(0,0);
                    return true;
                }else if(id == R.id.search){
                    context.startActivity(new Intent(context.getApplicationContext(),SearchActivity.class));
                    activity.overridePendingTransition(0,0);
                    return true;
                }else if (id == R.id.settings){
                    context.startActivity(new Intent(context.getApplicationContext(),OptionsActivity.class));
                    activity.overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });

    }
}
