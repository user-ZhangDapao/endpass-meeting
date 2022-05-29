package com.sdcz.endpass.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.blankj.utilcode.util.ThreadUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseDialog;

public class LoadingDialog extends BaseDialog {

    private ImageView ivLoading;
    private TextView tvLoading;
    private RotateAnimation rotateAnimation;
    private String loadingText;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.NormalDialog);
    }

    public LoadingDialog(@NonNull Context context, String loadingText) {
        super(context, R.style.NormalDialog);
        this.loadingText = loadingText;
    }

    public LoadingDialog(@NonNull Context context, @StringRes int textId) {
        super(context, R.style.NormalDialog);
        this.loadingText = context.getString(textId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        initView();
        if (!TextUtils.isEmpty(loadingText)) {
            tvLoading.setText(loadingText);
        }
    }

    @Override
    public void show() {
        if (!isShowing()) {
            super.show();
            showAnim();
        }
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            super.dismiss();
            if (rotateAnimation != null) {
                rotateAnimation.cancel();
            }
        }
    }

    private void showAnim() {
        if (rotateAnimation == null) {
            rotateAnimation = new RotateAnimation(0.0f, 3600.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(20000);
            rotateAnimation.setRepeatCount(-1);// -1为无限循环
        }
        if (rotateAnimation != null && ivLoading != null) {
            ivLoading.startAnimation(rotateAnimation);
        }
    }

    private void initView() {
        ivLoading = findViewById(R.id.iv_loading);
        tvLoading = findViewById(R.id.tv_loading);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    public void updateText(String text) {
        ThreadUtils.runOnUiThread(() -> {
            if (tvLoading != null) {
                tvLoading.setText(text);
            }
        });
    }

    public void updateText(@StringRes int stringId) {
        ThreadUtils.runOnUiThread(() -> {
            if (tvLoading != null) {
                tvLoading.setText(stringId);
            }
        });
    }
}
