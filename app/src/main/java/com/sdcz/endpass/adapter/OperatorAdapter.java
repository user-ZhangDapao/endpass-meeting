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
import com.sdcz.endpass.bean.OperatorItem;

/**
 * @Description:    管理员控制参会人的adapter
 */
public class OperatorAdapter extends RecyclerViewAdapter<OperatorItem> {

    @NonNull
    @Override
    public RecyclerViewHolder<OperatorItem> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_attendee_operation, parent, false);
        OperatorViewHolder holder = new OperatorViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                int pos = holder.getLayoutPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                onItemClickListener.onItemClick(OperatorAdapter.this, pos, holder.itemView);
            }
        });
        return holder;
    }

    /**
     * 根据类型返回数据
     * @param type OperatorItem.ItemType
     * @return 如果没有对应类型的操作，则返回null
     */
    @NonNull
    public OperatorItem getOperatorItem(@OperatorItem.ItemType int type) {
        for (OperatorItem item : data) {
            if (item.getType() == type) {
                return item;
            }
        }
        return null;
    }

    public int getPosition(OperatorItem operatorItem) {
        int index = 0;
        for (OperatorItem item : data) {
            if (item.equals(operatorItem)) {
                return index;
            }
            index++;
        }
        return index;
    }

    private static class OperatorViewHolder extends RecyclerViewHolder<OperatorItem> {

        private final ImageView imgOperationLogo;
        private final TextView tvDesc;

        public OperatorViewHolder(View itemView) {
            super(itemView);
            imgOperationLogo = itemView.findViewById(R.id.imgOperationLogo);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }

        @Override
        protected void onBindViewHolder(int position, OperatorItem item) {
            imgOperationLogo.setImageResource(item.getLogo());
            tvDesc.setText(item.getDesc());
        }
    }
}
