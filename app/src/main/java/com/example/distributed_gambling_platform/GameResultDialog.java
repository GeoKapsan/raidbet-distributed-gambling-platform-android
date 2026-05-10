package com.example.distributed_gambling_platform;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;


public class GameResultDialog extends Dialog {

    public enum ResultType {LOSE, WIN, JACKPOT};

    private final ResultType resultType;
    private final float resultAmount, bettingAmount, newBalance;

    private LinearLayout resultLose, resultWin, resultJackpot;
    private TextView tvLoseAmount, tvWinAmount, tvJackpotAmount, tvBet, tvNewBalance;
    private MaterialButton btnOkay;

    public GameResultDialog(Context context, ResultType resultType, float resultAmount, float bettingAmount,float newBalance) {
        super(context);
        this.resultType = resultType;
        this.resultAmount = resultAmount;
        this.bettingAmount = bettingAmount;
        this.newBalance = newBalance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_game_result);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        bindViews();
        applyState();
        setupButtons();
    }

    private void bindViews() {
        resultLose = findViewById(R.id.groupLose);
        resultWin = findViewById(R.id.groupWin);
        resultJackpot = findViewById(R.id.groupJackpot);

        tvLoseAmount = findViewById(R.id.tvLoseAmount);
        tvWinAmount = findViewById(R.id.tvWinAmount);
        tvJackpotAmount = findViewById(R.id.tvJackpotAmount);

        tvBet = findViewById(R.id.tvStatBet);
        tvNewBalance = findViewById(R.id.tvStatNewBalance);

        btnOkay = findViewById(R.id.btnOkay);
    }

    private void applyState() {
        resultLose.setVisibility(View.GONE);
        resultWin.setVisibility(View.GONE);
        resultJackpot.setVisibility(View.GONE);

        switch (resultType) {
            case LOSE:
                resultLose.setVisibility(View.VISIBLE);
                tvLoseAmount.setText("-$" + formatAmount(bettingAmount));
                break;

            case WIN:
                resultWin.setVisibility(View.VISIBLE);
                tvWinAmount.setText("+$" + formatAmount(resultAmount));
                break;

            case JACKPOT:
                resultJackpot.setVisibility(View.VISIBLE);
                tvJackpotAmount.setText("+$" + formatAmount(resultAmount));
                break;
        }

        tvBet.setText("$" + formatAmount(bettingAmount));
        tvNewBalance.setText("$" + formatAmount(newBalance));
    }

    private void setupButtons() {
        btnOkay.setOnClickListener(v -> {
            dismiss();
        });
    }

    private String formatAmount(float amount) {
        if (amount == Math.floor(amount)) {
            return String.format("%,.0f", amount);
        }
        return String.format("%,.2f", amount);
    }
}
