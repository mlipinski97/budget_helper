package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EditBudgetListFragment extends Fragment {

    private TextView listNameTextView;
    private TextView listNameErrorTextView;
    private TextView listValueTextView;
    private TextView listValueErrorTextView;
    private TextView dueDateTextView;
    private EditText listNameEditText;
    private EditText listValueEditText;
    private CalendarView dueDateCalendarView;
    private Button cancelButton;
    private Button confirmButton;
    private Account mAccount;
    private AccountManager mAccountManager;
    NewBudgetListFragment.OnFragmentClickListener mClickListener;
    Long currentlySelectedDate;

    public EditBudgetListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
