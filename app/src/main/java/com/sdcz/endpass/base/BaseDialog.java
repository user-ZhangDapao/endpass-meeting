package com.sdcz.endpass.base;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class BaseDialog extends Dialog implements LifecycleObserver {
    private static final String TAG = "BaseDialog";
    protected Context ctx;

    public BaseDialog(@NonNull Context context) {
        super(context);
        this.ctx = context;
        if (context instanceof LifecycleOwner) {
            LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(this);
        }
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.ctx = context;
        if (context instanceof LifecycleOwner) {
            LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(this);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        dismiss();
    }

    @Override
    public void show() {
        try {
            // 如果其context对象是activity，则需要先判断activity是否是活跃的，如果不是则直接忽略掉
            super.show();
        } catch (Exception exception) {
            Log.w(TAG, "show dialog exception:" + exception.getMessage());
        }
    }

    @Override
    public void dismiss() {
        try {
            // 如果其context对象是activity，则需要先判断activity是否是活跃的，如果不是则直接忽略掉
            super.dismiss();
        } catch (Exception exception) {
            Log.w(TAG, "dismiss dialog exception:" + exception.getMessage());
        }
    }
}
