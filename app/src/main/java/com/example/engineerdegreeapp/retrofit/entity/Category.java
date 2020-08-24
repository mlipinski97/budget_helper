package com.example.engineerdegreeapp.retrofit.entity;

import com.google.gson.annotations.SerializedName;

public class Category {

    private String categoryName;

    @SerializedName("categoryImage")
    private String categoryImage;

    private boolean isSelected;

    @SerializedName("deleted")
    private boolean isDeleted;


    public Category(String categoryName) {
        this.categoryName = categoryName;
    }

    public Category() {
    }

    public Category(String categoryName, String categoryImage, boolean isDeleted) {
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
        this.isDeleted = isDeleted;
    }

    public Category(String categoryName, String categoryImage) {
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
    }


    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
