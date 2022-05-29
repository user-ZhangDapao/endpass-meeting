package com.sdcz.endpass.presenter;

import android.app.Activity;
import android.util.Log;

import com.sdcz.endpass.Constants;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ILoginView;


/**
 * Author: Administrator
 * CreateDate: 2021/6/28 10:57
 * Description: @
 */
public class LoginPresenter extends BasePresenter<ILoginView> {

    public LoginPresenter(ILoginView view) {
        super(view);
    }

    public void doLogin(Activity activity, String loginName, String passWord){
        RequestUtils.postLogin(loginName, passWord, new MyObserver<String>(activity) {
            @Override
            public void onSuccess(String result) {
                SharedPrefsUtil.putString(Constants.SharedPreKey.Token, result);
                Log.d("TOKEN================>", result);
                iView.showData();
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

}
