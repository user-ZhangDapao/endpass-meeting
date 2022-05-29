package com.sdcz.endpass.base;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.sdcz.endpass.callback.OnItemClickListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class RecyclerViewAdapter<T>
        extends RecyclerView.Adapter<RecyclerViewHolder<T>> {

    protected final List<T> data = new ArrayList<>();
    protected OnItemClickListener onItemClickListener;

    @Override
    public final void onBindViewHolder(@NonNull RecyclerViewHolder<T> holder, int position) {
        T item = getItem(position);
        holder.bindViewHolder(position, item);
    }

    @Override
    public int getItemCount() {
        // 默认实现返回data的大小，需要修改的可以重写
        return this.data == null ? 0 : this.data.size();
    }

    /**
     * 设置元素的点击事件
     * @param listener OnItemClickListener
     */
    public final void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    /**
     * 获取所有元素
     * @return 所有元素
     */
    public final List<T> getData() {
        return data;
    }

    /**
     * 获取一个元素
     * @param position 数组下标
     * @return 一个元素
     */
    public final T getItem(int position) {
        if (data == null || position < 0 || position >= data.size()) {
            return null;
        } else {
            return data.get(position);
        }
    }

    /**
     * 在末端添加一个元素
     * @param item 一个元素
     */
    public final void add(T item) {
        if (!data.contains(item)) {
            data.add(item);
        }
    }

    /**
     * 插入一个元素
     * @param index 插入的位置
     * @param item 一个元素
     */
    public final void add(int index, T item) {
        if (!data.contains(item)) {
            data.add(index, item);
        }
    }

    /**
     * 在末端添加一组元素
     * @param items 一组元素
     */
    public final void addAll(List<T> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        data.addAll(items);
    }

    /**
     * 在末端添加一组元素
     * @param items 一组元素
     */
    public final void addAll(Collection<T> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        data.addAll(items);
    }

    /**
     * 插入一组元素
     * @param index 插入的位置
     * @param items 一组元素
     */
    public final void addAll(int index, List<T> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        data.addAll(index, items);
    }

    /**
     * 移除某个元素
     * @param item 要移除的元素
     */
    public final void remove(T item) {
        if (item == null) {
            return;
        }
        data.remove(item);
    }

    /**
     * 移除列表中的部分元素
     * @param items 要移除的元素
     */
    public final void removeAll(List<T> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        data.removeAll(items);
    }

    /**
     * 清空列表里的所有数据
     */
    public final void clear() {
        data.clear();
    }

    /**
     * 列表是否存在存入的元素
     * @param item 元素
     * @return true 存在
     */
    public final boolean contains(T item) {
        return data.contains(item);
    }

}
