package com.sdcz.endpass.bean;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Description: 会中设置项的key
 */
@StringDef({MeetingSettingsKey.KEY_HOR_FLIP,
        MeetingSettingsKey.KEY_BEAUTY_LEVEL,
        MeetingSettingsKey.KEY_AUDIO_DATA})
@Retention(RetentionPolicy.SOURCE)
public @interface MeetingSettingsKey {
    String KEY_HOR_FLIP = "capture_flip"; // 视频左右镜像
    String KEY_BEAUTY_LEVEL = "BEAUTY_LEVEL"; // 美颜等级
    String KEY_AUDIO_DATA = "AUDIO_DATA"; // 音频数据
}
