package com.example.distributed_gambling_platform;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

public class PlaceBetDialog extends Dialog {

    private final float balance;
    private final float minBet;
    private final float maxBet;
    private final OnBetPlacedListener listener;
    private float currentBet;

    private Slider slider;
    private TextView tvBetAmount;
    private TextView tvMinBet;
    private TextView tvMaxBet;
    private MaterialButton btnCancelBet, btnConfirmBet;

    public interface OnBetPlacedListener {
        void onBetPlaced(float amount);
    }

    public PlaceBetDialog(Context context, float balance, float minBet, float maxBet, OnBetPlacedListener listener) {
        super(context);
        this.balance = balance;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.listener = listener;
        this.currentBet = minBet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_place_bet);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        bindViews();
        setupSlider();
        setupButtons();
    }

    private void bindViews() {
        slider = findViewById(R.id.sliderBet);
        tvBetAmount = findViewById(R.id.tvBetAmount);
        tvMinBet = findViewById(R.id.tvMinBet);
        tvMaxBet = findViewById(R.id.tvMaxBet);
        btnCancelBet = findViewById(R.id.btnCancelBet);
        btnConfirmBet = findViewById(R.id.btnConfirmBet);
    }

    private void setupSlider() {
        float effectiveMax = Math.min(maxBet, balance);

        slider.setValueFrom(minBet);
        slider.setValueTo(effectiveMax);
        slider.setValue(minBet);
        slider.setStepSize((effectiveMax - minBet) / 100);

        tvMinBet.setText("Min $" + formatAmount(minBet));
        tvMaxBet.setText("Max $" + formatAmount(effectiveMax));
        updateBetDisplay(minBet);

        slider.addOnChangeListener((slider, value, fromUser) -> {
            currentBet = value;
            updateBetDisplay(value);
        });
    }

    private void setupButtons() {
        btnCancelBet.setOnClickListener(v -> dismiss());

        btnConfirmBet.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBetPlaced(currentBet);
            }
            dismiss();
        });
    }

    private void updateBetDisplay(float amount) {
        tvBetAmount.setText("$" + formatAmount(amount));
    }

    private String formatAmount(float amount) {
        if (amount == Math.floor(amount)) {
            return Integer.toString((int) amount);
        }
        return String.format("%.2f", amount);
    }
}