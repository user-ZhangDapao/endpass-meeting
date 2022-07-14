package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ICreateTaskView;

import java.security.KeyStore;
import java.util.Map;

/**
 * Author: Administrator
 * CreateDate: 2021/7/8 13:28
 * Description: @
 */
public class CreateTaskPresenter extends BasePresenter<ICreateTaskView> {
    public CreateTaskPresenter(ICreateTaskView view) {
        super(view);
    }

    public void addChannel(Activity activity, String channelName, String deptId, String details){
        RequestUtils.addChannel(channelName, deptId, details, new MyObserver<ChannelBean>(activity) {
            @Override
            public void onSuccess(ChannelBean result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }

    public void addChannelUser(Activity activity, String groupId, Integer[] userIds){
        RequestUtils.addChannelUser(groupId, userIds, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.addUserResult(result,groupId,userIds);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }

    public void deleteChannelUser(Activity activity, String channelCode, String userId){
        RequestUtils.deleteChannelUser(channelCode, userId, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.onDelete(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }

}
