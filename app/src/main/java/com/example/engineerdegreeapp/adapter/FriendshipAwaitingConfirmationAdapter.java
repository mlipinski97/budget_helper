package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.entity.Friendship;

import java.util.ArrayList;

public class FriendshipAwaitingConfirmationAdapter extends RecyclerView.Adapter<FriendshipAwaitingConfirmationAdapter.FriendshipAwaitingConfirmationViewHolder> {

    private ArrayList<Friendship> friendList;
    private int numberOfItems;
    private String loggedUsername;
    ListItemClickListener clickListener;

    public FriendshipAwaitingConfirmationAdapter(ArrayList<Friendship> friendList,
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
    public FriendshipAwaitingConfirmationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.friends_waiting_confirmation_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;


        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        return new FriendshipAwaitingConfirmationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendshipAwaitingConfirmationViewHolder holder, int position) {
        holder.bind(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }


    public interface ListItemClickListener {
        void onListItemClick(View v, Friendship friendship);
    }

    class FriendshipAwaitingConfirmationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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

        public FriendshipAwaitingConfirmationViewHolder(@NonNull View itemView) {
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
                acceptButton.setEnabled(false);
                acceptButton.setText(R.string.friend_request_sent);
                acceptButton.setBackgroundTintList(acceptButton.getContext().getResources().getColorStateList(R.color.disabled_button, null));
                acceptButton.setTextColor(acceptButton.getContext().getResources().getColor(R.color.darkCardBackgroundColor, null));
            }
            setFriendship(friendship);
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(v, getFriendship());
        }
    }
}
