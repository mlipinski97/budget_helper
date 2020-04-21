package com.example.engineerdegreeapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.accounts.Account;
import android.accounts.AccountManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.BudgetListAdapter;
import com.example.engineerdegreeapp.retrofit.BudgetListApi;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements BudgetListAdapter.ListItemClickListener,
        OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

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
    private TextView currentlyLoggedInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppTheme_NoActionBarDark);
        } else{
          setTheme(R.style.AppTheme_NoActionBarLight);
        }
        mainActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if(accounts.length > 0 ){
            mAccount = accounts[0];
        } else{
            return;
        }



        mTopToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mTopToolbar);
        drawer = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mTopToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        NavigationView navigationView = (NavigationView) findViewById(R.id.main_drawer_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        currentlyLoggedInTextView = headerView.findViewById(R.id.navigation_drawer_logged_user_text_view);
        currentlyLoggedInTextView.setText(mAccount.name);


        budgetListErrorTextView = findViewById(R.id.budget_list_loading_error);

        budgetListRecyclerView = findViewById(R.id.budget_list_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        budgetListRecyclerView.setLayoutManager(layoutManager);
        budgetListRecyclerView.setHasFixedSize(true);
        loadBudgetLists();
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
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int clickedItemId = item.getItemId();
        switch (clickedItemId){
            case R.id.drawer_logout:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                logout();
                break;
            case R.id.drawer_settings:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }
        return true;
    }

    private void loadBudgetLists(){
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.dark_mode_preference_key))){
            if(sharedPreferences.getBoolean(getString(R.string.dark_mode_preference_key), false)){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mTopToolbar.setBackgroundColor(Color.parseColor("#80000000"));

            } else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }
}