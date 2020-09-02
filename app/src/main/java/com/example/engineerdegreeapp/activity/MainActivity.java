    package com.example.engineerdegreeapp.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;
import com.example.engineerdegreeapp.communication.ToolbarMenuSortListener;
import com.example.engineerdegreeapp.fragment.BudgetListDetailsFragment;
import com.example.engineerdegreeapp.fragment.BudgetListFragment;
import com.example.engineerdegreeapp.fragment.CategoryBrowserFragment;
import com.example.engineerdegreeapp.fragment.EditBudgetListFragment;
import com.example.engineerdegreeapp.fragment.EditCategoryFragment;
import com.example.engineerdegreeapp.fragment.FriendsFragment;
import com.example.engineerdegreeapp.fragment.NewBudgetListFragment;
import com.example.engineerdegreeapp.fragment.NewCategoryFragment;
import com.example.engineerdegreeapp.fragment.NewExpenseFragment;
import com.example.engineerdegreeapp.fragment.ShareBudgetListFragment;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.retrofit.entity.Category;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import java.util.Locale;

import static com.example.engineerdegreeapp.util.AccountUtils.APP_ADMIN_ROLE;
import static com.example.engineerdegreeapp.util.DateUtils.dd_mm_yyy_sdf;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        BudgetListFragment.OnFragmentClickListener,
        NewBudgetListFragment.OnFragmentClickListener,
        BudgetListDetailsFragment.OnFragmentClickListener,
        NewExpenseFragment.OnFragmentClickListener,
        ToolbarChangeListener,
        EditBudgetListFragment.OnFragmentClickListener,
        ShareBudgetListFragment.OnFragmentClickListener,
        NewCategoryFragment.OnFragmentClickListener,
        EditCategoryFragment.OnFragmentClickListener,
        CategoryBrowserFragment.OnFragmentClickListener {

    private Account mAccount;
    private AccountManager mAccountManager;
    public Toolbar mTopToolbar;
    private DrawerLayout drawer;
    private TextView currentlyLoggedInTextView;
    private MenuItem edit_budget_list_menu_item;
    private MenuItem edit_budget_users_menu_item;
    private MenuItem edit_sort_expenses_menu_item;
    private ToolbarMenuSortListener toolbarMenuSortByListener;
    private BudgetList recentlyClickedBudgetList;
    private MenuItem addCategoryAdminMenuItem;
    private MenuItem editCategoryAdminMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.AppTheme_NoActionBarDark);
        } else {
            setTheme(R.style.AppTheme_NoActionBarLight);
        }
        super.onCreate(savedInstanceState);


        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return;
        }
        setContentView(R.layout.activity_main);
        mTopToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mTopToolbar);

        drawer = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mTopToolbar,
                R.string.drawer_navigation_drawer_open, R.string.drawer_navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.main_drawer_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        currentlyLoggedInTextView = headerView.findViewById(R.id.navigation_drawer_logged_user_text_view);
        currentlyLoggedInTextView.setText(mAccount.name);
        Menu drawerContentMenu = navigationView.getMenu();
        addCategoryAdminMenuItem = drawerContentMenu.findItem(R.id.drawer_admin_add_category);
        editCategoryAdminMenuItem = drawerContentMenu.findItem(R.id.drawer_admin_edit_category);

        if (mAccountManager.getUserData(mAccount, LoginActivity.SERVER_USER_ROLE).equals(APP_ADMIN_ROLE)) {
            addCategoryAdminMenuItem.setVisible(true);
            editCategoryAdminMenuItem.setVisible(true);
        } else {
            addCategoryAdminMenuItem.setVisible(false);
            editCategoryAdminMenuItem.setVisible(false);
        }

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

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
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
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.drawer_friends:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("budget_list_fragment")
                        .replace(R.id.main_fragment_layout_holder, new FriendsFragment()).commit();
                break;
            case R.id.drawer_admin_add_category:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("budget_list_fragment")
                        .replace(R.id.main_fragment_layout_holder, new NewCategoryFragment()).commit();
                break;
            case R.id.drawer_admin_edit_category:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("budget_list_fragment")
                        .replace(R.id.main_fragment_layout_holder, new CategoryBrowserFragment()).commit();
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
        if (key.equals(getString(R.string.language_preference_key))) {
            setLocale(sharedPreferences.getString(getString(R.string.language_preference_key), "en"));
            reload();
        }

    }


    private void setLocale(String language) {
        Locale locale;
        if (language.toLowerCase().equals("en") || language.toLowerCase().equals("gb") || language.toLowerCase().equals("us")) {
            locale = new Locale("en");
        } else {
            locale = new Locale(language.toLowerCase(), language.toUpperCase());
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", language);
        editor.apply();
    }

    private void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("My_Lang", Locale.getDefault().getCountry());
        Log.d("loadLocale language:", language);
        setLocale(language);
    }

    @Override
    public void onFragmentClickInteraction(int clickedElementId) {
        switch (clickedElementId) {
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
                        .add(R.id.main_fragment_layout_holder, new BudgetListDetailsFragment((long) recentlyClickedBudgetList.getId(),
                                recentlyClickedBudgetList.getName(),
                                dd_mm_yyy_sdf.format(recentlyClickedBudgetList.getDueDate()),
                                dd_mm_yyy_sdf.format(recentlyClickedBudgetList.getStartingDate()),
                                recentlyClickedBudgetList.getCurrencyCode()))
                        .commit();
                break;
            case R.id.new_category_button_confirm:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.main_fragment_layout_holder, new BudgetListFragment())
                        .commit();
                break;
            case R.id.edit_category_button_confirm:
            case R.id.new_budget_list_button_cancel:
            case R.id.new_expense_button_cancel:
            case R.id.edit_budget_list_button_cancel:
            case R.id.share_budget_list_button_cancel:
            case R.id.new_category_button_cancel:
            case R.id.edit_category_button_cancel:
                getSupportFragmentManager().popBackStack();
                break;
            default:
                Log.d("MainActivity", "onFragmentClickInteraction clicked element ID: " + clickedElementId);
                break;
        }
    }

    @Override
    public void onFragmentClickInteraction(int clickedElementId, String listDueDate, String listStartingDate, Long budgetListId) {
        switch (clickedElementId) {
            case R.id.budget_list_details_floating_action_button:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("budget_list_details_fragment")
                        .replace(R.id.main_fragment_layout_holder, new NewExpenseFragment(listDueDate,
                                listStartingDate, budgetListId, recentlyClickedBudgetList.getCurrencyCode())).commit();
                break;
        }

    }

    @Override
    public void onFragmentBudgetListElementClickInteraction(BudgetList budgetList) {
        this.recentlyClickedBudgetList = budgetList;
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                .addToBackStack("budget_list_fragment")
                .replace(R.id.main_fragment_layout_holder, new BudgetListDetailsFragment((long) recentlyClickedBudgetList.getId(),
                        recentlyClickedBudgetList.getName(),
                        dd_mm_yyy_sdf.format(recentlyClickedBudgetList.getDueDate()),
                        dd_mm_yyy_sdf.format(recentlyClickedBudgetList.getStartingDate()),
                        recentlyClickedBudgetList.getCurrencyCode())).commit();
    }

    @Override
    public void onFragmentCategoryBrowserElementClickInteraction(Category category) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                .addToBackStack("category_browser_fragment")
                .replace(R.id.main_fragment_layout_holder, new EditCategoryFragment(category.getCategoryName())).commit();
    }

    @Override
    public void changeToolbarTitle(String name) {
        getSupportActionBar().setTitle(name);
    }

    @Override
    public void restoreToolbarTitle() {
        getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_budget_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.edit_budget_list_menu_item:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("budget_list_details_fragment")
                        .replace(R.id.main_fragment_layout_holder, new EditBudgetListFragment(recentlyClickedBudgetList.getName(),
                                String.valueOf(recentlyClickedBudgetList.getValue()),
                                dd_mm_yyy_sdf.format(recentlyClickedBudgetList.getDueDate()),
                                dd_mm_yyy_sdf.format(recentlyClickedBudgetList.getStartingDate()),
                                (long) recentlyClickedBudgetList.getId(),
                                recentlyClickedBudgetList.getCurrencyCode()))
                        .commit();
                return true;
            case R.id.edit_budget_users_menu_item:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("budget_list_details_fragment")
                        .replace(R.id.main_fragment_layout_holder, new ShareBudgetListFragment((long) recentlyClickedBudgetList.getId()))
                        .commit();
                return true;
            case R.id.edit_sort_expenses_menu_item:
                toolbarMenuSortByListener.showSortDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        edit_budget_list_menu_item = menu.findItem(R.id.edit_budget_list_menu_item);
        edit_budget_users_menu_item = menu.findItem(R.id.edit_budget_users_menu_item);
        edit_sort_expenses_menu_item = menu.findItem(R.id.edit_sort_expenses_menu_item);
        return true;
    }

    @Override
    public void showEditButtons() {
        edit_budget_users_menu_item.setVisible(true);
        edit_budget_list_menu_item.setVisible(true);
        edit_sort_expenses_menu_item.setVisible(true);
    }

    @Override
    public void hideEditButtons() {
        edit_budget_users_menu_item.setVisible(false);
        edit_budget_list_menu_item.setVisible(false);
        edit_sort_expenses_menu_item.setVisible(false);
    }

    @Override
    public void showOnlySortButton() {
        edit_budget_users_menu_item.setVisible(false);
        edit_budget_list_menu_item.setVisible(false);
        edit_sort_expenses_menu_item.setVisible(true);
    }

    @Override
    public void changeSortButtonListener(ToolbarMenuSortListener listener) {
        toolbarMenuSortByListener = listener;
    }

}
