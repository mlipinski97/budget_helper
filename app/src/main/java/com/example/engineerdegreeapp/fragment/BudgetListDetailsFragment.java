package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.ExpenseAdapter;
import com.example.engineerdegreeapp.retrofit.ExpenseApi;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BudgetListDetailsFragment extends Fragment implements ExpenseAdapter.ListItemClickListener,
        View.OnClickListener{

    private final String EXPENSES_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/expenses/";

    private Account mAccount;
    private AccountManager mAccountManager;
    private TextView expenseListErrorTextView;
    private OnFragmentClickListener mClickListener;
    private RecyclerView expenseListRecyclerView;
    private Long budgetListId;
    private String budgetListName;
    private ArrayList<Expense> expenseList;
    private ExpenseAdapter expenseAdapter;
    private FloatingActionButton newExpenseFloatingActionButton;
    private String budgetListDueDate;
    private List<Long> expenseIdToChangeDoneState = new LinkedList<>();
    private ArrayList<Expense> selectedExpenses = new ArrayList<>();
    private Button deleteButton;

    public BudgetListDetailsFragment() {
    }

    public BudgetListDetailsFragment(Long budgetListId, String budgetListName, String dueDate) {
        this.budgetListId = budgetListId;
        this.budgetListName = budgetListName;
        this.budgetListDueDate = dueDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_budget_list_details, container, false);

        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }
        deleteButton = rootView.findViewById(R.id.budget_list_details_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Expense e : selectedExpenses){
                    deleteSelectedExpenses(e.getId());
                }
                expenseListRecyclerView = rootView.findViewById(R.id.expense_recycler_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                expenseListRecyclerView.setLayoutManager(layoutManager);
                expenseListRecyclerView.setHasFixedSize(true);
                loadExpenseList();
                loadExpenseList();
                deleteButton.setVisibility(View.INVISIBLE);
            }
        });
        expenseListErrorTextView = rootView.findViewById(R.id.budget_list_details_loading_error);
        newExpenseFloatingActionButton = rootView.findViewById(R.id.budget_list_details_floating_action_button);
        newExpenseFloatingActionButton.setOnClickListener(this);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date strDate = null;
        try {
            strDate = sdf.parse(budgetListDueDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (System.currentTimeMillis() > strDate.getTime()) {
            newExpenseFloatingActionButton.setEnabled(false);
        }

        expenseListRecyclerView = rootView.findViewById(R.id.expense_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        expenseListRecyclerView.setLayoutManager(layoutManager);
        expenseListRecyclerView.setHasFixedSize(true);
        loadExpenseList();
        return rootView;
    }

    private void loadExpenseList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EXPENSES_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExpenseApi expenseApi = retrofit.create(ExpenseApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<List<Expense>> call = expenseApi.getExpensesFromBudgetList(auth, budgetListId);
        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("loadExpenseList()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    expenseListErrorTextView.setVisibility(View.INVISIBLE);
                    expenseList = new ArrayList<>(response.body());
                    expenseAdapter = new ExpenseAdapter(expenseList, expenseList.size(), BudgetListDetailsFragment.this, getContext());
                    expenseListRecyclerView.setAdapter(expenseAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                expenseListErrorTextView.setVisibility(View.VISIBLE);
                Log.d("loadExpenseList()", "onFailure call failed");
            }
        });
    }

    private void updateExpenseDoneStatus(Long expenseId){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EXPENSES_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExpenseApi expenseApi = retrofit.create(ExpenseApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<Expense> call = expenseApi.patchDoneState(auth, expenseId);
        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if(!response.isSuccessful()) {
                    try {
                        Log.d("updateExpenseDoneStatus()", response.errorBody().string());
                    } catch (IOException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else{
                    Log.d("updateExpenseDoneStatus()", "onResponse: sucessful doneStatusChange - id = " + expenseId);
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                Log.d("updateExpenseDoneStatus()", "onFailure: failed doneStatusChange - id = " + expenseId);

            }
        });

    }

    private void deleteSelectedExpenses(Long expenseId){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EXPENSES_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ExpenseApi expenseApi = retrofit.create(ExpenseApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Void> call = expenseApi.deleteExpense(auth, expenseId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
               if(!response.isSuccessful()){
                   try{
                       Log.d("deleteSelectedExpenses()", response.errorBody().string());
                   } catch (IOException e){
                       e.printStackTrace();
                   }
               } else{
                   Log.d("deleteSelectedExpenses()", "deleted expense with id: " + expenseId);
               }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("deleteSelectedExpenses()", "onFailure while deleting expense with id: " + expenseId);
            }
        });
    }

    @Override
        public void onListItemLongClick(View v, Expense expense) {
        expense.setSelected(!expense.isSelected());
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            v.setBackgroundColor(expense.isSelected() ?
                    getContext().getResources().getColor(R.color.darkCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.darkCardBackgroundColor, null) );
        } else {
            v.setBackgroundColor(expense.isSelected() ?
                    getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.lightCardBackgroundColor, null) );
        }
        if(expense.isSelected()){
            selectedExpenses.add(expense);
        } else{
            selectedExpenses.remove(expense);
        }
        if(!selectedExpenses.isEmpty()){
            deleteButton.setVisibility(View.VISIBLE);
        } else{
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onListItemDoneStateChange(Long changedStateExpenseId) {
        Toast.makeText(getContext(), "changed checkbox on expense with id: " + changedStateExpenseId, Toast.LENGTH_SHORT).show();
        if(expenseIdToChangeDoneState.contains(changedStateExpenseId)){
            expenseIdToChangeDoneState.remove(changedStateExpenseId);
        } else{
            expenseIdToChangeDoneState.add(changedStateExpenseId);
        }

    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.budget_list_details_floating_action_button:
                mClickListener.onFragmentClickInteraction(clickedItemId, budgetListDueDate, budgetListId);
                break;
        }
    }

    public interface OnFragmentClickListener{
        void onFragmentClickInteraction(int clickedElementId, String listDueDate, Long budgetListId);
        void onFragmentLongClickInteraction();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mClickListener = (OnFragmentClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement BudgetListDetailsFragment.OnFragmentClickListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (Long l : expenseIdToChangeDoneState ) {
            Log.d("updateExpenseDoneStatus()", "onPause: updateExpenseDoneStatus() called");
            updateExpenseDoneStatus(l);
        }
    }


}
