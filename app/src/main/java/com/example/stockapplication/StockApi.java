package com.example.stockapplication;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class StockApi {
    private final Context context;
    private RequestQueue requestQueue;
    public static final String STOCK_DATA_API = "STOCK_DATA";
    public static final String TRENDING_API = "TRENDING";
    public static final String SEARCH_API = "SEARCH";
    public static final String DAILY_GAINER_API = "DAILY_GAINER";
    public static final String DAILY_LOSER_API = "DAILY_LOSER";
    public static final String CHART_API = "CHART_API";
    public static final String SPARK_API = "SPARK_API";
    // CHART RANGES
    public static final String DAILY_RANGE = "DAILY_RANGE";
    public static final String FIVE_DAY_RANGE = "FIVE_DAY_RANGE";
    public static final String ONE_MONTH_RANGE = "ONE_MONTH_RANGE";
    public static final String SIX_MONTH_RANGE = "SIX_MONTH_RANGE";
    public static final String YTD_RANGE = "YTD_RANGE";
    public static final String YEAR_RANGE = "YEAR_RANGE";
    public static final String FIVE_YEAR_RANGE = "FIVE_YEAR_RANGE";
    public static final String ALL_TIME_RANGE = "ALL_TIME_RANGE";
    private final HashMap<String, Pair<String,String>> chartApiHelper = new HashMap<String,Pair<String,String>>(){{
        put(DAILY_RANGE,new Pair<>("2m","1d"));
        put(FIVE_DAY_RANGE,new Pair<>("15m","5d"));
        put(ONE_MONTH_RANGE,new Pair<>("1h","1mo"));
        put(SIX_MONTH_RANGE,new Pair<>("1d","6mo"));
        put(YTD_RANGE,new Pair<>("1d","ytd"));
        put(YEAR_RANGE,new Pair<>("1wk","1y"));
        put(FIVE_YEAR_RANGE,new Pair<>("1mo","5y"));
        put(ALL_TIME_RANGE,new Pair<>("1mo","max"));
    }};
    // Front page info symbols and their names
    // Use these because these are better and simpler names than api returns
    public static final LinkedHashMap<String,String>  FRONT_PAGE_SYMBOL_MAP = new LinkedHashMap<String,String>(){{
        put("^GSPC","S&P 500");
        put("^DJI","DOW 30");
        put("^IXIC","NASDAQ");
        put("^RUT","Russel 2000");
        put("CL=F","Oil");
        put("GC=F","Gold");
        put("SI=F","Silver");
        put("EURUSD=X","EUR/USD");
        put("^TNX","10-Yr Bond");
        put("GBPUSD=X","GBP/USD");
        put("JPY=X","USD/JPY");
        put("BTC-USD","BITCOIN");
        put("^CMC200","CMC Crypto");
        put("^FTSE","FTSE 100");
        put("^N225","Nikkei 225");
    }};
    public StockApi(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());


    }
    /**
     * Each api method has api path with added arguments and
     * String constant name of api
     */
    private Pair<String,String> stockDataApi(String symbol){
        return new Pair<>("https://query1.finance.yahoo.com/v7/finance/quote?formatted=true&lang=en-US&region=US&symbols="+symbol+"&fields=symbol%2CshortName%2ClongName%2CregularMarketPrice%2CregularMarketChange%2CregularMarketChangePercent",
                STOCK_DATA_API
        );
    }
    private Pair<String,String> trendingApi(int count){
        return new Pair<>(
                "https://query1.finance.yahoo.com/v1/finance/trending/US?count="+count,
                TRENDING_API
        );
    }
    private Pair<String,String> chartApi(String ticker, Pair<String,String> rangePair){
        return new Pair<>("https://query1.finance.yahoo.com/v8/finance/chart/"+ticker+"?region=US&lang=en-US&includePrePost=false&indicators=close&interval="+rangePair.first+"&useYfid=true&range="+rangePair.second,CHART_API);
    }
    private Pair<String,String> searchApi(String args,int queryCount){
        return new Pair<>("https://query2.finance.yahoo.com/v1/finance/search?newsCount=0&enableFuzzyQuery=false&enableEnhancedTrivialQuery=true&region=US&lang=en-US&q="+args+"&quotesCount="+queryCount,
                SEARCH_API
        );
    }
    private Pair<String,String> dailyGainerApi(int queryCount){
        return new Pair<>("https://query2.finance.yahoo.com/v1/finance/screener/predefined/saved?corsDomain=finance.yahoo.com&formatted=false&lang=en-US&region=US&scrIds=day_gainers&count="+queryCount,
                DAILY_GAINER_API
        );
    }
    private Pair<String,String> dailyLoserApi(int queryCount){
        return new Pair<>("https://query1.finance.yahoo.com/v1/finance/screener/predefined/saved?corsDomain=finance.yahoo.com&formatted=false&lang=en-US&region=US&scrIds=day_losers&count="+queryCount,
                DAILY_LOSER_API
        );
    }



    /**
     * Get trending tickers api
     * @param count Amount to fetch
     * @param cb After finish callback
     */
    public void getTrending(int count,StockApiCallback cb){
        Pair<String,String> API = trendingApi(count);
        // Trending api returns tickers so after fetching tickers
        // Get data seperately for them
        fetchData(API, new StockApiCallback() {
            @Override
            public void onSuccess(List<StockData> response, Context context) {
                List<String> tickers = new ArrayList<>();
                for(StockData stock:response){
                    tickers.add(stock.getSymbol());
                }
                getByTickerNames(tickers,cb);
            }

            @Override
            public void onError(VolleyError error, Context context) {

            }
        });
    }

    /**
     * Find stocks from api
     * @param args Argument to use
     * @param queryCount Amount to results to get
     * @param cb After finish callback
     */
    public void getSearchResults(String args,int queryCount,StockApiCallback cb){
        Pair<String,String> API = searchApi(args,queryCount);
        // Returns tickers so after fetching tickers
        // Get data seperately for them
        fetchData(API, new StockApiCallback() {
            @Override
            public void onSuccess(List<StockData> response, Context context) {
                List<String> tickers = new ArrayList<>();
                for(StockData stock:response){
                    tickers.add(stock.getSymbol());
                }
                getByTickerNames(tickers,cb);
            }

            @Override
            public void onError(VolleyError error, Context context) {

            }
        });

    }

    /**
     * Get chart for stock using ticker
     * @param ticker Ticker to get chart for
     * @param type Timeframe string
     * @param cb After finish callback
     */
    public void getChart(String ticker,String type, StockApiCallback cb){
        Pair<String,String> rangePair = chartApiHelper.get(type);
        assert rangePair != null;
        Pair<String,String> API = chartApi(ticker,rangePair);
        fetchData(API,cb);

    }

    /**
     * Fetch data for frontpage symbols
     * @param cb After finish callback
     */
    public void getFrontPageSymbols(StockApiCallback cb){
        Set<String> mapKeys = FRONT_PAGE_SYMBOL_MAP.keySet();
        List<String> symbols = new ArrayList<>(mapKeys);
        getByTickerNames(symbols,cb);
    }

    /**
     * Daily gainer and daily loser both use same logic
     * getDailyMovers uses these two
     * @param count Amoun to get
     * @param cb After finish callback
     */
    public void getDailyGainers(int count, StockApiCallback cb){
        Pair<String,String> API = dailyGainerApi(count);
        fetchData(API,cb);
    }
    public void getDailyLosers(int count, StockApiCallback cb){
        Pair<String,String> API = dailyLoserApi(count);
        fetchData(API,cb);
    }
    public void getDailyMovers(int count,StockApiCallback cb){
       getDailyLosers(count, new StockApiCallback() {
           @Override
           public void onSuccess(List<StockData> loserResponse, Context context) {
               getDailyGainers(count, new StockApiCallback() {
                   @Override
                   public void onSuccess(List<StockData> winnerResponse, Context context) {
                       winnerResponse.addAll(loserResponse);
                       cb.onSuccess(winnerResponse,context);
                   }

                   @Override
                   public void onError(VolleyError error, Context context) {

                   }
               });
           }

           @Override
           public void onError(VolleyError error, Context context) {

           }
       });
    }

    /**
     * Fetch stockdata by ticker names
     * @param tickers List of tickers
     * @param cb After finish callback
     */
    public void getByTickerNames(List<String> tickers, StockApiCallback cb){
        String joinedTickers ="";
        joinedTickers = TextUtils.join("%2C",tickers);
        Pair<String,String> API = stockDataApi(joinedTickers);
        fetchData(API,cb);

    }

    /**
     * Main method for fetching data
     * Needs Pair which contains api name and api path
     * @param API Pair which contains api name and api path
     * @param callback After finish callback
     */
    private void fetchData(Pair<String,String> API,StockApiCallback callback){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API.first,
                response -> {
                    // After fetch, parse json, put the to list and return
                    List<StockData> responseArr;
                    switch(Objects.requireNonNull(API.second)){
                        case STOCK_DATA_API:
                            responseArr  = stockDataApiParse(response);
                            break;
                        case TRENDING_API:
                            responseArr = trendingApiParse(response);
                            break;
                        case SEARCH_API:
                            responseArr = searchApiParse(response);
                            break;
                        case DAILY_GAINER_API:
                        case DAILY_LOSER_API:
                            responseArr = dailyMoverApiParse(response);
                            break;
                        case CHART_API:
                            responseArr = chartApiParse(response);
                            break;
                        default:
                            responseArr = new ArrayList<>();
                            break;

                    }
                    callback.onSuccess(responseArr,context);
                }, error -> callback.onError(error,context));

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context);
        }
        requestQueue.add(stringRequest);
    }

    /**
     * Parser for chart api
     * @param json JSON from volley
     * @return parsed List of stockData
     */
    private List<StockData> chartApiParse(String json){
        List<StockData> stockList = new ArrayList<>();
        try {
            JSONObject mainObject = new JSONObject(json);
            mainObject = mainObject.getJSONObject("chart");
            JSONArray dataArr = mainObject.getJSONArray("result");
            JSONObject dataObject = dataArr.getJSONObject(0);
            JSONObject meta = dataObject.getJSONObject("meta");
            JSONArray timestamp =  dataObject.getJSONArray("timestamp");
            JSONObject ind =  dataObject.getJSONObject("indicators");
            JSONObject quote = ind.getJSONArray("quote").getJSONObject(0);
            JSONArray dataPoints = quote.getJSONArray("close");
            double previousClose = meta.getDouble("chartPreviousClose");
            StockData stock = new StockData(null,null,null,0,0,false,null);
            stock.setPreviousClose(previousClose);
            // Chart datapoints saved in LinkedHashMap
            // LinkedHashMap used because data order can't change
            LinkedHashMap<Long,Float> datasetMap = new LinkedHashMap<>();
            for (int i = 0; i <timestamp.length(); i++) {
                if(i<timestamp.length() && i<dataPoints.length()){
                    String longValue = timestamp.getString(i);
                    String doubleValue = dataPoints.getString(i);
                    // Some tickers' JSON arrays have null values
                    if(!longValue.equalsIgnoreCase("null") && !doubleValue.equalsIgnoreCase("null")){
                        datasetMap.put(Long.valueOf(longValue),Float.valueOf(doubleValue));
                    }

                }
            }
            stock.setChartData(datasetMap);
            stockList.add(stock);
        } catch (JSONException e) {
            
            e.printStackTrace();
        }
        return stockList;
    }
    /**
     * Parser for stock data
     * @param json JSON from volley
     * @return parsed List of stockData
     */
    private List<StockData> stockDataApiParse(String json){
        List<StockData> stockList = new ArrayList<>();
        try {
            JSONObject mainObject = new JSONObject(json);
            mainObject = mainObject.getJSONObject("quoteResponse");
            JSONArray dataArr = mainObject.getJSONArray("result");
            for (int i = 0; i <dataArr.length(); i++) {
                JSONObject obj = dataArr.getJSONObject(i);
               // String symbol, String market, String name, double percentChange, double marketPrice, boolean isFavourite,String uuid
                String symbol = obj.getString("symbol");
                String market = obj.getString("fullExchangeName");
                String name = obj.getString("shortName");
                JSONObject priceObj = obj.getJSONObject("regularMarketPrice");
                double price = priceObj.getDouble("raw");
                JSONObject percentObj = obj.getJSONObject("regularMarketChangePercent");
                double percent = percentObj.getDouble("raw");
                StockData stock = new StockData(symbol,market,name,percent,price,false,null);
                stockList.add(stock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stockList;
    }
    /**
     * Parser for Trending data
     * @param json JSON from volley
     * @return parsed List of stockData
     */
    private List<StockData> trendingApiParse(String json){
        List<StockData> stockList = new ArrayList<>();
        try {
            JSONObject mainObject = new JSONObject(json);
            mainObject = mainObject.getJSONObject("finance");
            JSONArray dataArr = mainObject.getJSONArray("result");
            dataArr = dataArr.getJSONObject(0).getJSONArray("quotes");

            for (int i = 0; i <dataArr.length(); i++) {
                JSONObject obj = dataArr.getJSONObject(i);
                // String symbol, String market, String name, double percentChange, double marketPrice, boolean isFavourite,String uuid
                String symbol = obj.getString("symbol");
                StockData stock = new StockData(symbol,null,null,0,0,false,null);
                stockList.add(stock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stockList;
    }

    /**
     * Parser for search data
     * @param json JSON from volley
     * @return parsed List of stockData
     */
    private List<StockData> searchApiParse(String json){
        List<StockData> stockList = new ArrayList<>();
        try {
            JSONObject mainObject = new JSONObject(json);
            JSONArray dataArr = mainObject.getJSONArray("quotes");

            for (int i = 0; i <dataArr.length(); i++) {
                JSONObject obj = dataArr.getJSONObject(i);
                // String symbol, String market, String name, double percentChange, double marketPrice, boolean isFavourite,String uuid
                String symbol = obj.getString("symbol");
                StockData stock = new StockData(symbol,null,null,0,0,false,null);
                stockList.add(stock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stockList;
    }

    /**
     * Parser for daily movers
     * @param json JSON from volley
     * @return parsed List of stockData
     */
    private List<StockData> dailyMoverApiParse(String json){
        List<StockData> stockList = new ArrayList<>();
        try {
            JSONObject mainObject = new JSONObject(json);
            mainObject = mainObject.getJSONObject("finance").getJSONArray("result").getJSONObject(0);
            JSONArray dataArr = mainObject.getJSONArray("quotes");

            for (int i = 0; i <dataArr.length(); i++) {
                JSONObject obj = dataArr.getJSONObject(i);
                // String symbol, String market, String name, double percentChange, double marketPrice, boolean isFavourite,String uuid
                String symbol = obj.getString("symbol");
                String market = obj.getString("fullExchangeName");
                String name = obj.getString("shortName");
                double price = obj.getDouble("regularMarketPrice");
                double percent = obj.getDouble("regularMarketChangePercent");
                StockData stock = new StockData(symbol,market,name,percent,price,false,null);
                stockList.add(stock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stockList;

    }



}
