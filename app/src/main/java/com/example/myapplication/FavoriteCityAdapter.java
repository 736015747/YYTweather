package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class FavoriteCityAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> cities;

    public FavoriteCityAdapter(Context context, List<String> cities) {
        super(context, 0, cities);
        this.context = context;
        this.cities = cities;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 获取当前位置的城市
        String city = cities.get(position);

        // 初始化视图或复用已存在的视图
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        // 设置城市名称
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(city);

        return convertView;
    }
}


