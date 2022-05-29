package com.sdcz.endpass.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class InterruptibleLinearLayout extends LinearLayout {

    private boolean interruptEvent = false;

    public InterruptibleLinearLayout(Context context) {
        super(context);
    }

    public InterruptibleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InterruptibleLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setInterruptEvent(boolean interrupt) {
        interruptEvent = interrupt;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return interruptEvent;
    }
}
