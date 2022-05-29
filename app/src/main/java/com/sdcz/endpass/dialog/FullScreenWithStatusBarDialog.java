package com.sdcz.endpass.dialog;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseDialog;

/**
 * [全屏不覆盖系统顶部状态栏对话框]
 */
public abstract class FullScreenWithStatusBarDialog extends BaseDialog {
    /**
     * 构造器
     * */
    public FullScreenWithStatusBarDialog(Context context, boolean statusBarVisible) {
        this(context, statusBarVisible
                ? R.style.dialog_full_screen_with_status_bar_style
                : R.style.dialog_full_screen_style);
    }

    public FullScreenWithStatusBarDialog(Context context, int themeId) {
        super(context, themeId);
    }

    @Override
    public void show() {
        setFullScreen();
        setWindowAnimations();
        super.show();
    }

    private void setFullScreen() {
        Window win = getWindow();
        if (win == null) {
            return;
        }
        win.getDecorView().setPadding(0, 0, 0, 0);
        LayoutParams lp = win.getAttributes();

        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);
    }

    private void setWindowAnimations() {
        if (getWindow() != null) {
            getWindow().setWindowAnimations(R.style.dialog_right_enter_left_exit_animation);
        }
    }
}
