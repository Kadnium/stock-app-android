package com.example.stockapplication;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

public class ChartActivity extends AppCompatActivity {
    BottomNavigationHandler bottomNavigationHandler;
    AppData appData;
    SensorHandler sensorHandler;
    StockApi stockApi;
    LineChart chart;
    Gson gson = new Gson();
    StockData intentStock;
    List<Long> longList = new ArrayList<>();
    private void initBackend() {
        appData = AppData.parseAppDataFromSharedPrefs(this);
        sensorHandler = new SensorHandler(this, null);
        stockApi = new StockApi(this);

    }
    private void initChart(){
        SwipeRefreshLayout sv = findViewById(R.id.swipeContainer);
        chart = findViewById(R.id.stockChart);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
        @ColorInt int color = typedValue.data;
        // disable description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);
        // set listeners
        // chart.setOnChartValueSelectedListener(this);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        // scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        //  pinch zoom along both axis
        chart.setPinchZoom(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setSpaceBottom(30);
        chart.getAxisLeft().setTextColor(color);
        chart.getXAxis().setTextColor(color);
        chart.setNoDataText("");


    }

    private int setStockRow(StockData stock, float firstValue, float lastValue){
        TextView tSymbol, tFullName, tPercent, tStockPrice;
        ImageView favouriteStatus;
        tPercent = findViewById(R.id.priceChange);
        tStockPrice = findViewById(R.id.stockPrice);
        tFullName = findViewById(R.id.stockName);
        tSymbol = findViewById(R.id.stockTicker);

        tSymbol.setText(stock.getSymbol());
        String name = stock.getName();
        tFullName.setText(name);
        tStockPrice.setText(String.valueOf(lastValue));

        String sign = "+";
        if(lastValue-firstValue<0){
            tPercent.setTextColor(getColor(R.color.red));
            sign = "-";
        }else{
            tPercent.setTextColor(getColor(R.color.green));
        }
        double formattedPercentChange = stock.formatDouble((lastValue/firstValue)*100-100);
        tPercent.setText(sign+""+formattedPercentChange+"%");
        favouriteStatus = findViewById(R.id.favouriteStatus);
        boolean isFavourite= stock.isFavourite();
        favouriteStatus.setImageResource(isFavourite?R.drawable.ic_favourite:R.drawable.ic_not_favourite);
        favouriteStatus.setOnClickListener(v -> {
            if(isFavourite){
                appData.removeFromFavourites(stock);
                stock.setFavourite(false);
            }else{
                appData.addToFavourites(stock);
                stock.setFavourite(true);
            }
            setStockRow(stock,firstValue,lastValue);
        });
        return sign.equals("+")?getColor(R.color.green):getColor(R.color.red);


    }
    private void initChartData(String range){
        if(intentStock == null){
            Intent intent = getIntent();
            Gson gson = new Gson();
            String stockJson = intent.getStringExtra("Stock");
            intentStock = gson.fromJson(stockJson, StockData.class);
        }
        ProgressBar spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);
        stockApi.getChart(intentStock.getSymbol(), range, new StockApiCallback() {
            @Override
            public void onSuccess(List<StockData> response, Context context) {
                if(response.size()>0){

                    StockData stock = response.get(0);
                    List<Entry> yValues = new ArrayList<>();
                    LinkedHashMap<Long,Float> chartData = stock.getChartData();
                    int index = 0;
                    longList.clear();
                    Object[] floatData = chartData.values().toArray();
                    float lastValue = (float) floatData[chartData.size() -1];
                    int chartColor = setStockRow(intentStock, (float) stock.getPreviousClose(),lastValue);
                    for(Map.Entry<Long,Float> point:chartData.entrySet()){
                        longList.add(point.getKey());
                        yValues.add(new Entry(index,point.getValue()));
                        index++;
                    }
                    LineDataSet set1 = new LineDataSet(yValues,"");
                    set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    set1.setDrawFilled(true);
                    set1.setFillColor(chartColor);
                    set1.setFillAlpha(80);
                    set1.setCubicIntensity(0.2f);
                    set1.setFillAlpha(110);
                    set1.setColor(chartColor);
                    set1.setDrawCircles(false);
                    set1.setLineWidth(1f);
                    set1.setDrawValues(true);

                    List<ILineDataSet> dataSet = new ArrayList<>();
                    dataSet.add(set1);
                    LineData data = new LineData(dataSet);
                    chart.setData(data);
                    //chart.getAxisLeft().setAxisMinimum(0);


                    chart.getXAxis().setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            if(index>longList.size()-1){
                                return "-";
                            }
                            return new SimpleDateFormat("dd.MM").format(new Date(longList.get(index)*1000));



                        }
                    });


                    chart.notifyDataSetChanged();
                    chart.invalidate();
                    spinner.setVisibility(View.INVISIBLE);
                }


            }

            @Override
            public void onError(VolleyError error, Context context) {

            }
        });
    }
    private void initButtons(){
        HashMap<Integer,String> buttonMap = new HashMap<>();
        buttonMap.put(R.id.dayButton,StockApi.DAILY_RANGE);
        buttonMap.put(R.id.fiveDayButton,StockApi.FIVE_DAY_RANGE);
        buttonMap.put(R.id.monthButton,StockApi.ONE_MONTH_RANGE);
        buttonMap.put(R.id.sixMonthButton,StockApi.SIX_MONTH_RANGE);
        buttonMap.put(R.id.ytdButton,StockApi.YTD_RANGE);
        buttonMap.put(R.id.oneYearButton,StockApi.YEAR_RANGE);
        buttonMap.put(R.id.fiveYearButton,StockApi.FIVE_YEAR_RANGE);
        for(Map.Entry<Integer,String> buttonEntry:buttonMap.entrySet()){
            Button b = findViewById(buttonEntry.getKey());
            b.setOnClickListener(v -> {
                initChartData(buttonEntry.getValue());
            });
           /* b.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    b.setBackgroundColor(getColor(R.color.green));
                    //unPressOtherButtons(buttonEntry.getKey(),buttonMap);
                    initChartData(buttonEntry.getValue());
                    return true;
                }
            });*/

        }


    }
    private void unPressOtherButtons(int current,HashMap<Integer,String> buttonMap) {
        for(Map.Entry<Integer,String> buttonEntry:buttonMap.entrySet()){
            if(buttonEntry.getKey() != current){
            Button b = findViewById(buttonEntry.getKey());
                b.setBackgroundColor(getColor(R.color.red));
            //b.setPressed(false);
        }
    }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        getSupportActionBar().hide();
        bottomNavigationHandler = new BottomNavigationHandler(this,appData);
        bottomNavigationHandler.initNavigation(R.id.bottomNav, -1);


    }

    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        initChart();
        initChartData(StockApi.DAILY_RANGE);
        initButtons();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When app is closed
        if(appData != null){
            AppData.saveAppDataToSharedPrefs(this,appData,true);
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        // Activity change
        if(appData != null){
            AppData.saveAppDataToSharedPrefs(this,appData,false);
        }


    }

    @Override
    public void finish() {
        super.finish();
        // Override back button default animation
        overridePendingTransition(0,0);
    }



}