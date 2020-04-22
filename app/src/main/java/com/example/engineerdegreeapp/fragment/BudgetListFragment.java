package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.BudgetListAdapter;
import com.example.engineerdegreeapp.retrofit.BudgetListApi;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BudgetListFragment extends Fragment implements BudgetListAdapter.ListItemClickListener,
        View.OnClickListener {

    private BudgetListAdapter budgetListAdapter;
    private TextView budgetListErrorTextView;
    private RecyclerView budgetListRecyclerView;
    private Toast mToast;
    private ArrayList<BudgetList> budgetLists;
    private Account mAccount;
    private AccountManager mAccountManager;
    private FloatingActionButton addListFloatingActionButton;
    OnFragmentClickListener mClickListener;


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

        loadBudgetLists();
        return rootView;
    }

    private void loadBudgetLists() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/budgetlist/")
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
                }
            }

            @Override
            public void onFailure(Call<List<BudgetList>> call, Throwable t) {
                budgetListErrorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onListItemClick(int clickedBudgetListId) {
        if (mToast != null) {
            mToast.cancel();
        }
        String clickMessage = "(fragment)Here will open list #" + clickedBudgetListId;
        mToast = Toast.makeText(getContext(), clickMessage, Toast.LENGTH_SHORT);
        mToast.show();
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
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mClickListener = (OnFragmentClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement OnFragmentClickListener");
        }
    }
}
