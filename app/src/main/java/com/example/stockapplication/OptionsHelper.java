package com.example.stockapplication;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OptionsHelper {
    TextInputEditText stockPriceInput, stockAmountInput, averagePriceInput, calculateNewAverageInput, calculateWantedAverageInput;
    AppCompatButton calculateNewAverageButton, calculateWantedAverageButton;
    Context context;
    public OptionsHelper(Context context) {
        this.context = context;
    }

    public void initAveragePriceFields(){
        if(context instanceof AppCompatActivity){
            AppCompatActivity act = (AppCompatActivity) context;
            stockPriceInput = act.findViewById(R.id.stockPriceInput);
            stockAmountInput = act.findViewById(R.id.stockAmountInput);
            averagePriceInput = act.findViewById(R.id.averagePriceInput);

            calculateNewAverageInput = act.findViewById(R.id.calculateNewAverageInput);
            calculateNewAverageButton = act.findViewById(R.id.calculateNewAverageButton);

            calculateWantedAverageInput = act.findViewById(R.id.calculateWantedAverageInput);
            calculateWantedAverageButton = act.findViewById(R.id.calculateWantedAverageButton);

            calculateWantedAverageButton.setOnClickListener(v -> {
                //calculateWantedAveragePrice();
                Toast.makeText(((AppCompatActivity) context).getApplicationContext(), "TEST", Toast.LENGTH_SHORT).show();

            });

            calculateNewAverageButton.setOnClickListener(v->{
                Toast.makeText(((AppCompatActivity) context).getApplicationContext(), "TEST", Toast.LENGTH_SHORT).show();
               // calculateNewAveragePrice();
            });


        }
    }
    private boolean checkViews(){
        return  stockPriceInput != null && stockAmountInput != null &&
                averagePriceInput != null && calculateNewAverageInput != null &&
                calculateNewAverageButton != null && calculateWantedAverageInput != null &&
                calculateWantedAverageButton != null;
    }

    private double getInputValue(TextInputEditText input){
        return Double.parseDouble(input.getText().toString());

    }
    public void calculateNewAveragePrice(){
        if(checkViews()){
            double stockPrice = getInputValue(stockPriceInput);
            double stockAmount = getInputValue(stockAmountInput);
            double averagePrice = getInputValue(averagePriceInput);

            double moneyAmount = getInputValue(calculateNewAverageInput);

            double stockAmountToGet = moneyAmount / stockPrice;
            double bottomDivider = stockAmountToGet + stockAmount;
            double topDivider = averagePrice * stockAmount + stockAmountToGet * stockPrice;

            BigDecimal bd = new BigDecimal(topDivider/bottomDivider).setScale(2, RoundingMode.HALF_UP);


            Toast toast = Toast.makeText(context, "Uusi keskikurssi olisi: "+bd.toString(), Toast.LENGTH_SHORT);
            toast.show();

        }
    }


    public void calculateWantedAveragePrice(){
        if(checkViews()){
            double stockPrice = getInputValue(stockPriceInput);
            double stockAmount = getInputValue(stockAmountInput);
            double averagePrice = getInputValue(averagePriceInput);

            double newAveragePrice = getInputValue(calculateWantedAverageInput);

            double bottomDivider = newAveragePrice - stockPrice;
            double topDivider = (-1 * stockAmount) * (newAveragePrice - averagePrice);

            BigDecimal bd = new BigDecimal(topDivider/bottomDivider).setScale(2, RoundingMode.HALF_UP);


            Toast toast = Toast.makeText(context, "Osakkeita pitäis ostaa: "+bd.toString()+" kpl", Toast.LENGTH_SHORT);
            toast.show();


        }
    }




}
