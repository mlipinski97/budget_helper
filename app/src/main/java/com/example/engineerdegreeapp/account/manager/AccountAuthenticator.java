package com.example.engineerdegreeapp.account.manager;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import com.example.engineerdegreeapp.activity.LoginActivity;
import com.example.engineerdegreeapp.retrofit.UserApi;
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.engineerdegreeapp.util.AccountUtils.APP_NAME;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private final String USERS_API_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/users/";


    private final Context mContext;
    private String globalAuthToken;

    public AccountAuthenticator(Context context) {
        super(context);
        mContext = context;
    }


    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType,
                             String authTokenType,
                             String[] requiredFeatures,
                             Bundle options) throws NetworkErrorException {

        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.ARG_AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);

        final Bundle replyBundle = new Bundle();
        replyBundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return replyBundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
                               Account account,
                               String authTokenType,
                               Bundle options) throws NetworkErrorException {

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        if (authToken.isEmpty()) {
            final String password = accountManager.getPassword(account);
            if (password != null) {
                checkCredentialsAndLogIn(account.name, password);
                authToken = globalAuthToken;
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(LoginActivity.ARG_AUTH_TOKEN_TYPE, authTokenType);

        if (null != account) {
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        }

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    private void checkCredentialsAndLogIn(final String login, String password) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(USERS_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApi budgetListApi = retrofit.create(UserApi.class);

        String credentials = login + ":" + password;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<UserAuth> call = budgetListApi.getAccount(auth);
        call.enqueue(new Callback<UserAuth>() {
            @Override
            public void onResponse(Call<UserAuth> call, Response<UserAuth> response) {
                if (!response.isSuccessful()) {
                    globalAuthToken = null;
                } else {
                    final DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
                    globalAuthToken = login + "_" + APP_NAME + "_" + df.format(new Date());
                }
            }

            @Override
            public void onFailure(Call<UserAuth> call, Throwable t) {
                globalAuthToken = null;
            }
        });
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
