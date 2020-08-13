package com.example.engineerdegreeapp.retrofit.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class BudgetList {

    private int id;

    private String name;


    @SerializedName("budgetValue")
    private double value;

    private Date dueDate;
    private Date startingDate;

    @SerializedName("remainingValue")
    private double remainingValue;

    private boolean isSelected;

    private String currencyCode;

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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Date getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }
}
