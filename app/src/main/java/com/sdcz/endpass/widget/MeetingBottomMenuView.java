package com.sdcz.endpass.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.Transition;

import com.comix.meeting.entities.BaseUser;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseMeetingMenuBar;
import com.sdcz.endpass.callback.BottomMenuLocationUpdateListener;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.nativeapi.adaptor.RolePermission;
import com.sdcz.endpass.ui.activity.UserPopActivity;

public class MeetingBottomMenuView extends BaseMeetingMenuBar {
    private static final String TAG = "MeetingBottomMenuView";
    private static final String MIPMAP = "mipmap";
    private static final String MIPMAP_NAME = "tb_mic_open";

    private MotionLayout rootLayout;
    public ConstraintLayout menuContentView;
    public AppCompatTextView micMenu;
    public TextView cameraMenu;
    public TextView shareMenu;
    private TextView quitMenu;
    public TextView attenderMenu;//参会人
    private TextView moreMenu;
    private TextView stopShareView;
    private ImageView changeSubTabView;
    private TextView tabSumTv;
    private ImageView shareMarkView;
    private ImageView sharedRotating;
    private ImageView sharedDownloadView;
    private MeetingBottomMenuListener meetingBottomMenuListener;
    private ConstraintLayout meetingSharedToolBar;
    private ImageView nailSharedBarButton;
    private final boolean isShowSharedBar = true;
    private boolean isAlwaysShowSharedBar;
    private static final int ANIMATION_HIDE_OVERALL_TYPE = 1;
    private static final int ANIMATION_SHOW_TYPE = 2;
    private static final int ANIMATION_HIDE_SHARE_BAR_TYPE = 3;
    private ValueAnimator startAnimator;
    private ValueAnimator endAnimator;
    private BottomMenuLocationUpdateListener bottomMenuLocationUpdateListener;
    private UserManager userModel;
    private MicViewState currentMicIconState;
    private CameraViewState currentCameraViewState;

    private String channelCode;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetingBottomMenuView(@NonNull Context context, String channelCode) {
        super(context);
        this.channelCode = channelCode;
    }


    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public MeetingBottomMenuView(@NonNull Context context, @Nullable AttributeSet attrs, String channelCode) {
        super(context, attrs);
        this.channelCode = channelCode;
    }

    /**
     * 初始化视图
     *
     * @return 根View
     */
    @Override
    protected ConstraintLayout initView() {
        LayoutInflater.from(context).inflate(R.layout.meeting_bottom_menu_bar, this, true);
        rootLayout = findViewById(R.id.menu_root_layout);
        menuContentView = rootLayout.findViewById(R.id.menu_content_layout);
        micMenu = findViewById(R.id.tv_menu_mic);
        cameraMenu = findViewById(R.id.tv_menu_camera);
        shareMenu = findViewById(R.id.tv_menu_share);
        quitMenu = findViewById(R.id.tv_menu_quit);
        moreMenu = findViewById(R.id.tv_menu_more);
        attenderMenu = findViewById(R.id.tv_menu_attender);
        //共享工具条UI
        nailSharedBarButton = findViewById(R.id.im_shared_lock);
        meetingSharedToolBar = findViewById(R.id.shared_bar_layout);
        stopShareView = findViewById(R.id.tv_stop_share);
        changeSubTabView = findViewById(R.id.im_change_label);
        tabSumTv = findViewById(R.id.tv_tab_sum);
        shareMarkView = findViewById(R.id.im_change_edit);
        sharedRotating = findViewById(R.id.im_shared_rotating);
        sharedDownloadView = findViewById(R.id.im_shared_download);
        userModel = SdkUtil.getUserManager();
        setMicStateNone();
        setCameraStateNone();
        return rootLayout;

    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {
        applyConstraintSet.clone(rootLayout);
        resetConstraintSet.clone(rootLayout);
        setShareLockState(true);
    }


    /**
     * 初始化事件监听
     */
    @Override
    protected void initEvent() {
        //设置点击事件监听
        micMenu.setOnClickListener(this);
        cameraMenu.setOnClickListener(this);
        shareMenu.setOnClickListener(this);
        quitMenu.setOnClickListener(this);
        attenderMenu.setOnClickListener(this);
        moreMenu.setOnClickListener(this);
        stopShareView.setOnClickListener(this);
        shareMarkView.setOnClickListener(this);
        changeSubTabView.setOnClickListener(this);
        sharedRotating.setOnClickListener(this);
        sharedDownloadView.setOnClickListener(this);
        nailSharedBarButton.setOnClickListener(this);
    }

    /**
     * @Description: 设置麦克风图标状态
     * @Author: xingwt
     * @return:
     * @Param: micIconState 麦克风的视图状态
     * @Param: isLocalDisable 是否关闭麦克风
     * @CreateDate:2021/3/18 20:18
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/18 20:18
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void setMicIconState(MicViewState micIconState, boolean isLocalDisable) {
        setMicIconState(micIconState, !isLocalDisable, 0);
    }

    /**
     * @Description: 设置麦克风图标状态
     * @Author:
     * @return:
     * @Param: micIconState 麦克风的视图状态
     * @Param: isLocalDisable 是否关闭麦克风
     * @Param: soundEnergy 麦克风的能量值
     * @CreateDate:
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/18 20:18
     * @UpdateRemark: 更新说明  添加能量值
     * @Version: 1.0
     */
    public void setMicIconState(MicViewState micIconState, boolean isLocalDisable, int soundEnergy) {
        micMenu.setActivated(isLocalDisable);
        currentMicIconState = micIconState;
        switch (micIconState) {
            case AUDIO_STATE_DONE:
                int id;
                if (!isLocalDisable) {
                    id = getResources().getIdentifier(MIPMAP_NAME + getMicStateLogo(soundEnergy),
                            MIPMAP, getContext().getPackageName());
                } else {
                    id = getResources().getIdentifier(MIPMAP_NAME + getMicStateLogo(0),
                            MIPMAP, getContext().getPackageName());
                }
                setMenuDrawableType(micMenu,
                        id, R.color.white, R.string.meetingui_mute);
                break;
            case AUDIO_STATE_WAITING:
                setMenuDrawableType(micMenu,
                        R.drawable.selector_mic_await_speak_state,
                        R.color.white, R.string.meetingui_select_give_apply);
                break;

            case AUDIO_STATE_NONE:
                setMicStateNone();
                break;

            default:
        }

    }

    public MicViewState getCurrentMicIconState() {
        return currentMicIconState;
    }

    /**
     * 设置视频Icon状态
     *
     * @param cameraIconState 当前视频图标的状态
     *                        //正在广播视频
     *                        CameraViewState.CAMERA_STATE_DONE,
     *                        //正在广播视频，摄像头处于禁用状态
     *                        CameraViewState.CAMERA_STATE_DONE_FORBIDDEN,
     *                        //未广播视频
     *                        CameraViewState.CAMERA_STATE_NONE,
     *                        //未广播视频，麦克风为不可用状态
     *                        CameraViewState.CAMERA_STATE_NONE_FORBIDDEN,
     *                        //正在申请广播视频
     *                        CameraViewState.CAMERA_STATE_WAITING,
     *                        //在申请广播视频，麦克风为不可用状态
     *                        CameraViewState.CAMERA_STATE_WAITING_FORBIDDEN,
     */
    public void setCameraIconState(CameraViewState cameraIconState, boolean isLocalDisable) {
        cameraMenu.setActivated(isLocalDisable);
        currentCameraViewState = cameraIconState;
        switch (cameraIconState) {
            case CAMERA_STATE_DONE:
                setMenuDrawableType(cameraMenu,
                        R.drawable.selector_camera_open_state, R.color.white, R.string.meetingui_stop_video);
                break;
            case CAMERA_STATE_WAITING:
                setMenuDrawableType(cameraMenu,
                        R.drawable.selector_camera_applying_state,
                        R.color.white, R.string.meetingui_select_give_apply);
                break;

            case CAMERA_STATE_NONE:
                setCameraStateNone();
                break;
            default:
        }

    }

    /**
     * @Description: 返回麦克风能量等级 当前 0~18
     * @Author: xingwt
     * @return:
     * @Param: energy 0~100
     * @CreateDate: 2021/3/18 20:31
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/18 20:31
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public int getMicStateLogo(int energy) {
        int chekerNum = 100 / 18;
        chekerNum = energy / chekerNum;
        if (chekerNum < 0) {
            chekerNum = 0;
        }
        if (chekerNum > 18) {
            chekerNum = 18;
        }
        return chekerNum;
    }

    public CameraViewState getCurrentCameraViewState() {
        return currentCameraViewState;
    }

    /**
     * 设置Mic没有广播时的文本
     */
    private void setMicStateNone() {
        BaseUser local = userModel.getLocalUser();
        if (local.isSpeechDone() || local.isSpeechWait()) {
            return;
        }
        int none = R.string.meetingui_unmute;
        int id = R.drawable.selector_mic_stop_speak_state;
        if (!SdkUtil.getPermissionManager().checkUserPermission(local.getUserId(), true, RolePermission.BROADCAST_OWN_AUDIO)) {
            none = R.string.meetingui_select_apply_for;
            id = R.drawable.selector_mic_stop_speak_state;// R.mipmap.tb_mic_disable;
        }
        setMenuDrawableType(micMenu,
                id, R.color.white, none);
    }

    private void setCameraStateNone() {
        BaseUser local = userModel.getLocalUser();
        if (local.isVideoDone() || local.isVideoWait()) {
            return;
        }
        int none = R.string.meetingui_turn_on_video;
        int id = R.drawable.selector_camera_close_state;
        if (!SdkUtil.getPermissionManager().checkUserPermission(local.getUserId(), true, RolePermission.BROADCAST_OWN_VIDEO)) {
            none = R.string.meetingui_select_apply_for;
            id = R.drawable.selector_camera_close_state;// R.mipmap.tb_mic_disable;
        }

        setMenuDrawableType(cameraMenu,
                id, R.color.white, none);
    }

    /**
     * 设置菜单图标和字体样式
     *
     * @param itemView      需要设置的itemView
     * @param drawableResId 图标id
     * @param colorResId    字体颜色Id
     */
    private void setMenuDrawableType(TextView itemView, @DrawableRes int drawableResId,
                                     @ColorRes int colorResId, @StringRes int strId) {
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable drawable = getResources().getDrawable(drawableResId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        itemView.setCompoundDrawables(null, drawable, null, null);
        itemView.setTextColor(getResources().getColor(colorResId));
        itemView.setText(strId);
    }


    /**
     * 在开始动画前，需要修改约束布局中的一些约束关系，否则动画无效
     *
     * @return 约束布局参数
     */
    private LayoutParams createSlideHideLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        layoutParams.height = 0;
        layoutParams.matchConstraintPercentHeight = 0.55F;
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.leftToLeft = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        return layoutParams;
    }


    /**
     * 隐藏底部菜单栏
     */
    @Override
    public void slideHide() {
        cancelTimer();
        if (endAnimator != null) {
            boolean running = endAnimator.isRunning();
            if (running) {
                return;
            }
        }
        menuContentView.setLayoutParams(createSlideHideLayoutParams());
        endAnimator = slideAnimation(ANIMATION_HIDE_OVERALL_TYPE);
    }


    /**
     * 显示底部菜单栏
     */
    @Override
    public void slideShow() {
        if (startAnimator != null) {
            boolean running = startAnimator.isRunning();
            if (running) {
                return;
            }
        }
        restartTime();
        startAnimator = slideAnimation(ANIMATION_SHOW_TYPE);
    }


    /**
     * 隐藏底部菜单栏
     */
    public void fadeHide() {
        rootLayout.setVisibility(INVISIBLE);
    }

    /**
     * 显示菜单栏
     */
    public void fadeShow() {
        rootLayout.setVisibility(VISIBLE);
    }

    /**
     * 菜单点击事件回调
     *
     * @param itemView 被点击的View
     */
    @Override
    public void onClick(View itemView) {
        //每一次点击菜单的时候都重新开始计时，超过5秒没有操作回调onOverTimeListener()

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime < MIX_TIME) {
            return;
        }
        lastTime = currentTime;
        if (meetingBottomMenuListener != null) {
            int id = itemView.getId();
            if (id == R.id.tv_menu_share) {
                restartTime();
                meetingBottomMenuListener.onClickSharedListener(itemView);
            } else if (id == R.id.tv_menu_quit) {
                restartTime();
                meetingBottomMenuListener.onClickQuitListener(itemView);
            } else if (id == R.id.tv_menu_mic) {
                restartTime();
                meetingBottomMenuListener.onClickMicListener(itemView);
            } else if (id == R.id.tv_menu_camera) {
                restartTime();
                meetingBottomMenuListener.onClickCameraListener(itemView);
            } else if (id == R.id.tv_menu_more) {
                restartTime();
                meetingBottomMenuListener.onClickMoreListener(itemView);
            } else if (id == R.id.tv_stop_share) {
                meetingBottomMenuListener.onClickStopShareItemListener();
            } else if (id == R.id.im_change_label) {
                meetingBottomMenuListener.onClickChangeTabItemListener();
            } else if (id == R.id.im_change_edit) {
                meetingBottomMenuListener.onClickShareMarkItemListener();
            } else if (id == R.id.im_shared_rotating) {
                meetingBottomMenuListener.onClickShareRotatingItemListener();
            } else if (id == R.id.im_shared_download) {
                meetingBottomMenuListener.onClickShareDownloadItemListener();
            } else if (id == R.id.im_shared_lock) {
                setShareLockState(!isAlwaysShowSharedBar);
            } else if (id == R.id.tv_menu_attender) {
                meetingBottomMenuListener.onClickAttendeeListener(channelCode);
            }
        }
    }

    private void setShareLockState(boolean isLocked) {
        int resId = isLocked
                ? R.drawable.select_share_tool_lock : R.drawable.select_share_tool_unlock_default;
        nailSharedBarButton.setImageResource(resId);
        isAlwaysShowSharedBar = isLocked;
        if (!isAlwaysShowSharedBar && !isShowMenuBar) {
            slideAnimation(ANIMATION_HIDE_SHARE_BAR_TYPE);
        }
    }


    /**
     * 滑动动画（隐藏和显示底部菜单栏的动画）
     *
     * @param animType 动画类型 (#ANIMATION_HIDE_TYPE 隐藏动画) (#ANIMATION_SHOW_TYPE 显示动画）
     */
    private ValueAnimator slideAnimation(final int animType) {
        float startValue = 0;
        float endValue = 0;
        int meetingSharedToolBarHeight = meetingSharedToolBar.getMeasuredHeight();
        int hideHeight = rootLayout.getMeasuredHeight();
        if (isShowSharedBar && !isAlwaysShowSharedBar) {
            hideHeight = hideHeight + meetingSharedToolBarHeight;
        }

        if (animType == ANIMATION_HIDE_OVERALL_TYPE) {
            startValue = meetingSharedToolBarHeight;
            endValue = hideHeight;
        } else if (animType == ANIMATION_SHOW_TYPE) {
            startValue = hideHeight;
            endValue = meetingSharedToolBarHeight;
        } else if (animType == ANIMATION_HIDE_SHARE_BAR_TYPE) {
            startValue = rootLayout.getMeasuredHeight();
            endValue = startValue + meetingSharedToolBarHeight;
        }

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startValue, endValue);
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
        LayoutParams layoutParams = (LayoutParams) menuContentView.getLayoutParams();
        int finalHideHeight = hideHeight;
        valueAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            layoutParams.topMargin = (int) (animatedValue + 0.5);
            menuContentView.requestLayout();
            if (bottomMenuLocationUpdateListener != null) {
                float marginBottom = finalHideHeight - animatedValue;
                bottomMenuLocationUpdateListener.onUpdateShareBarLocationListener(marginBottom);
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (animType == ANIMATION_HIDE_OVERALL_TYPE) {
                    isShowMenuBar = false;
                } else if (animType == ANIMATION_SHOW_TYPE) {
                    isShowMenuBar = true;
                }
            }
        });
        return valueAnimator;
    }


    /**
     * 显示共享工具条
     */
    public void showSharedBar() {
        meetingSharedToolBar.setVisibility(View.VISIBLE);
    }

    /**
     * 设置标注按钮是否可用
     */
    public void setMarkViewEnabled(boolean enabled) {
        shareMarkView.setEnabled(enabled);
    }
    /**
     * 设置标签按钮是否可用
     */
    public void setSubTabEnabled(boolean enabled) {
        changeSubTabView.setEnabled(enabled);
    }

    /**
     * 设置旋转按钮是否可用
     */
    public void setRotatingViewEnabled(boolean enabled) {
        sharedRotating.setEnabled(enabled);
    }

    /**
     * 设置保存按钮是否可用
     */
    public void setSaveViewEnabled(boolean enabled) {
        sharedDownloadView.setEnabled(enabled);
    }

    public void setTabNum(int num) {
        tabSumTv.setText(String.valueOf(num));
    }

    /**
     * 隐藏共享工具条
     */
    public void hideSharedBar() {
        meetingSharedToolBar.setVisibility(View.INVISIBLE);
    }

    /**
     * 底部菜单位置发送变化时，回调监听
     *
     * @param bottomMenuLocationUpdateListener 监听接口
     */
    public void setBottomMenuLocationUpdateListener(BottomMenuLocationUpdateListener bottomMenuLocationUpdateListener) {
        this.bottomMenuLocationUpdateListener = bottomMenuLocationUpdateListener;
    }


    public interface MeetingBottomMenuListener {

        /**
         * 点击麦克风按钮回调
         *
         * @param micMenuView 当前被点击的View
         */
        void onClickMicListener(View micMenuView);

        /**
         * 点击相机按钮回调
         *
         * @param cameraMenuView 当前被点击的view
         */
        void onClickCameraListener(View cameraMenuView);

        /**
         * 点击 共享 按钮回调
         *
         * @param sharedMenuView 当前被点击的View
         */
        void onClickSharedListener(View sharedMenuView);

        /**
         * 点击 退出 按钮回调
         *
         * @param quitMenuView 当前被点击的View
         */
        void onClickQuitListener(View quitMenuView);

        /**
         * 点击 更多 按钮回调
         *
         * @param moreMenuView 当前被点击的View
         */
        void onClickMoreListener(View moreMenuView);

        /**
         * 点击“参会人”按钮回调
         */
        void onClickAttendeeListener(String channelCode);

        /**
         * 点击共享工具条锁定按钮
         *
         * @param isLock 是否锁定
         */
        void onClickShareBarLockListener(boolean isLock);

        /**
         * 点击共享工具条中的“停止共享按钮”
         */
        void onClickStopShareItemListener();

        /**
         * 点击共享工具条中的“切换页签”按钮
         */
        void onClickChangeTabItemListener();

        /**
         * 点击共享工具条中的“标记”按钮
         */
        void onClickShareMarkItemListener();

        /**
         * 点击共享工具条中的“旋转”按钮
         */
        void onClickShareRotatingItemListener();

        /**
         * 点击共享工具条中的“下载”按钮
         */
        void onClickShareDownloadItemListener();


    }


    /**
     * 添加底部菜单栏事件监听
     *
     * @param meetingBottomMenuListener MeetingBottomMenuListener
     */
    public void addMeetingBottomMenuListener(MeetingBottomMenuListener meetingBottomMenuListener) {
        this.meetingBottomMenuListener = meetingBottomMenuListener;
    }

    /**
     * 清理工作
     */
    @Override
    public void recycle() {
        if (startAnimator != null) {
            startAnimator.cancel();
        }
        if (endAnimator != null) {
            endAnimator.cancel();
        }

        meetingBottomMenuListener = null;
        super.recycle();
    }


    /**
     * 动画开始回调
     *
     * @param transition transition
     * @see Transition.TransitionListener
     */
    @Override
    public void onTransitionStart(Transition transition) {

    }

    /**
     * 动画结束回调
     *
     * @param transition 动画转换器
     * @see Transition.TransitionListener
     */
    @Override
    public void onTransitionEnd(Transition transition) {

    }

    /**
     * 麦克风Item视图状态
     */
    public enum MicViewState {
        //正在发言
        AUDIO_STATE_DONE,
        //未发言
        AUDIO_STATE_NONE,
        //正在申请发言
        AUDIO_STATE_WAITING,
    }

    /**
     * 麦克风Item视图状态
     */
    public enum CameraViewState {
        //正在广播视频
        CAMERA_STATE_DONE,
        //未广播视频
        CAMERA_STATE_NONE,
        //正在申请广播视频
        CAMERA_STATE_WAITING,
    }

}
