package com.example.engineerdegreeapp.retrofit.entity;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.Date;

public class Expense {

    private Long id;
    private String name;
    private Double amount;
    private Date dateOfExpense;
    private UserAuth expenseOwner;
    private Long budgetListId;
    private BudgetList budgetList;
    private boolean done;
    private boolean isSelected;
    @SerializedName("category")
    private Category category;

    public Expense() {
    }

    public Expense(Long id, Double amount, Date dateOfExpense, UserAuth expenseOwner, boolean done, Long budgetListId) {
        this.id = id;
        this.amount = amount;
        this.dateOfExpense = dateOfExpense;
        this.expenseOwner = expenseOwner;
        this.done = done;
        this.budgetListId = budgetListId;
    }
    public Expense(Long id, Double amount, Date dateOfExpense, UserAuth expenseOwner, boolean done, Long budgetListId, Category category) {
        this.id = id;
        this.amount = amount;
        this.dateOfExpense = dateOfExpense;
        this.expenseOwner = expenseOwner;
        this.done = done;
        this.budgetListId = budgetListId;
        this.category = category;
    }

    public Expense(Long id, String name, Double amount, Date dateOfExpense, UserAuth expenseOwner, Long budgetListId, BudgetList budgetList, boolean done, boolean isSelected, Category category) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.dateOfExpense = dateOfExpense;
        this.expenseOwner = expenseOwner;
        this.budgetListId = budgetListId;
        this.budgetList = budgetList;
        this.done = done;
        this.isSelected = isSelected;
        this.category = category;
    }

    public String getExpenseOwnerName(){
        return expenseOwner.getUsername();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDateOfExpense() {
        return dateOfExpense;
    }

    public void setDateOfExpense(Date dateOfExpense) {
        this.dateOfExpense = dateOfExpense;
    }

    public UserAuth getExpenseOwner() {
        return expenseOwner;
    }

    public void setExpenseOwner(UserAuth expenseOwner) {
        this.expenseOwner = expenseOwner;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Long getBudgetListId() {
        return budgetListId;
    }

    public void setBudgetListId(Long budgetListId) {
        this.budgetListId = budgetListId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BudgetList getBudgetList() {
        return budgetList;
    }

    public void setBudgetList(BudgetList budgetList) {
        this.budgetList = budgetList;
    }
}
