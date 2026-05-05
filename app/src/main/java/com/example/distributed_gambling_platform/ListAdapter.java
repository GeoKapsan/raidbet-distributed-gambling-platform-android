package com.example.distributed_gambling_platform;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

        TextView listItemText = (TextView) listItem.findViewById(R.id.list_item);
        ImageView listItemImage = (ImageView) listItem.findViewById(R.id.image);
        Button listItemBtnRate = (Button) listItem.findViewById(R.id.btnRate);

        ListItem item = (ListItem) getItem(position);

        listItemText.setText(item.text);
        listItemImage.setImageBitmap(item.image);

        listItemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlayClick(item);
            }
        });

        listItemBtnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRateClick(item);
            }
        });

        return listItem;
    }
}
