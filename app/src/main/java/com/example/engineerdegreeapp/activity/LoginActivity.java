package com.example.engineerdegreeapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.fragment.LoginFragment;
import com.example.engineerdegreeapp.fragment.RegistrationFragment;
import com.example.engineerdegreeapp.util.AccountUtils;


public class LoginActivity extends AppCompatActivity implements
        RegistrationFragment.OnFragmentClickListener,
        LoginFragment.OnFragmentClickListener{

    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_AUTH_TOKEN_TYPE = "authTokenType";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "isAddingNewAccount";
    public static final String PARAM_USER_PASSWORD = "password";
    private String authToken;
    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authToken = null;
        mAccountManager = AccountManager.get(this);

        // Ask for an auth token
        //mAccountManager.getAuthTokenByFeatures(AccountUtils.ACCOUNT_TYPE, AccountUtils.AUTH_TOKEN_TYPE, null, this, null, null, new GetAuthTokenCallback(), null);
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length >= 1){
            startAndGoToMainActivity();
        }


        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .add(R.id.login_fragment_layout_holder ,loginFragment)
                .commit();
    }

    @Override
    public void onClickInteraction(int clickedElementId) {
        switch (clickedElementId){
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
}
