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
import android.widget.ProgressBar;
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
    OptionsHelper optionsHelper;

    public void initBackend(){
        appData = AppData.getInstance(this);
        sensorHandler =appData.getSensorHandler(this);
        optionsHelper = new OptionsHelper(this);
        optionsHelper.initAveragePriceFields();

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.settings);

    }

    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        initThemeSpinner();
        initSettingSwitches();
        bottomNavigationHandler.refresh();



    }


    private void setAppTheme(int selected){
        int theme = AppData.getThemeSetting(selected);
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
        boolean accelometerEnabled = appData.isAccelometerEnabled();//;AppData.getSettingFromPrefs(this,AppData.ACCELOMETER_ENABLED);
        boolean ligthSensorEnabled = appData.isLightSensorEnabled();//AppData.getSettingFromPrefs(this,AppData.LIGHT_SENSOR_ENABLED);

        accelometerSwitch.setChecked(accelometerEnabled);
        lightSwitch.setChecked(ligthSensorEnabled);
        setSpinnerEnabled(!ligthSensorEnabled);
        accelometerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appData.setAccelometerEnabled(isChecked);
            //AppData.setSettingToPrefs(this,AppData.ACCELOMETER_ENABLED,isChecked?1:0);
            sensorHandler.updateSensors(isChecked,ligthSensorEnabled);

        });
        lightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appData.setLightSensorEnabled(isChecked);
            //AppData.setSettingToPrefs(this,AppData.LIGHT_SENSOR_ENABLED,isChecked?1:0);
            setSpinnerEnabled(!isChecked);
            sensorHandler.updateSensors(accelometerEnabled,isChecked);

        });
    }
    private void setSpinnerEnabled(boolean value){
        spinner.setClickable(value);
        spinner.setEnabled(value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(appData != null){
            AppData.saveAppDataToSharedPrefs(this,appData,false);
        }

        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }
    @Override
    public void finish() {
        super.finish();
        //AppData.saveAppDataToSharedPrefs(this,appData,false);
        // override back button default animation
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }




}