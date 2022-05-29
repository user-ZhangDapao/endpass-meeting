package com.sdcz.endpass.util;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.blankj.utilcode.util.ScreenUtils;

/**
 * @Description 软键盘工具类
 */
public class SoftKeyboardHelper {
    public static final String TAG = "SoftKeyboard_Debug";
    private InputMethodManager imm;
    private final Context context;
    private int lastScreenOrientation;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public SoftKeyboardHelper(Context context) {
        this.context = context;
        if (ScreenUtils.isPortrait()) {
            lastScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            lastScreenOrientation = Configuration.ORIENTATION_LANDSCAPE;
        }
    }


    /**
     * 显示软键盘
     *
     * @param view 触发软键盘的EditText
     */
    public synchronized void showSoftKeyboard(EditText view) {
        if (imm == null) {
            imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        imm.showSoftInput(view, 0);
    }

    /**
     * 隐藏软键盘
     *
     * @param view 触发软键盘的EditText
     */
    public synchronized void hideSoftKeyboard(EditText view) {
        if (imm == null) {
            imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * 监听软键盘弹出监听
     *
     * @param parentLayout         父容器View
     * @param softKeyboardListener 监听接口
     */
    public void setKeyboardListener(final View parentLayout, final SoftKeyboardListener softKeyboardListener) {
        if (softKeyboardListener == null || parentLayout == null) {
            return;
        }
        parentLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int screenHeight = ScreenUtils.getScreenHeight();
                int currentScreenOrientation = -1;
                if (ScreenUtils.isPortrait()) {
                    currentScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
                } else {
                    currentScreenOrientation = Configuration.ORIENTATION_LANDSCAPE;
                }
                //排除横竖屏切换引起的布局变化
                if (lastScreenOrientation != currentScreenOrientation) {
                    lastScreenOrientation = currentScreenOrientation;
                    return;
                }

                int defaultHeight = screenHeight / 3;
                if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > defaultHeight)) {
                    softKeyboardListener.onSoftKeyboardShow(0);
                } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > defaultHeight)) {
                    softKeyboardListener.onSoftKeyboardHide(0);
                }

            }
        });
    }


    /**
     * 软键盘事件监听接口
     */
    public interface SoftKeyboardListener {
        /**
         * 软键盘弹出监听回调
         *
         * @param softKeyboardHeight 软键盘高度
         */
        void onSoftKeyboardShow(int softKeyboardHeight);

        /**
         * 软键盘隐藏监听回调
         *
         * @param softKeyboardHeight 软键盘高度
         */
        void onSoftKeyboardHide(int softKeyboardHeight);
    }


}
