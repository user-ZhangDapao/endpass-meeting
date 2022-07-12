package com.sdcz.endpass.presenter;

import android.app.Activity;
import android.content.Context;

import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMailListView;

import java.security.KeyStore;

/**
 * Author: Administrator
 * CreateDate: 2021/6/28 14:23
 * Description: @
 */
public class MailListPresenter extends BasePresenter<IMailListView> {
    public MailListPresenter(IMailListView view) {
        super(view);
    }

    public void getUserInfo(Activity activity){
        RequestUtils.getUser("", new MyObserver<UserEntity>(activity) {
            @Override
            public void onSuccess(UserEntity result) {
                if (result != null){
                    SharedPrefsUtil.putUserInfo(result);
                    iView.showUserInfo(result);
                }
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

    public void getContactList(Activity activity){
        String role = SharedPrefsUtil.getRoleId();
        if (role.equals("manage") || role.equals("admin")){
            getContactList(activity, "0");
        }else {
            getContactList(activity, SharedPrefsUtil.getDeptId() + "");
        }
    }

    public void getContactList(Activity activity, String deptId){
        RequestUtils.getContactList(deptId, new MyObserver<MailListBean>(activity) {
            @Override
            public void onSuccess(MailListBean result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",null != errorMsg ? errorMsg : "");
            }
        });
    }

    public void postCollectStatus(Activity activity , String collectUserId){
        RequestUtils.postCollectStatus( collectUserId, new MyObserver<Integer>(activity) {
            @Override
            public void onSuccess(Integer result) {
                iView.showStatus(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

    public void collectUser(Activity activity, String collectUserId){
        RequestUtils.collectUser(collectUserId, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.cancelLikeSuccess(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

    public void creatRecord(Activity activity, String collectUserId, int recordType, Long inviteCode){
        RequestUtils.creatRecord(recordType,inviteCode,Long.parseLong(collectUserId), new MyObserver<String>(activity) {
            @Override
            public void onSuccess(String channelCode) {
                iView.creatRecordSuccess(channelCode,collectUserId,recordType,inviteCode);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }


}
