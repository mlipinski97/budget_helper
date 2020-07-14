package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.ExpenseAdapter;
import com.example.engineerdegreeapp.retrofit.ExpenseApi;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BudgetListDetailsFragment extends Fragment implements ExpenseAdapter.ListItemClickListener,
        View.OnClickListener{

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
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/expenses/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExpenseApi expenseApi = retrofit.create(ExpenseApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<List<Expense>> call = expenseApi.getExepnsesFromBudgetList(auth, budgetListId);
        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (!response.isSuccessful()) {
                    try {
                        System.out.println(response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    expenseListErrorTextView.setVisibility(View.INVISIBLE);
                    expenseList = new ArrayList<>(response.body());
                    expenseAdapter = new ExpenseAdapter(expenseList, expenseList.size(), BudgetListDetailsFragment.this);
                    expenseListRecyclerView.setAdapter(expenseAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                expenseListErrorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onListItemClick(Long clickedExpenseId) {
        System.out.println(clickedExpenseId);
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

}
