package com.example.stockapplication;

import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StockApi {
    private final Context context;
    private RequestQueue requestQueue;
    static final String STOCK_DATA_API = "STOCK_DATA";
    static final String TRENDING_API = "TRENDING";
    static final String SEARCH_API = "SEARCH";
    static final String DAILY_GAINER_API = "DAILY_GAINER";
    static final String DAILY_LOSER_API = "DAILY_LOSER";
    public StockApi(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    private Pair<String,String> stockDataApi(String symbol){
        return new Pair<>("https://query1.finance.yahoo.com/v7/finance/quote?formatted=true&lang=en-US&region=US&symbols="+symbol+"&fields=symbol%2CshortName%2ClongName%2CregularMarketPrice%2CregularMarketChange%2CregularMarketChangePercent",
                STOCK_DATA_API
        );
    }
    // Will return only symbols
    private Pair<String,String> trendingApi(int count){
        return new Pair<>(
                "https://query1.finance.yahoo.com/v1/finance/trending/US?count="+count,
                TRENDING_API
        );
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

    public void getTrending(int count,StockApiCallback cb){
        Pair<String,String> API = trendingApi(count);

        fetchData(API, new StockApiCallback() {
            @Override
            public void onSuccess(List<StockData> response, Context context) {
                List<String> tickers = new ArrayList<>();
                for(StockData stock:response){
                    tickers.add(stock.getSymbol());
                }
                // parse response to JSON
                // loop tickers and add them to array
                getByTickerNames(tickers,cb);
            }

            @Override
            public void onError(VolleyError error, Context context) {

            }
        });
    }
    public void getSearchResults(String args,int queryCount,StockApiCallback cb){
        Pair<String,String> API = searchApi(args,queryCount);
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
    public void getByTickerNames(List<String> tickers, StockApiCallback cb){
        // TODO fix for lower versions
        String joinedTickers ="";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            joinedTickers = String.join("%2C",tickers);
        }
        Pair<String,String> API = stockDataApi(joinedTickers);
        fetchData(API,cb);

    }

    private void fetchData(Pair<String,String> API,StockApiCallback callback){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API.first,
                response -> {
                    List<StockData> responseArr = null;
                    switch(API.second){
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
                        default:
                            responseArr = new ArrayList<>();

                    }
                    callback.onSuccess(responseArr,context);
                }, error -> {
                    callback.onError(error,context);
        });

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context);
        }
        requestQueue.add(stringRequest);
    }


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
