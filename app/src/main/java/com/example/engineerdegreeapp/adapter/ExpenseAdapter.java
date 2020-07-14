package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.entity.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private ArrayList<Expense> expenseList;
    private int numberOfItems;
    final private ListItemClickListener clickListener;

    public ExpenseAdapter(ArrayList<Expense> expenseList, int numberOfItems, ListItemClickListener clickListener) {
        this.expenseList = expenseList;
        this.numberOfItems = numberOfItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.expense_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        ExpenseViewHolder expenseAdapter = new ExpenseViewHolder(view);
        return expenseAdapter;
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(expenseList.get(position));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }

    public interface ListItemClickListener {
        void onListItemClick(Long clickedExpenseId);
        void onListItemDoneStateChange(Long changedStateExpenseId);
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView expenseNameTextView;
        TextView expenseValueTextView;
        TextView expenseOwnerNameTextView;
        TextView expenseDateOfExpenseTextView;
        CheckBox doneCheckbox;

        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            expenseNameTextView = itemView.findViewById(R.id.expense_item_name_text_view);
            expenseValueTextView = itemView.findViewById(R.id.expense_item_value_text_view);
            expenseOwnerNameTextView = itemView.findViewById(R.id.expense_item_owner_text_view);
            expenseDateOfExpenseTextView = itemView.findViewById(R.id.expense_item_due_date_text_view);
            doneCheckbox = itemView.findViewById(R.id.expense_item_done_check_box);
            itemView.setOnClickListener(this);
            doneCheckbox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    clickListener.onListItemDoneStateChange(id);
                }
            });
        }

        public void bind(Expense expense) {
            expenseNameTextView.setText(expense.getName());
            expenseValueTextView.setText(String.valueOf(expense.getAmount()));
            expenseOwnerNameTextView.setText(expense.getExpenseOwnerName());

            if(expense.getDateOfExpense() != null){
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                expenseDateOfExpenseTextView.setText(sdf.format(expense.getDateOfExpense()).toString());
            }else{
                expenseDateOfExpenseTextView.setText("No due date found(old data)");
            }
            doneCheckbox.setChecked(expense.isDone());
            id = expense.getId();
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(id);
        }
    }
}
