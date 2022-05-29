package com.sdcz.endpass.util;

import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;

/**
 * @Description 该类用于保持会中的一些临时数据，这些临时数据只对当次和当前会议室有效
 *      例如“更多”里面的某些按钮状态，聊天中因某种原因导致未发送出去的信息等等，这些数据在退出会议后被清空
 */
public class MeetingTempDataUtils {
    public static final String TEMP_MSG_KEY = "TEMP_MSG";
    private static final String TAG = "MeetingTempDataUtils";
    private static volatile MeetingTempDataUtils meetingTempDataUtils = null;
    private Bundle tempDataContainer;

    private MeetingTempDataUtils() {

    }

    /**
     * 获取实例
     *
     * @return MeetingTempDataUtils
     */
    public static MeetingTempDataUtils getInstance() {
        if (meetingTempDataUtils == null) {
            synchronized (MeetingTempDataUtils.class) {
                if (meetingTempDataUtils == null) {
                    meetingTempDataUtils = new MeetingTempDataUtils();
                }
            }
        }

        if (meetingTempDataUtils.tempDataContainer == null) {
            synchronized (MeetingTempDataUtils.class) {
                if (meetingTempDataUtils.tempDataContainer == null) {
                    meetingTempDataUtils.tempDataContainer = new Bundle();
                }
            }
        }
        return meetingTempDataUtils;
    }


    /**
     * 保存字符串
     *
     * @param key   key
     * @param value value
     */
    public void saveString(String key, String value) {
        Log.i(TAG, "saveString:" + tempDataContainer);
        Log.i(TAG, "saveString:" + meetingTempDataUtils.tempDataContainer);
        if (tempDataContainer != null) {
            tempDataContainer.putString(key, value);
        }
    }

    /**
     * 获取保存的字符串，
     *
     * @param key key
     * @return String
     */
    public String getString(String key) {
        if (tempDataContainer != null) {
            return tempDataContainer.getString(key);
        }
        return null;
    }

    /**
     * 带默认值的获取字符串
     *
     * @param key           key
     * @param defaultString 获取失败是返回的默认值
     * @return String
     */
    public String getString(String key, String defaultString) {
        if (tempDataContainer != null) {
            return tempDataContainer.getString(key, defaultString);
        }
        return defaultString;
    }

    /**
     * 清理字符串
     *
     * @param key key
     */
    public void cleanString(String key) {
        if (tempDataContainer != null) {
            tempDataContainer.putString(key, "");
        }
    }


    /**
     * 保存 Boolean
     *
     * @param key   key
     * @param value value
     */
    public void saveBoolean(String key, boolean value) {
        if (tempDataContainer != null) {
            tempDataContainer.putBoolean(key, value);
        }
    }

    /**
     * 获取 boolean
     *
     * @param key key
     * @return boolean
     */
    public boolean getBoolean(String key) {
        if (tempDataContainer != null) {
            return tempDataContainer.getBoolean(key, false);
        }
        return false;
    }

    /**
     * 获取 boolean (带默认值)
     *
     * @param key key
     * @return boolean
     */
    public boolean getBoolean(String key, boolean defaultString) {
        if (tempDataContainer != null) {
            return tempDataContainer.getBoolean(key, defaultString);
        }
        return defaultString;
    }


    /**
     * 保存一个Serializable对象
     *
     * @param key   key
     * @param value value
     */
    public void saveSerializable(String key, Serializable value) {
        if (tempDataContainer != null) {
            tempDataContainer.putSerializable(key, value);
        }
    }

    /**
     * 获取一个Serializable对象
     *
     * @param key key
     * @return Serializable
     */
    public Serializable getSerializable(String key) {
        if (tempDataContainer != null) {
            return tempDataContainer.getSerializable(key);
        }
        return null;
    }

    /**
     * 清理临时数据，在会中Activity销毁时调用该方法清空临时数据
     */
    public static void cleanTempData() {
        if (meetingTempDataUtils == null) {
            return;
        }
        if (meetingTempDataUtils.tempDataContainer != null) {
            meetingTempDataUtils.tempDataContainer.clear();
            meetingTempDataUtils.tempDataContainer = null;
        }
        meetingTempDataUtils = null;
    }
}
