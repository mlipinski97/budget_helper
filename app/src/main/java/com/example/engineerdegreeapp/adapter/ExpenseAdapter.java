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
import com.example.engineerdegreeapp.retrofit.entity.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {


    Context context;
    private ArrayList<Expense> expenseList;
    private int numberOfItems;
    final private ListItemClickListener clickListener;
    private String currencyCode;

    public ExpenseAdapter(ArrayList<Expense> expenseList, int numberOfItems, ListItemClickListener clickListener, Context context, String currencyCode) {
        this.expenseList = expenseList;
        this.numberOfItems = numberOfItems;
        this.clickListener = clickListener;
        this.context = context;
        this.currencyCode = currencyCode;
    }


    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.expense_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        return new ExpenseViewHolder(view);
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
        void onListItemClick(View v, Expense expense);

        void onListItemLongClick(View v, Expense expense);

        void onListItemDoneStateChange(Long changedStateExpenseId);
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,
            View.OnClickListener {

        TextView expenseNameTextView;
        TextView expenseValueTextView;
        TextView expenseOwnerNameTextView;
        TextView expenseDateOfExpenseTextView;
        TextView expenseCategoryTextView;
        CheckBox doneCheckbox;
        CardView cardView;


        private Expense expense;

        public Expense getExpense() {
            return expense;
        }

        public void setExpense(Expense expense) {
            this.expense = expense;
        }


        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.expense_item_card_view);
            cardView.setOnLongClickListener(this);
            cardView.setOnClickListener(this);
            expenseCategoryTextView = itemView.findViewById(R.id.expense_item_category_text_view);
            expenseNameTextView = itemView.findViewById(R.id.expense_item_name_text_view);
            expenseValueTextView = itemView.findViewById(R.id.expense_item_value_text_view);
            expenseOwnerNameTextView = itemView.findViewById(R.id.expense_item_owner_text_view);
            expenseDateOfExpenseTextView = itemView.findViewById(R.id.expense_item_due_date_text_view);
            doneCheckbox = itemView.findViewById(R.id.expense_item_done_check_box);
            doneCheckbox.setOnClickListener(v -> clickListener.onListItemDoneStateChange(expense.getId()));
        }

        public void bind(Expense expense) {
            expenseNameTextView.setText(expense.getName());
            String expenseValueString = String.valueOf(expense.getAmount()) + currencyCode;
            expenseValueTextView.setText(expenseValueString);
            expenseOwnerNameTextView.setText(expense.getExpenseOwnerName());
            expenseCategoryTextView.setText(expense.getCategory().getCategoryName());
            if (expense.isSelected()) {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.darkCardSelectedBackgroundColor, null));
                } else {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.lightCardBackgroundColor, null));
                }
            }
            if (expense.getDateOfExpense() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                expenseDateOfExpenseTextView.setText(sdf.format(expense.getDateOfExpense()).toString());
            } else {
                expenseDateOfExpenseTextView.setText("No due date found(old data)");
            }
            doneCheckbox.setChecked(expense.isDone());
            setExpense(expense);
        }


        @Override
        public boolean onLongClick(View v) {
            clickListener.onListItemLongClick(v, getExpense());
            return true;
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(v, getExpense());
        }
    }
}