package com.sdcz.endpass.model;

import android.content.Context;

import com.sdcz.endpass.bean.MeetingSettingsKey;
import com.sdcz.endpass.callback.OnSettingsChangedListener;

/**
 * @Description: 接收设置改变，然后保存在SharedPreferences，同时更新内存中的缓存
 */
public class MeetingSettingsCacheListener implements OnSettingsChangedListener {

    private final MeetingSettings localCache;
    private final AppCache appCache = AppCache.getInstance();

    /**
     * 构造函数
     */
    public MeetingSettingsCacheListener(Context context) {
        localCache = new MeetingSettings(context);
    }

    @Override
    public void onSettingsChanged(String key, Object value) {
        switch (key) {
            case MeetingSettingsKey.KEY_HOR_FLIP:
                appCache.setHorFlip((Boolean) value);
                localCache.setHorFlip((Boolean) value);
                break;
            case MeetingSettingsKey.KEY_BEAUTY_LEVEL:
                appCache.setBeautyLevel((Integer) value);
                localCache.setBeautyLevel((Integer) value);
                break;
            case MeetingSettingsKey.KEY_AUDIO_DATA:
                localCache.setWriteAudioData((Boolean) value);
                break;
            default:
                break;
        }
    }
}
















