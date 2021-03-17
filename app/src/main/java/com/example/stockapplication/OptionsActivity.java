package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class OptionsActivity extends AppCompatActivity  {
    AppData appData;
    public void initBackend(){
        if(appData == null){
            appData = AppData.parseAppDataFromSharedPrefs(this);
        }
    }


    private void setAppTheme(int selected){
        // WILL RERUN ONCREATE IF USE
        int theme = appData.getThemeSetting(selected);
        SharedPreferences sharedPreferences = getSharedPreferences(AppData.APP_DATA, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(AppData.SELECTED_THEME, selected);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(theme);
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        initBackend();
        BottomNavigationHandler bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.settings);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.theme_settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        // Avoid calling onSelected on init
        spinner.setSelection(AppData.getThemeFromPrefs(this),false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setAppTheme(position);
                //Toast.(parent.getContext(),text,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppData.saveAppDataToSharedPrefs(this,appData,true);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppData.saveAppDataToSharedPrefs(this,appData,false);

    }


}