package com.sdcz.endpass.base;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    public int position;
    public T item;

    public RecyclerViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * 当RecyclerViewAdapter调用onBindViewHolder时调用
     * @param position 数组下标
     * @param item 元素
     */
    public void bindViewHolder(int position, T item) {
        this.position = position;
        this.item = item;
        onBindViewHolder(position, item);
    }

    /**
     * 在此处绑定UI
     * @param position 数组下标
     * @param item 元素
     */
    protected abstract void onBindViewHolder(int position, T item);

}