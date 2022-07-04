package com.sdcz.endpass.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.util.GlideUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;

import org.json.JSONException;

import java.security.KeyStore;
import java.util.List;


public class SelectUserAdapter extends BaseQuickAdapter<UserEntity, BaseViewHolder> {

    public SelectUserAdapter(int layoutResId, @Nullable List<UserEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserEntity item) {
        helper.setText(R.id.tv_name,item.getNickName());
        RelativeLayout relativeLayout = helper.getView(R.id.rl_maillist_user);
        CheckBox checkbox = helper.getView(R.id.checkbox);
        TextView tvTask = helper.getView(R.id.tvTask);
        ImageView ivHead = helper.getView(R.id.ivHead);
        ImageView ivOnline = helper.getView(R.id.ivIsOnline);

        GlideUtils.showCircleImage(mContext, ivHead, item.getAvatar(), R.drawable.icon_head);

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
        tvTask.setText(null != item.getChannelName() ?"("+item.getChannelName() + "中)" : "(无任务)");

        for (UserEntity i: SharedPrefsUtil.getListUserInfo())
            if (i.getUserId() == item.getUserId()){
                checkbox.setChecked(true);
            }
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkbox.isChecked()){
                    checkbox.setChecked(false);
                }else {
                    checkbox.setChecked(true);
                }
            }
        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    List<UserEntity> selectUser = SharedPrefsUtil.getListUserInfo();
                    selectUser.add(item);
                    SharedPrefsUtil.putListUserInfo(selectUser);
                }else {
                    for (UserEntity info : SharedPrefsUtil.getListUserInfo()){
                        if (info.getUserId() == item.getUserId());
                    }
                    for (int i = 0; i < SharedPrefsUtil.getListUserInfo().size(); i++){
                        if (SharedPrefsUtil.getListUserInfo().get(i).getUserId() == item.getUserId()){
                            List<UserEntity> selectUser = SharedPrefsUtil.getListUserInfo();
                            selectUser.remove(i);
                            SharedPrefsUtil.putListUserInfo(selectUser);
                            break;
                        }
                    }
                }
            }
        });


        if (item.getUserId() == 2){
            checkbox.setEnabled(false);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
