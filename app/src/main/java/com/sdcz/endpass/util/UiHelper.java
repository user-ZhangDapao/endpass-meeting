package com.sdcz.endpass.util;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Looper;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class UiHelper {

    public static boolean isUiThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * 获取默认方向
     *
     * @param context 上下文
     * @return 设备默认方向
     */
    public static int getDeviceDefaultOrientation(Context context) {
        try {
            Configuration config = context.getResources().getConfiguration();
            Display display;
            WindowManager windowManager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            display = windowManager.getDefaultDisplay();
            int rotation = display != null ? display.getRotation() : 0;
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
                if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    return Configuration.ORIENTATION_LANDSCAPE;
                }
            } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    return Configuration.ORIENTATION_LANDSCAPE;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Configuration.ORIENTATION_PORTRAIT;
    }
}
