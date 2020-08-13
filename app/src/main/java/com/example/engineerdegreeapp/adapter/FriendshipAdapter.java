package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.entity.Friendship;

import java.util.ArrayList;

public class FriendshipAdapter extends RecyclerView.Adapter<FriendshipAdapter.FriendshipViewHolder> {

    private ArrayList<Friendship> friendShipList;
    private int numberOfItems;
    private String loggedUsername;
    private OnCreateStateChangeListener onCreateStateChangeListener;
    private ListItemClickListener clickListener;
    private Context context;

    public FriendshipAdapter(ArrayList<Friendship> friendShipList,
                             int numberOfItems,
                             String loggedUsername,
                             ListItemClickListener clickListener,
                             Context context,
                             OnCreateStateChangeListener onCreateStateChangeListener) {
        this.friendShipList = friendShipList;
        this.numberOfItems = numberOfItems;
        this.loggedUsername = loggedUsername;
        this.clickListener = clickListener;
        this.context = context;
        this.onCreateStateChangeListener = onCreateStateChangeListener;
    }

    @NonNull
    @Override
    public FriendshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.friends_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;


        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        return new FriendshipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendshipViewHolder holder, int position) {
        holder.bind(friendShipList.get(position));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }

    public interface ListItemClickListener {
        void onListItemClick(View v, Friendship friendship, CheckBox checkBox);
    }

    public interface OnCreateStateChangeListener {
        void onCreateStateChanged(View v, CheckBox checkBox);
    }

    class FriendshipViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

        public FriendshipViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.friends_item_card_view);
            cardView.setOnClickListener(this);
            friendUsernameTextView = itemView.findViewById(R.id.friends_item_username_text_view);
            selectedCheckbox = itemView.findViewById(R.id.friends_item_done_check_box);
        }

        public void bind(Friendship friendship) {
            if (friendship.isSelected()) {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.darkCardSelectedBackgroundColor, null));
                } else {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.lightCardBackgroundColor, null));
                }
                selectedCheckbox.setVisibility(View.VISIBLE);
                onCreateStateChangeListener.onCreateStateChanged(cardView, selectedCheckbox);
            }
            if (friendship.getFriend().getUsername().equals(loggedUsername)) {
                friendUsernameTextView.setText(friendship.getRequester().getUsername());
            } else {
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
