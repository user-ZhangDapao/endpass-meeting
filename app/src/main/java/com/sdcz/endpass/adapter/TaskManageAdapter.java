package com.sdcz.endpass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.ChannelBean;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/7/8 10:48
 * Description: @
 */
public class TaskManageAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<ChannelBean> mData;
    private ItemListener mListener;

    public TaskManageAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public TaskManageAdapter(Context mContext, List<ChannelBean> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    public void setData(List<ChannelBean> mData){
        this.mData = mData;
        notifyDataSetChanged();
    }

    public void setListener(ItemListener mListener){
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_task_manage,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tvName.setText(mData.get(position).getChannelName());
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvName;
        ImageView ivDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivDelete = itemView.findViewById(R.id.ivDelete);

            if (mListener != null){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.InfoClick(mData.get(getAdapterPosition()).getChannelCode(), mData.get(getAdapterPosition()).getChannelName());
                    }
                });

                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.deleteClick(mData.get(getAdapterPosition()).getChannelCode());
                    }
                });
            }

        }
    }

    public interface ItemListener{
        void deleteClick(String groupId);
        void InfoClick(String groupId,String channelCode);
    }
}
