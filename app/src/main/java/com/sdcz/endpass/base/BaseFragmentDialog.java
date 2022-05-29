package com.sdcz.endpass.base;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.ScreenUtils;
import com.sdcz.endpass.R;

import java.lang.reflect.Field;

public class BaseFragmentDialog extends DialogFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        boolean portrait = ScreenUtils.isPortrait();
        getDialog().getWindow().setGravity(portrait ? Gravity.BOTTOM : Gravity.BOTTOM | Gravity.END);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setWindowAnimations(R.style.bottom_dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    /**
     * @Description: 针对 illegalAccessException: Can not perform this action after onSaveInstanceState
     * @Description: onSaveInstanceState方法是在该Activity即将被销毁前调用来保存Activity数据的，如果在保存玩状态后再给它添加Fragment就会出错
     * @Description: 把commit（）方法替换成 commitAllowingStateLoss()
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/16 16:40
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/16 16:40
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            Field fieldDismissed = this.getClass().getSuperclass().getDeclaredField("mDismissed");
            Field fieldShownByMe = this.getClass().getSuperclass().getDeclaredField("mShownByMe");
            fieldDismissed.setAccessible(true);
            fieldShownByMe.setAccessible(true);
            fieldDismissed.setBoolean(this, false);
            fieldShownByMe.setBoolean(this, true);
        } catch (NoSuchFieldException noSuchFieldException) {
            noSuchFieldException.printStackTrace();
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        }
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}
