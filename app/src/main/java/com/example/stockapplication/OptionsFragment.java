package com.example.stockapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class OptionsFragment extends Fragment {

    AppData appData;
    SensorHandler sensorHandler;
    Spinner spinner;
    boolean spinnerClicked = false;
    OptionsHelper optionsHelper;
    View fragmentView;
    SwipeRefreshLayout swipeRefreshLayout;
    public void initBackend(){
        appData = AppData.getInstance(getContext());
        sensorHandler =appData.getSensorHandler(getContext());
        optionsHelper = new OptionsHelper(getContext(),fragmentView);
        optionsHelper.initAveragePriceFields();
        swipeRefreshLayout = getActivity().findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setEnabled(false);


    }

    public OptionsFragment() {
    }

    public static OptionsFragment newInstance() {
        return new OptionsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_options, container, false);
        //bottomNavigationHandler = new BottomNavigationHandler(getContext(),appData);
      //  bottomNavigationHandler.initNavigation(R.id.bottomNav,R.id.settings);
        // Inflate the layout for this fragment
        return fragmentView;
    }


    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        initThemeSpinner();
        initSettingSwitches();



    }


    private void setAppTheme(int selected){
        int theme = AppData.getThemeSetting(selected);
        AppData.setSettingToPrefs(getContext(),AppData.SELECTED_THEME,selected);
        AppCompatDelegate.setDefaultNightMode(theme);
        //getActivity().recreate();
    }
    private void initThemeSpinner(){
        spinner = fragmentView.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),R.array.theme_settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        spinner.setSelection(AppData.getSettingFromPrefs(getContext(),AppData.SELECTED_THEME),false);
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
        SwitchCompat accelometerSwitch = fragmentView.findViewById(R.id.accelometerSwitch);
        SwitchCompat lightSwitch = fragmentView.findViewById(R.id.lightSwitch);
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
    public void onPause() {
        super.onPause();
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }







}