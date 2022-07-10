package com.sdcz.endpass.presenter;


import android.app.Activity;
import android.content.Context;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.DemoApp;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMobileMeetingView;

import java.util.ArrayList;
import java.util.List;

public class MobileMeetingPresenter extends BasePresenter<IMobileMeetingView> {
    public MobileMeetingPresenter(IMobileMeetingView view) {
        super(view);
    }


    public void checkChannelAdmin(Context activity, String channelCode){
        RequestUtils.checkChannelAdmin(channelCode, new MyObserver<Boolean>(activity) {
            @Override
            public void onSuccess(Boolean result) {
                iView.showData(result);
            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
    }

    public void checkAdmin(String channelCode) {
        String role = SharedPrefsUtil.getRoleId();

        if (role.isEmpty()) {
            MobileMeetingActivity.isAdmin = false;
            return;
        } else if (role.equals("manage") || role.equals("admin")) {
            MobileMeetingActivity.isAdmin = true;
            return;
        } else if (role.equals("normal")){
            MobileMeetingActivity.isAdmin = false;
            return;
        }else{
            checkChannelAdmin(DemoApp.getContext(),channelCode);
        }
    }


    /**
     * 获取主会场id
     * @param channelCode
     * @return
     */
    public List<UserEntity> getChannelUser(String channelCode){
        RequestUtils.getChannelByCode(channelCode, new MyObserver<ChannelBean>() {
            @Override
            public void onSuccess(ChannelBean result) {
                iView.venueId(result.getVenue());
            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
        return new ArrayList<>();
    }

    public void addChannelUser(Activity activity, String groupId, String[] userIds){
        RequestUtils.addChannelUser(groupId, userIds, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                ToastUtils.showLong("添加成功");
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }


}
