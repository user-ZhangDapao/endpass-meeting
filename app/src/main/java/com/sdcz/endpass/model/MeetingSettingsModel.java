package com.sdcz.endpass.model;

import android.content.Context;
import android.util.Log;

import com.sdcz.endpass.bean.MeetingSettingsKey;
import com.sdcz.endpass.callback.OnSettingsChangedListener;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: 分发设置项改变事件
 */
public class MeetingSettingsModel {

    private static class Singleton {
        private static final MeetingSettingsModel INSTANCE = new MeetingSettingsModel();
    }

    /**
     * 获取单例
     */
    public static MeetingSettingsModel getInstance() {
        return Singleton.INSTANCE;
    }

    private final CopyOnWriteArrayList<OnSettingsChangedListener> observers = new CopyOnWriteArrayList<>();
    private MeetingSettingsCacheListener meetingSettingsCacheListener;

    /**
     * 应用启动时（目前需要入会时读取），从缓存中读取设置项到内存，后面的改变同时设置到SP和内存
     */
    public void initSettings(Context context) {
        if (meetingSettingsCacheListener == null) {
            meetingSettingsCacheListener = new MeetingSettingsCacheListener(context);
            addListener(meetingSettingsCacheListener);
        }
        AppCache config = AppCache.getInstance();
        MeetingSettings settings = new MeetingSettings(context);
        config.setHorFlip(settings.isHorFlip());
        config.setBeautyLevel(settings.getBeautyLevel());
    }

    /**
     * addListener
     */
    public void addListener(OnSettingsChangedListener listener) {
        if (listener == null || observers.contains(listener)) {
            return;
        }
        observers.add(listener);
    }

    /**
     * removeListener
     */
    public void removeListener(OnSettingsChangedListener listener) {
        if (listener == null || !observers.contains(listener)) {
            return;
        }
        observers.remove(listener);
    }

    /**
     * 通知设置项改变，应用设置项的模块根据需要做出自己的反应(目前只设置到内存缓存和sp缓存)
     * @param key {@link MeetingSettingsKey}
     * @param value value
     */
    public void updateSettings(@MeetingSettingsKey String key, Object value) {
        Log.i("MeetingSettings", key + ": " + value);
        if (observers.isEmpty()) {
            return;
        }
        for (OnSettingsChangedListener listener : observers) {
            listener.onSettingsChanged(key, value);
        }
    }

}
