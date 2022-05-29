package com.sdcz.endpass.model;

import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.comix.meeting.utils.SharedPreferencesHelper;
import com.sdcz.endpass.bean.MeetingSettingsKey;

/**
 *  会议设置缓存数据的存取
 */
public class MeetingSettings {

    private static final String XML_TABLE = "Xml";
    private static final String XML_ONLINE = "OnlinePreference";
    private final SharedPreferencesHelper defaultFile;

    /**
     * 构造函数
     */
    public MeetingSettings(@NonNull Context context) {
        defaultFile = new SharedPreferencesHelper(context, null);
    }

    /**
     * 视频左右镜像
     */
    public void setHorFlip(boolean horFlip) {
        defaultFile.putBoolean(MeetingSettingsKey.KEY_HOR_FLIP, horFlip);
    }

    /**
     * 视频左右镜像
     */
    public boolean isHorFlip() {
        return defaultFile.getBoolean(MeetingSettingsKey.KEY_HOR_FLIP, false);
    }

    /**
     * 美颜等级
     */
    public void setBeautyLevel(@IntRange(from = 1, to = 5) int level) {
        defaultFile.putInt(MeetingSettingsKey.KEY_BEAUTY_LEVEL, level);
    }

    /**
     * 美颜等级
     */
    public @IntRange(from = 1, to = 5)
    int getBeautyLevel() {
        return defaultFile.getInt(MeetingSettingsKey.KEY_BEAUTY_LEVEL, 0);
    }

    /**
     * 会中音频数据
     */
    public void setWriteAudioData(Boolean value) {
        defaultFile.putBoolean(MeetingSettingsKey.KEY_AUDIO_DATA, value);
    }

    public boolean isWriteAudioData() {
        return defaultFile.getBoolean(MeetingSettingsKey.KEY_AUDIO_DATA, false);
    }
}













