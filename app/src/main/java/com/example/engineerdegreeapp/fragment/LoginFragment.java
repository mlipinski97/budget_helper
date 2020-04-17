package com.example.engineerdegreeapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginFragment extends Fragment implements View.OnClickListener{

    private final String USERS_API_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/users/";

    Button registerButton;
    Button loginButton;
    EditText usernameEditText;
    EditText passwordEditText;
    TextView errorMessageTextView;

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
                checkCredentialsAndLogIn();
                break;
            case R.id.login_register_button:
                mClickListener.onClickInteraction(clickedItemId);
                errorMessageTextView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void checkCredentialsAndLogIn(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(USERS_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApi budgetListApi = retrofit.create(UserApi.class);

        String credentials = usernameEditText.getText().toString() + ":" + passwordEditText.getText().toString();
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<UserAuth> call = budgetListApi.getAccount(auth);
        call.enqueue(new Callback<UserAuth>() {
            @Override
            public void onResponse(Call<UserAuth> call, Response<UserAuth> response) {
                if(!response.isSuccessful()){
                    if( response.code() == 401){
                        errorMessageTextView.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(getActivity(),"There was a problem logging in", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    errorMessageTextView.setVisibility(View.INVISIBLE);
                    mClickListener.onClickInteraction(R.id.login_sign_in_button);
                }
            }

            @Override
            public void onFailure(Call<UserAuth> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to login check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface OnFragmentClickListener{
        void onClickInteraction(int clickedElementId);
    }
}
