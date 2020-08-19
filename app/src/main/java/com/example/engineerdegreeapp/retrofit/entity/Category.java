package com.example.engineerdegreeapp.retrofit.entity;

import com.google.gson.annotations.SerializedName;

public class Category {

    private String categoryName;

    @SerializedName("categoryImage")
    private String categoryImage;


    public Category(String categoryName) {
        this.categoryName = categoryName;
    }

    public Category() {
    }

    public Category(String categoryName, String categoryImage) {
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
