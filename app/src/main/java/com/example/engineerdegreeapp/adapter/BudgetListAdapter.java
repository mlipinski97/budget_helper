package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.entity.BudgetList;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BudgetListAdapter extends RecyclerView.Adapter<BudgetListAdapter.BudgetListViewHolder> {

    private ArrayList<BudgetList> budgetLists;
    private int numberOfItems;
    final private ListItemClickListener clickListener;

    public BudgetListAdapter(ArrayList<BudgetList> budgetLists, int numberOfItems, ListItemClickListener clickListener) {
        this.budgetLists = budgetLists;
        this.numberOfItems = numberOfItems;
        this.clickListener = clickListener;
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

    public interface ListItemClickListener{
        void onListItemClick(int clickedBudgetListId, String clickedBudgetListName, String listDueDate);
    }

    class BudgetListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView budgetListName;
        TextView budgetListValue;
        TextView budgetListRemainingValue;
        TextView budgetListDueDate;

        public Integer getBudgetListId() {
            return budgetListId;
        }

        public void setBudgetListId(Integer budgetListId) {
            this.budgetListId = budgetListId;
        }

        private Integer budgetListId;

        public BudgetListViewHolder(@NonNull View itemView) {
            super(itemView);
            budgetListName = (TextView) itemView.findViewById(R.id.budget_list_name_text_view);
            budgetListValue = (TextView) itemView.findViewById(R.id.budget_list_value_text_view);
            budgetListRemainingValue = (TextView) itemView.findViewById(R.id.budget_list_remaining_value_text_view);
            budgetListDueDate = (TextView) itemView.findViewById(R.id.budget_list_due_date_text_view);
            itemView.setOnClickListener(this);
        }

        public void bind(BudgetList budgetList){
            budgetListName.setText(budgetList.getName());
            budgetListValue.setText(String.valueOf(budgetList.getValue()));
            budgetListRemainingValue.setText(String.valueOf(budgetList.getRemainingValue()));
            if(budgetList.getDueDate() != null){
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                budgetListDueDate.setText(sdf.format(budgetList.getDueDate()).toString());
            }else{
                budgetListDueDate.setText("No due date found(old data)");
            }

            budgetListId = budgetList.getId();
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(budgetListId, budgetListName.getText().toString(), budgetListDueDate.getText().toString());
        }
    }
}
