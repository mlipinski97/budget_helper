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
import com.example.engineerdegreeapp.adapter.ExpenseAdapter;
import com.example.engineerdegreeapp.adapter.FriendsAdapter;
import com.example.engineerdegreeapp.adapter.FriendsAwaitingConfirmationAdapter;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;
import com.example.engineerdegreeapp.retrofit.UserApi;
import com.example.engineerdegreeapp.retrofit.entity.Expense;
import com.example.engineerdegreeapp.retrofit.entity.Friendship;
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;
import com.example.engineerdegreeapp.util.AccountUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FriendsFragment extends Fragment implements FriendsAdapter.ListItemClickListener,
        FriendsAwaitingConfirmationAdapter.ListItemClickListener{

    private final String FRIENDSHIP_BASE_URL = "https://engineer-degree-project.herokuapp.com/api/users/friendship/";

    private Account mAccount;
    private AccountManager mAccountManager;
    private ArrayList<Friendship> friendships;
    private RecyclerView friendsRecyclerView;
    private RecyclerView waitingFriendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private FriendsAwaitingConfirmationAdapter friendsAwaitingConfirmationAdapter;
    private Button searchButton;
    private EditText searchEditText;
    private String fragmentTitle;
    private ToolbarChangeListener toolbarChangeListener;
    private TextView friendsLoadingErrorTextView;
    private TextView waitingFriendsErrorTextView;
    private Button unfriendButton;
    private ArrayList<Friendship> selectedFriendships = new ArrayList<>();

    public FriendsFragment(){
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
            for(Friendship f : selectedFriendships){
                Toast.makeText(getContext(), f.getFriend().getUsername() + " " + f.getRequester().getUsername(), Toast.LENGTH_SHORT).show();
            }
        });
        searchButton = rootView.findViewById(R.id.friends_fragment_search_button);
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
        loadAllFriends();
        return rootView;
    }

    private void loadAllFriends(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FRIENDSHIP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApi userApi = retrofit.create(UserApi.class);

        String loginCredential = mAccount.name;
        String passwordCredential = mAccountManager.getPassword(mAccount);
        String credentials = loginCredential + ":" + passwordCredential;
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Call<List<Friendship>> call = userApi.getFriends(auth);
        call.enqueue(new Callback<List<Friendship>>() {
            @Override
            public void onResponse(Call<List<Friendship>> call, Response<List<Friendship>> response) {
                if (!response.isSuccessful()) {
                    try {
                        Log.d("loadAllFriends()", response.errorBody().string());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else{
                    friendsLoadingErrorTextView.setVisibility(View.INVISIBLE);
                    friendships = new ArrayList<>(response.body());
                    ArrayList<Friendship> confirmedFriendships = friendships.stream()
                            .filter(f -> f.isAccepted())
                            .collect(Collectors.toCollection(ArrayList::new));
                    ArrayList<Friendship> waitingForConfirmationFriendships = friendships.stream()
                            .filter(f -> !f.isAccepted())
                            .collect(Collectors.toCollection(ArrayList::new));

                    friendsAdapter = new FriendsAdapter(confirmedFriendships,
                            confirmedFriendships.size(),
                            loginCredential,
                            FriendsFragment.this);
                    friendsRecyclerView.setAdapter(friendsAdapter);
                    friendsAwaitingConfirmationAdapter = new FriendsAwaitingConfirmationAdapter(waitingForConfirmationFriendships,
                            waitingForConfirmationFriendships.size(),
                            loginCredential,
                            FriendsFragment.this);
                    waitingFriendsRecyclerView.setAdapter(friendsAwaitingConfirmationAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Friendship>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            toolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException f){
            throw new ClassCastException(context.toString() + "must implement ToolbarChangeListener");

        }
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
                    : getContext().getResources().getColor(R.color.darkCardBackgroundColor, null) );
        } else {
            v.setBackgroundColor(friendship.isSelected() ?
                    getContext().getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                    : getContext().getResources().getColor(R.color.lightCardBackgroundColor, null) );
        }
        if(friendship.isSelected()){
            selectedFriendships.add(friendship);
            checkBox.setVisibility(View.VISIBLE);
        } else{
            selectedFriendships.remove(friendship);
            checkBox.setVisibility(View.INVISIBLE);
        }
        if(!selectedFriendships.isEmpty()){
            unfriendButton.setVisibility(View.VISIBLE);
        } else{
            unfriendButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onListItemClick(View v, Friendship friendship) {
        int clickedItemId = v.getId();
        switch (clickedItemId) {
            case R.id.friends_waiting_confirmation_item_decline_button:
                Toast.makeText(getContext(), "decline", Toast.LENGTH_SHORT).show();
                break;
            case R.id.friends_waiting_confirmation_item_accept_button:
                Toast.makeText(getContext(), "accept", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
