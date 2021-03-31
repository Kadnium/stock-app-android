package com.example.stockapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.MPPointD;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class ChartFragment extends Fragment {
    BottomNavigationHandler bottomNavigationHandler;
    AppData appData;
    SensorHandler sensorHandler;
    StockApi stockApi;
    LineChart chart;
    StockData intentStock;
    List<Long> longList = new ArrayList<>();
    Object[] floatArray;
    String timeFrame =StockApi.DAILY_RANGE;
    TextView priceText,dateText;
    SwipeRefreshLayout swipeRefreshLayout;
    View fragmentView;
    private void initBackend() {
        appData = AppData.getInstance(getContext());
        sensorHandler = appData.getSensorHandler(getContext());
        sensorHandler.setOnShakeCallback(()->initChartData(timeFrame,null));
        stockApi = appData.getStockApi(getContext());
        priceText=fragmentView.findViewById(R.id.chartPriceInfo);
        dateText=fragmentView.findViewById(R.id.chartDateInfo);
        swipeRefreshLayout = getActivity().findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            appData.setRefreshing(true);
            initChartData(timeFrame,()->{
                appData.setRefreshing(false);
                swipeRefreshLayout.setRefreshing(false);
            });



        });

    }

    private void setChartInfoTexts(int index){
        if(index<floatArray.length && index<longList.size() && index>0){
            if(floatArray[index] != null){
                priceText.setText(floatArray[index]+"");
            }
            if(longList.get(index) != null){
                dateText.setText(formatTimestamp(longList.get(index),"FORMAT_ALL"));
            }

        }
    }
    private void initChart(){
        chart = fragmentView.findViewById(R.id.stockChart);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
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
        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MPPointD point = chart.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(event.getX(),event.getY());
                setChartInfoTexts((int)point.x);
                return false;
            }
        });


    }

    private int setStockRow(StockData stock, float firstValue, float lastValue){
        TextView tSymbol, tFullName, tPercent, tStockPrice;
        ImageView favouriteStatus;
        tPercent = fragmentView.findViewById(R.id.priceChange);
        tStockPrice = fragmentView.findViewById(R.id.stockPrice);
        tFullName = fragmentView.findViewById(R.id.stockName);
        tSymbol = fragmentView.findViewById(R.id.stockTicker);

        tSymbol.setText(stock.getSymbol());
        String name = stock.getName();
        tFullName.setText(name);
        tStockPrice.setText(String.valueOf(lastValue));

        String sign = "+";
        if(lastValue-firstValue<0){
            tPercent.setTextColor(getContext().getColor(R.color.red));
            sign = "-";
        }else{
            tPercent.setTextColor(getContext().getColor(R.color.green));
        }
        double formattedPercentChange =lastValue==0.0 || firstValue==0.0? 0:stock.formatDouble((lastValue/firstValue)*100-100);
        tPercent.setText(sign+""+formattedPercentChange+"%");
        favouriteStatus = fragmentView.findViewById(R.id.favouriteStatus);
        boolean isFavourite = stock.isFavourite();
        favouriteStatus.setImageResource(isFavourite?R.drawable.ic_favourite:R.drawable.ic_not_favourite);
        favouriteStatus.setOnClickListener(v -> {
            if(isFavourite){
                stock.setFavourite(false);
                int favouriteIndex = appData.removeFromFavourites(stock);
                if(favouriteIndex != -1){
                    appData.updateFavouriteStatuses(stock,appData.getTrendingList(),false);
                    appData.updateFavouriteStatuses(stock,appData.getMostChanged(),false);
                }
            }else{
                stock.setFavourite(true);
                appData.addToFavourites(stock);
                appData.updateFavouriteStatuses(stock,appData.getTrendingList(),true);
                appData.updateFavouriteStatuses(stock,appData.getMostChanged(),true);
            }
            setStockRow(stock,firstValue,lastValue);
        });
        return sign.equals("+")?getContext().getColor(R.color.green):getContext().getColor(R.color.red);


    }
    private String getTimeFormat(String args){
        switch (args){
            case StockApi.DAILY_RANGE:
                return "HH:mm";
            case StockApi.FIVE_DAY_RANGE:
            case StockApi.ONE_MONTH_RANGE:
                return "dd/MM";
            case StockApi.SIX_MONTH_RANGE:
            case StockApi.YTD_RANGE:
            case StockApi.FIVE_YEAR_RANGE:
            case StockApi.ALL_TIME_RANGE:
                return "MM/yyyy";
            case "FORMAT_ALL":
                return "dd/MM/yyyy HH:mm:ss";
            default:
                return "yyyy";

        }

    }
    private String formatTimestamp(long timestamp,String customTimeFrame){
        String pattern;
        if(customTimeFrame!= null){
            pattern = getTimeFormat(customTimeFrame);
        }else{
            pattern = getTimeFormat(timeFrame);
        }
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat(pattern, Locale.getDefault());//;.format(new Date(timestamp * 1000));
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        return simpleDateFormat.format(new Date(timestamp * 1000));

    }
    private void initChartData(String range, HelperCallback callback){
        if(intentStock == null){
            Bundle bundle = getArguments();
            if(bundle != null){
                Gson gson = new Gson();
                String stockJson = bundle.getString("Stock");
                intentStock = gson.fromJson(stockJson, StockData.class);
            }

        }
        ProgressBar spinner = fragmentView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        stockApi.getChart(intentStock.getSymbol(), range, new StockApiCallback() {
            @Override
            public void onSuccess(List<StockData> response, Context context) {
                if(response.size()>0) {

                    StockData stock = response.get(0);
                    List<Entry> yValues = new ArrayList<>();
                    LinkedHashMap<Long, Float> chartData = stock.getChartData();
                    int index = 0;
                    longList.clear();
                    floatArray = chartData.values().toArray();
                    float lastValue = (float) floatArray[chartData.size() - 1];
                    int chartColor = setStockRow(intentStock, (float) stock.getPreviousClose(), lastValue);
                    for (Map.Entry<Long, Float> point : chartData.entrySet()) {
                        longList.add(point.getKey());
                        yValues.add(new Entry(index, point.getValue()));
                        index++;
                    }
                    LineDataSet set1;
                    if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
                        set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
                        set1.setValues(yValues);
                        set1.setColor(chartColor);
                        set1.setFillColor(chartColor);
                        chart.getData().notifyDataChanged();
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                    } else {
                        set1 = new LineDataSet(yValues, "");
                        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                        set1.setDrawFilled(true);
                        set1.setFillColor(chartColor);
                        set1.setFillAlpha(80);
                        set1.setCubicIntensity(0.2f);
                        set1.setFillAlpha(110);
                        set1.setColor(chartColor);
                        set1.setDrawCircles(false);
                        set1.setLineWidth(1f);
                        set1.setDrawValues(false);

                        LineData data = new LineData(set1);
                        ValueFormatter formatter = new ValueFormatter() {
                            @Override
                            public String getFormattedValue(float value) {
                                if(value>longList.size()-1){
                                    return "";
                                }
                                int index = (int) value;
                                return formatTimestamp(longList.get(index),null);
                            }
                        };
                        chart.getXAxis().setValueFormatter(formatter);
                        chart.setData(data);
                        chart.invalidate();
                    }


                }
                spinner.setVisibility(View.INVISIBLE);
                if(callback != null){
                    callback.onComplete();
                }



            }

            @Override
            public void onError(VolleyError error, Context context) {
                if(callback != null){
                    callback.onComplete();
                }
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
        buttonMap.put(R.id.maxButton,StockApi.ALL_TIME_RANGE);
        for(Map.Entry<Integer,String> buttonEntry:buttonMap.entrySet()){
            Button b = fragmentView.findViewById(buttonEntry.getKey());
            b.setOnClickListener(v -> {
                timeFrame = buttonEntry.getValue();
                initChartData(buttonEntry.getValue(),null);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_chart, container, false);
      //  bottomNavigationHandler = new BottomNavigationHandler(getContext(),appData);
        //bottomNavigationHandler.initNavigation(R.id.bottomNav, -1);
        // Inflate the layout for this fragment
        return fragmentView;
    }

    @Override
    public void onStart(){
        super.onStart();
        initBackend();
        initChart();
        initChartData(StockApi.DAILY_RANGE,null);
        initButtons();
    }




    @Override
    public void onPause() {
        super.onPause();
        if(sensorHandler != null){
            sensorHandler.unRegisterSensors();
        }

    }




    public ChartFragment() {
    }

    public static ChartFragment newInstance() {
        return new ChartFragment();
    }



}