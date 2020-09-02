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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.FriendAdapter;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;
import com.example.engineerdegreeapp.retrofit.BudgetListApi;
import com.example.engineerdegreeapp.retrofit.UserApi;
import com.example.engineerdegreeapp.retrofit.entity.Friendship;
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;
import com.example.engineerdegreeapp.util.AccountUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ShareBudgetListFragment extends Fragment implements View.OnClickListener,
        FriendAdapter.ListItemClickListener,
        FriendAdapter.OnCreateStateChangeListener {

    private final String USERS_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/users/";
    private final String FRIENDSHIP_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/users/friendship/";
    private final String BUDGET_LIST_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/budgetlist/";


    private OnFragmentClickListener mClickListener;
    private ToolbarChangeListener toolbarChangeListener;
    private Button searchButton;
    private EditText searchEditText;
    private Button cancelButton;
    private Button shareButton;
    private Button revokeButton;
    private Account mAccount;
    private AccountManager mAccountManager;
    private RecyclerView alreadySharedRecyclerView;
    private RecyclerView notSharedRecyclerView;
    private FriendAdapter alreadySharedAdapter;
    private FriendAdapter notSharedAdapter;
    private TextView sharedNoPositionsTextView;
    private TextView notSharedNoPositionsTextView;
    private TextView sharedErrorTextView;
    private TextView notSharedErrorTextView;
    private Long budgetListId;
    private ArrayList<UserAuth> friendsWithPermission;
    private ArrayList<UserAuth> friendsWithoutPermission;

    private ArrayList<View> selectedFriendsViewsWithPermission = new ArrayList<>();
    private ArrayList<CheckBox> selectedFriendsCheckboxesWithPermission = new ArrayList<>();

    private ArrayList<View> selectedFriendsViewsWithoutPermission = new ArrayList<>();
    private ArrayList<CheckBox> selectedFriendsCheckboxesWithoutPermission = new ArrayList<>();

    public ShareBudgetListFragment(Long budgetListId) {
        this.budgetListId = budgetListId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share_budget_list, container, false);

        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }

        sharedNoPositionsTextView = rootView.findViewById(R.id.share_budget_list_shared_no_positions_text_view);
        notSharedNoPositionsTextView = rootView.findViewById(R.id.share_budget_list_not_shared_no_positions_text_view);
        sharedErrorTextView = rootView.findViewById(R.id.share_budget_list_shared_loading_error_text_view);
        notSharedErrorTextView = rootView.findViewById(R.id.share_budget_list_not_shared_loading_error_text_view);
        toolbarChangeListener.changeToolbarTitle(getResources().getString(R.string.share_budget_list_toolbar_name));
        toolbarChangeListener.hideEditButtons();
        searchButton = rootView.findViewById(R.id.share_budget_list_username_search_button);
        searchButton.setOnClickListener(this);
        revokeButton = rootView.findViewById(R.id.share_budget_list_button_revoke);
        revokeButton.setOnClickListener(this);
        searchEditText = rootView.findViewById(R.id.share_budget_list_username_search_edit_text);
        cancelButton = rootView.findViewById(R.id.share_budget_list_button_cancel);
        cancelButton.setOnClickListener(this);
        shareButton = rootView.findViewById(R.id.share_budget_list_button_share);
        shareButton.setOnClickListener(this);
        sharedNoPositionsTextView = rootView.findViewById(R.id.share_budget_list_shared_no_positions_text_view);
        notSharedNoPositionsTextView = rootView.findViewById(R.id.share_budget_list_not_shared_no_positions_text_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager notSharedLayoutManager = new LinearLayoutManager(getContext());
        alreadySharedRecyclerView = rootView.findViewById(R.id.share_budget_list_shared_recycler_view);
        alreadySharedRecyclerView.setLayoutManager(layoutManager);
        alreadySharedRecyclerView.setHasFixedSize(true);
        notSharedRecyclerView = rootView.findViewById(R.id.share_budget_list_not_shared_recycler_view);
        notSharedRecyclerView.setLayoutManager(notSharedLayoutManager);
        notSharedRecyclerView.setHasFixedSize(true);
        getAllFriendsWithPermission(budgetListId);
        return rootView;
    }

    private void getAllFriendsWithPermission(Long budgetListId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(USERS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UserApi userApi = retrofit.create(UserApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<List<UserAuth>> alreadySharedCall = userApi.getAllFriendsByBudgetListId(auth, budgetListId);

        alreadySharedCall.enqueue(new Callback<List<UserAuth>>() {
            @Override
            public void onResponse(Call<List<UserAuth>> call, Response<List<UserAuth>> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("getAllFriendsWithPermission()", response.errorBody().string());
                        friendsWithPermission = null;
                        notSharedErrorTextView.setVisibility(View.VISIBLE);
                        sharedErrorTextView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        friendsWithPermission = null;
                        notSharedErrorTextView.setVisibility(View.VISIBLE);
                        sharedErrorTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    friendsWithPermission = response.body().stream()
                            .filter(u -> !u.getUsername().equals(mAccount.name)).collect(Collectors.toCollection(ArrayList::new));
                    getAllFriendships();

                }
            }

            @Override
            public void onFailure(Call<List<UserAuth>> call, Throwable t) {
                Log.d("getAllFriendsWithPermission()", "onFailure - call failed");
                friendsWithPermission = null;
                notSharedErrorTextView.setVisibility(View.VISIBLE);
                sharedErrorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getAllFriendships() {
        if (friendsWithPermission == null) {
            notSharedErrorTextView.setVisibility(View.VISIBLE);
            sharedErrorTextView.setVisibility(View.VISIBLE);
            return;
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FRIENDSHIP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UserApi userApi = retrofit.create(UserApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<List<Friendship>> allFriendshipsCall = userApi.getFriendships(auth);
        allFriendshipsCall.enqueue(new Callback<List<Friendship>>() {
            @Override
            public void onResponse(Call<List<Friendship>> call, Response<List<Friendship>> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("getAllFriendships()", response.errorBody().string());
                        notSharedErrorTextView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        notSharedErrorTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    ArrayList<Friendship> allFriendshipsList = response.body().stream().filter(f -> f.isAccepted()).collect(Collectors.toCollection(ArrayList::new));
                    ArrayList<UserAuth> allFriends = new ArrayList<>(getFriendsFromFriendships(allFriendshipsList));
                    ArrayList<String> usernamesWithPermisson = friendsWithPermission.stream().map(UserAuth::getUsername).collect(Collectors.toCollection(ArrayList::new));
                    friendsWithoutPermission = allFriends
                            .stream()
                            .filter(f -> !usernamesWithPermisson.contains(f.getUsername()))
                            .collect(Collectors.toCollection(ArrayList::new));

                    notSharedAdapter = new FriendAdapter(friendsWithoutPermission,
                            friendsWithoutPermission.size(),
                            ShareBudgetListFragment.this,
                            FriendAdapter.AdapterType.TYPE_FRIENDS_WITHOUT_PERMISSION,
                            getContext(),
                            ShareBudgetListFragment.this);
                    notSharedRecyclerView.setAdapter(notSharedAdapter);
                    alreadySharedAdapter = new FriendAdapter(friendsWithPermission,
                            friendsWithPermission.size(),
                            ShareBudgetListFragment.this,
                            FriendAdapter.AdapterType.TYPE_FRIENDS_WITH_PERMISSION,
                            getContext(),
                            ShareBudgetListFragment.this);
                    alreadySharedRecyclerView.setAdapter(alreadySharedAdapter);
                    if (alreadySharedAdapter.getItemCount() < 1) {
                        sharedNoPositionsTextView.setVisibility(View.VISIBLE);
                    } else {
                        sharedNoPositionsTextView.setVisibility(View.INVISIBLE);
                    }
                    if (notSharedAdapter.getItemCount() < 1) {
                        notSharedNoPositionsTextView.setVisibility(View.VISIBLE);
                    } else {
                        notSharedNoPositionsTextView.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Friendship>> call, Throwable t) {
                Log.d("getAllFriendships()", "onFailure - call failed");
                notSharedErrorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void shareBudgetList(String username) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BUDGET_LIST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Void> call = budgetListApi.shareBudgetList(auth, username, budgetListId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("shareBudgetList()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("shareBudgetList()", "shared budgetlist with id: " + budgetListId + " with user: " + username);
                    getAllFriendsWithPermission(budgetListId);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("shareBudgetList()", "onFailure while trying to share budgetlist with id: " + budgetListId + " with user: " + username);
            }
        });
    }

    private void shareManyBudgetLists(List<String> userNamesToShare) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BUDGET_LIST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Void> call = budgetListApi.shareManyBudgetLists(auth, userNamesToShare, budgetListId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("shareManyBudgetLists()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    userNamesToShare.forEach(userName -> {
                        Log.d("shareManyBudgetLists()", "shared budgetlist with id: " + budgetListId + " with user: " + userName);
                    });
                    getAllFriendsWithPermission(budgetListId);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                userNamesToShare.forEach(userName -> {
                    Log.d("shareManyBudgetLists()", "onFailure while trying to share budgetlist with id: " + budgetListId + " with user: " + userName);
                });
            }
        });
    }

    private void revokeBudgetList(String username) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BUDGET_LIST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Void> call = budgetListApi.revokeBudgetList(auth, username, budgetListId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("revokeBudgetList()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("revokeBudgetList()", "revoked budgetlist with id: " + budgetListId + " from user: " + username);
                    getAllFriendsWithPermission(budgetListId);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("revokeBudgetList()", "onFailure while trying to revoke budgetlist with id: " + budgetListId + " from user: " + username);
            }
        });
    }

    private void revokeManyBudgetLists(List<String> userNamesToRevoke) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BUDGET_LIST_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BudgetListApi budgetListApi = retrofit.create(BudgetListApi.class);
        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        Call<Void> call = budgetListApi.revokeManyBudgetLists(auth, userNamesToRevoke, budgetListId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("revokeManyBudgetLists()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    userNamesToRevoke.forEach(userName -> {
                        Log.d("revokeManyBudgetLists()", "revoked budgetlist with id: " + budgetListId + " from user: " + userName);
                    });
                    getAllFriendsWithPermission(budgetListId);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                userNamesToRevoke.forEach(userName -> {
                    Log.d("revokeManyBudgetLists()", "onFailure while trying to revoke budgetlist with id: " + budgetListId + " from user: " + userName);
                });
            }
        });
    }

    @Override
    public void onListItemClick(View v, UserAuth user, CheckBox checkBox, FriendAdapter.AdapterType adapterType) {
        user.setSelected(!user.isSelected());
        switch (adapterType) {
            case TYPE_FRIENDS_WITH_PERMISSION:
                shareButton.setVisibility(View.INVISIBLE);
                hideAllCheckboxes(selectedFriendsCheckboxesWithoutPermission);
                restoreViewColors(selectedFriendsViewsWithoutPermission);
                unSelectUsers(friendsWithoutPermission);

                selectedFriendsViewsWithoutPermission = new ArrayList<>();

                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    v.setBackgroundColor(user.isSelected() ?
                            getContext().getResources().getColor(R.color.darkCardSelectedBackgroundColor, null)
                            : getContext().getResources().getColor(R.color.darkCardBackgroundColor, null));
                } else {
                    v.setBackgroundColor(user.isSelected() ?
                            getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                            : getContext().getResources().getColor(R.color.lightCardBackgroundColor, null));
                }
                if (user.isSelected()) {
                    friendsWithPermission.get(friendsWithPermission.indexOf(user)).setSelected(true);
                    selectedFriendsViewsWithPermission.add(v);
                    selectedFriendsCheckboxesWithPermission.add(checkBox);
                    checkBox.setVisibility(View.VISIBLE);
                } else {
                    friendsWithPermission.get(friendsWithPermission.indexOf(user)).setSelected(false);
                    selectedFriendsViewsWithPermission.remove(v);
                    selectedFriendsCheckboxesWithPermission.remove(checkBox);
                    checkBox.setVisibility(View.INVISIBLE);
                }
                boolean isAnyWithPermissionSelected = false;
                for (UserAuth u : friendsWithPermission) {
                    if (u.isSelected()) {
                        isAnyWithPermissionSelected = true;
                        break;
                    }
                }
                if (isAnyWithPermissionSelected) {
                    revokeButton.setVisibility(View.VISIBLE);
                } else {
                    revokeButton.setVisibility(View.INVISIBLE);
                }
                break;
            case TYPE_FRIENDS_WITHOUT_PERMISSION:
                revokeButton.setVisibility(View.INVISIBLE);
                hideAllCheckboxes(selectedFriendsCheckboxesWithPermission);
                restoreViewColors(selectedFriendsViewsWithPermission);
                unSelectUsers(friendsWithPermission);

                selectedFriendsViewsWithPermission = new ArrayList<>();
                selectedFriendsCheckboxesWithPermission = new ArrayList<>();
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    v.setBackgroundColor(user.isSelected() ?
                            getContext().getResources().getColor(R.color.darkCardSelectedBackgroundColor, null)
                            : getContext().getResources().getColor(R.color.darkCardBackgroundColor, null));
                } else {
                    v.setBackgroundColor(user.isSelected() ?
                            getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                            : getContext().getResources().getColor(R.color.lightCardBackgroundColor, null));
                }
                if (user.isSelected()) {
                    friendsWithoutPermission.get(friendsWithoutPermission.indexOf(user)).setSelected(true);
                    selectedFriendsViewsWithoutPermission.add(v);
                    selectedFriendsCheckboxesWithoutPermission.add(checkBox);
                    checkBox.setVisibility(View.VISIBLE);
                } else {
                    friendsWithoutPermission.get(friendsWithoutPermission.indexOf(user)).setSelected(false);
                    selectedFriendsViewsWithoutPermission.remove(v);
                    selectedFriendsCheckboxesWithoutPermission.remove(checkBox);
                    checkBox.setVisibility(View.INVISIBLE);
                }
                boolean isAnyWithoutPermissionSelected = false;
                for (UserAuth u : friendsWithoutPermission) {
                    if (u.isSelected()) {
                        isAnyWithoutPermissionSelected = true;
                        break;
                    }
                }
                if (isAnyWithoutPermissionSelected) {
                    shareButton.setVisibility(View.VISIBLE);
                } else {
                    shareButton.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    @Override
    public void onCreateStateChanged(View v, CheckBox checkBox, FriendAdapter.AdapterType adapterType) {
        switch (adapterType) {
            case TYPE_FRIENDS_WITH_PERMISSION:
                selectedFriendsViewsWithPermission.add(v);
                selectedFriendsCheckboxesWithPermission.add(checkBox);
                checkBox.setVisibility(View.VISIBLE);
                break;
            case TYPE_FRIENDS_WITHOUT_PERMISSION:
                selectedFriendsViewsWithoutPermission.add(v);
                selectedFriendsCheckboxesWithoutPermission.add(checkBox);
                checkBox.setVisibility(View.VISIBLE);
                break;
        }
    }

    public interface OnFragmentClickListener {
        void onFragmentClickInteraction(int clickedElementId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mClickListener = (ShareBudgetListFragment.OnFragmentClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ShareBudgetListFragment.OnFragmentClickListener");
        }
        try {
            toolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException f) {
            throw new ClassCastException(context.toString() + " must implement ToolbarChangeListener");

        }
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.share_budget_list_username_search_button:
                searchAndReloadLists();
                break;
            case R.id.share_budget_list_button_share:
                ArrayList<String> userNamesToShare = new ArrayList<>();
                friendsWithoutPermission.forEach(f -> {
                    if (f.isSelected()) {
                        userNamesToShare.add(f.getUsername());
                    }
                });
                if (userNamesToShare.size() > 1) {
                    shareManyBudgetLists(userNamesToShare);
                } else {
                    shareBudgetList(userNamesToShare.get(0));
                }
                shareButton.setVisibility(View.INVISIBLE);
                break;
            case R.id.share_budget_list_button_revoke:
                ArrayList<String> userNamesToRevoke = new ArrayList<>();

                friendsWithPermission.forEach(f -> {
                    if (f.isSelected()) {
                        userNamesToRevoke.add(f.getUsername());
                    }
                });
                if (userNamesToRevoke.size() > 1) {
                    revokeManyBudgetLists(userNamesToRevoke);
                } else {
                    revokeBudgetList(userNamesToRevoke.get(0));
                }
                revokeButton.setVisibility(View.INVISIBLE);
                break;
            case R.id.share_budget_list_button_cancel:
                mClickListener.onFragmentClickInteraction(clickedItemId);
                break;
        }
    }


    private List<UserAuth> getFriendsFromFriendships(List<Friendship> friendships) {
        ArrayList<UserAuth> friendList = new ArrayList<>();
        for (Friendship f : friendships) {
            if (f.getRequester().getUsername().equals(mAccount.name)) {
                friendList.add(f.getFriend());
            } else {
                friendList.add(f.getRequester());
            }
        }
        return friendList;
    }

    private void hideAllCheckboxes(ArrayList<CheckBox> checkboxes) {
        checkboxes.forEach(ch -> ch.setVisibility(View.INVISIBLE));
    }

    private void restoreViewColors(ArrayList<View> views) {
        views.forEach(view -> {
                    if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.darkCardBackgroundColor, null));
                    } else {
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.lightCardBackgroundColor, null));
                    }
                }
        );
    }

    private void unSelectUsers(ArrayList<UserAuth> users) {
        users.forEach(u -> u.setSelected(false));
    }

    private void searchAndReloadLists() {
        String searchedPhrase = searchEditText.getText().toString();

        ArrayList<UserAuth> tempFriendsWithoutPermission = friendsWithoutPermission.stream()
                .filter(user -> user.getUsername().contains(searchedPhrase)).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<UserAuth> tempFriendsWithPermissionList = friendsWithPermission.stream()
                .filter(user -> user.getUsername().contains(searchedPhrase)).collect(Collectors.toCollection(ArrayList::new));

        hideAllCheckboxes(selectedFriendsCheckboxesWithPermission);
        hideAllCheckboxes(selectedFriendsCheckboxesWithoutPermission);
        restoreViewColors(selectedFriendsViewsWithoutPermission);
        restoreViewColors(selectedFriendsViewsWithPermission);

        selectedFriendsViewsWithPermission = new ArrayList<>();
        selectedFriendsCheckboxesWithPermission = new ArrayList<>();
        selectedFriendsViewsWithoutPermission = new ArrayList<>();
        selectedFriendsCheckboxesWithoutPermission = new ArrayList<>();


        notSharedAdapter = new FriendAdapter(tempFriendsWithoutPermission,
                tempFriendsWithoutPermission.size(),
                ShareBudgetListFragment.this,
                FriendAdapter.AdapterType.TYPE_FRIENDS_WITHOUT_PERMISSION,
                getContext(),
                this);
        notSharedRecyclerView.setAdapter(notSharedAdapter);
        alreadySharedAdapter = new FriendAdapter(tempFriendsWithPermissionList,
                tempFriendsWithPermissionList.size(),
                ShareBudgetListFragment.this,
                FriendAdapter.AdapterType.TYPE_FRIENDS_WITH_PERMISSION,
                getContext(),
                this);
        alreadySharedRecyclerView.setAdapter(alreadySharedAdapter);
    }

}