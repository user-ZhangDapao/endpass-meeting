package com.sdcz.endpass.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.base.RecyclerViewHolder;

/**
 * @Description: 参会人页面，Tab对应的View pager的adapter
 */
public class AttendeeCategoryAdapter extends RecyclerViewAdapter<Integer> {

    @NonNull
    @Override
    public RecyclerViewHolder<Integer> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recycler_item_attendee_category, parent, false);
        return new AttendeeCategoryViewHolder(view);
    }
}
