package com.sdcz.endpass.callback;

/**
 * @Description     会中菜单栏点击的回调事件
 */
public interface MeetingMenuEventManagerListener {

    /**
     * 切换前后摄像头
     */
    void onMenuManagerChangeCameraListener();

    /**
     * 关闭Activity
     */
    void onMenuManagerFinishActivityListener();
}
