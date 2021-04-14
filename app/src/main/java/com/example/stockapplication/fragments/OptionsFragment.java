package com.example.stockapplication.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.stockapplication.datahelpers.AppData;
import com.example.stockapplication.datahelpers.OptionsHelper;
import com.example.stockapplication.R;
import com.example.stockapplication.datahelpers.SensorHandler;

import java.util.Objects;


public class OptionsFragment extends Fragment {
    private AppData appData;
    private SensorHandler sensorHandler;
    private Spinner spinner;
    private boolean spinnerClicked = false;
    private OptionsHelper optionsHelper;
    private View fragmentView;
    private SwipeRefreshLayout swipeRefreshLayout;
    public void initBackend(){
        appData = AppData.getInstance(getContext());
        sensorHandler =appData.getSensorHandler(getContext());
        optionsHelper = new OptionsHelper(getContext(),fragmentView);
        optionsHelper.initAveragePriceFields();
        swipeRefreshLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.swipeContainer);
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
        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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

    /**
     * Sets app theme
     * @param selected Selected dropdown options
     */
    private void setAppTheme(int selected){
        int theme = AppData.getThemeSetting(selected);
        AppData.setSettingToPrefs(Objects.requireNonNull(getContext()),AppData.SELECTED_THEME,selected);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    /**
     * Init theme drop down
     */
    private void initThemeSpinner(){
        spinner = fragmentView.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),R.array.theme_settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        spinner.setSelection(AppData.getSettingFromPrefs(Objects.requireNonNull(getContext()),AppData.SELECTED_THEME),false);
        // Prevent onItemSelected getting called when auto theme switch is enabled
        spinner.setOnTouchListener((v, event) -> {
            spinnerClicked = true;
            return false;
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

    /**
     * Inits settings switches
     */
    private void initSettingSwitches(){
        SwitchCompat accelometerSwitch = fragmentView.findViewById(R.id.accelometerSwitch);
        SwitchCompat lightSwitch = fragmentView.findViewById(R.id.lightSwitch);
        boolean accelometerEnabled = appData.isAccelometerEnabled();
        boolean ligthSensorEnabled = appData.isLightSensorEnabled();

        accelometerSwitch.setChecked(accelometerEnabled);
        lightSwitch.setChecked(ligthSensorEnabled);
        setSpinnerEnabled(!ligthSensorEnabled);
        accelometerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appData.setAccelometerEnabled(isChecked);
            sensorHandler.updateSensors(isChecked,ligthSensorEnabled);

        });
        lightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appData.setLightSensorEnabled(isChecked);
            // Disable theme selector if ligth setting is selected
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