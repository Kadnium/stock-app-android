package com.example.stockapplication;

public interface AdapterRefresh {
    //void refreshAdapter(int callerId);
    //void notifyAdd(int position,int callerId);
    //void notifyRemove(int position,int callerId);

    void onFavouriteAddClicked(int position, StockData stock);
    void onFavouriteRemoveClicked(int position, StockData stock);

}
