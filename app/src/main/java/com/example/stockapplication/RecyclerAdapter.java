package com.example.stockapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    List<StockData> stockList;
    Context context;
    public RecyclerAdapter(Context ctx, List<StockData> stockList){
        this.stockList = stockList;
        this.context = ctx;
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
        holder.priceChange.setText(String.valueOf(stockList.get(position).getPercentChange()));
        holder.stockPrice.setText(String.valueOf(stockList.get(position).getMarketPrice()));
        holder.stockName.setText(String.valueOf(stockList.get(position).getName()));
        holder.stockTicker.setText(String.valueOf(stockList.get(position).getSymbol()));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView priceChange, stockPrice, stockName, stockTicker;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            priceChange = itemView.findViewById(R.id.priceChange);
            stockPrice = itemView.findViewById(R.id.stockPrice);
            stockName = itemView.findViewById(R.id.stockName);
            stockTicker = itemView.findViewById(R.id.stockTicker);

        }
    }
}
