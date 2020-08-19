package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.item.CategoryItem;

import java.util.ArrayList;

public class CategorySpinnerAdapter extends ArrayAdapter<CategoryItem> {
    public CategorySpinnerAdapter(Context context, ArrayList<CategoryItem> categoryItems) {
        super(context, 0, categoryItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_spinner_item, parent, false);
        }
        ImageView categoryImageView = convertView.findViewById(R.id.category_spinner_image_view);
        TextView categoryTextView = convertView.findViewById(R.id.category_spinner_text_view);
        CategoryItem categoryItem = getItem(position);
        if (categoryItem != null) {
            categoryImageView.setImageBitmap(categoryItem.getCategoryImage());
            categoryTextView.setText(categoryItem.getCategoryName());
        }


        return convertView;
    }
}
