package com.example.engineerdegreeapp.adapter.item;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class CategoryItem {
    private String categoryName;
    private Bitmap categoryImage;

    public CategoryItem(String categoryName, Bitmap categoryImage) {
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Bitmap getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(Bitmap categoryImage) {
        this.categoryImage = categoryImage;
    }
}
