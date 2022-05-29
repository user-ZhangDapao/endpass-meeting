package com.sdcz.endpass.callback;

/**
 * @Description 会中多媒体设备禁用、启动事件监听接口
 */
public interface IMeetingMultimediaDisableListener {

    /**
     * 禁用麦克风时回调
     *
     * @param isDisable 是否被禁用 true 禁用   false 启用
     */
    void onDisableMicListener(boolean isDisable);

    /**
     * 禁用摄像头时回调
     *
     * @param isDisable 是否被禁用 true 禁用   false 启用
     */
    void onDisableCameraListener(boolean isDisable);


    /**
     * 视频预览 开/关 回调
     * @param openVideoPreview true 打开视频预览 false 关闭视频预览
     */
    void onVideoPreviewSwitchChangeListener(boolean openVideoPreview);
}
