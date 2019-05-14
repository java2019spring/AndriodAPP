package com.example.javaproject;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

import static java.security.AccessController.getContext;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder> {
    private Context mContext;
    private List<Dish> mDishList;

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        ImageView dishImage;
        TextView dishName;

        public ViewHolder(View view)
        {
            super(view);
            cardView = (CardView) view;
            dishImage = (ImageView) view.findViewById(R.id.dish_image);
            dishName = (TextView) view.findViewById(R.id.dish_name);
        }
    }
    public DishAdapter(List<Dish>dishList)
    {
        mDishList = dishList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (mContext == null)
            mContext = parent.getContext();

        View view = LayoutInflater.from(mContext).inflate(R.layout.dish_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Dish dish = mDishList.get(position);
        holder.dishName.setText(dish.getName());
        Glide.with(mContext).load(dish.getImageId()).into(holder.dishImage);
    }
    @Override
    public int getItemCount()
    {
        return mDishList.size();
    }
}
