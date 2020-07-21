package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;
import com.example.engineerdegreeapp.retrofit.BudgetListApi;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.example.engineerdegreeapp.util.RegexUtils;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditBudgetListFragment extends Fragment implements View.OnClickListener {

    private TextView listNameErrorTextView;
    private TextView listValueErrorTextView;
    private EditText listNameEditText;
    private EditText listValueEditText;
    private CalendarView dueDateCalendarView;
    private Account mAccount;
    private AccountManager mAccountManager;
    Long currentlySelectedDate;
    private Button cancelButton;
    private Button confirmButton;
    OnFragmentClickListener mClickListener;
    private String listName;
    private String listAmount;
    private String listSelectedDate;
    private Long listId;
    ToolbarChangeListener toolbarChangeListener;


    public EditBudgetListFragment(){
    }

    public EditBudgetListFragment(String listName, String listAmount, String listSelectedDate, Long listId) {
        this.listName = listName;
        this.listAmount = listAmount;
        this.listSelectedDate = listSelectedDate;
        this.listId = listId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_budget_list, container, false);

        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }

        toolbarChangeListener.changeToolbarTitle(getResources().getString(R.string.edit_budget_list_toolbar_name));
        toolbarChangeListener.hideEditButtons();
        listNameErrorTextView = rootView.findViewById(R.id.edit_budget_list_name_error_text_view);
        listValueErrorTextView = rootView.findViewById(R.id.edit_budget_list_amount_error_text_view);
        listNameEditText = rootView.findViewById(R.id.edit_budget_list_name_edit_text);
        listNameEditText.setText(listName);
        listValueEditText = rootView.findViewById(R.id.edit_budget_list_amount_edit_text);
        listValueEditText.setText(listAmount);
        dueDateCalendarView = rootView.findViewById(R.id.edit_budget_list_calendar_view);
        dueDateCalendarView.setMinDate((new Date().getTime()));
        try {
            dueDateCalendarView.setDate(new SimpleDateFormat("dd-MM-yyyy").parse(listSelectedDate).getTime(), true, true);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dueDateCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth);
                currentlySelectedDate = c.getTimeInMillis();
            }
        });

        cancelButton = rootView.findViewById(R.id.edit_budget_list_button_cancel);
        cancelButton.setOnClickListener(this);
        confirmButton = rootView.findViewById(R.id.edit_budget_list_button_confirm);
        confirmButton.setOnClickListener(this);

        return rootView;
    }

    private void updateBudgetList(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/budgetlist/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String selectedDate = sdf.format(new Date(currentlySelectedDate));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", listNameEditText.getText().toString())
                .addFormDataPart("budgetValue", listValueEditText.getText().toString().replace(",", "."))
                .addFormDataPart("dueDate", selectedDate)
                .build();

        Call<BudgetList> call = budgetListApi.patchBudgetList(auth, listId ,requestBody);

        call.enqueue(new Callback<BudgetList>() {
            @Override
            public void onResponse(Call<BudgetList> call, Response<BudgetList> response) {
                if (!response.isSuccessful()) {
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
                } else{
                    Toast.makeText(getActivity(), "List successfully updated", Toast.LENGTH_SHORT).show();
                    mClickListener.onFragmentClickInteraction(R.id.new_budget_list_button_confirm);
                }
            }

            @Override
            public void onFailure(Call<BudgetList> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to update list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId){
            case R.id.edit_budget_list_button_confirm:
                if(isMoneyRegexSafe() && isNameValid()){
                    updateBudgetList();
                } else{
                    if(!isMoneyRegexSafe()){
                        listValueErrorTextView.setVisibility(View.VISIBLE);
                    } else{
                        listValueErrorTextView.setVisibility(View.INVISIBLE);
                    }
                    if(!isNameValid()){
                        listNameErrorTextView.setVisibility(View.VISIBLE);
                    } else{
                        listNameErrorTextView.setVisibility(View.INVISIBLE);
                    }
                }
                break;
            case R.id.edit_budget_list_button_cancel:
                mClickListener.onFragmentClickInteraction(clickedItemId);
                break;
        }
    }

    public interface OnFragmentClickListener{
        void onFragmentClickInteraction(int clickedElementId);
    }

    private boolean isMoneyRegexSafe(){
        String moneyValue = listValueEditText.getText().toString();
        return RegexUtils.isMoneyAmountRegexSafe(moneyValue);
    }

    private boolean isNameValid(){
        String name = listNameEditText.getText().toString();
        return !name.isEmpty();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mClickListener = (EditBudgetListFragment.OnFragmentClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement EditBudgetListFragment.OnFragmentClickListener");
        }
        try{
            toolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException f){
            throw new ClassCastException(context.toString() + "must implement ToolbarChangeListener");

        }
    }

}
