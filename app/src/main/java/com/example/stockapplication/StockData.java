package com.example.stockapplication;

import java.util.UUID;

/* {
         "fullExchangeName": "NasdaqGS",
         "symbol": "ASML",
         "gmtOffSetMilliseconds": -18000000,
         "language": "en-US",
         "regularMarketTime": {
         "raw": 1615323602,
         "fmt": "4:00PM EST"
         },
         "regularMarketChangePercent": {
         "raw": 7.433445,
         "fmt": "7.43%"
         },
         "quoteType": "EQUITY",
         "tradeable": false,
         "regularMarketPreviousClose": {
         "raw": 502.19,
         "fmt": "502.19"
         },
         "exchangeTimezoneName": "America/New_York",
         "regularMarketChange": {
         "raw": 37.330017,
         "fmt": "37.33"
         },
         "exchangeDataDelayedBy": 0,
         "firstTradeDateMilliseconds": 795277800000,
         "exchangeTimezoneShortName": "EST",
         "regularMarketPrice": {
         "raw": 539.52,
         "fmt": "539.52"
         },
         "marketState": "PRE",
         "market": "us_market",
         "quoteSourceName": "Delayed Quote",
         "priceHint": 2,
         "exchange": "NMS",
         "sourceInterval": 15,
         "region": "US",
         "shortName": "ASML Holding N.V. - New York Re",
         "triggerable": true,
         "longName": "ASML Holding N.V."
         },*/
public class StockData {
    private String symbol;
    private String market;
    private String name;
    private String uuid;
    private double percentChange;
    private double marketPrice;
    private boolean isFavourite;

    public StockData(String symbol, String market, String name, double percentChange, double marketPrice, boolean isFavourite,String uuid) {
        this.symbol = symbol;
        this.market = market;
        this.name = name;
        this.percentChange = percentChange;
        this.marketPrice = marketPrice;
        this.isFavourite = isFavourite;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setPercentChange(double percentChange) {
        this.percentChange = percentChange;
    }

    public void setMarketPrice(double marketPrice) {
        this.marketPrice = marketPrice;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }



    public String getSymbol() {
        return symbol;
    }

    public String getMarket() {
        return market;
    }

    public String getName() {
        return name;
    }

    public double getPercentChange() {
        return percentChange;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public static String generateUuid(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
