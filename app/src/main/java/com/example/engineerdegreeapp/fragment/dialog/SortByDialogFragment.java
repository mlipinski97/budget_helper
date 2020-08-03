package com.example.engineerdegreeapp.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


public class SortByDialogFragment extends DialogFragment {

    public SortByDialogFragment(onDialogItemClickedListener onDialogItemClickedListener, String title, String[] itemList) {
        this.onDialogItemClickedListener = onDialogItemClickedListener;
        this.title = title;
        this.itemList = itemList;
    }

    private String title;
    private onDialogItemClickedListener onDialogItemClickedListener;
    private String[] itemList;

    public interface onDialogItemClickedListener {
        void onDialogItemClick(int which);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setItems(itemList , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDialogItemClickedListener.onDialogItemClick(which);
                    }
                });

        return builder.create();
    }

}