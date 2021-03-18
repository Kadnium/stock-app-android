package com.example.stockapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{
    List<StockData> stockList;
    Context context;
    AppData data;
    int viewId;
    AdapterRefresh refresh;
    public RecyclerAdapter(Context ctx, List<StockData> stockList,AppData data,int viewId,AdapterRefresh refresh){
        this.stockList = stockList;
        this.context = ctx;
        this.data = data;
        this.viewId = viewId;
        this.refresh = refresh;
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
        holder.favouriteStatus.setImageResource(stock.isFavourite()?R.drawable.ic_favourite:R.drawable.ic_not_favourite);
        holder.favouriteStatus.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            StockData stock1 = stockList.get(adapterPosition);
            if(stock1.isFavourite()){
               // CLICKED FROM MOST CHANGED OR SEARCH/TRENDING
               // Uuid is null for non favourites
               // dont remove from list, only modify
               if(stock1.getUuid() == null){
                   stock1.setFavourite(false);
                   notifyItemChanged(adapterPosition);
               }else{
                   // CLICKED FROM FAVOURITES LIST
                   // remove from current list
                   stockList.remove(adapterPosition);
                   notifyItemRemoved(adapterPosition);
               }
               // callback to do custom logic
               refresh.onFavouriteRemoveClicked(adapterPosition, stock1);


           }else{
               // stock is not yet favourite so can't be on favourite list
               // add to favourite list
               stock1.setFavourite(true);
               notifyItemChanged(adapterPosition);
               // callback to do custom logic
               refresh.onFavouriteAddClicked(adapterPosition, stock1);


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



    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView priceChange, stockPrice, stockName, stockTicker;
        ImageView favouriteStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            priceChange = itemView.findViewById(R.id.priceChange);
            stockPrice = itemView.findViewById(R.id.stockPrice);
            stockName = itemView.findViewById(R.id.stockName);
            stockTicker = itemView.findViewById(R.id.stockTicker);
            favouriteStatus = itemView.findViewById(R.id.favouriteStatus);

        }
    }
}
