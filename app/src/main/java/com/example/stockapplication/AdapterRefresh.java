package com.example.stockapplication;

public interface AdapterRefresh {
    void onFavouriteAddClicked(int position, StockData stock);
    void onFavouriteRemoveClicked(int position, StockData stock);

}
