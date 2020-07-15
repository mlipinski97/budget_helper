package com.example.engineerdegreeapp.retrofit.entity;

import java.time.LocalDate;
import java.util.Date;

public class Expense {

    private Long id;
    private String name;
    private Double amount;
    private Date dateOfExpense;
    private UserAuth expenseOwner;
    private Long budgetListId;
    private boolean done;
    private boolean isSelected;

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
}
