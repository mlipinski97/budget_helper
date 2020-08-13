package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.example.engineerdegreeapp.util.DateUtils.dd_mm_yyy_sdf;

public class BudgetListAdapter extends RecyclerView.Adapter<BudgetListAdapter.BudgetListViewHolder> {

    private ArrayList<BudgetList> budgetLists;
    private int numberOfItems;
    final private ListItemClickListener clickListener;
    private Context context;

    public BudgetListAdapter(ArrayList<BudgetList> budgetLists, int numberOfItems, ListItemClickListener clickListener, Context context) {
        this.budgetLists = budgetLists;
        this.numberOfItems = numberOfItems;
        this.clickListener = clickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public BudgetListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.budget_list_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;


        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        BudgetListViewHolder budgetListAdapter = new BudgetListViewHolder(view);
        return budgetListAdapter;
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetListViewHolder holder, int position) {
        holder.bind(budgetLists.get(position));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }

    public interface ListItemClickListener {
        void onListItemClick(View v, BudgetList budgetList);

        void onListItemLongClick(View v, BudgetList budgetList);
    }

    class BudgetListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        TextView budgetListName;
        TextView budgetListValue;
        TextView budgetListRemainingValue;
        TextView budgetListDueDate;
        CardView cardView;

        private BudgetList budgetList;

        public BudgetList getBudgetList() {
            return budgetList;
        }

        public void setBudgetList(BudgetList budgetList) {
            this.budgetList = budgetList;
        }


        public BudgetListViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.budget_list_item_card_view);
            cardView.setOnLongClickListener(this);
            budgetListName = itemView.findViewById(R.id.budget_list_name_text_view);
            budgetListValue = itemView.findViewById(R.id.budget_list_value_text_view);
            budgetListRemainingValue = itemView.findViewById(R.id.budget_list_remaining_value_text_view);
            budgetListDueDate = itemView.findViewById(R.id.budget_list_due_date_text_view);
            cardView.setOnClickListener(this);
        }

        public void bind(BudgetList budgetList) {
            if (budgetList.isSelected()) {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.darkCardSelectedBackgroundColor, null));
                } else {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.lightCardBackgroundColor, null));
                }
            }

            budgetListName.setText(budgetList.getName());
            String budgetListValueString = context.getResources().getString(R.string.budget_list_value_text) + " " + budgetList.getValue() + budgetList.getCurrencyCode();
            budgetListValue.setText(budgetListValueString);
            String budgetListRemainingValueString = context.getResources().getString(R.string.budget_list_remaining_value_text) + " " + budgetList.getRemainingValue() + budgetList.getCurrencyCode();
            budgetListRemainingValue.setText(budgetListRemainingValueString);
            if (budgetList.getDueDate() != null) {
                budgetListDueDate.setText(dd_mm_yyy_sdf.format(budgetList.getDueDate()).toString());
            } else {
                budgetListDueDate.setText("No due date found(old data)");
            }

            setBudgetList(budgetList);
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(v, getBudgetList());
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onListItemLongClick(v, getBudgetList());
            return true;
        }
    }
}
