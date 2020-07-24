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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.adapter.FriendshipAdapter;
import com.example.engineerdegreeapp.adapter.FriendshipAwaitingConfirmationAdapter;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;
import com.example.engineerdegreeapp.retrofit.UserApi;
import com.example.engineerdegreeapp.retrofit.entity.Friendship;
import com.example.engineerdegreeapp.util.AccountUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FriendsFragment extends Fragment implements FriendshipAdapter.ListItemClickListener,
        FriendshipAwaitingConfirmationAdapter.ListItemClickListener,
        View.OnClickListener{

    private final String FRIENDSHIP_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/users/friendship/";

    private Account mAccount;
    private AccountManager mAccountManager;
    private ArrayList<Friendship> friendships;
    private RecyclerView friendsRecyclerView;
    private RecyclerView waitingFriendsRecyclerView;
    private FriendshipAdapter friendshipAdapter;
    private FriendshipAwaitingConfirmationAdapter friendshipAwaitingConfirmationAdapter;
    private Button searchButton;
    private EditText searchEditText;
    private String fragmentTitle;
    private ToolbarChangeListener toolbarChangeListener;
    private TextView friendsLoadingErrorTextView;
    private TextView waitingFriendsErrorTextView;
    private Button unfriendButton;
    private ArrayList<Friendship> selectedFriendships = new ArrayList<>();
    private EditText popupFindFriendEditText;
    private Button popupAddFriendButton;
    private TextView popupErrorTextView;
    private FloatingActionButton addFriendFloatingActionButton;

    public FriendsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            mAccount = accounts[0];
        } else {
            return null;
        }
        fragmentTitle = getContext().getResources().getString(R.string.friend_fragment_title);
        toolbarChangeListener.hideEditButtons();
        toolbarChangeListener.changeToolbarTitle(fragmentTitle);
        unfriendButton = rootView.findViewById(R.id.friends_fragment_unfriend_button);
        unfriendButton.setOnClickListener(v -> {
            for (Friendship f : selectedFriendships) {
                if(mAccount.name.equals(f.getFriend().getUsername())){
                    deleteFriendship(f.getRequester().getUsername());
                } else{
                    deleteFriendship(f.getFriend().getUsername());
                }
            }
            unfriendButton.setVisibility(View.INVISIBLE);
            loadAllFriends();
        });
        searchButton = rootView.findViewById(R.id.friends_fragment_search_button);
        searchButton.setOnClickListener(this);
        searchEditText = rootView.findViewById(R.id.friends_fragment_search_edit_text);
        friendsLoadingErrorTextView = rootView.findViewById(R.id.friends_fragment_loading_error);
        waitingFriendsErrorTextView = rootView.findViewById(R.id.friends_fragment_waiting_loading_error);
        friendsRecyclerView = rootView.findViewById(R.id.friends_fragment_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        friendsRecyclerView.setLayoutManager(layoutManager);
        friendsRecyclerView.setHasFixedSize(true);
        waitingFriendsRecyclerView = rootView.findViewById(R.id.friends_fragment_waiting_recycler_view);
        LinearLayoutManager waitingLayoutManager = new LinearLayoutManager(getContext());
        waitingFriendsRecyclerView.setLayoutManager(waitingLayoutManager);
        waitingFriendsRecyclerView.setHasFixedSize(true);
        addFriendFloatingActionButton = rootView.findViewById(R.id.friends_fragment_floating_action_button);
        addFriendFloatingActionButton.setOnClickListener(this);
        loadAllFriends();



        return rootView;
    }

    public void onButtonShowPopupWindowClick() {

        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(R.layout.popup_window_add_friend);
        alertDialog = builder.create();
        alertDialog.show();
        popupErrorTextView = alertDialog.findViewById(R.id.popup_window_add_friend_error_text_view);
        popupFindFriendEditText = alertDialog.findViewById(R.id.popup_window_add_friend_edit_text);
        popupAddFriendButton = alertDialog.findViewById(R.id.popup_window_add_friend_button);
        popupAddFriendButton.setOnClickListener(v -> {
            if(popupFindFriendEditText.getText().toString().isEmpty()){
                popupErrorTextView.setText(getContext().getResources().getString(R.string.friend_send_friend_request_error_text_view));
                popupErrorTextView.setVisibility(View.VISIBLE);
            } else{
                popupErrorTextView.setVisibility(View.INVISIBLE);
                sendFriendRequest(popupFindFriendEditText.getText().toString(), alertDialog);
            }

        });


    }

    private void loadAllFriends() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FRIENDSHIP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApi userApi = retrofit.create(UserApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<List<Friendship>> call = userApi.getFriendships(auth);
        call.enqueue(new Callback<List<Friendship>>() {
            @Override
            public void onResponse(Call<List<Friendship>> call, Response<List<Friendship>> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("loadAllFriends()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    friendsLoadingErrorTextView.setVisibility(View.INVISIBLE);
                    friendships = new ArrayList<>(response.body());
                    ArrayList<Friendship> confirmedFriendships = friendships.stream()
                            .filter(f -> f.isAccepted())
                            .collect(Collectors.toCollection(ArrayList::new));
                    ArrayList<Friendship> waitingForConfirmationFriendships = friendships.stream()
                            .filter(f -> !f.isAccepted())
                            .collect(Collectors.toCollection(ArrayList::new));

                    friendshipAdapter = new FriendshipAdapter(confirmedFriendships,
                            confirmedFriendships.size(),
                            loginCredential,
                            FriendsFragment.this);
                    friendsRecyclerView.setAdapter(friendshipAdapter);
                    friendshipAwaitingConfirmationAdapter = new FriendshipAwaitingConfirmationAdapter(waitingForConfirmationFriendships,
                            waitingForConfirmationFriendships.size(),
                            loginCredential,
                            FriendsFragment.this);
                    waitingFriendsRecyclerView.setAdapter(friendshipAwaitingConfirmationAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Friendship>> call, Throwable t) {
                Log.d("loadAllFriends()", "onFailure - call failed");
            }
        });
    }

    public void deleteFriendship(String username){
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(FRIENDSHIP_BASE_URL)
                .build();

        UserApi userApi = retrofit.create(UserApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<Void> call = userApi.deleteFriendship(auth, username);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("deleteFriendship()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else{
                    Log.d("deleteFriendship()", "deleted friendship: " + loginCredential + " - " + username);
                    loadAllFriends();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("deleteFriendship()", "onFailure while deleting friendship: " + loginCredential + " - " + username);
            }
        });
    }

    public void acceptFriendship(String username){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FRIENDSHIP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApi userApi = retrofit.create(UserApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<Friendship> call = userApi.acceptFriendship(auth, username);
        call.enqueue(new Callback<Friendship>() {
            @Override
            public void onResponse(Call<Friendship> call, Response<Friendship> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("acceptFriendship()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else{
                    Log.d("acceptFriendship()", "accepted friendship: " + loginCredential + " - " + username);
                    loadAllFriends();
                }
            }

            @Override
            public void onFailure(Call<Friendship> call, Throwable t) {
                Log.d("acceptFriendship()", "onFailure while accepting friendship: " + loginCredential + " - " + username);
            }
        });
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            toolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException f) {
            throw new ClassCastException(context.toString() + "must implement ToolbarChangeListener");

        }
    }

    private void sendFriendRequest(String username , AlertDialog alertDialog) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(FRIENDSHIP_BASE_URL)
                .build();
        UserApi userApi = retrofit.create(UserApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<Friendship> call = userApi.addFriendship(auth, username);
        call.enqueue(new Callback<Friendship>() {
            @Override
            public void onResponse(Call<Friendship> call, Response<Friendship> response) {
                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d("sendFriendRequest()", errorBody);
                        JSONObject jObjError = new JSONObject(errorBody);
                        String errorMessage = jObjError.getString("errors")
                                .replace("\"", "")
                                .replace("[", "")
                                .replace("]", "");
                        popupErrorTextView.setText(errorMessage);
                        popupErrorTextView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else{
                    Log.d("sendFriendRequest()", "friendship request sent: " + loginCredential + " - " + username);
                    loadAllFriends();
                    alertDialog.hide();
                }
            }

            @Override
            public void onFailure(Call<Friendship> call, Throwable t) {
                Log.d("sendFriendRequest()", "onFailure while requesting friendship: " + loginCredential + " - " + username);
                alertDialog.hide();
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.friend_send_friend_request_unknown_error), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        toolbarChangeListener.restoreToolbarTitle();
    }

    @Override
    public void onListItemClick(View v, Friendship friendship, CheckBox checkBox) {
        friendship.setSelected(!friendship.isSelected());
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            v.setBackgroundColor(friendship.isSelected() ?
                    getContext().getResources().getColor(R.color.darkCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.darkCardBackgroundColor, null));
        } else {
            v.setBackgroundColor(friendship.isSelected() ?
                    getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.lightCardBackgroundColor, null));
        }
        if (friendship.isSelected()) {
            selectedFriendships.add(friendship);
            checkBox.setVisibility(View.VISIBLE);
        } else {
            selectedFriendships.remove(friendship);
            checkBox.setVisibility(View.INVISIBLE);
        }
        if (!selectedFriendships.isEmpty()) {
            unfriendButton.setVisibility(View.VISIBLE);
        } else {
            unfriendButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onListItemClick(View v, Friendship friendship) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.friends_waiting_confirmation_item_decline_button:
                if(mAccount.name.equals(friendship.getFriend().getUsername())){
                    deleteFriendship(friendship.getRequester().getUsername());
                } else{
                    deleteFriendship(friendship.getFriend().getUsername());
                }
                break;
            case R.id.friends_waiting_confirmation_item_accept_button:
                acceptFriendship(friendship.getRequester().getUsername());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int clickedItemId = v.getId();
        switch (clickedItemId){
            case R.id.friends_fragment_search_button:
                searchAndReloadLists();
                break;
            case R.id.friends_fragment_floating_action_button:
                onButtonShowPopupWindowClick();
                break;
        }
    }

    private void searchAndReloadLists(){
        ArrayList<Friendship> filteredFriendship = friendships.stream()
                .filter(f ->
                        (f.getRequester().getUsername().equals(mAccount.name)
                                && f.getFriend().getUsername().contains(searchEditText.getText().toString()))
                                || (f.getFriend().getUsername().equals(mAccount.name)
                                && f.getRequester().getUsername().contains(searchEditText.getText().toString()))
                ).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Friendship> confirmedFriendships = filteredFriendship.stream()
                .filter(f -> f.isAccepted())
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Friendship> waitingForConfirmationFriendships = filteredFriendship.stream()
                .filter(f -> !f.isAccepted())
                .collect(Collectors.toCollection(ArrayList::new));

        friendshipAdapter = new FriendshipAdapter(confirmedFriendships,
                confirmedFriendships.size(),
                mAccount.name,
                FriendsFragment.this);
        friendsRecyclerView.setAdapter(friendshipAdapter);
        friendshipAwaitingConfirmationAdapter = new FriendshipAwaitingConfirmationAdapter(waitingForConfirmationFriendships,
                waitingForConfirmationFriendships.size(),
                mAccount.name,
                FriendsFragment.this);
        waitingFriendsRecyclerView.setAdapter(friendshipAwaitingConfirmationAdapter);
    }

}
