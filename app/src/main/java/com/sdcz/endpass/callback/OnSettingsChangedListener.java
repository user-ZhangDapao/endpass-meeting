package com.sdcz.endpass.callback;


import com.sdcz.endpass.bean.MeetingSettingsKey;

/**
 * @Description: 设置项改变事件通知
 */
public interface OnSettingsChangedListener {
    /**
     * 设置项改变事件
     * @param key {@link MeetingSettingsKey}
     * @param value 基本类型，Boolean, String, int, long, float
     */
    void onSettingsChanged(@MeetingSettingsKey String key, Object value);
}
