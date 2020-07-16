package com.example.engineerdegreeapp.communication;

public interface ToolbarChangeListener {
    void changeToolbarTitle(String name);
    void restoreToolbarTitle();
    void showEditButton();
    void hideEditButton();
}
