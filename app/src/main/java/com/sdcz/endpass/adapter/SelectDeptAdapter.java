package com.sdcz.endpass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.util.SharedPrefsUtil;

import java.security.KeyStore;
import java.util.List;


public class SelectDeptAdapter extends RecyclerView.Adapter{

    private Context mcontext;
    private List<MailListBean.DeptBean> mData;

    public SelectDeptAdapter(Context mcontext, List<MailListBean.DeptBean> mData) {
        this.mcontext = mcontext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.item_select_dept_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        //展示单个条目数
        MailListBean.DeptBean data = mData.get(position);
        viewHolder.tvName.setText(data.getDeptName());
        if ((data.getDeptId()+"").equals(SharedPrefsUtil.getString(Constants.SharedPreKey.SELECT_DEPT_ID))){
            viewHolder.radioButton.setChecked(true);
        }else {
            viewHolder.radioButton.setChecked(false);
        }


        if (onItemClickListener != null) {
            viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onRadioButtomClick(data.getDeptId()+"",data.getDeptName());

                }
            });
            viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(data.getDeptId()+"",data.getDeptName());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 :mData.size();
    }

    /**
     * 用于缓存控件
     */
    class ViewHolder extends RecyclerView.ViewHolder{
        RadioButton radioButton;
        TextView tvName;
        RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            radioButton = itemView.findViewById(R.id.rb);
            relativeLayout = itemView.findViewById(R.id.rl_maillist_list);

        }
    }

    public interface OnItemClickListener {
        //单选 监听
        void onItemClick(String deptId,String groupName);
        //下一页面监听
        void onRadioButtomClick(String deptId,String groupName);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
