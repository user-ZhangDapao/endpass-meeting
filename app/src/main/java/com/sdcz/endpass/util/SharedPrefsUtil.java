package com.sdcz.endpass.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.google.firebase.auth.UserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.bean.UserEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description     SP缓存类
 */
public class SharedPrefsUtil {
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
     * clean preferences
     */
    public static void clean(Context ctx) {
        SharedPreferences.Editor sp = ctx.getSharedPreferences(MEETING_UI_SP_NAME, Context.MODE_PRIVATE).edit();
        sp.remove("MEETING_UI_SP_NAME");
        sp.clear();
        sp.commit();
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


    public static JSONObject getJSONValue(String key) {

        String allUserJSON = sharedPreferences.getString(key, "");

        try {
            if (allUserJSON==""){
                return new JSONObject();
            }
            return new JSONObject(allUserJSON);
        } catch (JSONException e) {
            return new JSONObject();
        }
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



    public static void putUserInfo(UserEntity obj) {
        if (sharedPreferences == null) {
            return;
        }
        String objString = JSON.toJSONString(obj);// fastjson的方法，需要导包的
        sharedPreferences.edit().putString(Constants.SharedPreKey.UserInfo, objString).commit();
    }

    /**
     *
     * 这里传入一个类就是我们所需要的实体类(obj)
     * @return 返回我们封装好的该实体类(obj)
     */
    public static UserEntity getUserInfo() {
        if (sharedPreferences == null) {
            return JSON.parseObject("", UserEntity.class);
        }
        String objString = sharedPreferences.getString(Constants.SharedPreKey.UserInfo, "");
        return JSON.parseObject(objString, UserEntity.class);
    }

    public static String getRoleId(){
        if (SharedPrefsUtil.getUserInfo() == null) return "";
       return SharedPrefsUtil.getUserInfo().getRoles().get(0).getRoleKey();
    }


    public static List<UserEntity> getListUserInfo() {
        if (sharedPreferences == null) {
            return new ArrayList<>();
        }
        String peopleListJson = sharedPreferences.getString(Constants.SharedPreKey.SELECT_USER_LIST, "");
        if (peopleListJson != "" && peopleListJson != null) {
            return new Gson().fromJson(peopleListJson, new TypeToken<List<UserEntity>>() {
            }.getType()); //将json字符串转换成List集合
        }
        return new ArrayList<>();
    }


    public static void putListUserInfo(List<UserEntity> value) {
        if (sharedPreferences == null) {
            return;
        }
        if (null == value){
            sharedPreferences.edit().putString(Constants.SharedPreKey.SELECT_USER_LIST, "").apply();
            return;
        }
        sharedPreferences.edit().putString(Constants.SharedPreKey.SELECT_USER_LIST, new Gson().toJson(value)).apply();
    }


    public static int getDeptId(){
       return SharedPrefsUtil.getUserInfo().getDeptId();
    }

    public static int getUserId(){
       return SharedPrefsUtil.getUserInfo().getUserId();
    }

    public static String getUserIdString(){
        return String.valueOf(SharedPrefsUtil.getUserInfo().getUserId());
    }

}