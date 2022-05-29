package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMineView;

import java.security.KeyStore;


/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:18
 * Description: @
 */
public class MinePresenter extends BasePresenter<IMineView> {
    public MinePresenter(IMineView view) {
        super(view);
    }

    public void getUserInfo(Activity activity ){
        RequestUtils.getUser("", new MyObserver<UserEntity>(activity) {
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



}
