package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.UserApi;
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;
import com.example.engineerdegreeapp.util.AccountUtils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private final String USERS_API_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/users/";

    Button registerButton;
    Button loginButton;
    EditText usernameEditText;
    EditText passwordEditText;
    TextView errorMessageTextView;
    private AccountManager mAccountManager;

    OnFragmentClickListener mClickListener;

    public LoginFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        usernameEditText = rootView.findViewById(R.id.login_username_edit_text);
        passwordEditText = rootView.findViewById(R.id.login_password_edit_text);
        loginButton = rootView.findViewById(R.id.login_sign_in_button);
        loginButton.setOnClickListener(this);
        registerButton = rootView.findViewById(R.id.login_register_button);
        registerButton.setOnClickListener(this);
        errorMessageTextView = rootView.findViewById(R.id.login_error_message);
        mAccountManager = AccountManager.get(getContext());
        return rootView;
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


    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId){
            case R.id.login_sign_in_button:
                hideKeyboard();
                checkCredentialsAndLogIn();
                break;
            case R.id.login_register_button:
                hideKeyboard();
                mClickListener.onFragmentClickInteraction(clickedItemId);
                errorMessageTextView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void checkCredentialsAndLogIn(){
        registerButton.setEnabled(false);
        loginButton.setEnabled(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(USERS_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApi budgetListApi = retrofit.create(UserApi.class);

        final String loginCredential = usernameEditText.getText().toString();
        final String passwordCredential = passwordEditText.getText().toString();
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<UserAuth> call = budgetListApi.getAccount(auth);
        call.enqueue(new Callback<UserAuth>() {
            @Override
            public void onResponse(Call<UserAuth> call, Response<UserAuth> response) {
                if(!response.isSuccessful()){
                    registerButton.setEnabled(true);
                    loginButton.setEnabled(true);
                    if(response.code() == 401){
                        errorMessageTextView.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(getActivity(),"There was a problem logging in", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    errorMessageTextView.setVisibility(View.INVISIBLE);
                    Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
                    if (accounts.length > 0)
                    {
                        registerButton.setEnabled(true);
                        loginButton.setEnabled(true);
                        Toast toast = Toast.makeText(getActivity(), getString(R.string.login_account_manager_already_exists), Toast.LENGTH_LONG);
                        toast.show();
                    }
                    else
                    {
                        final Account account = new Account(loginCredential, AccountUtils.ACCOUNT_TYPE);
                        final DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
                        String authToken = loginCredential + "_" + AccountUtils.APP_NAME + "_" + df.format(new Date());

                        Bundle bundle = new Bundle();

                        mAccountManager.addAccountExplicitly(account, passwordCredential, bundle);
                        mAccountManager.setAuthToken(account, AccountUtils.AUTH_TOKEN_TYPE, authToken);

                        mClickListener.onFragmentClickInteraction(R.id.login_sign_in_button);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserAuth> call, Throwable t) {
                registerButton.setEnabled(true);
                loginButton.setEnabled(true);
                Toast.makeText(getActivity(), "Failed to login check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface OnFragmentClickListener{
        void onFragmentClickInteraction(int clickedElementId);
    }

    public void hideKeyboard() {
        View view =  getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
