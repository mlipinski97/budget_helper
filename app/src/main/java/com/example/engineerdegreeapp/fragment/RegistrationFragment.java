package com.example.engineerdegreeapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.UserApi;
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;
import com.example.engineerdegreeapp.util.RegexUtils;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistrationFragment extends Fragment implements View.OnClickListener {

    private final String USERS_API_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/users/";

    Button confirmRegisterButton;
    Button cancelRegisterButton;
    EditText usernameEditText;
    EditText passwordEditText;
    EditText passwordConfirmationEditText;

    OnFragmentClickListener mClickListener;

    public RegistrationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);
        usernameEditText = rootView.findViewById(R.id.register_username_edit_text);
        passwordEditText = rootView.findViewById(R.id.register_password_edit_text);
        passwordConfirmationEditText = rootView.findViewById(R.id.register_password_confirmation_edit_text);
        confirmRegisterButton = rootView.findViewById(R.id.register_confirm_button);
        confirmRegisterButton.setOnClickListener(this);
        cancelRegisterButton = rootView.findViewById(R.id.register_cancel_button);
        cancelRegisterButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mClickListener = (RegistrationFragment.OnFragmentClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement OnFragmentClickListener");
        }
    }

    public interface OnFragmentClickListener{
        void onFragmentClickInteraction(int clickedElementId);
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId){
            case R.id.register_confirm_button:
                hideKeyboard();
                if(isMailUsernameValid() && isPasswordValid() && doPasswordsMatch()){
                    registerNewAccount();
                } else{
                    if(!isMailUsernameValid()){
                        Toast.makeText(getActivity(), getString(R.string.register_email_not_valid),Toast.LENGTH_LONG).show();
                    } else if(!isPasswordValid()){
                        Toast.makeText(getActivity(), getString(R.string.register_bad_password),Toast.LENGTH_LONG).show();
                    } else if(!doPasswordsMatch()){
                        Toast.makeText(getActivity(), getString(R.string.register_passwords_does_not_match),Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.register_cancel_button:
                hideKeyboard();
                mClickListener.onFragmentClickInteraction(clickedItemId);
                break;
        }
    }

    private boolean isPasswordValid(){
        String password = passwordEditText.getText().toString();
        return RegexUtils.isPasswordRegexSafe(password);
    }
    private boolean isMailUsernameValid(){
        String username = usernameEditText.getText().toString();
        return RegexUtils.isMailUsernameRegexSafe(username);
    }

    private boolean doPasswordsMatch(){
        String passwordConfirmation = passwordConfirmationEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        return password.equals(passwordConfirmation);
    }

    private void registerNewAccount(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(USERS_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApi budgetListApi = retrofit.create(UserApi.class);


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", usernameEditText.getText().toString())
                .addFormDataPart("password", passwordEditText.getText().toString())
                .addFormDataPart("passwordConfirmation", passwordConfirmationEditText.getText().toString())
                .build();
        Call<UserAuth> call = budgetListApi.registerUser(requestBody);

        call.enqueue(new Callback<UserAuth>() {
            @Override
            public void onResponse(Call<UserAuth> call, Response<UserAuth> response) {
                if(!response.isSuccessful()){
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String errorMessage = jObjError.getString("errors")
                                .replace("\"", "")
                                .replace("[", "")
                                .replace("]", "");

                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    return;
                } else{
                    Toast.makeText(getActivity(), "User Registered", Toast.LENGTH_SHORT).show();
                    mClickListener.onFragmentClickInteraction(R.id.register_confirm_button);
                }
            }

            @Override
            public void onFailure(Call<UserAuth> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to register", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void hideKeyboard() {
        View view =  getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
