package com.example.stockapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class OptionsActivity extends AppCompatActivity  {
    AppData appData;
    BottomNavigationHandler bottomNavigationHandler;
    SensorHandler sensorHandler;
    Spinner spinner;
    boolean spinnerClicked = false;
    public void initBackend(){
        if(appData == null){
            appData = AppData.parseAppDataFromSharedPrefs(this);
        }
        if(sensorHandler == null){
            sensorHandler = new SensorHandler(this, null);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        initBackend();
        bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.settings);
        initThemeSpinner();
        initSettingSwitches();

    }


    private void setAppTheme(int selected){
        int theme = appData.getThemeSetting(selected);
        AppData.setSettingToPrefs(this,AppData.SELECTED_THEME,selected);
        AppCompatDelegate.setDefaultNightMode(theme);
        recreate();
    }
    private void initThemeSpinner(){
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.theme_settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        spinner.setSelection(AppData.getSettingFromPrefs(this,AppData.SELECTED_THEME),false);
        // Prevent onItemSelected getting called when auto theme switch is enabled
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                spinnerClicked = true;
                return false;
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinnerClicked){
                    setAppTheme(position);
                    spinnerClicked = false;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void initSettingSwitches(){
        SwitchCompat accelometerSwitch = findViewById(R.id.accelometerSwitch);
        SwitchCompat lightSwitch = findViewById(R.id.lightSwitch);
        int accelometerEnabled = AppData.getSettingFromPrefs(this,AppData.ACCELOMETER_ENABLED);
        int ligthSensorEnabled = AppData.getSettingFromPrefs(this,AppData.LIGHT_SENSOR_ENABLED);

        accelometerSwitch.setChecked(accelometerEnabled == 1);
        lightSwitch.setChecked(ligthSensorEnabled == 1);
        setSpinnerEnabled(ligthSensorEnabled == 0);
        accelometerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppData.setSettingToPrefs(this,AppData.ACCELOMETER_ENABLED,isChecked?1:0);
            sensorHandler.updateSensors();

        });
        lightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppData.setSettingToPrefs(this,AppData.LIGHT_SENSOR_ENABLED,isChecked?1:0);
            setSpinnerEnabled(!isChecked);
            sensorHandler.updateSensors();

        });
    }
    private void setSpinnerEnabled(boolean value){
        spinner.setClickable(value);
        spinner.setEnabled(value);
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
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }
    @Override
    public void finish() {
        super.finish();
        // override back button default animation
        overridePendingTransition(0,0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(bottomNavigationHandler != null){
            bottomNavigationHandler.refresh();
        }
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
            sensorHandler.updateSensors();
        }

    }


}