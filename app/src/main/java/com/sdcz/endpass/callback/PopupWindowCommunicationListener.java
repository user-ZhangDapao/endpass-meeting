package com.sdcz.endpass.callback;


import com.sdcz.endpass.widget.PopupWindowBuilder;

/**
 * @Description 和PopupWindow容器和它的视图通信的接口
 */
public interface PopupWindowCommunicationListener {


    /**
     * 设置和PopupWindow通信接口
     *
     * @param popupWindowCommunicationInterior 监听接口
     */
    void setPopupWindowCorrelationListener(PopupWindowCommunicationInterior popupWindowCommunicationInterior);


    /**
     * 回收PopupWindow视图中相关资源
     */
    default void recycle() {

    }

    interface PopupWindowCommunicationInterior {


        void dismissChildren();

        /**
         * 关闭Popup
         */
        void dismissDialog();

        /**
         * 返回上一个视图
         */
        default void back() {
        }


        PopupWindowBuilder getPopupWindowBuilder();
    }
}
