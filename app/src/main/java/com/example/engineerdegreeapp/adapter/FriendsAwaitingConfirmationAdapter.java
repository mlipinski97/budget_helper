package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.entity.Friendship;

import java.util.ArrayList;

public class FriendsAwaitingConfirmationAdapter extends RecyclerView.Adapter<FriendsAwaitingConfirmationAdapter.FriendsAwaitingConfirmationViewHolder> {

    private ArrayList<Friendship> friendList;
    private int numberOfItems;
    private String loggedUsername;
    ListItemClickListener clickListener;

    public FriendsAwaitingConfirmationAdapter(ArrayList<Friendship> friendList,
                                              int numberOfItems,
                                              String loggedUsername,
                                              ListItemClickListener clickListener) {
        this.friendList = friendList;
        this.numberOfItems = numberOfItems;
        this.loggedUsername = loggedUsername;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public FriendsAwaitingConfirmationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.friends_waiting_confirmation_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;


        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        return new FriendsAwaitingConfirmationAdapter.FriendsAwaitingConfirmationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAwaitingConfirmationViewHolder holder, int position) {
        holder.bind(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }


    public interface ListItemClickListener {
        void onListItemClick(View v, Friendship friendship);
    }

    class FriendsAwaitingConfirmationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView friendUsernameTextView;
        CardView cardView;
        Button declineButton;
        Button acceptButton;

        private Friendship friendship;

        public Friendship getFriendship() {
            return friendship;
        }

        public void setFriendship(Friendship friendship) {
            this.friendship = friendship;
        }

        public FriendsAwaitingConfirmationViewHolder(@NonNull View itemView) {
            super(itemView);
            friendUsernameTextView = itemView.findViewById(R.id.friends_waiting_confirmation_item_username_text_view);
            cardView = itemView.findViewById(R.id.friends_waiting_confirmation_item_card_view);
            declineButton = itemView.findViewById(R.id.friends_waiting_confirmation_item_decline_button);
            acceptButton = itemView.findViewById(R.id.friends_waiting_confirmation_item_accept_button);
            declineButton.setOnClickListener(this);
            acceptButton.setOnClickListener(this);
        }

        public void bind(Friendship friendship){
            if(friendship.getFriend().getUsername().equals(loggedUsername)){
                friendUsernameTextView.setText(friendship.getRequester().getUsername());
            } else{
                friendUsernameTextView.setText(friendship.getFriend().getUsername());
            }
            setFriendship(friendship);
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(v, getFriendship());
        }
    }
}
