package com.sdcz.endpass.base;

import android.content.Context;

import com.sdcz.endpass.bean.UserEntity;

public interface IBaseView {

    /**
     * 显示加载
     */
    void showLoading();

    /**
     * 隐藏加载
     */
    void hideLoading();

    /**
     * 显示空
     */
    void showEmpty();

    /**
     * 显示错误信息
     */
    void showError();

    /**
     * 显示提示
     * @param msg
     */
    void showToast(String msg);

    /**
     * 获取上下文
     * @return
     */
    Context getContext();

    /**
     * 展示网络请求错误的回调
     * @param status
     * @param msg
     */
    void showOnFailure(String status, String msg);

}
