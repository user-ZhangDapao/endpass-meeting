package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IUserInfoView;

import java.io.File;
import java.security.KeyStore;


/**
 * Author: Administrator
 * CreateDate: 2021/6/30 10:24
 * Description: @
 */
public class UserInfoPresenter extends BasePresenter<IUserInfoView> {
    public UserInfoPresenter(IUserInfoView view) {
        super(view);
    }

    public void getUserInfo(Activity activity ){
        RequestUtils.getUser("",new MyObserver<UserEntity>(activity) {
            @Override
            public void onSuccess(UserEntity result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

    public void updateUser(Activity activity ,String nickName, String phonenumber,String userCode, String lon, String lat){
        RequestUtils.updateUser(nickName, phonenumber, userCode, lon, lat ,new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.reviseSuccess(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }


    public void updateHeadImg(Activity activity, String imgBase64){
        com.alibaba.fastjson.JSONObject postInfo = new com.alibaba.fastjson.JSONObject();
        postInfo.put("avatarfile",imgBase64);
        RequestUtils.updateHeadImg(postInfo, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.reviseSuccess(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }


    public void updateImg(Activity activity, String filePath){
        RequestUtils.updateImg(filePath, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.reviseSuccess(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

//
//    public void updateUserRealname(Activity activity ,String userId ,String realName){
//        RequestUtils.updateUserRealname(userId, realName, new MyObserver<Object>(activity) {
//            @Override
//            public void onSuccess(Object result) {
//                iView.updataName(result);
//            }
//
//            @Override
//            public void onFailure(Throwable e, String errorMsg) {
//                iView.showOnFailure("",errorMsg);
//            }
//        });
//    }

}
