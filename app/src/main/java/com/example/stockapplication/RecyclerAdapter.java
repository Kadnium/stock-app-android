package com.example.stockapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{
    List<StockData> stockList;
    final Context context;
    final AppData data;
    final int viewId;
    final AdapterRefresh refresh;
    final Gson gson;
    public RecyclerAdapter(Context ctx, List<StockData> stockList,AppData data,int viewId,AdapterRefresh refresh){
        this.stockList = stockList;
        this.context = ctx;
        this.data = data;
        this.viewId = viewId;
        this.refresh = refresh;
        this.gson = new Gson();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.stock_row,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // priceChange, stockPrice, stockName, stockTicker;
        StockData stock = stockList.get(position);
        double change = stock.getPercentChange();
        if(change<0){
            holder.priceChange.setTextColor(context.getColor(R.color.red));
        }else{
            holder.priceChange.setTextColor(context.getColor(R.color.green));
        }

        holder.priceChange.setText(stock.getPercentChange()+"%");
        holder.stockPrice.setText(String.valueOf(stock.getMarketPrice()));
        holder.stockName.setText(String.valueOf(stock.getName()));
        holder.stockTicker.setText(String.valueOf(stock.getSymbol()));
        if(this.viewId != R.id.favouriteRecyclerView && !stock.isFavourite()){
            if(data.isStockInFavouriteList(stock.getSymbol())){
                stock.setFavourite(true);
            }

        }

        holder.mainLayout.setOnClickListener(v->{
            //Intent intent = new Intent(context.getApplicationContext(),ChartActivity.class);
            //setIntentData(intent);
            String stockString = gson.toJson(stock);
            FragmentTransaction fragmentTransaction=((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
           // fragmentTransaction.setCustomAnimations(R.anim.slide_in_right,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
            ChartFragment chartFragment = ChartFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString("Stock",stockString);
            chartFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragmentContainer,chartFragment);
            fragmentTransaction.addToBackStack(AppData.CHART_FRAGMENT);
            fragmentTransaction.commit();
           // activity.overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
        });
        holder.favouriteStatus.setImageResource(stock.isFavourite()?R.drawable.ic_favourite:R.drawable.ic_not_favourite);
        holder.favouriteStatus.setOnClickListener(v -> {
            if(!data.isRefreshing()){
                int adapterPosition = holder.getAdapterPosition();
                StockData selectedStock = stockList.get(adapterPosition);
                if(selectedStock.isFavourite()){
                    // CLICKED FROM MOST CHANGED OR SEARCH/TRENDING
                    // Uuid is null for non favourites
                    // dont remove from list, only modify
                    if(selectedStock.getUuid() == null){
                        selectedStock.setFavourite(false);
                        notifyItemChanged(adapterPosition);
                    }else{
                        // CLICKED FROM FAVOURITES LIST
                        // remove from current list
                        stockList.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    }
                    // Callback to do custom logic
                    refresh.onFavouriteRemoveClicked(selectedStock);

                }else{
                    // Stock is not yet favourite so can't be on favourite list
                    // Add to favourite list
                    selectedStock.setFavourite(true);
                    notifyItemChanged(adapterPosition);
                    // Callback to do custom logic
                    refresh.onFavouriteAddClicked(selectedStock);


                }
            }


        });


    }

    public void setStockList(List<StockData> list){
        this.stockList = list;
    }
    @Override
    public int getItemCount() {
        return stockList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView priceChange;
        final TextView stockPrice;
        final TextView stockName;
        final TextView stockTicker;
        final ImageView favouriteStatus;
        final ConstraintLayout mainLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            priceChange = itemView.findViewById(R.id.priceChange);
            stockPrice = itemView.findViewById(R.id.stockPrice);
            stockName = itemView.findViewById(R.id.stockName);
            stockTicker = itemView.findViewById(R.id.stockTicker);
            favouriteStatus = itemView.findViewById(R.id.favouriteStatus);
            mainLayout = itemView.findViewById(R.id.stockCardLayout);

        }
    }
}
