package com.example.stockapplication;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class StockApi {
    private Context context;
    private RequestQueue requestQueue;

    public StockApi(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    private String stockDataApi(String symbol){
        return "https://query1.finance.yahoo.com/v7/finance/quote?formatted=true&lang=en-US&region=US&symbols="+symbol+"&fields=symbol%2CshortName%2ClongName%2CregularMarketPrice%2CregularMarketChange%2CregularMarketChangePercent";
    }

    private String trendingApi(int count){
        return "https://query1.finance.yahoo.com/v1/finance/trending/US?count="+count;
    }

    private String searchApi(String args,int queryCount){
        return "https://query2.finance.yahoo.com/v1/finance/search?newsCount=0&enableFuzzyQuery=false&enableEnhancedTrivialQuery=true&region=US&lang=en-US&q="+args+"&quotesCount="+queryCount;
    }

    private String dailyGainerApi(int queryCount){
        return "https://query2.finance.yahoo.com/v1/finance/screener/predefined/saved?corsDomain=finance.yahoo.com&formatted=false&lang=en-US&region=US&scrIds=day_gainers&count="+queryCount;
    }

    private String dailyLoserApi(int queryCount){
        return "https://query1.finance.yahoo.com/v1/finance/screener/predefined/saved?corsDomain=finance.yahoo.com&formatted=false&lang=en-US&region=US&scrIds=day_losers&count="+queryCount;
    }

    public void getTrending(int count,StockApiCallback cb){
        String API = trendingApi(count);
        fetchData(API,cb);
    }
    public void getSearchResults(String args,int queryCount,StockApiCallback cb){
        String API = searchApi(args,queryCount);
        fetchData(API,cb);

    }
    public void getDailyGainers(int count, StockApiCallback cb){
        String API = dailyGainerApi(count);
        fetchData(API,cb);
    }
    public void getDailyLosers(int count, StockApiCallback cb){
        String API = dailyLoserApi(count);
        fetchData(API,cb);
    }

    public void getByTickerNames(String[] tickers,StockApiCallback cb){
        // TODO fix for lower versions
        String joinedTickers ="";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            joinedTickers = String.join("%",tickers);
        }
        String API = stockDataApi(joinedTickers);
        fetchData(API,cb);

    }

    private void fetchData( String API,StockApiCallback callback){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API,
                response -> {
                    callback.onSuccess(response,context);
                }, error -> {
                    callback.onError(error,context);
        });

        if(requestQueue != null){
            requestQueue.add(stringRequest);
        }
    }


}
