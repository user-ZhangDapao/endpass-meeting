package com.sdcz.endpass.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.sdcz.endpass.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 *
 * @author Gwt
 * @date 2017/12/20
 */

public class StatusBarUtils {

    private static final int TRANSLUCENT_VIEW_ID = R.id.translucent_view;
    public static final int IS_SET_PADDING_KEY = 54648632;
    /**
     * 设备状态栏为透明色
     *
     * @param activity
     */
    public static void setTransparentStatusBar(Activity activity, View topView) {
        setTranslucentStatusBar(activity, topView, 0);
    }

    /**
     * 设置ImageView为第一控件的可以调整透明度的状态栏
     *
     * @param activity
     */
    public static void setTranslucentStatusBar(Activity activity, View topView, int alpha) {
        setARGBStatusBar(activity, topView, 0, 0, 0, alpha);
    }

    /**
     * 设置透明状态栏版本的状态栏的ARGB
     * @param activity
     * @param topView
     * @param r
     * @param g
     * @param b
     * @param alpha
     */
    public static void setARGBStatusBar(Activity activity, View topView, int r, int g, int b, int alpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(Color.argb(alpha, r, g, b));

            ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
            View statusBarView = contentView.findViewById(TRANSLUCENT_VIEW_ID);
            if(statusBarView != null) {
                statusBarView.setFitsSystemWindows(true);
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setARGBStatusViewToAct(activity, r, g, b, alpha);
        }

        if (topView != null) {
            boolean isSetPadding = topView.getTag(IS_SET_PADDING_KEY) != null;
            if (!isSetPadding) {
//                MLog.e("app", "top" + topView.getPaddingTop() + "...bottom" + topView.getPaddingBottom());
                topView.setPadding(topView.getPaddingLeft(), topView.getPaddingTop() + getStatusBarHeight(activity), topView.getPaddingRight(), topView.getPaddingBottom());
//                MLog.e("app", "top" + topView.getPaddingTop() + "...bottom" + topView.getPaddingBottom());
                topView.setTag(IS_SET_PADDING_KEY, true);
            }
        }
    }

    /**
     * 设置状态栏view的ARGB，如果找到状态栏view则直接设置，否则创建一个再设置
     *
     * @param activity
     * @param statusBarAlpha
     */
    private static void setARGBStatusViewToAct(Activity activity, int r, int g, int b, int statusBarAlpha) {

        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View fakeStatusBarView = contentView.findViewById(TRANSLUCENT_VIEW_ID);
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.getVisibility() == View.GONE) {
                fakeStatusBarView.setVisibility(View.VISIBLE);
            }
            fakeStatusBarView.setBackgroundColor(Color.argb(statusBarAlpha, r, g, b));
        } else {
            contentView.addView(createARGBStatusBarView(activity, r, g, b, statusBarAlpha));
        }
    }

    /**
     * 创建和状态栏一样高的矩形，用于改变状态栏ARGB
     *
     * @param activity
     * @param r
     * @param g
     * @param b
     * @param alpha
     * @return
     */
    private static View createARGBStatusBarView(Activity activity, int r, int g, int b, int alpha) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(Color.argb(alpha, r, g, b));
        statusBarView.setId(TRANSLUCENT_VIEW_ID);
        return statusBarView;
    }

    /**
     * 得到statusbar高度
     *
     * @param activity
     * @return
     */
    private static int getStatusBarHeight(Activity activity) {
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return activity.getResources().getDimensionPixelSize(resourceId);
    }


    public static void setStatusBarLightMode(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //如果是6.0以上将状态栏文字改为黑色，并设置状态栏颜色
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                activity.getWindow().setStatusBarColor(color);

                //fitsSystemWindow 为 false, 不预留系统栏位置.
//                ViewGroup mContentView = (ViewGroup) activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
//                View mChildView = mContentView.getChildAt(0);
//                if (mChildView != null) {
//                    ViewCompat.setFitsSystemWindows(mChildView, true);
//                    ViewCompat.requestApplyInsets(mChildView);
//                }

            }
        }
    }

    public static void recoverStatusBarTextColor(Activity activity, int color) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }

    }

    public static void setTranslucentBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow.setStatusBarColor(Color.TRANSPARENT)
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    /**
     * 设置Android状态栏的字体颜色，状态栏为亮色的时候字体和图标是黑色，状态栏为暗色的时候字体和图标为白色
     *
     * @param dark 状态栏字体是否为深色
     */
    public static  void setStatusBarFontIconDark(boolean dark, Activity activity) {
        // 小米MIUI
        try {
            Window window = activity.getWindow();
            Class clazz = activity.getWindow().getClass();
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getDeclaredField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {       //清除黑色字体
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        // 魅族FlymeUI
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        // android6.0+系统
        // 这个设置和在xml的style文件中用这个<item name="android:windowLightStatusBar">true</item>属性是一样的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dark) {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

}
