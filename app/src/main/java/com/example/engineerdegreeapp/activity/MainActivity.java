package com.example.engineerdegreeapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.accounts.Account;
import android.accounts.AccountManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.fragment.BudgetListDetailsFragment;
import com.example.engineerdegreeapp.fragment.BudgetListFragment;
import com.example.engineerdegreeapp.fragment.NewBudgetListFragment;
import com.example.engineerdegreeapp.fragment.NewExpenseFragment;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        BudgetListFragment.OnFragmentClickListener,
        NewBudgetListFragment.OnFragmentClickListener,
        BudgetListDetailsFragment.OnFragmentClickListener,
        NewExpenseFragment.OnFragmentClickListener {

    private Account mAccount;
    private AccountManager mAccountManager;
    public Toolbar mTopToolbar;
    private DrawerLayout drawer;
    private TextView currentlyLoggedInTextView;
    private Long recentlyClickedListElementId;
    private String recentlyClickedListElementName;
    private String recentlyClickedDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.AppTheme_NoActionBarDark);
        } else {
            setTheme(R.style.AppTheme_NoActionBarLight);
        }
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

    private Fragment getVisibleFragment() {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
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
            case R.id.new_expense_button_confirm:
                getSupportFragmentManager().popBackStack("budget_list_details_fragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .add(R.id.main_fragment_layout_holder, new BudgetListDetailsFragment(recentlyClickedListElementId
                                , recentlyClickedListElementName
                                , recentlyClickedDueDate))
                        .commit();

                break;
            case R.id.new_budget_list_button_cancel:
            case R.id.new_expense_button_cancel:
                getSupportFragmentManager().popBackStack();
                break;
            default:
                Log.d("MainActivity", "onFragmentClickInteraction clicked element ID: " + clickedElementId);
                break;
        }
    }

    @Override
    public void onFragmentClickInteraction(int clickedElementId, String listDueDate, Long budgetListId) {
        switch (clickedElementId){
            case R.id.budget_list_details_floating_action_button:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("budget_list_details_fragment")
                        .replace(R.id.main_fragment_layout_holder, new NewExpenseFragment(listDueDate, budgetListId)).commit();
                break;
        }

    }

    @Override
    public void onFragmentLongClickInteraction() {
/*        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                .addToBackStack("budget_list_details_fragment")
                .replace(R.id.main_fragment_layout_holder, new NewExpenseFragment(listDueDate, budgetListId)).commit();*/
    }

    @Override
    public void onFragmentBudgetListElementClickInteraction(Long clickedListElementId, String clickedListElementName, String dueDate) {
        this.recentlyClickedListElementId = clickedListElementId;
        this.recentlyClickedListElementName = clickedListElementName;
        this.recentlyClickedDueDate = dueDate;
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                .addToBackStack("budget_list_fragment")
                .replace(R.id.main_fragment_layout_holder, new BudgetListDetailsFragment(clickedListElementId,
                        clickedListElementName,
                        dueDate)).commit();
    }

}

///TODO editing expenses, budget lists