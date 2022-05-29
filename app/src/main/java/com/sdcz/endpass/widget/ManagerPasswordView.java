package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BasePopupWindowContentView;


/**
 * @Author wuyr
 * @Date 2020/12/30 19:28
 * @Description
 */
public class ManagerPasswordView extends BasePopupWindowContentView implements View.OnClickListener {
    private final Context context;
    private ManagerPasswordViewListener managerPasswordViewListener;
    private EditText inputPassword;
    private ConstraintLayout parentView;
    long lastOnClickTime = 0;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public ManagerPasswordView(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public ManagerPasswordView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.meeting_manager_password_layout, this, true);
        TextView cancelView = findViewById(R.id.tv_cancel);
        cancelView.setOnClickListener(this);
        TextView confirmView = findViewById(R.id.tv_confirm);
        confirmView.setOnClickListener(this);
        parentView = findViewById(R.id.parent_content_view);
        inputPassword = findViewById(R.id.et_input_password);
        if (!ScreenUtils.isPortrait()) {
            updateLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        }
    }


    @Override
    public void onClick(View itemView) {
        if (managerPasswordViewListener == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastOnClickTime < 300) {
            return;
        }
        lastOnClickTime = currentTime;
        int id = itemView.getId();
        if (id == R.id.tv_cancel) {
            dismissPopupWindow();
            managerPasswordViewListener.onClickCancelManagerPasswordListener();
        } else if (id == R.id.tv_confirm) {
            managerPasswordViewListener.onClickConfirmManagerPasswordListener(getInputPasswordText());

        }
    }


    public String getInputPasswordText() {
        Editable editable = inputPassword.getText();
        return editable.toString();
    }


    private void updateLayoutParams(int orientation) {
        LayoutParams layoutParams = (LayoutParams) parentView.getLayoutParams();
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.matchConstraintPercentWidth = 0.40F;
        } else {
            layoutParams.matchConstraintPercentWidth = 0.75F;
        }
        parentView.requestLayout();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            updateLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        } else {
            updateLayoutParams(Configuration.ORIENTATION_PORTRAIT);
        }
    }


    public void setManagerPasswordViewListener(ManagerPasswordViewListener managerPasswordViewListener) {
        this.managerPasswordViewListener = managerPasswordViewListener;
    }

    public interface ManagerPasswordViewListener {
        void onClickCancelManagerPasswordListener();

        void onClickConfirmManagerPasswordListener(String passwordText);
    }
}
