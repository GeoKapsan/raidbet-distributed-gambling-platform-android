package com.example.distributed_gambling_platform;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class GameFilterDialog extends Dialog {

    // views

    private ChipGroup chipGroupRisk, chipGroupBettingCategory;

    private Chip chipRiskAny, chipRiskLow, chipRiskMedium, chipRiskHigh, chipBetCatAny, chipBetCatLow, chipBetCatMedium, chipBetCatHigh;

    private ImageButton star1, star2, star3, star4, star5;

    private MaterialButton btnResetFilters, btnApplyFilters;

    // listener that acts when 'Apply Filters' button is pressed
    public interface OnAppliedFiltersListener {
        void onAppliedFilters(String risk, String cat, int stars);
    }

    private final OnAppliedFiltersListener listener;

    // filters
    int stars;

    // constructor
    public GameFilterDialog(Context context, OnAppliedFiltersListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_game_filters);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        bindViews();
        setupStars();
        setupButtons();
    }

    private void bindViews() {
        // risk level chips
        chipGroupRisk = (ChipGroup) findViewById(R.id.chipGroupRisk);
        chipRiskAny = (Chip) findViewById(R.id.chipRiskAny);
        chipRiskLow = (Chip) findViewById(R.id.chipRiskLow);
        chipRiskMedium = (Chip) findViewById(R.id.chipRiskMedium);
        chipRiskHigh = (Chip) findViewById(R.id.chipRiskHigh);

        // betting category chips
        chipGroupBettingCategory = (ChipGroup) findViewById(R.id.chipGroupCategory);
        chipBetCatAny = (Chip) findViewById(R.id.chipCatAny);
        chipBetCatLow = (Chip) findViewById(R.id.chipCatLow);
        chipBetCatMedium = (Chip) findViewById(R.id.chipCatMedium);
        chipBetCatHigh = (Chip) findViewById(R.id.chipCatHigh);

        // rating stars
        star1 = (ImageButton) findViewById(R.id.star1);
        star2 = (ImageButton) findViewById(R.id.star2);
        star3 = (ImageButton) findViewById(R.id.star3);
        star4 = (ImageButton) findViewById(R.id.star4);
        star5 = (ImageButton) findViewById(R.id.star5);

        // reset and apply filters buttons
        btnResetFilters = (MaterialButton) findViewById(R.id.btnResetFilters);
        btnApplyFilters= (MaterialButton) findViewById(R.id.btnApplyFilters);
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

    private void setupButtons() {
        btnResetFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetToDefaults();
            }
        });

        btnApplyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAppliedFilters(
                            getRiskLevel(),
                            getBettingCategory(),
                            stars
                    );
                }
                dismiss();
            }
        });
    }

    private void resetToDefaults() {
        chipRiskAny.setChecked(true);
        chipBetCatAny.setChecked(true);
        setStarRating(0);
    }

    private String getRiskLevel() {
        int chipId = chipGroupRisk.getCheckedChipId();
        if (chipId == R.id.chipRiskLow)    return "low";
        if (chipId == R.id.chipRiskMedium) return "medium";
        if (chipId == R.id.chipRiskHigh)   return "high";
        return "any";
    }

    private String getBettingCategory() {
        int chipId = chipGroupBettingCategory.getCheckedChipId();
        if (chipId == R.id.chipCatLow)    return "$";
        if (chipId == R.id.chipCatMedium) return "$$";
        if (chipId == R.id.chipCatHigh)   return "$$$";
        return "any";
    }
}
