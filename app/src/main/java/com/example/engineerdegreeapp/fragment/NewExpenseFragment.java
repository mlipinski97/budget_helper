package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.CategorySpinnerAdapter;
import com.example.engineerdegreeapp.adapter.item.CategoryItem;
import com.example.engineerdegreeapp.retrofit.CategoryApi;
import com.example.engineerdegreeapp.retrofit.ExpenseApi;
import com.example.engineerdegreeapp.retrofit.entity.Category;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.example.engineerdegreeapp.util.DecimalInputFilter;
import com.example.engineerdegreeapp.util.RegexUtils;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.engineerdegreeapp.util.DateUtils.dd_mm_yyy_sdf;

public class NewExpenseFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private TextView expenseNameTextView;
    private TextView expenseNameErrorTextView;
    private TextView expenseValueTextView;
    private TextView expenseValueErrorTextView;
    private TextView dueDateTextView;
    private EditText expenseNameEditText;
    private EditText expenseValueEditText;
    private CalendarView dueDateCalendarView;
    private Button cancelButton;
    private Button confirmButton;
    private Account mAccount;
    private AccountManager mAccountManager;
    private Long budgetListId;
    private OnFragmentClickListener mClickListener;
    private Long currentlySelectedDate;
    private String listDueDate;
    private Spinner categorySpinner;
    private Category selectedCategory;
    private TextView currencyTextView;
    private String currencyCode;
    private String listStartingDate;

    public NewExpenseFragment() {

    }

    public NewExpenseFragment(String listDueDate, String listStartingDate, Long budgetListId, String currencyCode) {
        this.budgetListId = budgetListId;
        this.listDueDate = listDueDate;
        this.selectedCategory = new Category();
        this.selectedCategory.setCategoryName("Others");
        this.currencyCode = currencyCode;
        this.listStartingDate = listStartingDate;
    }

    @SuppressLint("SimpleDateFormat")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_expense, container, false);

        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }

        currencyTextView = rootView.findViewById(R.id.new_expense_currency_text_view);
        currencyTextView.setText(currencyCode);
        categorySpinner = rootView.findViewById(R.id.new_expense_spinner);
        categorySpinner.setOnItemSelectedListener(this);
        expenseNameTextView = rootView.findViewById(R.id.new_expense_name_text_view);
        expenseNameErrorTextView = rootView.findViewById(R.id.new_expense_name_error_text_view);
        expenseValueTextView = rootView.findViewById(R.id.new_expense_amount_text_view);
        expenseValueErrorTextView = rootView.findViewById(R.id.new_expense_amount_error_text_view);
        dueDateTextView = rootView.findViewById(R.id.new_expense_due_date_text_view);
        expenseNameEditText = rootView.findViewById(R.id.new_expense_name_edit_text);
        expenseValueEditText = rootView.findViewById(R.id.new_expense_amount_edit_text);
        expenseValueEditText.setFilters(new InputFilter[]{new DecimalInputFilter(10, 2)});
        dueDateCalendarView = rootView.findViewById(R.id.new_expense_calendar_view);

        try {
            dueDateCalendarView.setMaxDate(dd_mm_yyy_sdf.parse(listDueDate).getTime());
            dueDateCalendarView.setMinDate(dd_mm_yyy_sdf.parse(listDueDate).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        currentlySelectedDate = dueDateCalendarView.getDate();
        dueDateCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
            currentlySelectedDate = c.getTimeInMillis();
        });
        cancelButton = rootView.findViewById(R.id.new_expense_button_cancel);
        cancelButton.setOnClickListener(this);
        confirmButton = rootView.findViewById(R.id.new_expense_button_confirm);
        confirmButton.setOnClickListener(this);
        loadCategorySpinnerData();
        return rootView;
    }

    private void loadCategorySpinnerData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/category/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CategoryApi categoryApi = retrofit.create(CategoryApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<List<Category>> call = categoryApi.getAllCategories(auth);
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("loadCategorySpinnerData()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("loadCategorySpinnerData()", "loaded all categories");
                    ArrayList<Category> categories = new ArrayList<>(response.body());
                    categories = categories.stream().filter(c -> !c.isDeleted()).collect(Collectors.toCollection(ArrayList::new));
                    Map<String, Bitmap> mappedCategories = transformToCategoryItems(categories);
                    ArrayList<CategoryItem> categoryItems = new ArrayList<>();
                    mappedCategories.entrySet().forEach(e -> categoryItems.add(new CategoryItem(e.getKey(), e.getValue())));
                    CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(getContext(), categoryItems);
                    categorySpinner.setAdapter(adapter);
//TODO: change icon for every category
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.d("loadCategorySpinnerData()", "onFailure while loading spinner data");
                t.printStackTrace();
            }
        });

    }

    private Map<String, Bitmap> transformToCategoryItems(ArrayList<Category> categories){
        ArrayList<String> categoryNames = categories.stream().map(Category::getCategoryName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> categoryImageStrings = categories.stream().map(Category::getCategoryImage).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Bitmap> categoryBitmaps = new ArrayList<>();

        categoryImageStrings.forEach(c -> {
            byte[] decodedString = Base64.decode(c, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            categoryBitmaps.add(decodedByte);
        });

        return IntStream.range(0, categoryNames.size()).boxed()
                .collect(Collectors.toMap(categoryNames::get, categoryBitmaps::get));
    }

    private void postExpense() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/expenses/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExpenseApi expenseApi = retrofit.create(ExpenseApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        String selectedDate = dd_mm_yyy_sdf.format(new Date(currentlySelectedDate));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", expenseNameEditText.getText().toString())
                .addFormDataPart("amount", expenseValueEditText.getText().toString().replace(",", "."))
                .addFormDataPart("dateOfExpense", selectedDate)
                .build();
        Call<Expense> call = expenseApi.postExpense(auth, budgetListId, selectedCategory.getCategoryName(), requestBody);
        call.enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (!response.isSuccessful()) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String errorMessage = jObjError.getString("errors")
                                .replace("\"", "")
                                .replace("[", "")
                                .replace("]", "");
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        Log.d("postExpense()", "onResponse: " + errorMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Expense successfully added", Toast.LENGTH_SHORT).show();
                    mClickListener.onFragmentClickInteraction(R.id.new_expense_button_confirm);
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                Toast.makeText(getActivity(), "Failed to create new expense", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.new_expense_button_cancel:
                hideKeyboard();
                mClickListener.onFragmentClickInteraction(clickedItemId);
                break;
            case R.id.new_expense_button_confirm:
                hideKeyboard();
                if (isMoneyRegexSafe() && isNameValid()) {
                    postExpense();
                } else {
                    if (!isMoneyRegexSafe()) {
                        expenseValueErrorTextView.setVisibility(View.VISIBLE);
                    } else {
                        expenseValueErrorTextView.setVisibility(View.INVISIBLE);
                    }
                    if (!isNameValid()) {
                        expenseNameErrorTextView.setVisibility(View.VISIBLE);
                    } else {
                        expenseNameErrorTextView.setVisibility(View.INVISIBLE);
                    }
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        CategoryItem category = (CategoryItem) parent.getItemAtPosition(position);
        selectedCategory.setCategoryName(category.getCategoryName());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public interface OnFragmentClickListener {
        void onFragmentClickInteraction(int clickedElementId);
    }

    private boolean isMoneyRegexSafe() {
        String moneyValue = expenseValueEditText.getText().toString();
        return RegexUtils.isMoneyAmountRegexSafe(moneyValue);
    }

    private boolean isNameValid() {
        String name = expenseNameEditText.getText().toString();
        return !name.isEmpty();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mClickListener = (OnFragmentClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement NewExpenseFragment.OnFragmentClickListener");
        }
    }

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
