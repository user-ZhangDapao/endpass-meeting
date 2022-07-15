package com.sdcz.endpass.adapter;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.inpor.nativeapi.adaptor.OnlineUserInfo;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.UserEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Author: Administrator
 * CreateDate: 2021/5/17 9:44
 * Description: @
 */
public class MailListAdapter extends BaseQuickAdapter<MailListBean.DeptBean, BaseViewHolder> {

    private onItemClick monItemClick;


    public interface onItemClick{
        void onClick(String deptId,String groupName);
    }


    public MailListAdapter(int layoutResId, @Nullable List<MailListBean.DeptBean> data, onItemClick onItemClick) {
        super(layoutResId, data);
        this.monItemClick = onItemClick;
    }


    public void setOnLinUserList(){
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, MailListBean.DeptBean item) {
        helper.setText(R.id.tv_name,item.getDeptName()+"");
        TextView textView = helper.getView(R.id.tv_class_sum);
        Set<Long> map = SdkUtil.getContactManager().getOnlineDeviceInfo().keySet();

        if (null != item.getMdtUserIds() && null != map){
            int onlineUsers = 0;
            for (Long userId : item.getMdtUserIds()){
                if (map.contains(userId)){
                    onlineUsers++;
                }
            }
            textView.setText("("+onlineUsers + "/" +item.getUserCount()+")");
        }else {
            textView.setText("("+item.getUserCount()+")");
        }

        RelativeLayout relativeLayout = helper.getView(R.id.rl_maillist_list);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monItemClick.onClick(item.getDeptId()+"",item.getDeptName());
            }
        });
    }


}
