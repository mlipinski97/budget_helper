package com.example.engineerdegreeapp.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.fragment.LoginFragment;
import com.example.engineerdegreeapp.fragment.RegistrationFragment;
import com.example.engineerdegreeapp.util.AccountUtils;


public class LoginActivity extends AppCompatActivity implements
        RegistrationFragment.OnFragmentClickListener,
        LoginFragment.OnFragmentClickListener {

    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_AUTH_TOKEN_TYPE = "authTokenType";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "isAddingNewAccount";
    public static final String PARAM_USER_PASSWORD = "password";
    public static final String SERVER_USER_ROLE = "serverUserRole";
    private SharedPreferences mPreferences;
    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkSharedPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAccountManager = AccountManager.get(this);

        // Ask for an auth token
        //mAccountManager.getAuthTokenByFeatures(AccountUtils.ACCOUNT_TYPE, AccountUtils.AUTH_TOKEN_TYPE, null, this, null, null, new GetAuthTokenCallback(), null);
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length >= 1) {
            startAndGoToMainActivity();
        }


        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .add(R.id.login_fragment_layout_holder, loginFragment)
                .commit();
    }

    @Override
    public void onFragmentClickInteraction(int clickedElementId) {
        switch (clickedElementId) {
            case R.id.login_sign_in_button:
                startAndGoToMainActivity();
                break;
            case R.id.login_register_button:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_right, R.anim.exit_to_left)
                        .addToBackStack("login_fragment")
                        .replace(R.id.login_fragment_layout_holder, new RegistrationFragment()).commit();
                break;
            case R.id.register_confirm_button: {
                getSupportFragmentManager().popBackStack();
            }
            case R.id.register_cancel_button: {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    private void startAndGoToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void checkSharedPreferences() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mPreferences.getBoolean(getString(R.string.dark_mode_preference_key), false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}
