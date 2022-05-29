package com.sdcz.endpass;

import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;

public class LiveDataBus {

    /**
     *  屏幕旋转
     */
    public static final String KEY_ORIENTATION = "orientation";
    /**
     *  会中Configuration变化
     */
    public static final String KEY_MEETING_ACTIVITY_CONFIG = "meeting_activity_config";
    private static LiveDataBus sInstance;
    private final HashMap<String, MutableLiveData<?>> liveDataMap;

    private LiveDataBus() {
        liveDataMap = new HashMap<>();
    }

    public static LiveDataBus getInstance() {
        if (sInstance == null) {
            synchronized (LiveDataBus.class) {
                if (sInstance == null) {
                    sInstance = new LiveDataBus();
                }
            }
        }
        return sInstance;
    }

    public <T> MutableLiveData<T> getLiveData(String key) {
        if (!liveDataMap.containsKey(key)) {
            liveDataMap.put(key, new MutableLiveData<T>());
        }
        return (MutableLiveData<T>) liveDataMap.get(key);
    }
}
