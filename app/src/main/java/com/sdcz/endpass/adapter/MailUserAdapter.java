package com.sdcz.endpass.adapter;

import static com.inpor.nativeapi.adaptor.RoomUserInfo.USER_OFFLINE;
import static com.inpor.nativeapi.adaptor.RoomUserInfo.USER_ONLINE;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.util.GlideUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/7/2 16:51
 * Description: @
 */
public class MailUserAdapter extends BaseQuickAdapter<UserEntity, BaseViewHolder> {

    private onItemClick onItemClick;

    public interface onItemClick{
        void onClick(UserEntity item);
    }

    public MailUserAdapter(int layoutResId, @Nullable List<UserEntity> data, onItemClick onclick) {
        super(layoutResId, data);
        this.onItemClick = onclick;
    }

    @Override
    protected void convert(BaseViewHolder helper, UserEntity item) {
        String state = item.getChannelName() == null ? "(无任务)" : "(" + item.getChannelName() + "中)";
        RelativeLayout rlMaillistUser = helper.getView(R.id.rl_maillist_user);
        helper.setText(R.id.tv_name,item.getNickName());
        helper.setText(R.id.tvState,state);
        ImageView ivOnline = helper.getView(R.id.iv_online);



        ImageView ivHead = helper.getView(R.id.ivHead);
        GlideUtils.showCircleImage(ivHead.getContext(), ivHead, item.getAvatar(), R.drawable.icon_head);

        switch (item.getIsOnline()) {
            case 0:
                ivOnline.setBackgroundResource(R.drawable.icon_online);
                break;
            case 1:
                ivOnline.setBackgroundResource(R.drawable.icon_online_green);
                break;
            case 2:
                ivOnline.setBackgroundResource(R.drawable.icon_online_orange);
                break;
            default:
                break;
        }

        if (item.getUserId() == SharedPrefsUtil.getUserId()){
            ivOnline.setBackgroundResource(R.drawable.icon_online_green);
        }

        rlMaillistUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.onClick(item);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
