package com.sdcz.endpass.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @Description     SP缓存类
 */
public class SharedPreferencesUtils {
    private static SharedPreferences sharedPreferences;
    private static final String MEETING_UI_SP_NAME = "MEETING_UI_SHARED_PREFERENCES";
    public static final String TAG = "SharedPreferencesUtils";

    /**
     * init preferences
     *
     * @param ctx 上下文
     */
    public static void init(Context ctx) {
        if (sharedPreferences != null) {
            return;
        }
        if (ctx instanceof Application) {
            sharedPreferences = ctx.getSharedPreferences(MEETING_UI_SP_NAME, Context.MODE_PRIVATE);
        } else {
            sharedPreferences = ctx.getApplicationContext()
                    .getSharedPreferences(MEETING_UI_SP_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * save key-value
     *
     * @param key   键
     * @param value 值
     */
    public static void putString(String key, String value) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        if (sharedPreferences == null) {
            return null;
        }
        return sharedPreferences.getString(key, null);
    }

    public static String getString(String key, String defaultValue) {
        if (sharedPreferences == null) {
            return null;
        }
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * save key-value
     *
     * @param key   键
     * @param value 值
     */
    public static void putInt(String key, int value) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt(key, value).apply();
    }

    /**
     * get key-value
     *
     * @param key 键
     */
    public static int getInt(String key) {
        if (sharedPreferences == null) {
            return 0;
        }
        return sharedPreferences.getInt(key, 0);
    }


    /**
     * save key-value
     *
     * @param key   键
     * @param value 值
     */
    public static void putFloat(String key, float value) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putFloat(key, value).apply();
    }

    /**
     * get key-value
     *
     * @param key 键
     */
    public static float getFloat(String key) {
        if (sharedPreferences == null) {
            return 0f;
        }
        return sharedPreferences.getFloat(key, 0);
    }


    /**
     * 保存Boolean
     *
     * @param key   key
     * @param value value
     */
    public static void putBoolean(String key, boolean value) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    /**
     * 获取Boolean
     *
     * @param key key
     * @return boolean
     */
    public static boolean getBoolean(String key) {
        if (sharedPreferences == null) {
            return false;
        }
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * 获取 Boolean
     *
     * @param key      key
     * @param defValue 默认值
     * @return boolean
     */
    public static boolean getBoolean(String key, boolean defValue) {
        if (sharedPreferences == null) {
            return defValue;
        }
        return sharedPreferences.getBoolean(key, defValue);
    }
}