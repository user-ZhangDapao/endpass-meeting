package com.sdcz.endpass.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BasePopupWindowContentView;

/**
 * @Description 通用的popupWindow视图
 */
public class GlobalPopupView extends BasePopupWindowContentView implements View.OnClickListener {
    private GlobalPopupListener globalPopupListener;
    private TextView contentText;
    private long lastTime;
    private TextView confirmView;
    private View divisionView;
    private TextView cancelView;
    private TextView titleView;
    private ConstraintLayout rootContentView;

    public GlobalPopupView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.global_popup_layout, this, true);
        rootContentView = findViewById(R.id.root_content_view);
        cancelView = findViewById(R.id.tv_cancel);
        cancelView.setOnClickListener(this);
        confirmView = findViewById(R.id.tv_confirm);
        confirmView.setOnClickListener(this);
        contentText = findViewById(R.id.tv_content);
        divisionView = findViewById(R.id.view_division_2);
        titleView = findViewById(R.id.tv_title);
        if (!ScreenUtils.isPortrait()) {
            setLandscapeParams();
        }

    }



    public void setContentText(String contentStr) {
        contentText.setText(contentStr);
    }

    public void setContentText(@StringRes int strId) {
        contentText.setText(strId);
    }

    public void setRightButtonText(String contentStr) {
        confirmView.setText(contentStr);
    }

    public void setRightButtonText(@StringRes int strId) {
        confirmView.setText(strId);
    }

    public void setTitleText(@StringRes int strId) {
        titleView.setText(strId);
    }

    public void setTitleText(String str) {
        titleView.setText(str);
    }


    @Override
    public void onClick(View itemView) {

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime < 300) {
            return;
        }
        lastTime = currentTime;
        int id = itemView.getId();
        if (id == R.id.tv_cancel) {
            dismissPopupWindow();
            if (globalPopupListener != null) {
                globalPopupListener.onClickCancelListener();
            }

        } else if (id == R.id.tv_confirm) {
            dismissPopupWindow();
            if (globalPopupListener != null) {
                globalPopupListener.onClickConfirmListener();
            }

        }
    }


    /**
     * 仅仅显示一个按钮
     */
    public void onlyOneButton() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) confirmView.getLayoutParams();
        layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        cancelView.setVisibility(GONE);
        divisionView.setVisibility(GONE);
        confirmView.setText(R.string.meetingui_i_see);
        confirmView.setBackgroundResource(R.drawable.select_button_shadow_right_left);

    }

    public void setGlobalPopupListener(GlobalPopupListener globalPopupListener) {
        this.globalPopupListener = globalPopupListener;
    }

    public interface GlobalPopupListener {
        default void onClickCancelListener() {
        }

        void onClickConfirmListener();
    }


    /**
     * 从竖屏切换到横屏时调用
     */
    @Override
    public void onLandscapeListener() {
        setLandscapeParams();
    }


    /**
     * 从横屏切换到竖屏时调用
     */
    @Override
    public void onPortraitListener() {
        setPortraitLayoutParams();
    }


    private void setLandscapeParams() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) rootContentView.getLayoutParams();
        layoutParams.matchConstraintPercentWidth = 0.35F;
        rootContentView.requestLayout();
    }


    private void setPortraitLayoutParams() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) rootContentView.getLayoutParams();
        layoutParams.matchConstraintPercentWidth = 0.68F;
        rootContentView.requestLayout();
    }


    /**
     * 横竖屏切换回调
     *
     * @param newConfig 当前Configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onLandscapeListener();
        } else {
            onPortraitListener();
        }
    }
}
