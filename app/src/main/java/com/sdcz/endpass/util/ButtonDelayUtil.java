package com.sdcz.endpass.util;

/**
 * Author: Administrator
 * CreateDate: 2021/7/23 10:30
 * Description: @ 按键延时工具类,用于防止按键连点
 */
public class ButtonDelayUtil {

    private static final int MIN_CLICK_DELAY_TIME = 700;
    private static long lastClickTime;

    public static boolean isFastClick(){
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }
}
