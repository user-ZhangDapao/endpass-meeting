package com.sdcz.endpass.adapter;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.MailListBean;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/5/17 9:44
 * Description: @
 */
public class MailListAdapter extends BaseQuickAdapter<MailListBean.DeptBean, BaseViewHolder> {

    private onItemClick monItemClick;

//    private List<FspUserInfo> mOnLinUserList;

    public interface onItemClick{
        void onClick(String deptId,String groupName);
    }


    public MailListAdapter(int layoutResId, @Nullable List<MailListBean.DeptBean> data, onItemClick onItemClick) {
        super(layoutResId, data);
        this.monItemClick = onItemClick;
    }

//    public void setOnLinUserList(List<FspUserInfo> onLinUserList){
//        this.mOnLinUserList = onLinUserList;
//        notifyDataSetChanged();
//    }

    public void setOnLinUserList(){
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, MailListBean.DeptBean item) {
        helper.setText(R.id.tv_name,item.getDeptName()+"");
        TextView textView = helper.getView(R.id.tv_class_sum);
//        if (null != mOnLinUserList){
//            int onlineUsers = 0;
//            for (int j = 0; j < mOnLinUserList.size(); j++) {
//                String userId = mOnLinUserList.get(j).getUserId() + "";
//                for (int i = 0; i < item.getList().size(); i++) {
//                    String hstUser = item.getList().get(i).getUserId();
//                    if (hstUser.equals(userId)) {
//                        onlineUsers++;
//                    }
//                }
//            }
//            textView.setText("("+onlineUsers + "/" +item.getTotal()+")");
//        }else {
            textView.setText("("+item.getUserCount()+")");
//        }

        RelativeLayout relativeLayout = helper.getView(R.id.rl_maillist_list);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monItemClick.onClick(item.getDeptId()+"",item.getDeptName());
            }
        });
    }


}
