package com.example.engineerdegreeapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.communication.ToolbarChangeListener;


public class ShareBudgetListFragment extends Fragment implements View.OnClickListener {

    private OnFragmentClickListener mClickListener;
    ToolbarChangeListener toolbarChangeListener;

    public ShareBudgetListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share_budget_list, container, false);

        toolbarChangeListener.changeToolbarTitle(getResources().getString(R.string.share_budget_list_toolbar_name));
        toolbarChangeListener.hideEditButtons();

        return rootView;
    }

    public interface OnFragmentClickListener{
        void onFragmentClickInteraction(int clickedElementId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mClickListener = (ShareBudgetListFragment.OnFragmentClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement ShareBudgetListFragment.OnFragmentClickListener");
        }
        try{
            toolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException f){
            throw new ClassCastException(context.toString() + " must implement ToolbarChangeListener");

        }
    }

    @Override
    public void onClick(View v) {

    }


}