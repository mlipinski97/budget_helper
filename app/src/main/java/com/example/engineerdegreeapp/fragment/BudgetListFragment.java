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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.BudgetListAdapter;
import com.example.engineerdegreeapp.retrofit.BudgetListApi;
import com.example.engineerdegreeapp.retrofit.ExpenseApi;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class BudgetListFragment extends Fragment implements BudgetListAdapter.ListItemClickListener,
        View.OnClickListener {

    private final String BUDGET_LIST_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/budgetlist/";

    private BudgetListAdapter budgetListAdapter;
    private TextView budgetListErrorTextView;
    private RecyclerView budgetListRecyclerView;
    private Toast mToast;
    private ArrayList<BudgetList> budgetLists;
    private Account mAccount;
    private AccountManager mAccountManager;
    private FloatingActionButton addListFloatingActionButton;
    OnFragmentClickListener mClickListener;
    private TextView accountDetailsTextView;
    private TextView accountDetailsListNameTextView;
    private TextView accountDetailsValueRemainingTextView;
    private TextView accountDetailsValueTotalTextView;
    private LinkedList<BudgetList> selectedBudgetLists = new LinkedList<>();
    private Button deleteButton;

    public BudgetListFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_budget_list, container, false);


        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }

        addListFloatingActionButton = rootView.findViewById(R.id.budget_list_floating_action_button);
        addListFloatingActionButton.setOnClickListener(this);
        budgetListErrorTextView = rootView.findViewById(R.id.budget_list_loading_error);

        budgetListRecyclerView = rootView.findViewById(R.id.budget_list_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        budgetListRecyclerView.setLayoutManager(layoutManager);
        budgetListRecyclerView.setHasFixedSize(true);
        accountDetailsTextView = rootView.findViewById(R.id.fragment_budget_list_greetings_details_text_view);
        accountDetailsTextView.setText(getString(R.string.user_greetings) + " " + mAccount.name);
        accountDetailsListNameTextView = rootView.findViewById(R.id.fragment_budget_list_close_to_date_text_view);
        accountDetailsValueRemainingTextView = rootView.findViewById(R.id.fragment_budget_list_close_to_date_value_remaining_text_view);
        accountDetailsValueTotalTextView = rootView.findViewById(R.id.fragment_budget_list_close_to_date_value_total_text_view);
        deleteButton = rootView.findViewById(R.id.budget_list_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(BudgetList bl : selectedBudgetLists){
                    deleteSelectedBudgetList((long) bl.getId());
                }
                budgetListRecyclerView = rootView.findViewById(R.id.budget_list_recycler_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                budgetListRecyclerView.setLayoutManager(layoutManager);
                budgetListRecyclerView.setHasFixedSize(true);

                loadBudgetLists();
                deleteButton.setVisibility(View.INVISIBLE);
            }

        });
        loadBudgetLists();
        return rootView;
    }

    private void loadBudgetLists() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BUDGET_LIST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

       Call<List<BudgetList>> call = budgetListApi.getBudgetLists(auth, loginCredential);
        call.enqueue(new Callback<List<BudgetList>>() {
            @Override
            public void onResponse(Call<List<BudgetList>> call, Response<List<BudgetList>> response) {
                if (!response.isSuccessful()) {
                    budgetListErrorTextView.setVisibility(View.VISIBLE);
                    return;
                } else {
                    budgetListErrorTextView.setVisibility(View.INVISIBLE);
                    budgetLists = new ArrayList<>(response.body());
                    budgetListAdapter = new BudgetListAdapter(budgetLists, budgetLists.size(), BudgetListFragment.this);
                    budgetListRecyclerView.setAdapter(budgetListAdapter);
                    fillUserHelpFrame();
                }
            }

            @Override
            public void onFailure(Call<List<BudgetList>> call, Throwable t) {
                budgetListErrorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void deleteSelectedBudgetList(Long id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BUDGET_LIST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Void> call = budgetListApi.deleteBudgetList(auth, id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(!response.isSuccessful()){
                    try{
                        Log.d("deleteSelectedBudgetList()", response.errorBody().string());
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                } else{
                    Log.d("deleteSelectedBudgetList()", "deleted budget list with id: " + id);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("deleteSelectedBudgetList()", "onFailure while deleting budget list with id: " + id);
            }
        });
    }

    private void fillUserHelpFrame(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        final Date currentTime = cal.getTime();
        Log.d("fillUserHelpFrame", "fillUserHelpFrame: current time - 1(yesterday): " + currentTime);
        List<BudgetList> filteredBudgetLists = budgetLists.stream()
                .filter(budgetList -> budgetList.getDueDate().after(currentTime))
                .sorted(Comparator.comparing(BudgetList::getDueDate))
                .collect(Collectors.toList());
        if(filteredBudgetLists.size() > 0){
            BudgetList elementToDisplay = filteredBudgetLists.get(0);
            accountDetailsListNameTextView.setText(R.string.closest_list_to_date);
            accountDetailsListNameTextView.append( " " + elementToDisplay.getName());
            accountDetailsValueRemainingTextView.setText(R.string.closest_list_to_date_value_remaining);
            accountDetailsValueRemainingTextView.append( " " + String.valueOf(elementToDisplay.getRemainingValue()));
            accountDetailsValueTotalTextView.setText(R.string.closest_list_to_date_value_total);
            accountDetailsValueTotalTextView.append( " " + String.valueOf(elementToDisplay.getValue()));
        } else{
            accountDetailsListNameTextView.setText(R.string.user_help_frame_no_list_found);
            accountDetailsValueRemainingTextView.setText("");
        }

    }
    @Override
    public void onListItemClick(int clickedBudgetListId, String clickedBudgetListName, String listDueDate, String clickedBudgetListAmount) {
        mClickListener.onFragmentBudgetListElementClickInteraction((long) clickedBudgetListId,
                clickedBudgetListName,
                listDueDate,
                clickedBudgetListAmount);

    }

    @Override
    public void onListItemLongClick(View v, BudgetList budgetList) {
        budgetList.setSelected(!budgetList.isSelected());
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            v.setBackgroundColor(budgetList.isSelected() ?
                    getContext().getResources().getColor(R.color.darkCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.darkCardBackgroundColor, null) );
        } else {
            v.setBackgroundColor(budgetList.isSelected() ?
                    getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.lightCardBackgroundColor, null) );
        }
        if(budgetList.isSelected()){
            selectedBudgetLists.add(budgetList);
        } else{
            selectedBudgetLists.remove(budgetList);
        }
        if(!selectedBudgetLists.isEmpty()){
            deleteButton.setVisibility(View.VISIBLE);
        } else{
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.budget_list_floating_action_button:
                mClickListener.onFragmentClickInteraction(clickedItemId);
                break;
        }
    }



    public interface OnFragmentClickListener{
        void onFragmentClickInteraction(int clickedElementId);
        void onFragmentBudgetListElementClickInteraction(Long clickedListElementId,
                                                         String clickedListElementName,
                                                         String dueDate,
                                                         String clickedBudgetListAmount);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mClickListener = (OnFragmentClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement BudgetListFragment.OnFragmentClickListener");
        }
    }
}
