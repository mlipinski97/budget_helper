package com.example.engineerdegreeapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineerdegreeapp.R;
import com.example.engineerdegreeapp.retrofit.entity.Category;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private ArrayList<Category> categoryList;
    private int numberOfItems;
    final private ListItemClickListener clickListener;

    public CategoryAdapter(Context context, ArrayList<Category> categoryList, int numberOfItems, ListItemClickListener clickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.numberOfItems = numberOfItems;
        this.clickListener = clickListener;
    }

    public interface ListItemClickListener {
        void onListItemClick(View v, Category category);

        void onListItemLongClick(View v, Category category);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        int listItemId = R.layout.category_adapter_item;
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(listItemId, parent, shouldAttachToParentImmediately);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return numberOfItems;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,
            View.OnClickListener {

        TextView categoryNameTextView;
        ImageView categoryImageView;
        CardView cardView;

        private Category category;

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.category_item_name_text_view);
            categoryImageView = itemView.findViewById(R.id.category_item_image_view);
            cardView = itemView.findViewById(R.id.category_item_card_view);
            cardView.setOnLongClickListener(this);
            cardView.setOnClickListener(this);
        }


        public void bind(Category category) {
            categoryNameTextView.setText(category.getCategoryName());
            if (category.getCategoryImage() != null) {
                byte[] decodedString = Base64.decode(category.getCategoryImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                categoryImageView.setImageBitmap(decodedByte);
            } else {
                categoryImageView.setImageResource(R.drawable.question_mark);
            }
            if (category.isSelected()) {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.darkCardSelectedBackgroundColor, null));
                } else {
                    cardView.setBackgroundColor(context.getResources().getColor(R.color.lightCardBackgroundColor, null));
                }
            }
            setCategory(category);
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(v, getCategory());
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onListItemLongClick(v, getCategory());
            return true;
        }
    }
}
