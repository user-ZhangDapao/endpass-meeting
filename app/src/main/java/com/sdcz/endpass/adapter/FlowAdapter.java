package com.sdcz.endpass.adapter;

import static com.sdcz.endpass.DemoApp.getContext;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.widget.FlowLayout;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/7/8 14:49
 * Description: @
 */
public class FlowAdapter extends FlowLayout.Adapter<FlowAdapter.FlowViewHolder> {

    private Context mContext;
    private List<UserEntity> mContentList;

    public FlowAdapter(Context mContext, List<UserEntity> mContentList) {
        this.mContext = mContext;
        this.mContentList = mContentList;
    }

    public void setData(List<UserEntity> mData){
        this.mContentList = mData;
    }

    @Override
    public FlowViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_flow, parent, false);
        // 给 View 设置 margin

        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(view.getLayoutParams());
        int margin = dip2Px(5);
        mlp.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(mlp);

        return new FlowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlowViewHolder holder, int position) {
        holder.content.setText(mContentList.get(position).getNickName());
    }

    @Override
    public int getItemCount() {
        return mContentList.size();
    }

    class FlowViewHolder extends FlowLayout.ViewHolder {
        TextView content;

        public FlowViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.tv_test_content);
        }
    }

    /**
     * dip-->px
     */
    public static int dip2Px(int dip) {
        /*
        1.  px/(ppi/160) = dp
        2.  px/dp = density
         */
        //取得当前手机px和dp的倍数关系
        float density = getResources().getDisplayMetrics().density;
        int px = (int) (dip * density + .5f);
        return px;
    }

    /**
     * 得到Resource对象
     */
    public static Resources getResources() {
        if (getContext() == null) {
            return Resources.getSystem();
        }
        return getContext().getResources();
    }
}

