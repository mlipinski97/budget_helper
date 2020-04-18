package com.example.engineerdegreeapp.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class AccountUtils {
    public static final String APP_NAME = "engineer_degree";
    public static final String ACCOUNT_TYPE = "engineer_degree";
    public static final String AUTH_TOKEN_TYPE = "engineer_degree.token_type";

    public static Account getAccount(Context context, String accountName) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.name.equalsIgnoreCase(accountName)) {
                return account;
            }
        }
        return null;
    }
}
