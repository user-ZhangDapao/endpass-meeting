package com.sdcz.endpass.model;

import com.sdcz.endpass.SdkUtil;
import com.inpor.base.sdk.video.VideoManager;

/**
 * Demo中一些缓存的值
 */
public class AppCache {

    private AppCache() {
    }

    private static AppCache sInstance;

    public static AppCache getInstance() {
        if (sInstance == null) {
            synchronized (AppCache.class) {
                if (sInstance == null) {
                    sInstance = new AppCache();
                }
            }
        }
        return sInstance;
    }

    private boolean cameraEnabled = true;
    private boolean micDisable = false;
    private boolean horFlip;              // 视频左右镜像
    private int beautyLevel;              // 美颜等级

    public boolean isHorFlip() {
        return horFlip;
    }

    public void setHorFlip(boolean horFlip) {
        this.horFlip = horFlip;
    }

    public int getBeautyLevel() {
        return beautyLevel;
    }

    public void setBeautyLevel(int beautyLevel) {
        this.beautyLevel = beautyLevel;
    }

    public boolean isCameraEnabled() {
        return cameraEnabled;
    }

    public void setCameraEnabled(boolean cameraEnabled) {
        this.cameraEnabled = cameraEnabled;
        VideoManager videomanager = SdkUtil.getVideoManager();
        videomanager.set_disable_local_access(!cameraEnabled);
        SdkUtil.getVideoManager().setCameraEnable(cameraEnabled);
    }

    public boolean isMicDisable() {
        return micDisable;
    }

    public void setMicDisable(boolean micDisable) {
        this.micDisable = micDisable;
        SdkUtil.getAudioManager().setMicrophoneEnable(micDisable);
    }
}
