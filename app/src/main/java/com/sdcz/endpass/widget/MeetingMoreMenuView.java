package com.sdcz.endpass.widget;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.bean.AudioEventOnWrap;
import com.sdcz.endpass.bean.CameraEventOnWrap;
import com.sdcz.endpass.callback.IMeetingMoreMenuListener;
import com.sdcz.endpass.model.AppCache;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.util.PermissionUtils;
import com.inpor.base.sdk.video.VideoManager;
//import com.sdcz.endpass.util.YUVUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * @Author wuyr
 * @Date 2020/12/10 17:13
 * @Description
 */
public class MeetingMoreMenuView extends BasePopupWindowContentView
        implements View.OnClickListener, ChatManager.UnReadMsgUpdateListener,
        CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MeetingMoreMenuView";
    private final Context context;
    private CheckBox enableMicCheckBox;
    //private CheckBox cameraCheckBox;
    private CheckBox videoCheckBox;
    private CheckBox changeCheckBox;
    private Bundle checkBoxStateBundle;
    private IMeetingMoreMenuListener meetingMoreContainerListener;
    private TextView chatMark;
    private TextView tvSetting;
    private TextView applyManagerView;
    private RelativeLayout exitMeetingLayout;
    private ConstraintLayout parentContentLayout;


    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetingMoreMenuView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }


    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public MeetingMoreMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }


    private void init() {
        checkBoxStateBundle = new Bundle();
        //麦克风默认是选中状态
        checkBoxStateBundle.putBoolean(String.valueOf(R.id.cb_select_mic), true);
        LayoutInflater.from(context)
                .inflate(R.layout.meeting_more_menu_layout, this, true);
        parentContentLayout = findViewById(R.id.cl_more_root_layout);
        exitMeetingLayout = findViewById(R.id.rl_exit_meeting);
        initView();
        boolean portrait = ScreenUtils.isPortrait();
        //横竖屏加载不同的布局
        if (!portrait) {
            onLandscapeListener();
        }
        initEvent();

    }


    private void initView() {
        ConstraintLayout moreMenuRootLayout = findViewById(R.id.cl_more_root_layout);
        TextView changeLayoutItem = moreMenuRootLayout.findViewById(R.id.tv_change_layout);
        changeLayoutItem.setOnClickListener(this);
        TextView tvGps = moreMenuRootLayout.findViewById(R.id.tv_gps);
        tvGps.setOnClickListener(this);
        TextView tvcancleGps = moreMenuRootLayout.findViewById(R.id.tv_cancelgps);
        tvcancleGps.setOnClickListener(this);
        TextView chatItem = moreMenuRootLayout.findViewById(R.id.chat);
        chatItem.setOnClickListener(this);
        TextView enableMicItem = moreMenuRootLayout.findViewById(R.id.tv_enable_mic);
        enableMicItem.setOnClickListener(this);

        enableMicCheckBox = moreMenuRootLayout.findViewById(R.id.cb_select_mic);
        recoverCheckBoxState(enableMicCheckBox);
        enableMicCheckBox.setOnCheckedChangeListener(this);
/*
        TextView enableCameraItem = moreMenuRootLayout.findViewById(R.id.tv_enable_camera);
        enableCameraItem.setOnClickListener(this);
        cameraCheckBox = moreMenuRootLayout.findViewById(R.id.cb_select_camera);
        recoverCheckBoxState(cameraCheckBox);
        cameraCheckBox.setOnCheckedChangeListener(this);
*/
        TextView openVideoItem = moreMenuRootLayout.findViewById(R.id.tv_open_video);
        openVideoItem.setOnClickListener(this);
        videoCheckBox = moreMenuRootLayout.findViewById(R.id.cb_select_video);
        recoverCheckBoxState(videoCheckBox);
        videoCheckBox.setOnCheckedChangeListener(this);

        //only test

        TextView changeVideoItem = moreMenuRootLayout.findViewById(R.id.tv_change_video);
        changeVideoItem.setOnClickListener(this);
        changeCheckBox = moreMenuRootLayout.findViewById(R.id.cb_change_video);
        //recoverCheckBoxState(changeCheckBox);
        VideoManager videomanager = SdkUtil.getVideoManager();
        boolean use_local_camera = videomanager.get_use_local_camera();
        changeCheckBox.setChecked(use_local_camera);
        changeCheckBox.setOnCheckedChangeListener(this);


        View closeBnt = moreMenuRootLayout.findViewById(R.id.im_close);
        closeBnt.setOnClickListener(this);
        chatMark = findViewById(R.id.tv_chat_mark);
        tvSetting = findViewById(R.id.tv_setting);
        tvSetting.setOnClickListener(this);

        applyManagerView = findViewById(R.id.tv_apply_manager);
        applyManagerView.setOnClickListener(this);


        //监听未读信息变化
        ChatManager.getInstance().setUnReadMsgUpdateListener(this);
        //初始化未读信息数量角标
        int unReadMsgNumber = ChatManager.getInstance().getUnReadMsgNumber();
        postChatMarkNumber(unReadMsgNumber);

        if (meetingMoreContainerListener != null) {
            boolean isManager = meetingMoreContainerListener.isManager();
            if (isManager) {
                setManagerViewText(R.string.meetingui_abandon_manager);
            } else {
                setManagerViewText(R.string.meetingui_apply_manager);
            }
        }

    }


    private void initEvent() {

    }

    public void setMicCheckBoxState(boolean state) {
        enableMicCheckBox.setChecked(state);
    }

    public void setCameraCheckBoxState(boolean state) {
        //cameraCheckBox.setChecked(state);
    }

    public void setVideoCheckBoxState(boolean state) {
        videoCheckBox.setChecked(state);
    }

    public void setManagerViewText(@StringRes int str) {
        applyManagerView.setText(context.getResources().getString(str));
    }

    @Override
    public void onClick(View itemView) {
        if (meetingMoreContainerListener == null) {
            return;
        }
        int id = itemView.getId();
        if (id == R.id.tv_change_layout) {
            meetingMoreContainerListener.onClickChangeLayoutItemListener();
        } else if (id == R.id.tv_gps) {
            meetingMoreContainerListener.onClickopenGPSListener();
        }  else if (id == R.id.tv_cancelgps) {
            meetingMoreContainerListener.onClickcancleGPSListener();
        }else if (id == R.id.chat) {
            meetingMoreContainerListener.onClickChatItemListener();
        } else if (id == R.id.tv_enable_mic) {
            changeCheckBoxState(enableMicCheckBox);
        } /*else if (id == R.id.tv_enable_camera) {
            changeCheckBoxState(cameraCheckBox);
        } */else if (id == R.id.tv_open_video) {
            changeCheckBoxState(videoCheckBox);
        }else if (id == R.id.close_background_view || id == R.id.im_close) {
            dismissPopupWindow();
        } else if (id == R.id.tv_setting) {
            meetingMoreContainerListener.onClickSettingListener();
            dismissPopupWindow();
        } else if (id == R.id.tv_apply_manager) {
            meetingMoreContainerListener.onClickApplyManagerListener();
        }
    }


    /**
     * CheckBox 选择事件监听
     *
     * @param buttonView CheckButton
     * @param isChecked  true为选中状态
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (meetingMoreContainerListener == null) {
            return;
        }
        int id = buttonView.getId();
        if (id == R.id.cb_select_mic) {
/*
            if( isChecked == true )
            {
                List<String> permissionList = PermissionUtils.requestMeetingPermission();
                if (permissionList != null && (permissionList.contains(Manifest.permission.RECORD_AUDIO))) {
                    if( EventBus.getDefault().isRegistered(this) == false ) {
                        EventBus.getDefault().register(this);
                    }
                    ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                            new String[]{Manifest.permission.RECORD_AUDIO}, 61);
                    buttonView.setChecked(false);
                    return;
                }
            }
*/
            meetingMoreContainerListener.dealDisableMicListener(isChecked);
        } /*else if (id == R.id.cb_select_camera) {
            meetingMoreContainerListener.dealDisableCameraListener(isChecked);
        } */else if (id == R.id.cb_select_video) {

            VideoManager videomanager = SdkUtil.getVideoManager();
            boolean use_local_camera = videomanager.get_use_local_camera();

            if( /*com.inpor.sdk.utils.SharedPreferencesUtils.getBoolean("use_local_camera",true)*/use_local_camera == false && isChecked == true )
            {
                videoCheckBox.setChecked(false);
                ToastUtils.showShort("正在使用YUV视频流，无法本地视频预览！");
                return;
            }

            //如果开关打开需要判断是否已经开启摄像机权限才能生效
            if( isChecked == true )
            {
                List<String> permissionList = PermissionUtils.requestMeetingPermission();
                if (permissionList != null && (permissionList.contains(Manifest.permission.CAMERA))) {
                    videoCheckBox.setChecked(false);
                    checkBoxStateBundle.putBoolean(String.valueOf(videoCheckBox.getId()), false);
                    if( EventBus.getDefault().isRegistered(this) == false ) {
                        EventBus.getDefault().register(this);
                    }
                    ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                            new String[]{Manifest.permission.CAMERA}, 60);
                    return;
                }
            }
            meetingMoreContainerListener.enableVideoPreviewListener(isChecked);
        }
        //only test

        else if( id == R.id.cb_change_video )
        {
            //changeCheckBox
            if( AppCache.getInstance().isCameraEnabled() == false  )
            {
                changeCheckBox.setChecked(!isChecked);
                return;
            }

            VideoManager videomanager = SdkUtil.getVideoManager();
            boolean use_local_camera = videomanager.get_use_local_camera();
            if( use_local_camera == isChecked)
            {
                return;
            }

            //VideoManager videomanager = SdkUtil.getVideoManager();
            videomanager.setLocalVideoCaptureEnable(isChecked);

            //checkBoxStateBundle.putBoolean(String.valueOf(changeCheckBox.getId()), isChecked);
            //YUVUtil.getInstance().change_to_use_local_camera(isChecked);


        }


    }


    /**
     * 恢复CheckBox选择状态
     *
     * @param checkBox CheckBox
     */
    private void recoverCheckBoxState(CheckBox checkBox) {
        boolean isChecked = checkBoxStateBundle.getBoolean(String.valueOf(checkBox.getId()));
        checkBox.setChecked(isChecked);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_camera_open(CameraEventOnWrap event_on) {
        if( event_on.description.isEmpty() == false )
        {
            meetingMoreContainerListener.dealDisableCameraListener(event_on.flag);
        }
        else {
            meetingMoreContainerListener.enableVideoPreviewListener(event_on.flag);
            videoCheckBox.setChecked(event_on.flag);
            checkBoxStateBundle.putBoolean(String.valueOf(videoCheckBox.getId()), event_on.flag);
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_audio_open(AudioEventOnWrap event_on) {
        meetingMoreContainerListener.dealDisableMicListener(event_on.flag);
        EventBus.getDefault().unregister(this);
    }

    /**
     * 切换CheckBox状态
     *
     * @param currentCheckBox 当前的CheckBox
     * @return CheckBox状态
     */
    private boolean changeCheckBoxState(CheckBox currentCheckBox) {
        boolean checked = currentCheckBox.isChecked();
        boolean currentCheckedState = !checked;
        currentCheckBox.setChecked(currentCheckedState);
        checkBoxStateBundle.putBoolean(String.valueOf(currentCheckBox.getId()), currentCheckedState);
        return currentCheckedState;
    }


    public void setIMeetingMoreMenuContainerListener(IMeetingMoreMenuListener meetingMoreContainerListener) {
        this.meetingMoreContainerListener = meetingMoreContainerListener;
    }


    /**
     * 横竖屏切换回调
     *
     * @param newConfig 当前Configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onLandscapeListener();
        } else {
            onPortraitListener();
        }
    }


    /**
     * 从竖屏切换到横屏时调用
     */
    @Override
    public void onLandscapeListener() {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.leftToLeft = R.id.guideline_vertical_left;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        parentContentLayout.setLayoutParams(layoutParams);
        parentContentLayout.setBackgroundResource(R.drawable.shape_select_shared_right);
    }


    /**
     * 从横屏切换到竖屏时调用
     */
    @Override
    public void onPortraitListener() {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.topToTop = R.id.guideline_horizontal_top;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.leftToLeft = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        parentContentLayout.setLayoutParams(layoutParams);
        parentContentLayout.setBackgroundResource(R.drawable.shape_select_shared_right);
    }


    /**
     * 设置聊天角标数字
     *
     * @param number 角标数值
     */
    public void postChatMarkNumber(int number) {
        boolean mainThread = ThreadUtils.isMainThread();
        if (mainThread) {
            updateChatMarkNumber(number);
        } else {
            this.post(() -> updateChatMarkNumber(number));
        }
    }

    private void updateChatMarkNumber(int number) {
        if (number == 0) {
            chatMark.setVisibility(GONE);
            return;
        }
        chatMark.setVisibility(VISIBLE);
        String contentText = "";
        if (number > 99) {
            contentText = "99+";
        } else {
            contentText = String.valueOf(number);
        }
        Log.i(TAG, "当前线程：" + Thread.currentThread().getName());
        chatMark.setText(contentText);
    }

    /**
     * 是否显示底部“结束会议”按钮
     *
     * @param isShow true 显示 反之则隐藏
     */
    public void setExitMeetingViewShowState(boolean isShow) {
        exitMeetingLayout.setVisibility(isShow ? VISIBLE : INVISIBLE);
    }


    @Override
    public void onUnReadMsgUpdateListener(int number) {
        postChatMarkNumber(number);
    }


    @Override
    public void recycle() {
        ChatManager.getInstance().setUnReadMsgUpdateListener(null);
        meetingMoreContainerListener = null;
    }
}
