package com.sdcz.endpass.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ThreadUtils;
import com.comix.meeting.MeetingModule;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.LayoutType;
import com.comix.meeting.entities.MeetingInfo;
import com.comix.meeting.entities.VideoInfo;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.CustomImageEvent;
import com.sdcz.endpass.model.MicEnergyMonitor;
//import com.sdcz.endpass.util.YUVUtil;
import com.inpor.base.sdk.video.CustomImageOnOffEvent;
import com.inpor.base.sdk.video.VideoManager;
import com.inpor.nativeapi.adaptor.RoomWndState;
import com.inpor.nativeapi.interfaces.ConfigChannel;
import com.inpor.nativeapi.interfaces.VideoRenderNotify;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author yinhui
 * @date create at 2020/11/30
 * @description
 */
public class VideoScreenView extends FrameLayout implements View.OnClickListener,
        View.OnTouchListener, MicEnergyMonitor.AudioEnergyListener {


    public static final int DISPLAY_MODE_TILE = 1;
    public static final int DISPLAY_MODE_RATIO_CUT = 2;
    public static final int DISPLAY_MODE_RATIO_INTEGRITY = 3;

    private static final String TAG = "VideoScreenView";
    private static final String MIPMAP = "mipmap";
    private static final String MIPMAP_NAME = "meeting_video_mic_b_open";
    private final MeetingModule proxy;
    private final VideoManager videoModel;

    @Override
    public void onAudioEnergyChanged(List<BaseUser> sources) {
        for (BaseUser user : sources) {
            if (videoInfo != null && videoInfo.getVideoUser() != null
                    && user.getUserId() == videoInfo.getVideoUser().getUserId()) {
                ThreadUtils.getMainHandler().post(() -> updateUserInfoTvImg(user));
                return;
            }
        }
    }


    public enum VideoState {
        /**
         * 初始化状态
         */
        STATE_NONE,
        /**
         * 播放
         */
        STATE_START,
        /**
         * 暂停
         */
        STATE_PAUSE
    }

    private VideoInfo videoInfo;
    /**
     * 绑定的VideoScreenView的ID
     */
    private long renderId;
    private final RenderNotify renderNotify;
    /**
     * 视频播放窗体
     */
    private SurfaceView surfaceView;

    //add by baodian
    //private SurfaceView custom_surfaceview;
    /*

     */

    private boolean isSurfaceCreated;
    /**
     * 自定义加载 ProgressBar View和动画效果
     */
    private View progressBackgroundView;
    private ProgressBar progressBar;
    /**
     * 展示用户信息的图片
     */
    protected TextView userInfoTv;
    private TextView videoInfoTextView;
    private ImageView backgroundImageView;
    /**
     * 当前surface的order状态
     */
    private boolean isMediaOverlay;
    private int displayMode = DISPLAY_MODE_RATIO_CUT;
    /**
     * 记录视频的状态
     */
    private VideoState videoState = VideoState.STATE_NONE;
    private boolean interruptReceive = false;
    private boolean enableCamera = true;
    private long lastClickTime;

    private String micType;

    /**
     * 点击次数
     */
    private int tapCount;
    private final Runnable singleTapRunnable = new Runnable() {
        @Override
        public void run() {
            tapCount = 0;
            if (videoInfo == null) {
                return;
            }
            MeetingInfo meetingInfo = proxy.getMeetingInfo();
            if (meetingInfo.layoutType == LayoutType.VIDEO_LAYOUT && meetingInfo.splitStyle
                    == RoomWndState.SplitStyle.SPLIT_STYLE_P_IN_P) {
                videoModel.changeVideoToFirst(videoInfo);
            }
        }
    };

    /**
     * 构造
     *
     * @param context 上下文
     */
    public VideoScreenView(@NonNull Context context) {
        super(context);
        // 视频布局
        createUserInfoTextView();
        createProgressBar();
        createVideoInfoTextView();
        createDisableImageView();
        //
        renderNotify = new RenderNotify();
        setOnTouchListener(this);
        VideoManager videoManager = SdkUtil.getVideoManager();
        proxy = videoManager.getMeetingModule();
        videoModel = SdkUtil.getVideoManager();

        if( EventBus.getDefault().isRegistered(this) == false ) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(singleTapRunnable);
        tapCount = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (videoInfo != null && !videoInfo.isLocalUser()) {
            ViewGroup.LayoutParams progressBarParam = progressBar.getLayoutParams();
            int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
            int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
            int size = Math.min(measuredWidth, measuredHeight) / 4;
            measureChild(progressBar, widthMeasureSpec, heightMeasureSpec);
            progressBarParam.width = Math.min(progressBar.getMeasuredWidth(), size);
            progressBarParam.height = Math.min(progressBar.getMeasuredHeight(), size);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View view) {
        if (view == surfaceView && videoInfo != null) {
            tapCount++;
            if (tapCount <= 1) {
                postDelayed(singleTapRunnable, 300);
            } else {
                removeCallbacks(singleTapRunnable);
                tapCount = 0;
                videoModel.changeVideoToFirst(videoInfo);
                videoModel.setVideoFullScreen(videoInfo, !videoInfo.isFullScreen());
            }
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (videoInfo != null) {
                tapCount++;
                if (tapCount <= 1) {
                    lastClickTime = System.currentTimeMillis();
                    postDelayed(singleTapRunnable, 300);
                } else {
                    removeCallbacks(singleTapRunnable);
                    tapCount = 0;
                    if (System.currentTimeMillis() - lastClickTime <= 300) {
                        BaseUser localUser = SdkUtil.getUserManager().getLocalUser();
                        boolean isBroadcaster = localUser.isBroadcaster();
                        boolean notFullScreen = !videoInfo.isFullScreen();
                        localUser.setBroadcaster(
                                localUser.isMainSpeakerDone()
                                        && (!videoInfo.getVideoUser().isLocalUser()
                                        || !SdkUtil.getVideoManager().isDisplayingLocalVideo()));
                        videoModel.setVideoFullScreen(videoInfo, notFullScreen);
                        localUser.setBroadcaster(isBroadcaster);
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setInterruptReceive(boolean interruptReceive) {
        this.interruptReceive = interruptReceive;
    }

    /**
     * 刷新用户信息
     *
     * @param baseUser 视频用户
     */
    public void refreshUserInfo(BaseUser baseUser) {
        if (baseUser == null) {
            return;
        }
        videoInfo.setVideoUser(baseUser);
        updateUserInfoTvName(baseUser);
    }

    /**
     * 刷新用户音频状态
     *
     * @param baseUser 视频用户
     */
    public void refreshUserAudioState(BaseUser baseUser) {
        if (baseUser == null) {
            return;
        }
        MicEnergyMonitor.getInstance().removeAudioSource(videoInfo.getVideoUser(), micType);
        videoInfo.setVideoUser(baseUser);
        MicEnergyMonitor.getInstance().addAudioSource(videoInfo.getVideoUser(), micType);
        updateUserInfoTvImg(baseUser);
    }

    /**
     * 设置视频窗口z序
     *
     * @param isMediaOverlay true or false
     */
    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        this.isMediaOverlay = isMediaOverlay;
        if (videoInfo == null || surfaceView == null) {
            return;
        }
        removeView(surfaceView);
        surfaceView.setZOrderOnTop(isMediaOverlay);
        surfaceView.setZOrderMediaOverlay(isMediaOverlay);
        addView(surfaceView, 0);
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能：将视频View和VideoInfo进行绑定，实现视频的打开操作以及相应效果┊
     * ┊                                                                      ┊
     * ┊ 参  数：info 需要与View绑定的单个视频模块信息                   ┊
     * ┊ 返回值：无                                                           ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    public void attachVideoInfo(VideoInfo info) {
        // info 是空 则返回
        if (info == null) {
            return;
        }
        this.videoInfo = info;
        //初始化监听能量条
        micType = MicEnergyMonitor.VIDEO_SCREEN_VIEW + hashCode();
        MicEnergyMonitor.getInstance().addAudioEnergyListener(this, micType);
        isSurfaceCreated = false;
        updateUserInfoTvName(videoInfo.getVideoUser());
        // 初始化 能量值
        updateUserInfoTvImg(videoInfo.getVideoUser());
        createSurfaceView();
        bindRender();
        //surface view 默认overlay
        surfaceView.setZOrderMediaOverlay(isMediaOverlay);
        // 每次attachVideoInfo时都需要重置surfaceView和backgroundImageView的可视状态，因为本地禁用时会改变
        if (surfaceView.getVisibility() != View.VISIBLE) {
            surfaceView.setVisibility(View.VISIBLE);
        }
        if (backgroundImageView.getVisibility() != View.GONE) {
            backgroundImageView.setVisibility(View.GONE);
        }
        addView(surfaceView);
        addView(backgroundImageView);
        addView(progressBackgroundView);
        addView(progressBar);
        addView(userInfoTv);
        addView(videoInfoTextView);
        updateUI(videoInfo.isReceiveVideo());
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能： 将视频关闭并与VideoInfo信息进行解绑                         ┊
     * ┊                                                                      ┊
     * ┊ 参  数： 无                                                          ┊
     * ┊ 返回值： 无                                                          ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    public void detachVideoInfo() {
        if (this.videoInfo == null) {
            return;
        }
        unBindRender();
        if (!videoInfo.isLocalUser()) {
            videoModel.stopReceiveVideo(videoInfo.getUserId(), videoInfo.getMediaId());
        }
        surfaceView.getHolder().removeCallback(surfaceCallback);
        /*
        if( custom_surfaceview != null )
        {
            custom_surfaceview.getHolder().removeCallback(_surfaceCallback);
        }
         */

        //YUVUtil.getInstance().setAlready_local_preview(false);
        VideoManager videomanager = SdkUtil.getVideoManager();
        videomanager.setAlready_local_preview(false);


        /*
        if( YUVUtil.getInstance().get_observe_send_data() != null )
        {
            YUVUtil.getInstance().get_observe_send_data().notify_detach_view(custom_surfaceview);
        }*/

        removeAllViews();
        videoInfo = null;
        isSurfaceCreated = false;
        videoState = VideoState.STATE_NONE;
        MicEnergyMonitor.getInstance().removeAudioEnergyListener(this, micType);
    }



    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "surfaceCreated");
            isSurfaceCreated = true;
            if (videoInfo == null) {
                return;
            }
            if (videoInfo.isLocalUser()) {
                renderId = videoModel.startLocalVideoView(videoInfo.getMediaId(), surfaceView, renderNotify);
                videoModel.setLocalRenderDisplayMode(videoInfo.getMediaId(), renderId, displayMode);
                setProgressViewVisibility(INVISIBLE);
                videoState = VideoState.STATE_START;
            } else {
                startOrPauseVideo(true);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "surfaceDestroyed");
            isSurfaceCreated = false;
            if (videoInfo == null) {
                return;
            }
            // 本地视频的,不用关闭相机，不然远程采集不到了
            if (videoInfo.isLocalUser()) {
                VideoManager videoManager = SdkUtil.getVideoManager();
                videoManager.stopLocalVideoView(videoInfo.getMediaId(), renderId);
                renderId = 0;
                videoState = VideoState.STATE_PAUSE;
            } else {
                // 远程视频的存在且是打开的情况下,防止退出应用的时候，底层关闭和surface的关闭重叠操作了
                startOrPauseVideo(false);
                setProgressViewVisibility(VISIBLE);
            }
        }
    };

    /**
     * 设置显示模式
     *
     * @param mode 显示模式
     */
    public void setDisplayMode(int mode) {
        this.displayMode = mode;
        if (videoInfo == null) {
            return;
        }
        if (videoInfo.isLocalUser()) {
            videoModel.setLocalRenderDisplayMode(videoInfo.getMediaId(), renderId, mode);
        } else {
            videoModel.setRemoteRenderDisplayMode(renderId, mode);
        }
    }

    /**
     * 当收到是否接收视频通知时，根据状态设置UI，以及开关视频
     *
     * @param receiveVideo true 显示视频；false 关闭视频
     */
    public void receiveVideo(boolean receiveVideo) {
        if (videoInfo == null) {
            return;
        }
        videoInfo.setReceiveVideo(receiveVideo);
        // 1.先设置UI
        updateUI(receiveVideo);
        // 2.再开/关视频(SurfaceView会调用surfaceCreated和surfaceDestroyed，已调用，不需要再调)
        // pauseOrResumeVideo(!receiveVideo);
    }

    /**
     * 禁用摄像头，远端显示disable图片，本地不显示
     *
     * @param enable true 不禁用；false 禁用
     */
    public void enableVideo(boolean enable) {
        if (videoInfo == null || !videoInfo.isLocalUser()) {
            return;
        }
        enableCamera = enable;
        surfaceView.setVisibility(enableCamera ? View.VISIBLE : View.INVISIBLE);
        backgroundImageView.setVisibility(enableCamera ? View.INVISIBLE : View.VISIBLE);
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能：创建用于显示用户信息的TextView，并进行相应的初始化和添加操作 ┊
     * ┊                                                                      ┊
     * ┊ 参  数：context 上下文                                               ┊
     * ┊ 返回值：无                                                           ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    private void createUserInfoTextView() {
        userInfoTv = new TextView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.leftMargin = 20;
        params.topMargin = 20;
        userInfoTv.setLayoutParams(params);
        userInfoTv.setBackgroundResource(R.drawable.username_bg_2);
        userInfoTv.setTextColor(Color.WHITE);
        userInfoTv.setSingleLine();
        userInfoTv.setMaxEms(15);
        userInfoTv.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        userInfoTv.setPadding(15, 0, 15, 0);
        userInfoTv.setGravity(Gravity.CENTER);
        userInfoTv.setTextSize(14);
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能：创建和初始化视频加载加载progressBar                          ┊
     * ┊                                                                      ┊
     * ┊ 参  数：无                                                           ┊
     * ┊ 返回值：无                                                           ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    private void createProgressBar() {
        progressBackgroundView = new View(getContext());
        LayoutParams backgroundParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        progressBackgroundView.setLayoutParams(backgroundParams);
        progressBackgroundView.setBackgroundColor(Color.BLACK);
        progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminateDrawable(ContextCompat.getDrawable(getContext(), R.drawable.video_loading));
        LayoutParams progressParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        progressParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressParams);
        setProgressViewVisibility(INVISIBLE);
    }

    /**
     * 显示视频信息
     */
    private void createVideoInfoTextView() {
        videoInfoTextView = new TextView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.bottomMargin = 20;
        videoInfoTextView.setLayoutParams(params);
        videoInfoTextView.setTextColor(0xFF1b69ce);
        videoInfoTextView.setSingleLine();
    }

    private void createDisableImageView() {
        backgroundImageView = new ImageView(getContext());
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        backgroundImageView.setLayoutParams(params);
        backgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backgroundImageView.setImageResource(R.mipmap.disable_camera);
        backgroundImageView.setVisibility(GONE);
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
     * ┊ 功  能： 创建surfaceView 并设置surfaceView的显示方式，以及相应回调
     * ┊
     * ┊ 参  数： 无
     * ┊ 返回值： 无
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
     */
    private void createSurfaceView() {
        // 视频窗口初始化
        if (surfaceView == null) {
            surfaceView = new SurfaceView(getContext());
            surfaceView.setVisibility(View.VISIBLE);
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView.setLayoutParams(layoutParams);
            //surfaceView.setOnClickListener(this);
        }
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.removeCallback(surfaceCallback);
        surfaceHolder.addCallback(surfaceCallback);

        //add by baodian
        if( this.videoInfo != null && this.videoInfo.isLocalUser() )
        {
            /*
            if( custom_surfaceview == null ) {
                custom_surfaceview = new SurfaceView(getContext());
                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                custom_surfaceview.setLayoutParams(layoutParams);
            }*/
            /*
            _surfaceHolder = custom_surfaceview.getHolder();
            _surfaceHolder.removeCallback(_surfaceCallback);
            _surfaceHolder.addCallback(_surfaceCallback);*/
            //addView(custom_surfaceview);
/*
            if( YUVUtil.getInstance().get_observe_send_data() != null )
            {
                YUVUtil.getInstance().get_observe_send_data().notify_attach_view(custom_surfaceview);
            }*/
            //YUVUtil.getInstance().setAlready_local_preview(true);
            VideoManager videomanager = SdkUtil.getVideoManager();
            videomanager.setAlready_local_preview(true);
/*
            if( use_local_camera == true ) {
                custom_surfaceview.setVisibility(View.INVISIBLE);
            }
            else
            {
                custom_surfaceview.setVisibility(View.VISIBLE);
                bringChildToFront(custom_surfaceview);

                if( YUVUtil.getInstance().get_observe_send_data() != null )
                {
                    YUVUtil.getInstance().get_observe_send_data().notify_to_draw(true);
                }
            }*/
        }

    }

    //add by baodian
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_image_open(CustomImageEvent event) {

        if( videoInfo != null && videoInfo.isLocalUser() )
        {
            if( event.datas != null )
            {
                //list_bitmaps.offer(event);
            }
        }

    }

    //add by baodian
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_of_image_open(CustomImageOnOffEvent event) {
/*
        if( videoInfo != null && videoInfo.isLocalUser() )
        {
            if( event.on == true ) {
                custom_surfaceview.setVisibility(View.VISIBLE);
                bringChildToFront(custom_surfaceview);
            }
            else
            {
                custom_surfaceview.setVisibility(View.INVISIBLE);
            }
        }
*/
    }




    private void updateUserInfoTvName(BaseUser user) {
        String nameAndMediaId;
        //视频路数大于1，要显示是第几路视频
        if (user.getRoomUserInfo().vclmgr.getChannelCount() > 1) {
            int mediaIndex = videoInfo.getMediaId() + 1;
            nameAndMediaId = user.getNickName() + " " + mediaIndex;
        } else {
            nameAndMediaId = user.getNickName();
        }
        userInfoTv.setText(nameAndMediaId);
    }


    private void updateUserInfoTvImg(BaseUser user) {
        Drawable startDrawable;
        if (user.isSpeechNone()) {
            //根据状态变更绑定解绑用户
            if (videoInfo != null && videoInfo.getVideoUser() != null) {
                MicEnergyMonitor.getInstance().removeAudioSource(videoInfo.getVideoUser(), micType);
            }
            startDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.meeting_video_mic_b_closed);
        } else if (user.isSpeechWait()) {
            //根据状态变更绑定解绑用户
            if (videoInfo != null && videoInfo.getVideoUser() != null) {
                MicEnergyMonitor.getInstance().removeAudioSource(videoInfo.getVideoUser(), micType);
            }
            startDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.meeting_video_mic_b_applying);
        } else {
            //根据状态变更绑定解绑用户
            if (videoInfo != null && videoInfo.getVideoUser() != null) {
                MicEnergyMonitor.getInstance().addAudioSource(videoInfo.getVideoUser(), micType);
            }
            int id = getResources().getIdentifier(MIPMAP_NAME + getMicStateLogo(user.getSoundEnergy()),
                    MIPMAP, getContext().getPackageName());
            startDrawable = ContextCompat.getDrawable(getContext(), id);
        }
        if (startDrawable != null) {
            startDrawable.setBounds(0, 0, 48, 48);
            userInfoTv.setCompoundDrawables(startDrawable, null, null, null);
        }
    }

    /**
     * 麦克风能量值
     *
     * @param energy 0~100
     * @return 对应的logo
     */
    public static int getMicStateLogo(int energy) {
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

    /**
     * 暂停\恢复视频
     *
     * @param pause 是否暂停
     */
    public void pauseOrResumeVideo(boolean pause) {
        if (this.videoInfo == null) {
            return;
        }
        if (videoInfo.isLocalUser()) {
            if (pause && renderId == 0) {
                return;
            }
            if (!pause && renderId != 0) {
                return;
            }
            surfaceView.setVisibility(pause ? View.GONE : View.VISIBLE);
        } else {
            Log.w(TAG, "pauseOrResumeVideo pause = " + pause);
            setInterruptReceive(pause);
            startOrPauseVideo(!pause);
        }
    }

    /**
     * 与渲染解绑
     */
    private void unBindRender() {
        if (renderId <= 0) {
            return;
        }
        VideoManager videoManager = SdkUtil.getVideoManager();
        if (videoInfo.isLocalUser()) {
            videoManager.stopLocalVideoView(videoInfo.getMediaId(), renderId);
        } else {
            renderNotify.detachToView();
            videoManager.stopRemoteVideoView(renderId);
        }
        renderId = 0;
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能：打开远端视频，并接收视频数据，同时设置预览和相关参数         ┊
     * ┊                                                                      ┊
     * ┊ 参  数：无                                                           ┊
     * ┊ 返回值：无                                                           ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    private void bindRender() {
        if (videoInfo.isLocalUser()) {
            Log.i(TAG, "you should add local render when surface created and remove on surface destroyed");
        } else {
            setProgressViewVisibility(VISIBLE);
            renderNotify.attachToView(this);
            renderId = videoModel.startRemoteVideoView(videoInfo.getUserId(), videoInfo.getMediaId(),
                    surfaceView, renderNotify);
            // 显示方式
            videoModel.setRemoteRenderDisplayMode(renderId, displayMode);
        }
    }

    private void startOrPauseVideo(boolean start) {
        final String nickName = videoInfo.getVideoUser().getNickName();
        if (interruptReceive) {
            Log.i(TAG, "Intercept video reception : name = " + nickName);
            start = false;
        }
        if (start) {
            if (videoState == VideoState.STATE_START) {
                Log.i(TAG, "Video already start : name = " + nickName);
                return;
            }
            if (surfaceView == null || !isSurfaceCreated) {
                Log.i(TAG, "Video already start : name = " + nickName);
                return;
            }
            // 设置渲染句柄
            videoModel.setRemoteRenderWnd(renderId, surfaceView, renderNotify);
            if (videoState == VideoState.STATE_NONE) {
                // 还未开始接收
                Log.i(TAG, "Start video when it is not started : name = " + nickName);
                // 开始接收并渲染
                setProgressViewVisibility(VISIBLE);
                videoModel.startReceiveVideo(videoInfo.getUserId(), videoInfo.getMediaId(), renderId);
            } else {
                // 暂停状态
                Log.i(TAG, "Start video when it is paused : name = " + nickName);
                if (ConfigChannel.getInstance().isHardwareDecode()) {
                    Log.i(TAG, "Start video when it is paused : HardwareDecode");
                    bindRender();
                    videoModel.startReceiveVideo(videoInfo.getUserId(), videoInfo.getMediaId(), renderId);
                } else {
                    setProgressViewVisibility(VISIBLE);
                    SdkUtil.getVideoManager().pauseRemoteVideoView(videoInfo.getUserId(), videoInfo.getMediaId(), false);
                    videoModel.pauseRender(renderId, false);
                }
            }
            videoState = VideoState.STATE_START;
        } else {
            if (videoState == VideoState.STATE_PAUSE || videoState == VideoState.STATE_NONE) {
                Log.i(TAG, " Video is paused or has not started : name = "
                        + nickName + ", state = " + videoState.name());
                return;
            }
            Log.i(TAG, "pause user video : name = " + nickName);
            if (ConfigChannel.getInstance().isHardwareDecode()) {
                Log.i(TAG, "pause user video : HardwareDecode");
                videoModel.stopReceiveVideo(videoInfo.getUserId(), videoInfo.getMediaId());
                // 移除底层句柄
                unBindRender();
            } else {
                SdkUtil.getVideoManager().pauseRemoteVideoView(videoInfo.getUserId(), videoInfo.getMediaId(), true);
                videoModel.pauseRender(renderId, true);
            }
            videoState = VideoState.STATE_PAUSE;
        }
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能：设置加载progressBar的显示和隐藏，并设置动画启动              ┊
     * ┊                                                                      ┊
     * ┊ 参  数：visibility 显示状态 VISIBLE、 INVISIBLE                      ┊
     * ┊ 返回值：无                                                           ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    private void setProgressViewVisibility(int visibility) {
        progressBackgroundView.setVisibility(visibility);
        progressBar.setVisibility(visibility);
        /*if (surfaceView != null *//*&& isMediaOverlay*//*) {
            int color = visibility == VISIBLE ? Color.BLACK : Color.TRANSPARENT;
            surfaceView.setBackgroundColor(color);
        }*/
    }

    private void updateUI(boolean receiveVideo) {
        int visible = receiveVideo ? View.VISIBLE : View.INVISIBLE;
        surfaceView.setVisibility(visible);
        userInfoTv.setVisibility(visible);
        videoInfoTextView.setVisibility(visible);
        if (!receiveVideo) {
            progressBackgroundView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void onLoadingCompleted(long userId, long renderId) {
        if (videoInfo != null && userId == videoInfo.getUserId()
                && renderId == this.renderId) {
            Log.i(TAG, "hide loading : name = " + videoInfo.getVideoUser().getNickName()
                    + ", renderId = " + renderId);
            setProgressViewVisibility(INVISIBLE);
        }
    }

    private static class RenderNotify implements VideoRenderNotify {

        private WeakReference<VideoScreenView> weakReference;

        void attachToView(VideoScreenView videoWindow) {
            if (weakReference != null) {
                weakReference.clear();
                weakReference = null;
            }
            weakReference = new WeakReference<>(videoWindow);
        }

        void detachToView() {
            if (weakReference != null) {
                weakReference.clear();
                weakReference = null;
            }
        }

        @Override
        public void onVideoRenderNotify(int code, long userId, long renderId) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                onVideoRenderInUiThread(userId, renderId);
            } else {
                if (weakReference == null) {
                    return;
                }
                VideoScreenView window = weakReference.get();
                if (window == null) {
                    return;
                }
                window.post(() -> onVideoRenderInUiThread(userId, renderId));
            }
        }

        void onVideoRenderInUiThread(long userId, long renderId) {
            if (weakReference == null) {
                return;
            }
            VideoScreenView window = weakReference.get();
            if (window == null) {
                return;
            }
            window.onLoadingCompleted(userId, renderId);
        }
    }
}
