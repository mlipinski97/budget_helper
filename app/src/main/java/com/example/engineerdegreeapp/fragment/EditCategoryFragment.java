package com.example.engineerdegreeapp.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;
import com.example.engineerdegreeapp.retrofit.CategoryApi;
import com.example.engineerdegreeapp.retrofit.entity.Category;
import com.example.engineerdegreeapp.util.AccountUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class EditCategoryFragment extends Fragment implements View.OnClickListener {

    private static final int RESULT_LOAD_IMAGE = 1;

    private Account mAccount;
    private AccountManager mAccountManager;
    private EditText categoryNameEditText;
    private ImageView categoryIconImageView;
    private Button cancelButton;
    private Button confirmButton;
    private OnFragmentClickListener mClickListener;
    private String oldCategoryName;
    private Bitmap decodedByte = null;
    private ToolbarChangeListener toolbarChangeListener;


    public EditCategoryFragment() {
    }

    public EditCategoryFragment(String categoryName) {
        this.oldCategoryName = categoryName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_category, container, false);


        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }

        toolbarChangeListener.changeToolbarTitle(getContext().getResources().getString(R.string.category_edit_toolbar_title));
        toolbarChangeListener.hideEditButtons();

        categoryNameEditText = rootView.findViewById(R.id.edit_category_name_edit_text);
        categoryIconImageView = rootView.findViewById(R.id.edit_category_icon_image_view);
        cancelButton = rootView.findViewById(R.id.edit_category_button_cancel);
        confirmButton = rootView.findViewById(R.id.edit_category_button_confirm);
        categoryIconImageView.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
        getAndLoadCategory(oldCategoryName);

        return rootView;
    }

    public interface OnFragmentClickListener {
        void onFragmentClickInteraction(int clickedElementId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mClickListener = (OnFragmentClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement EditCategoryFragment.OnFragmentClickListener");
        }
        try {
            toolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException f) {
            throw new ClassCastException(context.toString() + "must implement ToolbarChangeListener");

        }
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.edit_category_icon_image_view:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;
            case R.id.edit_category_button_confirm:
                editCategory();
                hideKeyboard();
                break;
            case R.id.edit_category_button_cancel:
                hideKeyboard();
                mClickListener.onFragmentClickInteraction(clickedItemId);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            categoryIconImageView.setImageURI(selectedImageUri);
        }
    }

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getAndLoadCategory(String CategoryName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/category/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CategoryApi categoryApi = retrofit.create(CategoryApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Category> call = categoryApi.getSingleCategory(auth, oldCategoryName);
        call.enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("getAndLoadCategory()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("getAndLoadCategory()", "loaded all categories");
                    Category category = response.body();
                    if (category.getCategoryImage() != null) {
                        byte[] decodedString = Base64.decode(category.getCategoryImage(), Base64.DEFAULT);
                        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        categoryIconImageView.setImageBitmap(decodedByte);
                    } else {
                        categoryIconImageView.setImageResource(R.drawable.question_mark);
                    }

                    categoryNameEditText.setText(category.getCategoryName());

                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Log.d("getAndLoadCategory()", "onFailure while calling load");
            }
        });
    }

    private void editCategory() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://engineer-degree-project.herokuapp.com/api/category/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CategoryApi categoryApi = retrofit.create(CategoryApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);


        Bitmap imageBitmap = ((BitmapDrawable) categoryIconImageView.getDrawable()).getBitmap();
        File f = new File(getContext().getCacheDir(), "photo.png");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fileName = categoryNameEditText.getText().toString() + "_icon_image";
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("categoryImage", fileName, RequestBody.create(MediaType.parse("image/png"), f));

        Call<Category> call = categoryApi.editCategory(auth, oldCategoryName, categoryNameEditText.getText().toString(), imagePart);
        call.enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("editCategory()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("editCategory()", "posted edited category" + categoryNameEditText.getText().toString());
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.category_edited_toast_text) + "(" + categoryNameEditText.getText().toString() + ")", Toast.LENGTH_SHORT).show();
                    mClickListener.onFragmentClickInteraction(R.id.edit_category_button_confirm);
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Log.d("onFailure()", "failed to edit category: " + categoryNameEditText.getText().toString());
                t.printStackTrace();
            }
        });
    }
}
