package com.example.stockapplication;

import android.content.Context;
import android.view.Gravity;
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
                calculateWantedAveragePrice();

            });

            calculateNewAverageButton.setOnClickListener(v->{
                calculateNewAveragePrice();
            });


        }
    }
    private boolean checkViews(){
        return  stockPriceInput != null && stockAmountInput != null &&
                averagePriceInput != null && calculateNewAverageInput != null &&
                calculateNewAverageButton != null && calculateWantedAverageInput != null &&
                calculateWantedAverageButton != null;
    }

    private Double getInputValue(TextInputEditText input){
        String value = input.getText().toString();
        if(value.isEmpty()){
            return null;

        }
        return Double.parseDouble(input.getText().toString());

    }
    public void calculateNewAveragePrice(){
        if(checkViews()){
            Double stockPrice = getInputValue(stockPriceInput);
            Double stockAmount = getInputValue(stockAmountInput);
            Double averagePrice = getInputValue(averagePriceInput);

            Double moneyAmount = getInputValue(calculateNewAverageInput);
            if(stockPrice == null || stockAmount==null||averagePrice==null || moneyAmount == null){
                setToast("Vaaditusta kentästä puuttuu arvo!", Toast.LENGTH_SHORT);
                return;
            }

            double stockAmountToGet = moneyAmount / stockPrice;
            double bottomDivider = stockAmountToGet + stockAmount;
            double topDivider = averagePrice * stockAmount + stockAmountToGet * stockPrice;

            BigDecimal bd = new BigDecimal(topDivider/bottomDivider).setScale(2, RoundingMode.HALF_UP);
            setToast("Uusi keskikurssi olisi: "+bd.toString(), Toast.LENGTH_LONG);


        }
    }

    private void setToast(String text,int length){
        Toast toast = Toast.makeText(context, text, length);
        toast.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
    public void calculateWantedAveragePrice(){
        if(checkViews()){
            Double stockPrice = getInputValue(stockPriceInput);
            Double stockAmount = getInputValue(stockAmountInput);
            Double averagePrice = getInputValue(averagePriceInput);

            Double newAveragePrice = getInputValue(calculateWantedAverageInput);
            if(stockPrice == null || stockAmount==null||averagePrice==null || newAveragePrice == null){
                setToast("Vaaditusta kentästä puuttuu arvo!",Toast.LENGTH_SHORT);
                return;
            }

            if(newAveragePrice.equals(stockPrice)){
                setToast("Ei mahdollista",Toast.LENGTH_LONG);
                return;
            }
            double bottomDivider = newAveragePrice - stockPrice;
            double topDivider = (-1 * stockAmount) * (newAveragePrice - averagePrice);

            BigDecimal amount = new BigDecimal(topDivider/bottomDivider).setScale(2, RoundingMode.HALF_UP);
            BigDecimal moneyAmount = new BigDecimal(amount.doubleValue()*stockPrice).setScale(2, RoundingMode.HALF_UP);
            if(amount.doubleValue()<0){
                setToast("Ei mahdollista",Toast.LENGTH_LONG);
                return;
            }
            setToast("Osakkeita pitäisi ostaa: "+amount.toString()+" kpl / "+moneyAmount.toString()+"€", Toast.LENGTH_LONG);

        }
    }




}
