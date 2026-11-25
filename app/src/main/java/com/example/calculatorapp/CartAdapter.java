package com.example.calculatorapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemActionListener {
        void onDeleteItem(CartItem item);
        void onEditItem(CartItem item);
    }

    private List<CartItem> cartList;
    private final OnCartItemActionListener actionListener;

    public CartAdapter(List<CartItem> cartList, OnCartItemActionListener actionListener) {
        this.cartList = cartList;
        this.actionListener = actionListener;
    }

    public void updateList(List<CartItem> newList) {
        this.cartList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        if (item == null) return;

        holder.tvName.setText(item.getProductName());
        holder.tvPrice.setText(item.getPrice());
        holder.imgProduct.setImageResource(getThumbnailResource(item.getProductName()));

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteItem(item);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditItem(item);
            }
        });
    }

    private int getThumbnailResource(String productName) {
        if (productName == null) return R.drawable.ic_iphone;
        String lower = productName.toLowerCase();
        if (lower.contains("iphone")) return R.drawable.ic_iphone;
        if (lower.contains("samsung")) return R.drawable.ic_samsung;
        if (lower.contains("xiaomi")) return R.drawable.ic_xiaomi;
        if (lower.contains("macbook")) return R.drawable.ic_macbook;
        if (lower.contains("ipad")) return R.drawable.ic_ipad;
        if (lower.contains("airpods")) return R.drawable.ic_airpods;
        return R.drawable.ic_iphone;
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView imgProduct, btnEdit, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            imgProduct = itemView.findViewById(R.id.imgCartItem);
            btnEdit = itemView.findViewById(R.id.btnEditCartItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteCartItem);
        }
    }
}