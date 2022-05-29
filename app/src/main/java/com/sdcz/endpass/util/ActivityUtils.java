package com.sdcz.endpass.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ActivityUtils {

    private static ActivityUtils activityUtils;
    private Map<String, Activity> activityMap = new HashMap<String, Activity>();
    private Activity activity;

    public static ActivityUtils getInstance() {
        if (activityUtils == null) {
            activityUtils = new ActivityUtils();

        }
        return activityUtils;
    }

    /**
     * 保存指定key值的activity（activity启动时调用）
     *
     * @param key
     * @param activity
     */
    public void addActivity(String key, Activity activity) {
        if (activityMap.get(key) == null) {
            activityMap.put(key, activity);
        }
    }

    /**
     * 移除指定key值的activity （activity关闭时调用）
     *
     * @param key
     */
    public void delActivity(String key) {
        Activity activity = activityMap.get(key);
        if (activity != null) {
            if (activity.isDestroyed() || activity.isFinishing()) {
                activityMap.remove(key);
                return;
            }
            activity.finish();
            activityMap.remove(key);
        }
    }

    public void moveAcitivityKey(String key) {
        Activity activity = activityMap.get(key);
        if (activity != null) {
            if (activity.isDestroyed() || activity.isFinishing()) {
                activityMap.remove(key);
                return;
            }
            activityMap.remove(key);
        }
    }

    /**
     * 获取某个activity实例
     * @param key
     * @return
     */
    public Activity getActivity(String key){
        Activity activity = activityMap.get(key);
        return activity;
    }

    /**
     * @desc 销毁所有的Activity
     */
    public void finishAll() {
        Iterator iter = activityMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            activity = (Activity) entry.getValue();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
    }

    public void StartActivity(Context context, Class clzss, Bundle bundle) {
        Intent intent = new Intent(context, clzss);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }

    public void StartFragmentToActivity(Context context, Class clzss, Bundle bundle) {
        Intent intent = new Intent(context, clzss);
        intent.putExtra("bundle", bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void finishActivity(AppCompatActivity activity, boolean isFinish) {
        if (!isFinish) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!activity.isDestroyed()) {
                activity.finish();
            }
        } else {
            activity.finish();
        }
    }
}
