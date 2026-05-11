package com.example.distributed_gambling_platform;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
public class RateGameDialog extends Dialog {

    // views
    private TextView tvRateGameName;
    private ImageButton star1, star2, star3, star4, star5;

    private MaterialButton btnResetRate, btnApplyRate;

    public interface OnAppliedRateListener {
        void onAppliedFilters(int stars);
    }

    private final String gameName;
    private final RateGameDialog.OnAppliedRateListener listener;

    // filters
    int stars;

    // constructor
    public RateGameDialog(Context context, String gameName, RateGameDialog.OnAppliedRateListener listener) {
        super(context);
        this.gameName = gameName;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_game_rate);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        bindViews();
        populateCurrentGame();
        setupStars();
        setupButtons();
    }

    private void bindViews() {
        // text view
        tvRateGameName = findViewById(R.id.tvRateGameName);

        // rating stars
        star1 = findViewById(R.id.rateStar1);
        star2 = findViewById(R.id.rateStar2);
        star3 = findViewById(R.id.rateStar3);
        star4 = findViewById(R.id.rateStar4);
        star5 = findViewById(R.id.rateStar5);

        // reset and apply filters buttons
        btnResetRate = findViewById(R.id.btnResetRate);
        btnApplyRate = findViewById(R.id.btnApplyRate);
    }

    private void setupStars() {
        setStarRating(0);

        star1.setOnClickListener(v -> setStarRating(1));
        star2.setOnClickListener(v -> setStarRating(2));
        star3.setOnClickListener(v -> setStarRating(3));
        star4.setOnClickListener(v -> setStarRating(4));
        star5.setOnClickListener(v -> setStarRating(5));
    }

    private void setStarRating(int rating) {
        stars = rating;

        ImageButton[] stars = {star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            boolean filled = (i < rating);
            stars[i].setImageResource(filled ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        }
    }

    private void populateCurrentGame() {
        tvRateGameName.setText(gameName);
    }

    private void setupButtons() {
        btnResetRate.setOnClickListener(v -> {
            resetToDefaults();
        });

        btnApplyRate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppliedFilters(
                        stars
                );
            }
            dismiss();
        });
    }

    private void resetToDefaults() {
        setStarRating(1);
    }
}
