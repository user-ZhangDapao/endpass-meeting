package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.view.IChangePassView;


/**
 * Author: Administrator
 * CreateDate: 2021/7/1 10:49
 * Description: @
 */
public class ChangePassPersenter extends BasePresenter<IChangePassView> {
    public ChangePassPersenter(IChangePassView view) {
        super(view);
    }

    public void changePassWord(Activity activity,String password, String newPassword){
        RequestUtils.changePassWord(password, newPassword, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("", errorMsg);
            }
        });
    }
}
