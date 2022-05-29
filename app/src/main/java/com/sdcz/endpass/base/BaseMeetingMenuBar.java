package com.sdcz.endpass.base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;

import com.sdcz.endpass.model.MeetingMenuTimer;


/**
 *  会中菜单栏的基类
 */
public abstract class BaseMeetingMenuBar extends ConstraintLayout
        implements Transition.TransitionListener,
        View.OnClickListener, MeetingMenuTimer.TimerListener {
    private static final String TAG = "BaseMeetingMenuBar";
    protected ConstraintSet applyConstraintSet = new ConstraintSet();
    protected ConstraintSet resetConstraintSet = new ConstraintSet();
    private static final int ANIMATION_DURATION = 300;
    private MeetingMenuTimer.Builder meetingMenuTimer;
    protected AutoTransition autoTransition;
    protected Context context;
    protected ConstraintLayout rootView;
    protected long lastTime;
    protected static final long MIX_TIME = 300;
    protected boolean isShowMenuBar = true;


    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public BaseMeetingMenuBar(@NonNull Context context) {
        super(context);
        this.context = context;
        rootView = initView();
        initPublicData(rootView);
        initEvent();
        initTimer();
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public BaseMeetingMenuBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        rootView = initView();
        initPublicData(rootView);
        initEvent();
        initTimer();
    }


    /**
     * 初始化公共数据
     *
     * @param rootView 根ViewGroup
     */
    private void initPublicData(ConstraintLayout rootView) {

        //初始化菜单弹进弹出动画参数和相关配置
        applyConstraintSet.clone(rootView);
        resetConstraintSet.clone(rootView);
        autoTransition = new AutoTransition();
        autoTransition.setDuration(ANIMATION_DURATION);
        //初始化子类参数，该方法为抽象方法由子类实现
        initData();
        //设置动画事件监听
        autoTransition.addListener(this);


    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (autoTransition != null) {
            autoTransition.removeListener(this);
        }
    }

    /**
     * 初始化定时器
     */
    private void initTimer() {
        meetingMenuTimer = new MeetingMenuTimer.Builder()
                .setOverTime(5000)
                .addTimerListener(this)
                .restartTime();
    }


    /**
     * 重新计时
     */
    public void restartTime() {
        Log.i(TAG, "重新计时");
        if (meetingMenuTimer != null) {
            meetingMenuTimer.restartTime();
        }
    }


    /**
     * 取消定时计时
     */
    public void cancelTimer() {
        Log.i(TAG, "取消计时");
        if (meetingMenuTimer != null) {
            meetingMenuTimer.cancelTimer();
        }

    }

    /**
     * 初始化视图
     *
     * @return 根布局
     */
    protected abstract ConstraintLayout initView();


    /**
     * 子类初始化数据
     */
    protected void initData() {

    }

    /**
     * 初始化事件监听
     */
    protected abstract void initEvent();


    /**
     * 隐藏视图
     */
    protected abstract void slideHide();

    /**
     * 显示视图
     */
    protected abstract void slideShow();


    /**
     * 菜单点击事件外部监听接口
     */
    public interface MenuOnClockListener {
        /**
         * 点击菜单item回调
         *
         * @param childItemView 当前点击的菜单View
         */
        void onMenuClock(View childItemView);
    }


    /**
     * 默认时间内无操作回调该方法
     *
     * @see MeetingMenuTimer.TimerListener
     */
    @Override
    public void onOverTimeListener() {
        //隐藏菜单
        this.post(this::slideHide);
    }


    /**
     * 菜单是否显示
     *
     * @return popupWindow是否為顯示狀態
     */
    public boolean isShowMenuBar() {
        return isShowMenuBar;
    }

    /**
     * 清理工作
     */
    protected void recycle() {
        if (meetingMenuTimer != null) {
            meetingMenuTimer.recycle();
            meetingMenuTimer = null;
        }

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
     * 动画被取消回调
     *
     * @param transition 动画转换器
     * @see Transition.TransitionListener
     */
    @Override
    public void onTransitionCancel(Transition transition) {

    }

    /**
     * 动画暂停回调
     *
     * @param transition 动画转换器
     * @see Transition.TransitionListener
     */
    @Override
    public void onTransitionPause(Transition transition) {

    }

    /**
     * 动画重新启动回调
     *
     * @param transition 动画转换器
     * @see Transition.TransitionListener
     */
    @Override
    public void onTransitionResume(Transition transition) {

    }


}
