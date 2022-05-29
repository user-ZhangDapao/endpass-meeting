package com.sdcz.endpass.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.Visibility;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.callback.PopupTransitionListener;
import com.sdcz.endpass.callback.PopupWindowCommunicationListener;
import com.sdcz.endpass.callback.PopupWindowStateListener;

/**
 * @Author wuyr
 * @Date 2020/12/18 10:43
 * @Description
 */
public class PopupWindowBuilder
        implements PopupWindowCommunicationListener.PopupWindowCommunicationInterior, PopupTransitionListener {

    private static final String TAG ="PopupWindowBuilder";
    private final PopupWindow popupWindow;
    private View parent;
    private int gravity = Gravity.BOTTOM;
    private int offsetX = 0;
    private int offsetY = 0;
    private View contentView;

    private final Context context;
    private final Slide inTransition;
    private final Slide outTransition;
    private final Fade fadeIn;
    private final Fade fadeOut;
    private boolean isUseAnimation = true;
    private long duration = 300;
    private final int slideEdge = Gravity.BOTTOM;
    private PopupWindowStateListener popupWindowStateListener;
    private final FrameLayout popupWindowRootView;
    private View lastContentView;
    private AnimationType currentAnimationType;
    private boolean outsideTouchable = true;
    private boolean isShowShade = true;
    private boolean isSuspendWindow;
    private WindowManager.LayoutParams windowLayoutParams;
    private WindowManager windowManager;
    private PopWindowCallBack popWindowCallBack;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public PopupWindowBuilder(Context context) {
        this.context = context;
        popupWindow = new PopupWindow();
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindowRootView = (FrameLayout) LayoutInflater.from(context)
                .inflate(R.layout.popup_parent_layout, null, true);
        popupWindow.setContentView(popupWindowRootView);
        popupWindow.setWidth(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(outsideTouchable);
        inTransition = new Slide();
        outTransition = new Slide();
        fadeIn = new Fade();
        fadeOut = new Fade();
        initEvent();

    }

    private void initEvent() {
        setWindowEnterAnimationListener(fadeIn);
        setWindowEnterAnimationListener(inTransition);
        setWindowExitAnimationListener(fadeOut);
        setWindowExitAnimationListener(outTransition);
        popupWindowRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View itemView) {
                if (outsideTouchable) {
                    dismissDialog();
                }

            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lastContentView = null;
                contentView = null;
                windowLayoutParams = null;
                windowManager = null;
                if (popWindowCallBack != null) {
                    popWindowCallBack.dimissCallBack();
                    popWindowCallBack = null;
                }
            }
        });

    }


    public PopupWindowBuilder setFocusable(boolean focusable) {
        popupWindow.setFocusable(focusable);
        return this;
    }

    /**
     * 是否显示半透明遮罩层
     *
     * @param isShowShade true 显示 反之不显示
     */
    private void isShowShade(boolean isShowShade) {
        this.isShowShade = isShowShade;
    }


    /**
     * 是否悬浮在一切页面之上
     *
     * @param isSuspendWindow true 悬浮于一切页面之上 反之则按照打开顺序显示
     */
    public PopupWindowBuilder setSuspendWindow(boolean isSuspendWindow) {
        this.isSuspendWindow = isSuspendWindow;
        initWindowParams();
        return this;
    }


    public boolean isSuspendWindow() {
        return isSuspendWindow;
    }

    private void initWindowParams() {
        // 建立item的缩略图
        if (windowLayoutParams == null) {
            windowLayoutParams = new WindowManager.LayoutParams();
            windowLayoutParams.gravity = Gravity.CENTER;// 这个必须加
            windowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            windowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            windowLayoutParams.format = PixelFormat.TRANSLUCENT;
            windowLayoutParams.windowAnimations = 0;
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

    }


    /**
     * 设置动画进入和退出动画
     */
    private void setAnimationParams() {
        if (!isUseAnimation) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Visibility tempIn;
            Visibility tempOut;

            if (currentAnimationType == AnimationType.FADE) {
                popupWindow.setEnterTransition(null);
                popupWindow.setExitTransition(null);
                tempIn = fadeIn;
                tempOut = fadeOut;
            } else {
                if (ScreenUtils.isPortrait()) {
                    inTransition.setSlideEdge(Gravity.BOTTOM);
                    outTransition.setSlideEdge(Gravity.BOTTOM);
                } else {
                    inTransition.setSlideEdge(Gravity.END);
                    outTransition.setSlideEdge(Gravity.END);
                }
                tempIn = inTransition;
                tempOut = outTransition;
            }

            tempIn.setDuration(duration);
            tempOut.setDuration(duration);
            tempIn.setInterpolator(new AccelerateDecelerateInterpolator());
            tempOut.setInterpolator(new AccelerateDecelerateInterpolator());
            popupWindow.setEnterTransition(tempIn);
            popupWindow.setExitTransition(tempOut);
            //将一些值恢复为默认值
            currentAnimationType = null;
            duration = 300;
        } else {
            popupWindow.setAnimationStyle(R.style.vertical_popup_style);
        }
    }

    private void setWindowEnterAnimationListener(Visibility tempIn) {
        //进入动画监听
        tempIn.addListener(new PopupTransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                if (popupWindowStateListener != null) {
                    popupWindowStateListener.onOpenPopupWindowListener();
                }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if (isShowShade) {
                    popupWindowRootView.setBackgroundResource(R.color.color_55000000);
                } else {
                    popupWindowRootView.setBackgroundResource(R.color.transparent);
                }
            }
        });
    }

    private void setWindowExitAnimationListener(Visibility tempOut) {
        //退出动画监听
        tempOut.addListener(new PopupTransitionListener() {
            @Override
            public void onTransitionEnd(Transition transition) {
                if (popupWindowStateListener != null) {
                    popupWindowStateListener.onClosePopupWindowListener();
                }

            }

            @Override
            public void onTransitionStart(Transition transition) {
                if (isShowShade) {
                    popupWindowRootView.setBackgroundResource(R.color.transparent);
                }
            }
        });
    }


    /**
     * 设置popupWindow进入、退出动画的持续时长，不设置默认为300
     *
     * @param duration 动画持续时长（单位毫秒）
     * @return PopupWindowBuilder
     */
    public PopupWindowBuilder setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    /**
     * 是否使用进入和退出动画，不设置默认为使用动画
     *
     * @param isUse true 使用动画  false 禁用动画
     * @return PopupWindowBuilder
     */
    public PopupWindowBuilder isUseAnimation(boolean isUse) {
        this.isUseAnimation = isUse;
        return this;
    }


    /**
     * 设置PopupWindow 的显示位置，不设置默认使用Activity的根View 并且显示在Activity底部
     *
     * @param parent parentView
     * @return PopupWindowBuilder
     */
    public PopupWindowBuilder setAtLocation(View parent) {
        this.parent = parent;
        return this;
    }

    /**
     * 设置PopupWindow 的显示位置，不设置默认使用Activity的根View 并且显示在Activity底部
     *
     * @param parent  parentView
     * @param gravity gravityModel
     * @return PopupWindowBuilder
     */
    public PopupWindowBuilder setAtLocation(View parent, int gravity) {
        this.parent = parent;
        this.gravity = gravity;
        return this;
    }

    /**
     * 设置PopupWindow 的显示位置，不设置默认使用Activity的根View 并且显示在Activity底部
     *
     * @param parent  parentView
     * @param gravity gravityModel
     * @param offsetX 偏移
     * @param offsetY 偏移
     * @return PopupWindowBuilder
     */
    public PopupWindowBuilder setAtLocation(View parent, int gravity, int offsetX, int offsetY) {
        this.parent = parent;
        this.gravity = gravity;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }


    /**
     * 设置popupWindow的显示视图
     *
     * @param contentView 显示的View
     * @return PopupWindowBuilder
     */
    public PopupWindowBuilder setContentView(View contentView) {
        if (contentView == null) {
            throw new RuntimeException("contentView cannot null");
        }
        Log.i(TAG,"重新设置视图：");
        lastContentView = this.contentView;
        this.contentView = contentView;
        if (contentView instanceof PopupWindowCommunicationListener) {
            ((PopupWindowCommunicationListener) contentView).setPopupWindowCorrelationListener(this);
        }
        return this;
    }

    /**
     * 设置动画的滑动模式,不设置默认从底部滑出
     * {@link Gravity#LEFT},左边滑出  {@link Gravity#TOP},顶部滑出
     * {@link Gravity#RIGHT},右边滑出 {@link Gravity#BOTTOM},底部滑出
     * {@link Gravity#START}, {@link Gravity#END}.
     */
    public PopupWindowBuilder setSlideMode(@androidx.transition.Slide.GravityFlag int slideEdge) {
        inTransition.setSlideEdge(slideEdge);
        outTransition.setSlideEdge(slideEdge);
        popupWindow.update();
        return this;
    }


    /**
     * 设置动画类型，不设置默认为AnimationType.SLIDE 滑出滑入
     *
     * @param animationType 动画类型
     *                      AnimationType.SLIDE 滑出滑入
     *                      AnimationType.FADE 淡出淡入
     */
    public PopupWindowBuilder setAnimationType(AnimationType animationType) {
        currentAnimationType = animationType;
        return this;
    }


    /**
     * 是否支持点击外部关闭Popup
     *
     * @param outsideTouchable ture 支持点击外部关闭，反之则不支持
     * @return PopupWindowBuilder
     */
    public PopupWindowBuilder setOutsideTouchable(boolean outsideTouchable) {
        this.outsideTouchable = outsideTouchable;
        return this;
    }

    /**
     * 显示PopupWindow,替换显示
     */
    public void show() {
        if (contentView == null) {
            throw new NullPointerException("contentView cannot null, "
                    + "please call #setContentView(...)# set contentView view");
        }
        if (isSuspendWindow && windowManager != null && windowLayoutParams != null) {
            windowManager.addView(contentView, windowLayoutParams);
        } else {
            show(contentView, false);
        }

    }

    /**
     * 显示PopupWindow
     *
     * @param contentView popupWindow的视图View
     */
    private void show(View contentView, boolean isCoverage) {
        if (contentView == null) {
            throw new NullPointerException("contentView cannot null");
        }
        setAnimationParams();
        if (!isCoverage) {
            popupWindowRootView.removeAllViews();
        }
        popupWindowRootView.addView(contentView);
        popupWindow.update();
        if (popupWindow.isShowing()) {
            //如果popupWindow正在显示，只替换布局
            return;
        }
        if (parent == null) {
            //如果没有调用setAtLocation(View parent)方法设置 parent 则默认使用Activity的根布局
            parent = ActivityUtils.getTopActivity().getWindow().getDecorView();
        }
        popupWindow.showAtLocation(parent, gravity, offsetX, offsetY);
    }


    /**
     * 覆盖显示
     */
    public void coverageShow() {
        show(contentView, true);
    }


    /**
     * PopupWindow的状态监听，用于提供给外界监听PopupWindow的打开和关闭状态
     *
     * @param popupWindowStateListener 状态监听接口
     */
    public void setPopupWindowStateListener(PopupWindowStateListener popupWindowStateListener) {
        this.popupWindowStateListener = popupWindowStateListener;
    }


    @Override
    public void dismissChildren() {
        int childCount = popupWindowRootView.getChildCount();
        if (childCount > 1) {
            popupWindowRootView.removeViewAt(childCount - 1);
        }
    }

    @Override
    public void dismissDialog() {
        if (isSuspendWindow && windowLayoutParams != null && windowManager != null) {
            windowManager.removeView(contentView);
            isSuspendWindow = false;
        } else {
            popupWindow.dismiss();
        }

    }

    /**
     * 返回上一个视图
     */
    @Override
    public void back() {
        if (lastContentView != null && !isSuspendWindow) {
            popupWindowRootView.removeAllViews();
            //处理横竖屏变化问题
            if (lastContentView instanceof BasePopupWindowContentView) {
                BasePopupWindowContentView lastContentView = (BasePopupWindowContentView) this.lastContentView;
                if (ScreenUtils.isPortrait()) {
                    lastContentView.onPortraitListener();
                } else {
                    lastContentView.onLandscapeListener();
                }
            }
            popupWindowRootView.addView(lastContentView);
            this.contentView = lastContentView;
            lastContentView = null;
            popupWindow.update();
        } else {
            dismissDialog();
        }
    }

    @Override
    public PopupWindowBuilder getPopupWindowBuilder() {
        return this;
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public PopupWindowBuilder setPopWindowCallBack(PopWindowCallBack popWindowCallBack) {
        this.popWindowCallBack = popWindowCallBack;
        return this;
    }

    /**
     * 动画类型
     * AnimationType.SLIDE 滑出滑入
     * AnimationType.FADE 淡出淡入
     */
    public enum AnimationType {
        /**
         * SLIDE 滑出滑入
         * FADE 淡出淡入
         */
        SLIDE,
        FADE
    }

    /**
     * @Description: 添加popWindow回调
     * @Author: xingwt
     * @CreateDate: 2021/3/19 10:11
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/19 10:11
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public interface PopWindowCallBack {
        /**
         * @Description: 当前 popwin 消失回调
         * @Author: xingwt
         * @return: void
         * @CreateDate: 2021/3/19 10:11
         * @UpdateUser: xingwt
         * @UpdateDate: 2021/3/19 10:11
         * @UpdateRemark: 更新说明
         * @Version: 1.0
         */
        void dimissCallBack();
    }
}
