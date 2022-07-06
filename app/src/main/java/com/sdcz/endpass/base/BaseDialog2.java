package com.sdcz.endpass.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.inpor.log.Logger;
import com.inpor.manager.util.UiHelper;
import com.sdcz.endpass.R;

import butterknife.ButterKnife;

/**
 * [function]
 * [detail]
 * Created by Sky on 2017/2/8.
 */

public abstract class BaseDialog2 extends Dialog {
    private static final String TAG = "BaseDialog";
    protected Context context;

    public BaseDialog2(Context context) {
        this(context, R.style.dialog_global_style);
    }

    public BaseDialog2(Context context, int themeId) {
        super(context, themeId);
        this.context = context;
    }

    @Override
    public void setContentView(@NonNull View view) {
        super.setContentView(view);
        ButterKnife.bind(this);
    }

    @Override
    public void setContentView(@NonNull View view, @Nullable ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        ButterKnife.bind(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    public void show() {
        try {
            // 如果其context对象是activity，则需要先判断activity是否是活跃的，如果不是则直接忽略掉
            if (context instanceof Activity) {
                if (UiHelper.isActivityActive((Activity) context)) {
                    super.show();
                } else {
                    Logger.info(TAG, "the activity is finishing return, do not call show");
                }

            } else {
                super.show();
            }

        } catch (Exception exception) {
            Logger.error(TAG, "show dialog exception", exception);
        }
    }

    @Override
    public void dismiss() {
        try {
            // 如果其context对象是activity，则需要先判断activity是否是活跃的，如果不是则直接忽略掉
            if (context instanceof Activity) {
                if (UiHelper.isActivityActive((Activity) context)) {
                    super.dismiss();
                } else {
                    Logger.warn(TAG, "the activity is finishing return, do not call dismiss");
                }
            } else {
                super.dismiss();
            }

        } catch (Exception exception) {
            Logger.error(TAG, "dismiss dialog exception", exception);
        }
    }

    protected abstract void initViews();

    protected abstract void initValues();

    protected abstract void initListener();

    /**
     * 如果传入的context是Activity
     * 或者(context instanceof contextWrapper).getBaseContext()为activity，当前Activity在运行返回true，否则返回false
     */
    protected boolean isAttachedActivityRunning() {
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof ContextWrapper) {
            Context tempContext = ((ContextWrapper) context).getBaseContext();
            activity = tempContext instanceof Activity ? (Activity) tempContext : null;
        }
        return UiHelper.isActivityActive(activity);
    }
}
