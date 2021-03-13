package com.example.stockapplication;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
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
    public void removeFromList(int position){
        stockList.remove(position);
        this.notifyItemRemoved(position);

    }


    //String symbol, String market, String name, double percentChange, double marketPrice, boolean isFavourite,String uuid
    public void addToFavourites(int position,StockData stock){
        data.addToFavourites(stock);
        refresh.onFavouriteAdded(data.getFavouriteData().size()-1,this.viewId);
        this.notifyItemChanged(position);
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
        holder.favouriteStatus.setImageResource(stock.isFavourite()?R.drawable.ic_favourite:R.drawable.ic_not_favourite);
        holder.favouriteStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                StockData stock = stockList.get(adapterPosition);
               if(stock.isFavourite()){
                   // CLICKED FROM MOST CHANGED
                   // Uuid is null for non favourites
                   // remove from favourite list and most changed list
                   // TODO CHECK FROM WHAT ACTIVITY
                   if(stock.getUuid() == null){
                       stock.setFavourite(false);
                       notifyItemChanged(adapterPosition);
                       int favouriteIndex = data.removeFromFavourites(stock);
                       // update favouritelist
                       refresh.onFavouriteRemoved(viewId,favouriteIndex);
                   }else{
                       // CLICKED FROM FAVOURITES LIST
                       // stock is in favourites and is in most changed list
                       // update favourites and most changed
                       data.updateMostChangedFavouriteStatus(stock,false);
                       // update favourites list
                       removeFromList(adapterPosition);
                       // update most changed list
                       refresh.onFavouriteRemoved(viewId,-1);

                   }


               }else{
                   // stock is not yet favourite so can't be on favourite list
                   // add to favourite list
                   stock.setFavourite(true);
                   addToFavourites(adapterPosition,stock);
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
