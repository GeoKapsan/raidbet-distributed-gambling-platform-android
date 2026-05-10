package com.example.distributed_gambling_platform;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddBalanceDialog extends Dialog {

    // view
    private TextInputLayout tilBalanceInput;
    private TextView tvCurrentBalance, tvBalanceAfter;
    private TextInputEditText etBalanceAmount;
    private MaterialButton btnCancelBalance, btnConfirmBalance;

    // on confirm listener
    public interface OnConfirmAddBalanceListener {
        void onConfirmAddBalance(float balance);
    }

    // balance
    float currentBalance;

    private final OnConfirmAddBalanceListener listener;

    // constructor
    public AddBalanceDialog(Context context, float currentBalance, OnConfirmAddBalanceListener listener) {
        super(context);
        this.currentBalance = currentBalance;
        this.listener = listener;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_balance);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        bindViews();
        populateCurrentBalance();
        setupInputField();
        setupButtons();
    }

    private void bindViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        tilBalanceInput = findViewById(R.id.tilBalanceInput);
        etBalanceAmount = findViewById(R.id.etBalanceAmount);
        tvBalanceAfter = findViewById(R.id.tvBalanceAfter);
        btnCancelBalance = findViewById(R.id.btnCancelBalance);
        btnConfirmBalance = findViewById(R.id.btnConfirmBalance);
    }

    private void populateCurrentBalance() {
        tvCurrentBalance.setText("$" + formatAmount(currentBalance));
        tvBalanceAfter.setText("$" + formatAmount(currentBalance));
    }

    private void setupInputField() {
        etBalanceAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateBalanceAfterPreview(s.toString());
                tilBalanceInput.setError(null);
            }
        });
    }


    private void setupButtons() {
        btnCancelBalance.setOnClickListener(v -> dismiss());
        btnConfirmBalance.setOnClickListener(v -> attemptConfirm());
    }

    private void attemptConfirm() {
        String raw = etBalanceAmount.getText() != null ? etBalanceAmount.getText().toString().trim() : "";

        if (raw.isEmpty()) {
            tilBalanceInput.setError("Please enter an amount");
            return;
        }

        float deposit;
        try {
            deposit = Float.parseFloat(raw);
        } catch (NumberFormatException e) {
            tilBalanceInput.setError("Invalid amount");
            return;
        }

        if (deposit <= 0) {
            tilBalanceInput.setError("Amount must be greater than $0");
            return;
        }

        float newBalance = currentBalance + deposit;
        if (listener != null) {
            listener.onConfirmAddBalance(newBalance);
        }
        dismiss();
    }

    private void updateBalanceAfterPreview(String rawText) {
        float deposit = 0f;
        try {
            if (!rawText.isEmpty()) {
                deposit = Float.parseFloat(rawText);
            }
        } catch (NumberFormatException ignored) {}

        float preview = currentBalance + Math.max(0f, deposit);
        tvBalanceAfter.setText("$" + formatAmount(preview));
    }

    private String formatAmount(float amount) {
        if (amount == Math.floor(amount)) {
            return String.format("%,.0f", amount);
        }
        return String.format("%,.2f", amount);
    }
}
