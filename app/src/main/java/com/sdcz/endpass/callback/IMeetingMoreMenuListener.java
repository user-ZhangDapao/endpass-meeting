package com.sdcz.endpass.callback;

/**
 * @Description 会中底部菜单点击回调
 */
public interface IMeetingMoreMenuListener {
    /**
     * 点击“切换布局”按钮回调
     */
    void onClickChangeLayoutItemListener();

    void onClickopenGPSListener();
    void onClickcancleGPSListener();

    /**
     * 点击“聊天” 按钮事件回调
     */
    void onClickChatItemListener();

    /**
     * 点击申请管理员
     */
    void onClickApplyManagerListener();

    /**
     * 点击设置按钮
     */
    void onClickSettingListener();

    void onClickFinishMeetingListener();

    boolean dealDisableMicListener(boolean isDisableAudio);

    boolean dealDisableCameraListener(boolean isDisableCamera);

    void enableVideoPreviewListener(boolean isDisableCamera);

    boolean isManager();
}