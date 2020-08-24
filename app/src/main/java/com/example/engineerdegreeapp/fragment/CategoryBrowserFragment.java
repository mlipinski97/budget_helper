package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.CategoryAdapter;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;
import com.example.engineerdegreeapp.retrofit.CategoryApi;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;
import com.example.engineerdegreeapp.retrofit.entity.Category;
import com.example.engineerdegreeapp.util.AccountUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CategoryBrowserFragment extends Fragment implements CategoryAdapter.ListItemClickListener,
        View.OnClickListener {

    private final String CATEGORY_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/category/";

    private Account mAccount;
    private AccountManager mAccountManager;
    private Button deleteButton;
    private ToolbarChangeListener toolbarChangeListener;
    private ArrayList<Category> selectedCategories = new ArrayList<>();
    private ArrayList<Category> categoryList;
    private CategoryAdapter expenseAdapter;
    private RecyclerView categoryListRecyclerView;
    private TextView categoryListErrorTextView;
    private OnFragmentClickListener mClickListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_browser, container, false);
        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }

        toolbarChangeListener.changeToolbarTitle(getContext().getResources().getString(R.string.category_browse_toolbar_title));
        toolbarChangeListener.hideEditButtons();
        deleteButton = rootView.findViewById(R.id.category_recycler_view_delete_button);
        deleteButton.setOnClickListener(this);
        categoryListErrorTextView = rootView.findViewById(R.id.category_recycler_view_loading_error_text_view);

        categoryListRecyclerView = rootView.findViewById(R.id.category_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        categoryListRecyclerView.setLayoutManager(layoutManager);
        categoryListRecyclerView.setHasFixedSize(true);
        loadCategoryList();
        return rootView;
    }

    @Override
    public void onListItemClick(View v, Category category) {
        if (selectedCategories.isEmpty()) {
            mClickListener.onFragmentCategoryBrowserElementClickInteraction(category);
        } else {
            onListItemLongClick(v, category);
        }
    }

    @Override
    public void onListItemLongClick(View v, Category category) {
        category.setSelected(!category.isSelected());
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            v.setBackgroundColor(category.isSelected() ?
                    getContext().getResources().getColor(R.color.darkCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.darkCardBackgroundColor, null));
        } else {
            v.setBackgroundColor(category.isSelected() ?
                    getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.lightCardBackgroundColor, null));
        }
        if (category.isSelected()) {
            selectedCategories.add(category);
        } else {
            selectedCategories.remove(category);
        }
        if (!selectedCategories.isEmpty()) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.category_recycler_view_delete_button:
                ArrayList<String> nameList = new ArrayList<>();
                for (Category c : selectedCategories) {
                    nameList.add(c.getCategoryName());
                }
                selectedCategories = new ArrayList<>();
                deleteManySelectedCategories(nameList);
                deleteButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void deleteManySelectedCategories(List<String> categoryNames){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CATEGORY_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        CategoryApi categoryApi = retrofit.create(CategoryApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Void> call = categoryApi.deleteManyCategories(auth, categoryNames);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("deleteManySelectedCategories()", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("deleteManySelectedCategories()", "deleted categories with names: " + categoryNames);
                    loadCategoryList();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("deleteManySelectedCategories()", "onFailure while deleting categories with names: " + categoryNames);

            }
        });
    }

    private void loadCategoryList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CATEGORY_BASE_URL)
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
                    categoryListErrorTextView.setVisibility(View.VISIBLE);
                    try {
                        Log.d("loadCategoryList()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    categoryListErrorTextView.setVisibility(View.INVISIBLE);
                    Log.d("loadCategoryList()", "loaded all categories");
                    categoryList = new ArrayList<>(response.body());
                    categoryList = categoryList.stream().filter(c -> !c.isDeleted()).collect(Collectors.toCollection(ArrayList::new));
                    expenseAdapter = new CategoryAdapter(getContext(), categoryList, categoryList.size(), CategoryBrowserFragment.this);
                    categoryListRecyclerView.setAdapter(expenseAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.d("loadCategoryList()", "onFailure while loading categories data");
                categoryListErrorTextView.setVisibility(View.INVISIBLE);
            }
        });

    }

    public interface OnFragmentClickListener {
        void onFragmentCategoryBrowserElementClickInteraction(Category category);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mClickListener = (OnFragmentClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement CategoryBrowserFragment.OnFragmentClickListener");
        }
        try {
            toolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException f) {
            throw new ClassCastException(context.toString() + "must implement ToolbarChangeListener");

        }
    }

}
