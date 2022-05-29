package com.sdcz.endpass.callback;

import android.view.View;

import com.sdcz.endpass.base.RecyclerViewAdapter;


/**
 * @Description: 定义RecyclerView的Item点击事件
 */
public interface OnItemClickListener {
    /**
     * RecyclerView的Item点击事件
     *
     * @param viewAdapter adapter
     * @param position position
     * @param itemView itemView
     */
    <T> void onItemClick(RecyclerViewAdapter<T> viewAdapter, int position, View itemView);
}
