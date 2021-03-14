package com.example.stockapplication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

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
        String tempName = name;
        if(tempName.length()>25){
            tempName =  name.substring(0,25);
            tempName+=".";

        }
        return tempName;
    }

    public double getPercentChange() {
        return formatDouble(percentChange);
    }

    public double getMarketPrice() {
        return formatDouble(marketPrice);
    }

    public boolean isFavourite() {
        return isFavourite;
    }
    private double formatDouble(double value){
        BigDecimal bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();

    }

    public static String generateUuid(){
        return UUID.randomUUID().toString();//.replace("-", "");
    }
}
