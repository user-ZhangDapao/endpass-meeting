package com.sdcz.endpass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.util.GlideUtils;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/7/9 14:35
 * Description: @
 */
public class TaskUserAadpter extends RecyclerView.Adapter {

    private Context mContext;
    private List<ChannerUser> mData;
    private onClickListener mListener;

    public TaskUserAadpter(Context mContext) {
        this.mContext = mContext;
    }

    public TaskUserAadpter(Context mContext, List<ChannerUser> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    public void setData(List<ChannerUser> mData){
        this.mData = mData;
        notifyDataSetChanged();
    }

    public void setListener(onClickListener mListener){
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_task_user, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
//        GlideUtils.showCircleImage(mContext,viewHolder.ivHead,mData.get(position).get(),R.drawable.icon_head);
        viewHolder.tvName.setText(mData.get(position).getNickName());
        viewHolder.tvDept.setText("(" + mData.get(position).getDeptName() + ")");
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView ivHead;
        ImageView ivDelete;
        TextView tvName;
        TextView tvDept;

        public ViewHolder(View itemView) {
            super(itemView);
            ivHead = itemView.findViewById(R.id.ivHead);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvName);
            tvDept = itemView.findViewById(R.id.tvState);

            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteClick(mData.get(getAdapterPosition()).getUserId()+"");
                }
            });

        }
    }

    public interface onClickListener{
        void onDeleteClick(String userId);
    }

}
