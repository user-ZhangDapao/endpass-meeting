package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ISearchView;

import java.security.KeyStore;
import java.util.List;


/**
 * Author: Administrator
 * CreateDate: 2021/6/30 14:50
 * Description: @
 */
public class SearchPresenter extends BasePresenter<ISearchView> {
    public SearchPresenter(ISearchView view) {
        super(view);
    }

    public void getUserByNameLike(Activity activity, String inputName){
        RequestUtils.getUserByNameLike(inputName, new MyObserver<List<UserEntity>>(activity) {
            @Override
            public void onSuccess(List<UserEntity> result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }

    public void postCollectStatus(Activity activity , String collectUserId){
        RequestUtils.postCollectStatus(collectUserId, new MyObserver<Integer>(activity) {
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

    public void creatRecord(Activity activity, String collectUserId, int recordType){
//        RequestUtils.creatRecord(recordType, new MyObserver<String>(activity) {
//            @Override
//            public void onSuccess(String result) {
//                iView.creatRecordSuccess(result,collectUserId,recordType);
//            }
//
//            @Override
//            public void onFailure(Throwable e, String errorMsg) {
//                iView.showOnFailure("",errorMsg);
//            }
//        });
    }

}
