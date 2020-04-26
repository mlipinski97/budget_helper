package com.example.engineerdegreeapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
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
import com.example.engineerdegreeapp.fragment.BudgetListFragment;
import com.example.engineerdegreeapp.fragment.LoginFragment;
import com.example.engineerdegreeapp.fragment.NewBudgetListFragment;
import com.example.engineerdegreeapp.fragment.RegistrationFragment;
import com.example.engineerdegreeapp.retrofit.BudgetListApi;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements BudgetListAdapter.ListItemClickListener,
        OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        BudgetListFragment.OnFragmentClickListener,
        NewBudgetListFragment.OnFragmentClickListener{

    private BudgetListAdapter budgetListAdapter;
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
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.AppTheme_NoActionBarDark);
        } else {
            setTheme(R.style.AppTheme_NoActionBarLight);
        }
        mainActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
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

        BudgetListFragment budgetListFragment = new BudgetListFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .add(R.id.main_fragment_layout_holder, budgetListFragment)
                .commit();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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
        if (mToast != null) {
            mToast.cancel();
        }
        String clickMessage = "Here will open list #" + clickedBudgetListId;
        mToast = Toast.makeText(this, clickMessage, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void logout() {
        mAccountManager.removeAccount(mAccount, null, null, null);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.finishAffinity();
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int clickedItemId = item.getItemId();
        switch (clickedItemId) {
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.dark_mode_preference_key))) {
            if (sharedPreferences.getBoolean(getString(R.string.dark_mode_preference_key), false)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mTopToolbar.setBackgroundColor(Color.parseColor("#80000000"));

            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    @Override
    public void onFragmentClickInteraction(int clickedElementId) {
        switch (clickedElementId){
            case R.id.budget_list_floating_action_button:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("budget_list_fragment")
                        .replace(R.id.main_fragment_layout_holder, new NewBudgetListFragment()).commit();
                break;
            case R.id.new_budget_list_button_confirm:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.main_fragment_layout_holder, new BudgetListFragment())
                        .commit();
                break;
            case R.id.new_budget_list_button_cancel:
                getSupportFragmentManager().popBackStack();
                break;
        }
    }
}