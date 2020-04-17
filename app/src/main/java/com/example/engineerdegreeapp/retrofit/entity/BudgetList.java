package com.example.engineerdegreeapp.retrofit.entity;

import com.google.gson.annotations.SerializedName;

public class BudgetList {

    private int id;

    private String name;

    @SerializedName("budgetValue")
    private double value;

    @SerializedName("remainingValue")
    private double remainingValue;

    public BudgetList() {
    }

    public BudgetList(int id, String name, double value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getRemainingValue() {
        return remainingValue;
    }

    public void setRemainingValue(double remainingValue) {
        this.remainingValue = remainingValue;
    }
}
