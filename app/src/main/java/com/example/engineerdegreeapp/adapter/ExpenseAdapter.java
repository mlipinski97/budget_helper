package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.activity.MainActivity;
import com.example.engineerdegreeapp.retrofit.entity.Expense;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>{


    Context context;
    private ArrayList<Expense> expenseList;
    private int numberOfItems;
    final private ListItemClickListener clickListener;

    public ExpenseAdapter(ArrayList<Expense> expenseList, int numberOfItems, ListItemClickListener clickListener, Context context) {
        this.expenseList = expenseList;
        this.numberOfItems = numberOfItems;
        this.clickListener = clickListener;
        this.context = context;
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
        void onListItemLongClick(View v, Expense expense);
        void onListItemDoneStateChange(Long changedStateExpenseId);

    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        TextView expenseNameTextView;
        TextView expenseValueTextView;
        TextView expenseOwnerNameTextView;
        TextView expenseDateOfExpenseTextView;
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
            expenseNameTextView = itemView.findViewById(R.id.expense_item_name_text_view);
            expenseValueTextView = itemView.findViewById(R.id.expense_item_value_text_view);
            expenseOwnerNameTextView = itemView.findViewById(R.id.expense_item_owner_text_view);
            expenseDateOfExpenseTextView = itemView.findViewById(R.id.expense_item_due_date_text_view);
            doneCheckbox = itemView.findViewById(R.id.expense_item_done_check_box);
            cardView.setOnLongClickListener(this);
            doneCheckbox.setOnClickListener(v -> clickListener.onListItemDoneStateChange(expense.getId()));
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
            this.expense = expense;

        }


        @Override
        public boolean onLongClick(View v) {
            clickListener.onListItemLongClick(v, expense);
            return true;
        }

    }
}

/*
{
            @Override
            public void onClick(View view) {
                expenseList.get(position).setSelected(!expenseList.get(position).isSelected());
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    view.setBackgroundColor(expenseList.get(position).isSelected() ?
                            context.getResources().getColor(R.color.darkCardSelectedBackgroundColor, null)
                            : context.getResources().getColor(R.color.darkCardBackgroundColor, null) );
                } else {
                    view.setBackgroundColor(expenseList.get(position).isSelected() ?
                            context.getResources().getColor(R.color.lightCardSelectedBackgroundColor, null)
                            : context.getResources().getColor(R.color.lightCardBackgroundColor, null) );
                }


                System.out.println("zaznaczenie?");
            }
        });
 */