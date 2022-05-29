package com.sdcz.endpass.base;


import android.app.Activity;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;

public class BasePresenter<V extends IBaseView> {
    protected V iView;

    public BasePresenter(V view) {
        attachView(view);
    }

    /**
     * 绑定view，一般在初始化中调用该方法
     * @param view
     */
    public void attachView(V view) {
        this.iView = view;
    }

    /**
     * 断开view，一般在onDestroy中调用
     */
    public void detachView() {
        this.iView = null;
    }

    /**
     * 是否与View建立连接
     * 每次调用业务请求的时候都要出先调用方法检查是否与View建立连接
     * @return
     */
    public boolean isAttachView() {
        return iView != null;
    }

    /**
     * 获取连接的view
     */
    public V getView(){
        return iView;
    }



    public void getUserInfo(Activity activity){
        RequestUtils.getUser("", new MyObserver<UserEntity>(activity) {
            @Override
            public void onSuccess(UserEntity result) {
                if (result != null){
                    SharedPrefsUtil.putUserInfo(result);
                }
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }
}
