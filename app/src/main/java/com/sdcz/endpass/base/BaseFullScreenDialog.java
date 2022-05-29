package com.sdcz.endpass.base;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.sdcz.endpass.R;


/**
 * @author yinhui
 * @date create at 2021/1/6
 * @description
 */
public class BaseFullScreenDialog extends BaseDialog {

    public BaseFullScreenDialog(@NonNull Context context) {
        super(context, R.style.meetingui_dialog_full_screen_with_status_bar_style);
    }

    public BaseFullScreenDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void show() {
        setWindowParams();
        super.show();
    }

    private void setWindowParams() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.meetingui_dialog_rele_animation);
    }
}
