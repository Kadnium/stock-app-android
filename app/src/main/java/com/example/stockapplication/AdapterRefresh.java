package com.example.stockapplication;

public interface AdapterRefresh {
    //void refreshAdapter(int callerId);
    //void notifyAdd(int position,int callerId);
    //void notifyRemove(int position,int callerId);

    void onFavouriteAdded(int callerId,int position);
    void onFavouriteRemoved(int callerId,int position);

}
