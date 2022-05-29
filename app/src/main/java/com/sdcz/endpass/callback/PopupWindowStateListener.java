package com.sdcz.endpass.callback;

/**
 * @Description PopupWindow状态监听接口
 */
public interface PopupWindowStateListener {
    /**
     * 打开PopupWindow时回调
     */
    void onOpenPopupWindowListener();

    /**
     * 关闭PopupWindow时回调
     */
    void onClosePopupWindowListener();
}
