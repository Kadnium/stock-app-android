package com.example.stockapplication.interfaces;

import com.example.stockapplication.datahelpers.StockData;

public interface AdapterRefresh {
    void onFavouriteAddClicked(StockData stock);
    void onFavouriteRemoveClicked(StockData stock);

}
