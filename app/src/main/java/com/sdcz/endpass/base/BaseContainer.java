package com.sdcz.endpass.base;

import android.content.Context;
import android.view.View;

import com.sdcz.endpass.callback.PopupWindowCommunicationListener;


/**
 * @Description     会中各菜单点击的P层
 */
public abstract class BaseContainer<T extends View> {
    protected T view;
    public Context context;
    protected PopupWindowCommunicationListener.PopupWindowCommunicationInterior interior;

    public BaseContainer(Context context) {
        this.context = context;
    }

    public BaseContainer(Context context, PopupWindowCommunicationListener
            .PopupWindowCommunicationInterior interior) {
        this.context = context;
        this.interior = interior;
    }

    public void setView(T view) {
        this.view = view;
    }

    public T getView() {
        return view;
    }

    /**
     * 显示菜单内容
     */
    public void show() {
        if (interior == null || view == null) {
            return;
        }
        interior.getPopupWindowBuilder().setContentView(view).show();
    }

    /**
     * 隐藏
     */
    public void dismiss() {
        if (interior == null) {
            return;
        }
        interior.dismissDialog();
    }

    /**
     * 返回上一级
     */
    public void back() {
        if (interior == null) {
            return;
        }
        interior.back();
    }


    /**
     * 资源回收
     */
    public void recycle(){

    }
}
