package com.sdcz.endpass.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.base.RecyclerViewHolder;
import com.sdcz.endpass.bean.LayoutItem;

/**
 * @Description: 切换布局UI的Adapter
 */
public class LayoutPickerAdapter extends RecyclerViewAdapter<LayoutItem> {

    @NonNull
    @Override
    public RecyclerViewHolder<LayoutItem> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_item_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                int pos = holder.getLayoutPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                onItemClickListener.onItemClick(LayoutPickerAdapter.this,pos, holder.itemView);
            }
        });
        return holder;
    }

    /**
     * 设置Item选中
     * @param position 选中Item
     */
    public void setSelectItem(int position) {
        if (position < 0 || position >= getData().size()) {
            return;
        }
        int size = data.size();
        for (int index = 0; index < size; index++) {
            data.get(index).setSelected(index == position);
        }
    }

    public static class ViewHolder extends RecyclerViewHolder<LayoutItem> {
        private final ImageView imgLayoutLogo;
        private final ImageView imgLayoutLogoMask;
        private final TextView tvLayoutName;

        /**
         * 构造函数
         */
        public ViewHolder(View itemView) {
            super(itemView);
            imgLayoutLogo = itemView.findViewById(R.id.imgLayoutLogo);
            imgLayoutLogoMask = itemView.findViewById(R.id.imgLayoutLogoMask);
            tvLayoutName = itemView.findViewById(R.id.tvLayoutName);
        }

        @Override
        protected void onBindViewHolder(int position, LayoutItem item) {
            imgLayoutLogo.setImageResource(item.getLayoutLogoResId());
            tvLayoutName.setText(item.getLayoutDescResId());
            imgLayoutLogoMask.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
        }
    }
}
