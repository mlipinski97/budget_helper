package com.example.engineerdegreeapp.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.engineerdegreeapp.R;

public class SortByDialogFragment extends DialogFragment {

    public SortByDialogFragment(onDialogItemClickedListener onDialogItemClickedListener){
        this.onDialogItemClickedListener = onDialogItemClickedListener;
    }

    private onDialogItemClickedListener onDialogItemClickedListener;

    public interface onDialogItemClickedListener {
        void onDialogItemClick(int which);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.friend_fragment_title)
                .setItems(R.array.sort_by_values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDialogItemClickedListener.onDialogItemClick(which);
                    }
                });

        return builder.create();
    }

}