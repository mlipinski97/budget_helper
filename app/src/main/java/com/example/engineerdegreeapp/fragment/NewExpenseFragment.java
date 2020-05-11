package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.example.engineerdegreeapp.util.RegexUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewExpenseFragment extends Fragment implements View.OnClickListener{

    private TextView expenseNameTextView;
    private TextView expenseNameErrorTextView;
    private TextView expenseValueTextView;
    private TextView expenseValueErrorTextView;
    private TextView dueDateTextView;
    private EditText expenseNameEditText;
    private EditText expenseValueEditText;
    private CalendarView dueDateCalendarView;
    private Button cancelButton;
    private Button confirmButton;
    private Account mAccount;
    private AccountManager mAccountManager;
    OnFragmentClickListener mClickListener;
    Long currentlySelectedDate;
    String listDueDate;

    public NewExpenseFragment(String listDueDate) {
        this.listDueDate = listDueDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_expense, container, false);

        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }
        expenseNameTextView = rootView.findViewById(R.id.new_expense_name_text_view);
        expenseNameErrorTextView = rootView.findViewById(R.id.new_expense_name_error_text_view);
        expenseValueTextView = rootView.findViewById(R.id.new_expense_amount_text_view);
        expenseValueErrorTextView = rootView.findViewById(R.id.new_expense_amount_error_text_view);
        dueDateTextView = rootView.findViewById(R.id.new_expense_due_date_text_view);
        expenseNameEditText = rootView.findViewById(R.id.new_expense_name_edit_text);
        expenseValueEditText = rootView.findViewById(R.id.new_expense_amount_edit_text);
        dueDateCalendarView = rootView.findViewById(R.id.new_expense_calendar_view);
        dueDateCalendarView.setMinDate((new Date().getTime()));
        try {
            dueDateCalendarView.setMaxDate(new SimpleDateFormat("dd-MM-yyyy").parse(listDueDate).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        currentlySelectedDate = dueDateCalendarView.getDate();
        dueDateCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth);
                currentlySelectedDate = c.getTimeInMillis();
            }
        });
        cancelButton = rootView.findViewById(R.id.new_expense_button_cancel);
        cancelButton.setOnClickListener(this);
        confirmButton = rootView.findViewById(R.id.new_expense_button_confirm);
        confirmButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.new_expense_button_cancel:
                mClickListener.onFragmentClickInteraction(clickedItemId);
                break;
            case R.id.new_expense_button_confirm:
                if(isMoneyRegexSafe() && isNameValid()){
                    Toast.makeText(getContext(), "boooooooop", Toast.LENGTH_SHORT).show();
                } else{
                    if(!isMoneyRegexSafe()){
                        expenseValueErrorTextView.setVisibility(View.VISIBLE);
                    }
                    if(!isNameValid()){
                        expenseNameErrorTextView.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    public interface OnFragmentClickListener{
        void onFragmentClickInteraction(int clickedElementId);
    }

    private boolean isMoneyRegexSafe(){
        String moneyValue = expenseValueEditText.getText().toString();
        return RegexUtils.isMoneyAmountRegexSafe(moneyValue);
    }

    private boolean isNameValid(){
        String name = expenseNameEditText.getText().toString();
        return !name.isEmpty();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mClickListener = (OnFragmentClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement NewExpenseFragment.OnFragmentClickListener");
        }
    }
}
