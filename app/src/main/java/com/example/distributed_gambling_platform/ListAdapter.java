package com.example.distributed_gambling_platform;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    public interface ListItemActionListener {
        void onPlayClick(ListItem item);
        void onRateClick(ListItem item);
    }

    ArrayList<ListItem> items;
    Context context;
    ListItemActionListener listener;

    public ListAdapter(ArrayList<ListItem> items, Context context, ListItemActionListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = LayoutInflater.from(this.context).inflate(R.layout.list_item, parent, false);

        LinearLayout linearLayoutGame = listItem.findViewById(R.id.linearLayoutGame);
        ImageView ivGameLogo = listItem.findViewById(R.id.ivGameLogo);
        TextView tvGameName = listItem.findViewById(R.id.tvGameName);
        TextView tvRiskLevel = listItem.findViewById(R.id.tvRiskLevel);
        TextView tvBetCat = listItem.findViewById(R.id.tvBetCat);
        TextView tvStars = listItem.findViewById(R.id.tvStars);
        ImageButton btnRateGame = listItem.findViewById(R.id.btnRateGame);

        ListItem item = (ListItem) getItem(position);

        tvGameName.setText(item.text);
        ivGameLogo.setImageBitmap(item.image);
        tvRiskLevel.setText(item.riskLevel);
        tvBetCat.setText("  ·  " + item.bettingCategory + "  ·  ");
        tvStars.setText("★ " + item.stars);

        linearLayoutGame.setOnClickListener(v -> listener.onPlayClick(item));

        btnRateGame.setOnClickListener(v -> listener.onRateClick(item));

        return listItem;
    }
}
