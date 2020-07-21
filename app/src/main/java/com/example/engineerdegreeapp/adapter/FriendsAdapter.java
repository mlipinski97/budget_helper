package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.entity.Friendship;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>{

    private ArrayList<Friendship> friendList;
    private int numberOfItems;
    private String loggedUsername;
    private ListItemClickListener clickListener;

    public FriendsAdapter(ArrayList<Friendship> friendList, int numberOfItems, String loggedUsername, ListItemClickListener clickListener) {
        this.friendList = friendList;
        this.numberOfItems = numberOfItems;
        this.loggedUsername = loggedUsername;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.friends_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;


        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        holder.bind(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }

    public interface ListItemClickListener {
        void onListItemClick(View v, Friendship friendship, CheckBox checkBox);
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView friendUsernameTextView;
        CardView cardView;
        CheckBox selectedCheckbox;

        private Friendship friendship;

        public Friendship getFriendship() {
            return friendship;
        }

        public void setFriendship(Friendship friendship) {
            this.friendship = friendship;
        }

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.friends_item_card_view);
            cardView.setOnClickListener(this);
            friendUsernameTextView = itemView.findViewById(R.id.friends_item_username_text_view);
            selectedCheckbox = itemView.findViewById(R.id.friends_item_done_check_box);
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
            clickListener.onListItemClick(v, getFriendship(), selectedCheckbox);
        }
    }
}
