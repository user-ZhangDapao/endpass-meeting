package com.sdcz.endpass.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.List;

public class DeviceUtil {

    /**
     * 检测设备是否支持发送屏幕共享
     *
     * @return 版本小于5.0，或者宽度小于720，或者高度小于1280，都不支持
     */
    public static boolean checkDeviceSupportVncSend(Context context) {
        return Build.VERSION.SDK_INT >= 21 && getScreenShortSideLength(context.getApplicationContext()) >= 720
                && getScreenLongSideLength(context.getApplicationContext()) >= 1280;
    }


    /**
     * 获得屏幕较短一边
     */
    public static int getScreenShortSideLength(Context context) {
        return Math.min(getScreenRealHeight(context), getScreenRealWidth(context));
    }

    /**
     * 获得屏幕较长一边
     */
    public static int getScreenLongSideLength(Context context) {
        return Math.max(getScreenRealHeight(context), getScreenRealWidth(context));
    }

    /**
     * 获得屏幕真实高度，包含虚拟按键的高度
     *
     * @param context context
     * @return 屏幕高度
     */
    public static int getScreenRealHeight(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();

        manager.getDefaultDisplay().getRealMetrics(metrics);

        return metrics.heightPixels;
    }

    /**
     * 获得屏幕真实宽度
     *
     * @param context context
     * @return 屏幕宽度
     */
    public static int getScreenRealWidth(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.widthPixels;
    }
    /**
     * 当前应用是否位于前台
     *
     * @param context 上下文
     * @return true 前台  flase 后台
     */
    public static boolean isAppOnForeground(Context context) {
        String appName = getCurrentProcessName();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    if (appProcess.processName.equals(appName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * 通过Application新的API获取进程名
     */
    public static String getCurrentProcessName() {
        String processName = null;
        try {
            final Method declaredMethod =
                    Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader())
                            .getDeclaredMethod("currentProcessName");
            declaredMethod.setAccessible(true);
            final Object invoke = declaredMethod.invoke(null);
            if (invoke instanceof String) {
                processName = (String) invoke;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return processName;
    }
}
