package com.example.stockapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

public class BottomNavigationHandler {
    Context context;
    BottomNavigationView bottomNavigationView;
    AppData appData;
    int navId;
    boolean selectedByCode = false;
    public BottomNavigationHandler(Context ctx) {
        this.context = ctx;

    }


    public void initNavigation(int viewId,int navId){
        if (!(context instanceof AppCompatActivity)) {
            return;
        }
        this.navId = navId;
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.getSupportActionBar().hide();
        bottomNavigationView = activity.findViewById(viewId);
        if(navId !=-1){
            bottomNavigationView.setSelectedItemId(navId);
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(!selectedByCode){
                    if(id == bottomNavigationView.getSelectedItemId()   && !(activity.getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) instanceof ChartFragment)) {
                        return true;
                    }else if (id == R.id.home){
                        FragmentTransaction fragmentTransaction=activity.getSupportFragmentManager().beginTransaction();
                       // fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
                        fragmentTransaction.replace(R.id.fragmentContainer,MainFragment.newInstance());
                        fragmentTransaction.addToBackStack(AppData.MAIN_FRAGMENT);
                        fragmentTransaction.commit();
                        return true;
                    }else if(id ==  R.id.search){
                        FragmentTransaction fragmentTransaction=activity.getSupportFragmentManager().beginTransaction();
                        //fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
                        fragmentTransaction.replace(R.id.fragmentContainer,SearchFragment.newInstance());
                        fragmentTransaction.addToBackStack(AppData.SEARCH_FRAGMENT);
                        fragmentTransaction.commit();

                        return true;
                    }else if (id == R.id.settings ){
                        FragmentTransaction fragmentTransaction=activity.getSupportFragmentManager().beginTransaction();
                       // fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
                        fragmentTransaction.replace(R.id.fragmentContainer,OptionsFragment.newInstance());
                        fragmentTransaction.addToBackStack(AppData.OPTIONS_FRAGMENT);
                        fragmentTransaction.commit();
                        return true;
                    }
                }else{
                    selectedByCode = false;
                    return true;
                }


                return false;
            }
        });

    }
    public void setSelectedItem(int navId){
        if(bottomNavigationView != null && bottomNavigationView.getSelectedItemId() != navId){
            selectedByCode = true;
            bottomNavigationView.setSelectedItemId(navId);
        }

    }

    public void setNavId(int navId) {
        this.navId = navId;
    }


    public void refresh(){
        if(bottomNavigationView != null && bottomNavigationView.getSelectedItemId() != this.navId){
            bottomNavigationView.setSelectedItemId(this.navId);

        }
    }


}
