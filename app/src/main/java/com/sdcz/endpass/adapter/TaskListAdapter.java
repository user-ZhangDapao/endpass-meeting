package com.sdcz.endpass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.ChannelBean;

import java.util.List;


/**
 * Author: Administrator
 * CreateDate: 2021/7/5 13:57
 * Description: @
 */
public class TaskListAdapter extends RecyclerView.Adapter {
    private Context context;
    private OnItemClickListener onItemClickListener;
    private List<ChannelBean> mData;

    public TaskListAdapter(Context context) {
        this.context = context;
    }

    public TaskListAdapter(Context context, List<ChannelBean> mData) {
        this.context = context;
        this.mData = mData;
    }

    public void setData(List<ChannelBean> data) {
        this.mData = data;
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tvTask.setText(mData.get(position).getChannelName());
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 :mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivJoin;
        TextView tvTask;
        RelativeLayout itemRoot;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTask = itemView.findViewById(R.id.tv_name);
            ivJoin = itemView.findViewById(R.id.img_join);
            itemRoot = itemView.findViewById(R.id.itemRoot);
            itemRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        if (getAdapterPosition() > mData.size()) return;
                        onItemClickListener.onSelectedItem(mData.get(getAdapterPosition()));
                    }
//                    notifyDataSetChanged();
                }
            });
            ivJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        if (getAdapterPosition() > mData.size()) return;
                        onItemClickListener.onJoinItem(mData.get(getAdapterPosition()).getChannelCode(), mData.get(getAdapterPosition()).getRoomId());
                    }
//                    notifyDataSetChanged();
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onSelectedItem(ChannelBean data);
        void onJoinItem(String Code, long roomId);
    }

    /**
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }


}
