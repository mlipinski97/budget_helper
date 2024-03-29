package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.ExpenseAdapter;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;
import com.example.engineerdegreeapp.communication.ToolbarMenuSortListener;
import com.example.engineerdegreeapp.fragment.dialog.SortByDialogFragment;
import com.example.engineerdegreeapp.retrofit.ExpenseApi;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BudgetListDetailsFragment extends Fragment implements ExpenseAdapter.ListItemClickListener,
        View.OnClickListener,
        ToolbarMenuSortListener,
        SortByDialogFragment.onDialogItemClickedListener {

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
    private String budgetListStartingDate;
    private List<Long> expenseIdToChangeDoneState = new LinkedList<>();
    private ArrayList<Expense> selectedExpenses = new ArrayList<>();
    private Button deleteButton;
    private ToolbarChangeListener toolbarChangeListener;
    private String currencyCode;

    public BudgetListDetailsFragment() {
    }

    public BudgetListDetailsFragment(Long budgetListId,
                                     String budgetListName,
                                     String budgetListDueDate,
                                     String budgetListStartingDate,
                                     String currencyCode) {
        this.budgetListId = budgetListId;
        this.budgetListName = budgetListName;
        this.budgetListDueDate = budgetListDueDate;
        this.currencyCode = currencyCode;
        this.budgetListStartingDate = budgetListStartingDate;
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
        toolbarChangeListener.changeToolbarTitle(budgetListName);
        toolbarChangeListener.showEditButtons();
        toolbarChangeListener.changeSortButtonListener(this);
        deleteButton = rootView.findViewById(R.id.budget_list_details_delete_button);
        deleteButton.setOnClickListener(this);
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
                    expenseAdapter = new ExpenseAdapter(expenseList, expenseList.size(),
                            BudgetListDetailsFragment.this,
                            getContext(),
                            currencyCode);
                    expenseListRecyclerView.setAdapter(expenseAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                expenseListErrorTextView.setVisibility(View.VISIBLE);
                Log.d("loadExpenseList()", "onFailure - call failed");
            }
        });
    }

    private void updateExpenseDoneStatus(Long expenseId) {
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
                if (!response.isSuccessful()) {
                    try {
                        Log.d("updateExpenseDoneStatus()", response.errorBody().string());
                    } catch (IOException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("updateExpenseDoneStatus()", "onResponse: sucessful doneStatusChange - id = " + expenseId);
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                Log.d("updateExpenseDoneStatus()", "onFailure: failed doneStatusChange - id = " + expenseId);

            }
        });

    }

    private void deleteSelectedExpenses(ArrayList<Long> expenseIdList) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EXPENSES_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ExpenseApi expenseApi = retrofit.create(ExpenseApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Void> call = expenseApi.deleteManyExpenses(auth, expenseIdList);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("deleteSelectedExpenses()", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("deleteSelectedExpenses()", "deleted expenses with id: " + expenseIdList);
                    loadExpenseList();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("deleteSelectedExpenses()", "onFailure while deleting expenses with id: " + expenseIdList);
            }
        });
    }

    @Override
    public void onListItemClick(View v, Expense expense) {

        if (selectedExpenses.isEmpty()) {
            Log.d("onListItemClick(View v, Expense expense)", "clicked expense list item while none items selected");
        } else {
            onListItemLongClick(v, expense);
        }
    }

    @Override
    public void onListItemLongClick(View v, Expense expense) {
        CardView cardView = (CardView)v;
        expense.setSelected(!expense.isSelected());
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            cardView.setCardBackgroundColor(expense.isSelected() ?
                    getContext().getResources().getColor(R.color.darkCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.darkCardBackgroundColor, null));
        } else {
            cardView.setCardBackgroundColor(expense.isSelected() ?
                    getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.lightCardBackgroundColor, null));
        }
        if (expense.isSelected()) {
            selectedExpenses.add(expense);
        } else {
            selectedExpenses.remove(expense);
        }
        if (!selectedExpenses.isEmpty()) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onListItemDoneStateChange(Long changedStateExpenseId) {
        Log.d("onListItemDoneStateChange()", "changed checkbox on expense with id: " + changedStateExpenseId);
        if (expenseIdToChangeDoneState.contains(changedStateExpenseId)) {
            expenseIdToChangeDoneState.remove(changedStateExpenseId);
        } else {
            expenseIdToChangeDoneState.add(changedStateExpenseId);
        }

    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.budget_list_details_floating_action_button:
                toolbarChangeListener.hideEditButtons();
                mClickListener.onFragmentClickInteraction(clickedItemId, budgetListDueDate, budgetListStartingDate, budgetListId);
                break;
            case R.id.budget_list_details_delete_button:
                ArrayList<Long> idList = new ArrayList<>();
                for (Expense e : selectedExpenses) {
                    idList.add(e.getId());
                }
                deleteSelectedExpenses(idList);
                deleteButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void showSortDialog() {
        SortByDialogFragment newFragment = new SortByDialogFragment(BudgetListDetailsFragment.this,
                getContext().getResources().getString(R.string.budget_list_details_dialog_sort_by_text),
                getContext().getResources().getStringArray(R.array.sort_by_expenses_values));
        newFragment.show(getParentFragmentManager(), "sortByExpenseDialog");
    }

    @Override
    public void onDialogItemClick(int which) {
        ArrayList<Expense> sortedExpenseList = new ArrayList<>();
        switch (which) {
            case 0:
                sortedExpenseList = expenseList.stream()
                        .sorted(Comparator.comparing(e -> e.getCategory().getCategoryName()))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case 1:
                sortedExpenseList = expenseList.stream()
                        .sorted(Comparator.comparing(Expense::getName))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case 2:
                sortedExpenseList = expenseList.stream()
                        .sorted(Comparator.comparing(Expense::getDateOfExpense))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case 3:
                sortedExpenseList = expenseList.stream()
                        .sorted(Comparator.comparing(Expense::getDateOfExpense).reversed())
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case 4:
                sortedExpenseList = expenseList.stream()
                        .sorted(Comparator.comparingDouble(Expense::getAmount))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case 5:
                sortedExpenseList = expenseList.stream()
                        .sorted(Comparator.comparingDouble(Expense::getAmount).reversed())
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case 6:
                sortedExpenseList = expenseList.stream()
                        .sorted(Comparator.comparing(Expense::getExpenseOwnerName))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
        }
        expenseAdapter = new ExpenseAdapter(sortedExpenseList,
                sortedExpenseList.size(),
                BudgetListDetailsFragment.this,
                getContext(),
                currencyCode);
        expenseListRecyclerView.setAdapter(expenseAdapter);
    }

    public interface OnFragmentClickListener {
        void onFragmentClickInteraction(int clickedElementId, String listDueDate, String listStartingDate, Long budgetListId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mClickListener = (OnFragmentClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement BudgetListDetailsFragment.OnFragmentClickListener");
        }
        try {
            toolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException f) {
            throw new ClassCastException(context.toString() + "must implement ToolbarChangeListener");

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (Long l : expenseIdToChangeDoneState) {
            Log.d("updateExpenseDoneStatus()", "onPause: updateExpenseDoneStatus() called");
            updateExpenseDoneStatus(l);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        toolbarChangeListener.restoreToolbarTitle();
        toolbarChangeListener.hideEditButtons();
    }
}
