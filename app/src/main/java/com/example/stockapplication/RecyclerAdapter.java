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

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    List<StockData> stockList;
    Context context;
    AppData data;
    public RecyclerAdapter(Context ctx, List<StockData> stockList,AppData data){
        this.stockList = stockList;
        this.context = ctx;
        this.data = data;
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
        this.notifyDataSetChanged();
        //this.notifyItemRemoved(position);

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
               StockData stock = stockList.get(position);
               if(stock.isFavourite()){
                   removeFromList(position);
                /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                       stockList.removeIf(t->t.getUuid().equals(stock.getUuid()));
                       data.setFavouriteData(stockList);
                   }*/
               }

            }
        });
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
