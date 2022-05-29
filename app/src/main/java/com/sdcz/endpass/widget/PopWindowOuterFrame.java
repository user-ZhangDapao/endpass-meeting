package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.ScreenUtils;
import com.sdcz.endpass.LiveDataBus;
import com.sdcz.endpass.R;

/**
 * @ClassName: PopWindowOuterFrame PopWindow 弹窗外边框
 * @Description:
 * @Author: xingwt
 * @CreateDate: 2021/4/8 16:24
 * @UpdateUser: xingwt
 * @UpdateDate: 2021/4/8 16:24
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PopWindowOuterFrame extends ConstraintLayout {
    private final Context context;
    private LinearLayout llLayout;
    private RelativeLayout rlLayout;
    private TextView tvTitle;
    private ImageView ivClose;
    private OnClickCallBack onClickCallBack;

    /**
     * @Description: 初始化
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:11
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:11
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public PopWindowOuterFrame(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    /**
     * @Description: 初始化
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:11
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:11
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public PopWindowOuterFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    /**
     * @Description: 初始化
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:11
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:11
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public PopWindowOuterFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     * @Description: 初始化
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:11
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:11
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public PopWindowOuterFrame(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    /**
     * @Description: 初始化
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:12
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:12
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    private void init() {
        inflate(context, R.layout.layout_pop_window_outer_frame, this);
        llLayout = findViewById(R.id.ll_layout);
        rlLayout = findViewById(R.id.rl_layout);
        tvTitle = findViewById(R.id.tv_title);
        ivClose = findViewById(R.id.im_close);
        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickCallBack != null) {
                    onClickCallBack.onBack();
                }
            }
        });
        MutableLiveData<Configuration> liveData = LiveDataBus.getInstance().
                getLiveData(LiveDataBus.KEY_MEETING_ACTIVITY_CONFIG);
        liveData.observeForever(this::configChange);
        if (ScreenUtils.isPortrait()) {
            initPortraitLayoutParams();
        } else {
            initLandLayoutParams();
        }
    }

    private void configChange(Configuration configuration) {
        int orientation = configuration.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onLandscapeListener();
        } else {
            onPortraitListener();
        }
    }

    /**
     * @Description: 显示tiitle
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:12
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:12
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void setTitleShow() {
        rlLayout.setVisibility(VISIBLE);
    }

    /**
     * @Description: 设置title文案
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:12
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:12
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    /**
     * @Description: 设置title文案
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:12
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:12
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void setTitle(int id) {
        tvTitle.setText(id);
    }

    /**
     * @Description: 添加内部布局
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/8 19:16
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:16
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void layoutAddView(View view) {
        llLayout.addView(view);
    }

    public void setOnClickCallBack(OnClickCallBack onClickCallBack) {
        this.onClickCallBack = onClickCallBack;
    }

    /**
     * 从竖屏切换到横屏时调用
     */
    public void onLandscapeListener() {
        initLandLayoutParams();
    }


    /**
     * 从横屏切换到竖屏时调用
     */
    public void onPortraitListener() {
        initPortraitLayoutParams();
    }

    private void initLandLayoutParams() {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.leftToLeft = R.id.guideline_vertical_left;
        llLayout.setBackgroundResource(R.drawable.shape_select_shared_right);
        llLayout.setLayoutParams(layoutParams);
    }

    private void initPortraitLayoutParams() {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.leftToLeft = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.topToTop = R.id.guideline_horizontal_top;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        llLayout.setBackgroundResource(R.drawable.shape_select_shared);
        llLayout.setLayoutParams(layoutParams);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MutableLiveData<Configuration> liveData = LiveDataBus.getInstance().
                getLiveData(LiveDataBus.KEY_MEETING_ACTIVITY_CONFIG);
        liveData.removeObserver(this::configChange);
    }

    /**
     * @ClassName: onClickCallBack
     * @Description: 点击回调
     * @Author: xingwt
     * @CreateDate: 2021/4/8 19:24
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/8 19:24
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public interface OnClickCallBack {
        /**
         * @Description: 关闭电机回调
         * @Author: xingwt
         * @return:
         * @Parame:
         * @CreateDate: 2021/4/8 19:25
         * @UpdateUser: xingwt
         * @UpdateDate: 2021/4/8 19:25
         * @UpdateRemark: 更新说明
         * @Version: 1.0
         */
        void onBack();
    }
}
