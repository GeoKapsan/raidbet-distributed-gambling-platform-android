package com.example.distributed_gambling_platform;

import android.graphics.Bitmap;

public class ListItem {

    public String text;
    public float minBet;
    public float maxBet;
    public String riskLevel;
    public String bettingCategory;
    public double stars;
    public float jackpot;
    public Bitmap image;

    public ListItem(String text, float minBet, float maxBet, String riskLevel, String bettingCategory, double stars, float jackpot, Bitmap image) {
        this.text = text;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.riskLevel = riskLevel;
        this.bettingCategory = bettingCategory;
        this.stars = stars;
        this.jackpot = jackpot;
        this.image = image;
    }
}
