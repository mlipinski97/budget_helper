package com.example.engineerdegreeapp.communication;

public interface ToolbarChangeListener {
    void changeToolbarTitle(String name);
    void restoreToolbarTitle();
    void showEditButtons();
    void hideEditButtons();
    void showOnlySortButton();
    void changeSortButtonListener(ToolbarMenuSortListener listener);
}
