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
import com.example.engineerdegreeapp.retrofit.entity.UserAuth;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder>{

    private ArrayList<UserAuth> friendList;
    private int numberOfItems;
    private ListItemClickListener clickListener;
    private OnCreateStateChangeListener onCreateStateChangeListener;
    private AdapterType adapterType;
    private Context context;

    public FriendAdapter(ArrayList<UserAuth> friendList,
                         int numberOfItems,
                         ListItemClickListener clickListener,
                         AdapterType adapterType,
                         Context context,
                         OnCreateStateChangeListener onCreateStateChangeListener) {
        this.friendList = friendList;
        this.numberOfItems = numberOfItems;
        this.clickListener = clickListener;
        this.adapterType = adapterType;
        this.context = context;
        this.onCreateStateChangeListener = onCreateStateChangeListener;
    }


    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.friends_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;


        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(friendList.get(position));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }

    public AdapterType getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(AdapterType adapterType) {
        this.adapterType = adapterType;
    }

    public interface ListItemClickListener {
        void onListItemClick(View v, UserAuth User, CheckBox checkBox, AdapterType adapterType);
    }
    public interface OnCreateStateChangeListener {
        void onCreateStateChanged(View v, CheckBox checkBox, AdapterType adapterType);
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView friendUsernameTextView;
        CardView cardView;
        CheckBox selectedCheckbox;

        private UserAuth user;

        public UserAuth getUser() {
            return user;
        }

        public void setUser(UserAuth user) {
            this.user = user;
        }

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.friends_item_card_view);
            cardView.setOnClickListener(this);
            friendUsernameTextView = itemView.findViewById(R.id.friends_item_username_text_view);
            selectedCheckbox = itemView.findViewById(R.id.friends_item_done_check_box);
        }

        public void bind(UserAuth user){
            if(user.isSelected()){
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.darkCardSelectedBackgroundColor, null));
                } else {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.lightCardBackgroundColor, null));
                }
                selectedCheckbox.setVisibility(View.VISIBLE);
                onCreateStateChangeListener.onCreateStateChanged(cardView, selectedCheckbox, adapterType);
            }

            friendUsernameTextView.setText(user.getUsername());
            setUser(user);
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(v, getUser(), selectedCheckbox, adapterType);
        }
    }

    public enum AdapterType {
        TYPE_NONE(0),
        TYPE_FRIENDS_WITH_PERMISSION(1),
        TYPE_FRIENDS_WITHOUT_PERMISSION(2);

        private Integer value;

        public Integer getValue()
        {
            return this.value;
        }

        AdapterType(Integer value)
        {
            this.value = value;
        }
    }

}
