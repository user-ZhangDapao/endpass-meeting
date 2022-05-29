package com.sdcz.endpass.base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.sdcz.endpass.callback.PopupWindowCommunicationListener;
import com.sdcz.endpass.widget.PopupWindowBuilder;


public class BasePopupWindowContentView extends ConstraintLayout implements PopupWindowCommunicationListener {

    private static final String TAG = "BasePopContentView";
    private PopupWindowCommunicationInterior popupWindowCommunicationInterior;
    private PopupWindowBuilder popupWindowBuilder;


    public BasePopupWindowContentView(@NonNull Context context) {
        super(context);

    }


    public BasePopupWindowContentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public void setPopupWindowCorrelationListener(PopupWindowCommunicationInterior popupWindowCommunicationInterior) {
        this.popupWindowCommunicationInterior = popupWindowCommunicationInterior;
    }

    /**
     * 关闭Popup的二级View
     */
    public void dismissChildrenPopupWindow() {
        if (popupWindowCommunicationInterior != null) {
            popupWindowCommunicationInterior.dismissChildren();
        }
    }




    /**
     * 关闭PopupWindow
     */
    public void dismissPopupWindow() {
        Log.i(TAG, "popupWindowCommunicationInterior:" + popupWindowCommunicationInterior);
        if (popupWindowCommunicationInterior != null) {
            popupWindowCommunicationInterior.dismissDialog();
        }
    }


    /**
     * 返回POP上一个视图
     */
    public void popupWindowBack() {
        Log.i(TAG, "popupWindowCommunicationInterior:" + popupWindowCommunicationInterior);
        if (popupWindowCommunicationInterior != null) {
            popupWindowCommunicationInterior.back();
        }
    }


    /**
     * 从竖屏切换到横屏时调用
     */
    public void onLandscapeListener() {

    }


    /**
     * 从横屏切换到竖屏时调用
     */
    public void onPortraitListener() {

    }


    /**
     * 获取PopupWindowBuilder
     *
     * @return PopupWindowBuilder
     */
    public PopupWindowBuilder getPopupWindowBuilder() {
        if (popupWindowCommunicationInterior != null) {
            return popupWindowCommunicationInterior.getPopupWindowBuilder();
        }
        return null;
    }


    /**
     * 如果子类想在popupWindow关闭时，回收某些资源，可以重新该方法
     */
    @Override
    public void recycle() {

    }
}
