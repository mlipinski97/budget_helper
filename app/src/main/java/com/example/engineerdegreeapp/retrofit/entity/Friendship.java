package com.example.engineerdegreeapp.retrofit.entity;

import com.google.gson.annotations.SerializedName;

public class Friendship {

    private UserAuth requester;

    private UserAuth friend;

    private boolean accepted;

    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public UserAuth getRequester() {
        return requester;
    }

    public void setRequester(UserAuth requester) {
        this.requester = requester;
    }

    public UserAuth getFriend() {
        return friend;
    }

    public void setFriend(UserAuth friend) {
        this.friend = friend;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
