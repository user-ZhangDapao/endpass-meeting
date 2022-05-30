package com.sdcz.endpass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.PosBean;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2022/2/17 15:02
 * Description: @
 */
public class PosAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<PosBean> mData;

    public PosAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<PosBean> mData){
        this.mData = mData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_pos, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.tvPos.setText(mData.get(position).getLon() + "," + mData.get(position).getLat());
        viewHolder.tvTime.setText(mData.get(position).getUploadTime());
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvPos;
        TextView tvTime;
        public ViewHolder(View itemView) {
            super(itemView);
            tvPos = itemView.findViewById(R.id.tvPos);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }


}
