package com.sdcz.endpass.presenter;


import android.app.Activity;
import android.content.Context;

import com.sdcz.endpass.DemoApp;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMobileMeetingView;

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
}
