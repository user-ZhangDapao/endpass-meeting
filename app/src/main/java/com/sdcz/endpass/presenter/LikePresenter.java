package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ILikeView;

import java.security.KeyStore;
import java.util.List;


/**
 * Author: Administrator
 * CreateDate: 2021/7/5 9:35
 * Description: @
 */
public class LikePresenter extends BasePresenter<ILikeView> {
    public LikePresenter(ILikeView view) {
        super(view);
    }

    public void getCollectList(Activity activity){
        RequestUtils.getCollectList(new MyObserver<List<UserEntity>>(activity) {
            @Override
            public void onSuccess(List<UserEntity> result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

    public void postCollectStatus(Activity activity , String collectUserId){
        RequestUtils.postCollectStatus(collectUserId, new MyObserver<Integer>(activity) {
            @Override
            public void onSuccess(Integer result) {
                iView.showCollectStatus(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

    public void collectUser(Activity activity,  String collectUserId){
        RequestUtils.collectUser(collectUserId, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.collectUserSuccess(result);
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
