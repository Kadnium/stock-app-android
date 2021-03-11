package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        BottomNavigationHandler bottomNavigationHandler = new BottomNavigationHandler(this);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.settings);
    }
}