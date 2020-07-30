package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.ExpenseApi;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.example.engineerdegreeapp.util.RegexUtils;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private Long budgetListId;
    OnFragmentClickListener mClickListener;
    Long currentlySelectedDate;
    String listDueDate;

    public NewExpenseFragment(){

    }

    public NewExpenseFragment(String listDueDate, Long budgetListId) {
        this.budgetListId = budgetListId;
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
        dueDateCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
            currentlySelectedDate = c.getTimeInMillis();
        });
        cancelButton = rootView.findViewById(R.id.new_expense_button_cancel);
        cancelButton.setOnClickListener(this);
        confirmButton = rootView.findViewById(R.id.new_expense_button_confirm);
        confirmButton.setOnClickListener(this);

        return rootView;
    }
    private void postExpense() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/expenses/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExpenseApi expenseApi = retrofit.create(ExpenseApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String selectedDate = sdf.format(new Date(currentlySelectedDate));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", expenseNameEditText.getText().toString())
                .addFormDataPart("amount", expenseValueEditText.getText().toString().replace(",", "."))
                .addFormDataPart("dateOfExpense", selectedDate)
                .build();

        Call<Expense> call = expenseApi.postExpense(auth, budgetListId, requestBody);
        System.out.println(expenseNameEditText.getText().toString());
        System.out.println(expenseValueEditText.getText().toString().replace(",", "."));
        System.out.println(selectedDate);
        System.out.println(budgetListId);
        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (!response.isSuccessful()) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String errorMessage = jObjError.getString("errors")
                                .replace("\"", "")
                                .replace("[", "")
                                .replace("]", "");
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else{
                    Toast.makeText(getActivity(), "Expense successfully added", Toast.LENGTH_SHORT).show();
                    mClickListener.onFragmentClickInteraction(R.id.new_expense_button_confirm);
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to create new expense", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.new_expense_button_cancel:
                hideKeyboard();
                mClickListener.onFragmentClickInteraction(clickedItemId);
                break;
            case R.id.new_expense_button_confirm:
                hideKeyboard();
                if(isMoneyRegexSafe() && isNameValid()){
                    postExpense();
                } else{
                    if(!isMoneyRegexSafe()){
                        expenseValueErrorTextView.setVisibility(View.VISIBLE);
                    } else{
                        expenseValueErrorTextView.setVisibility(View.INVISIBLE);
                    }
                    if(!isNameValid()){
                        expenseNameErrorTextView.setVisibility(View.VISIBLE);
                    } else{
                        expenseNameErrorTextView.setVisibility(View.INVISIBLE);
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

    public void hideKeyboard() {
        View view =  getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
