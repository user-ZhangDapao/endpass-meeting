package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.view.ITaskUserView;

import java.util.List;


/**
 * Author: Administrator
 * CreateDate: 2021/7/9 14:33
 * Description: @
 */
public class TaskUserPresenter extends BasePresenter<ITaskUserView> {
    public TaskUserPresenter(ITaskUserView view) {
        super(view);
    }

    public void queryChannelUser(Activity activity, String channelCode){
        RequestUtils.queryChannelUser(channelCode, new MyObserver<List<ChannerUser>>(activity) {
            @Override
            public void onSuccess(List<ChannerUser> result) {
                iView.showData(result);
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
                iView.onDelete(result,userId);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }

    public void addChannelUser(Activity activity, String groupId, String[] userIds){
        RequestUtils.addChannelUser(groupId, userIds, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.addUserResult(result);
//                FspManager.getInstance().invite(userIds, groupId, Constants.MEETING_TASK);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }

    public void updateChannel(Activity activity, String channelCode, String channelName){
        RequestUtils.updateChannel(channelCode, channelName, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.updateChannelResult(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }
}
