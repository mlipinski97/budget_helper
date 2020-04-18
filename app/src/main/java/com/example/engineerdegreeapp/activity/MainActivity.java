package com.example.engineerdegreeapp.activity;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.accounts.Account;
import android.accounts.AccountManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.BudgetListAdapter;
import com.example.engineerdegreeapp.retrofit.BudgetListApi;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.util.AccountUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements BudgetListAdapter.ListItemClickListener{

    private BudgetListAdapter budgetListAdapter;
    private TextView budgetListErrorTextView;
    private RecyclerView budgetListRecyclerView;
    private Toast mToast;
    private ArrayList<BudgetList> budgetLists;
    private MainActivity mainActivity;
    private Account mAccount;
    private AccountManager mAccountManager;
    private Toolbar mTopToolbar;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        mAccount = accounts[0];

        mTopToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mTopToolbar);
        drawer = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mTopToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        budgetListErrorTextView = findViewById(R.id.budget_list_loading_error);

        budgetListRecyclerView = findViewById(R.id.budget_list_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        budgetListRecyclerView.setLayoutManager(layoutManager);
        budgetListRecyclerView.setHasFixedSize(true);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/budgetlist/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential ;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<List<BudgetList>> call = budgetListApi.getBudgetLists(auth, loginCredential);
        call.enqueue(new Callback<List<BudgetList>>() {
            @Override
            public void onResponse(Call<List<BudgetList>> call, Response<List<BudgetList>> response) {
                if(!response.isSuccessful()){
                    budgetListErrorTextView.setVisibility(View.VISIBLE);
                    return;
                } else{
                    budgetListErrorTextView.setVisibility(View.INVISIBLE);
                    budgetLists = new ArrayList<>(response.body());
                    budgetListAdapter = new BudgetListAdapter(budgetLists, budgetLists.size(), MainActivity.this);
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
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else{
            super.onBackPressed();
        }
    }

    @Override
    public void onListItemClick(int clickedBudgetListId) {
        if(mToast != null){
            mToast.cancel();
        }
        String clickMessage = "Here will open list #" + clickedBudgetListId;
        mToast = Toast.makeText(this, clickMessage, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void logout(){
        mAccountManager.removeAccount(mAccount, null, null, null);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.finishAffinity();
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_options, menu);
        return true;
    }
}