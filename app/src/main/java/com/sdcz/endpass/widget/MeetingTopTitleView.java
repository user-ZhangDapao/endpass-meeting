package com.sdcz.endpass.widget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseMeetingMenuBar;
import com.inpor.base.sdk.video.CustomImageOnOffEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MeetingTopTitleView extends BaseMeetingMenuBar {
    private static final String TAG = "MeetingTopTitleView";
    private ConstraintLayout contentView;
    private ImageView volumeSwitch;
    private ImageView changeCamera;
    private ImageView imSetting;
    private TextView titleTextView;
    private TextView outTextView;//退出
    private ImageView ivInfo;
    private Chronometer chronometer;
    private MeetingTopTitleListener meetingTopTitleListener;
    private long lastClickTime;
    private boolean mutesWitch;

    private String channelCode;
    private int meetingType = 3;
    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetingTopTitleView(@NonNull Context context, String channelCode) {
        super(context);
        this.channelCode = channelCode;
        if(EventBus.getDefault().isRegistered(this) == false )
        {
            EventBus.getDefault().register(this);
        }
    }


    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public MeetingTopTitleView(@NonNull Context context, @Nullable AttributeSet attrs, String channelCode, int meetingType) {
        super(context, attrs);
        this.channelCode = channelCode;
        this.meetingType = meetingType;
        if(EventBus.getDefault().isRegistered(this) == false )
        {
            EventBus.getDefault().register(this);
        }
    }

    //changeCamera

    //add by baodian
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_of_image_open(CustomImageOnOffEvent event) {

            if( event.on == true || event.show_camera_change_icon == false ) {
                changeCamera.setVisibility(View.GONE);
            }
            else
            {
                changeCamera.setVisibility(View.VISIBLE);
            }

    }




    @Override
    protected ConstraintLayout initView() {
        LayoutInflater.from(context).inflate(R.layout.meeting_top_title_bar, this);
        rootView = findViewById(R.id.root_view);
        contentView = findViewById(R.id.content_view);
        volumeSwitch = findViewById(R.id.im_audio);
        changeCamera = findViewById(R.id.im_camera);
        titleTextView = findViewById(R.id.tv_title_content);
        chronometer = findViewById(R.id.meeting_time);
        outTextView = findViewById(R.id.tv_out);
        imSetting = findViewById(R.id.im_setting);
        ivInfo = findViewById(R.id.im_tip);
        if (meetingType != 3){
            imSetting.setVisibility(GONE);
        }
        return rootView;
    }

    @Override
    protected void initData() {
        initChronometer();
    }

    /**
     * 初始化计算器与时间格式
     */
    private void initChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    @Override
    protected void initEvent() {
        volumeSwitch.setOnClickListener(this);
        changeCamera.setOnClickListener(this);
        outTextView.setOnClickListener(this);
        imSetting.setOnClickListener(this);
        ivInfo.setOnClickListener(this);
    }


    public void setCameraIconState(boolean show) {
        changeCamera.setVisibility(show ? VISIBLE : GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        chronometer.stop();

    }

    /**
     * 设置标题内容
     *
     * @param titleText 需要显示的内容
     */
    public void setTitleText(String titleText) {
        titleTextView.setText(titleText);
    }

    /**
     * 隐藏底部菜单栏
     */
    @Override
    public void slideHide() {
        cancelTimer();
        TransitionManager.beginDelayedTransition(rootView, autoTransition);
        int measuredHeight = rootView.getMeasuredHeight();
        applyConstraintSet.setMargin(R.id.content_view, ConstraintSet.BOTTOM, measuredHeight);
        applyConstraintSet.applyTo(rootView);
    }


    /**
     * 显示顶部标题栏
     */
    @Override
    public void slideShow() {
        TransitionManager.beginDelayedTransition(rootView, autoTransition);
        resetConstraintSet.applyTo(rootView);
        //显示的菜单的时候开始计时，超过5秒没有操作回调onOverTimeListener()
        restartTime();
    }


    /**
     * 隐藏底部菜单栏
     */
    public void fadeHide() {
        contentView.setVisibility(GONE);
    }

    /**
     * 显示菜单栏
     */
    public void fadeShow() {
        contentView.setVisibility(VISIBLE);
    }


    /**
     * 菜单点击事件回调
     *
     * @param view 被点击的View
     */
    @Override
    public void onClick(View view) {
        final long defaultTime = 300;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < defaultTime) {
            return;
        }
        lastClickTime = currentTime;
        //每一次点击菜单的时候都重新开始计时，超过5秒没有操作回调onOverTimeListener()
        restartTime();
        if (meetingTopTitleListener != null) {
            int id = view.getId();
            if (id == R.id.im_camera) {
                meetingTopTitleListener.onClickChangeCameraItemListener(view);
            } else if (id == R.id.im_audio) {
                if (mutesWitch) {
                    volumeSwitch.setActivated(false);
                    meetingTopTitleListener.onClickOpenAudioListener();
                } else {
                    volumeSwitch.setActivated(true);
                    meetingTopTitleListener.onClickCloseAudioListener();
                }
                mutesWitch = !mutesWitch;
            } else if (id == R.id.tv_out) {
                meetingTopTitleListener.onClickQuitListener(view, meetingType);
            } else if (id == R.id.im_tip) {
                meetingTopTitleListener.onClickMeetingInfoListener();
            } else if (id == R.id.im_setting) {
                meetingTopTitleListener.onClickLeftOtherListener(channelCode);
            }
        }
    }


    /**
     * 设置扬声器静音状态
     *
     * @param isMute true 静音 false非静音
     */
    public void setVolumeSwitchState(boolean isMute) {
        if (isMute) {
            volumeSwitch.setActivated(false);
            mutesWitch = false;
        } else {
            volumeSwitch.setActivated(true);
            mutesWitch = true;
        }
    }

    /**
     * 顶部菜单栏点击事件监听
     *
     * @param meetingTopTitleListener 事件监听接口
     */
    public void addMeetingTopTitleListener(MeetingTopTitleListener meetingTopTitleListener) {
        this.meetingTopTitleListener = meetingTopTitleListener;
    }

    /**
     * 顶部菜单按钮点击事件监听
     */
    public interface MeetingTopTitleListener {
        /**
         * 点击切换相机item 回调
         *
         * @param view 当前被点击的View
         */
        void onClickChangeCameraItemListener(View view);

        /**
         * 点击“打开音频” 按钮回调
         */
        void onClickOpenAudioListener();

        /**
         * 点击“关闭音频” 按钮回调
         */
        void onClickCloseAudioListener();

        /**
         * 点击 退出 按钮回调
         *
         * @param quitMenuView 当前被点击的View
         */
        void onClickQuitListener(View quitMenuView, int meetingType);

        /**
         * 点击会议信息按钮回调
         */
        void onClickMeetingInfoListener();


        /**
         * 点击左上角其它
         */
        void onClickLeftOtherListener(String channelCode);
    }


    @Override
    public void recycle() {
        super.recycle();
        chronometer.stop();
        EventBus.getDefault().unregister(this);
    }
}
