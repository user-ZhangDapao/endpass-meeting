package com.sdcz.endpass.ui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.MeetingModule;
import com.comix.meeting.annotation.ScreenShareResultCode;
import com.comix.meeting.annotation.VideoQuality;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.LayoutType;
import com.comix.meeting.entities.MeetingInfo;
import com.comix.meeting.entities.ScreenShareOptions;
import com.comix.meeting.entities.WhiteBoard;
import com.comix.meeting.listeners.AudioModelListener;
import com.comix.meeting.listeners.MeetingModelListener;
import com.comix.meeting.listeners.ScreenSharingCreateListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.comix.meeting.listeners.WbCreateListener;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.LiveDataBus;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.AudioEventOnWrap;
import com.sdcz.endpass.bean.CameraAndAudioEventOnWrap;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.MeetingSettingsKey;
import com.sdcz.endpass.bean.StorageEventOnWrap;
import com.sdcz.endpass.callback.BottomMenuLocationUpdateListener;
import com.sdcz.endpass.callback.MeetingMenuEventManagerListener;
import com.sdcz.endpass.callback.MeetingRoomControl;
import com.sdcz.endpass.callback.OnSettingsChangedListener;
import com.sdcz.endpass.constant.Constant;
import com.sdcz.endpass.custommade.meetingover._manager._MeetingStateManager;
import com.sdcz.endpass.dialog.GlobalPopupView;
import com.sdcz.endpass.gps.PosService;
import com.sdcz.endpass.model.AppCache;
import com.sdcz.endpass.model.CameraObserver;
import com.sdcz.endpass.model.MeetingLifecycleObserver;
import com.sdcz.endpass.model.MeetingSettings;
import com.sdcz.endpass.model.MeetingSettingsModel;
import com.sdcz.endpass.model.NotificationUtil;
import com.sdcz.endpass.model.UiEntrance;
import com.sdcz.endpass.presenter.MeetingBottomAndTopMenuContainer;
import com.sdcz.endpass.presenter.MeetingQuitContainer;
import com.sdcz.endpass.presenter.MobileMeetingPresenter;
import com.sdcz.endpass.util.BrandUtil;
import com.sdcz.endpass.util.HeadsetMonitorUtil;
import com.sdcz.endpass.util.MediaUtils;
import com.sdcz.endpass.util.MeetingTempDataUtils;
import com.sdcz.endpass.util.UiHelper;
import com.sdcz.endpass.view.IMobileMeetingView;
import com.sdcz.endpass.widget.MeetingBottomMenuView;
import com.sdcz.endpass.widget.MeetingTopTitleView;
import com.sdcz.endpass.widget.PopupWindowBuilder;
import com.sdcz.endpass.widget.VariableLayout;
import com.inpor.base.sdk.audio.AudioManager;
import com.inpor.base.sdk.audio.RawCapDataSinkCallback;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.permission.PermissionManager;
import com.inpor.base.sdk.share.ScreenShareManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.nativeapi.adaptor.AudioParam;
import com.inpor.nativeapi.adaptor.Platform;
import com.inpor.nativeapi.adaptor.RolePermission;
import com.inpor.nativeapi.adaptor.RoomInfo;
import com.inpor.nativeapi.adaptor.RoomWndState;
import com.inpor.nativeapi.interfaces.RolePermissionEngine;
import com.inpor.sdk.PlatformConfig;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileMeetingActivity extends BaseActivity<MobileMeetingPresenter> implements IMobileMeetingView, MeetingModelListener,
        MeetingMenuEventManagerListener, BottomMenuLocationUpdateListener, AudioModelListener, MeetingRoomControl, RawCapDataSinkCallback, OnSettingsChangedListener {

    private static final String TAG = "MobileMeetingActivity";
    public static final String EXTRA_ANONYMOUS_LOGIN = "EXTRA_ANONYMOUS_LOGIN";
    public static final String EXTRA_ANONYMOUS_LOGIN_WITH_ROOMID = "EXTRA_ANONYMOUS_LOGIN_WITH_ROOMID";
    private RelativeLayout rootView;
    private VariableLayout variableLayout;
    private MeetingTopTitleView meetingTopTitleView;
    private MeetingBottomMenuView meetingBottomMenuView;
    private PopupWindowBuilder popupWindowBuilder;
    private MeetingBottomAndTopMenuContainer meetingBottomAndTopMenuContainer;
    private MeetingManager meetingManager;
    private Runnable countdownRunnable;
    private HeadsetMonitorUtil headsetMonitorUtil;
    public CameraObserver cameraObserver;
    private boolean exit;
    private MeetingModule proxy;
    private UserManager userModel;
    private AudioManager audioModel;
    private ScreenShareManager shareModel;
    private int objId = -1;
    private boolean isAnonymousLogin;
    private boolean isAnonymousLoginWithRoomId;

    private String channelCode;

    @Override
    protected void requestWindowSet(Bundle savedInstanceState) {
        super.requestWindowSet(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MeetingSettingsModel.getInstance().initSettings(this);
        int orientation = UiHelper.getDeviceDefaultOrientation(this);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (BrandUtil.checkoutHW()) {
            headsetMonitorUtil = new HeadsetMonitorUtil(this);
            headsetMonitorUtil.registerHeadsetPlugReceiver();
        }
        if (savedInstanceState == null) {
            getLifecycle().addObserver(new MeetingLifecycleObserver(this));
            cameraObserver = new CameraObserver();
            getLifecycle().addObserver(cameraObserver);
        }
        isAnonymousLogin = getIntent().getBooleanExtra(EXTRA_ANONYMOUS_LOGIN, false);
        channelCode = getIntent().getStringExtra(Constants.SharedPreKey.CHANNEL_CODE);
        isAnonymousLoginWithRoomId = getIntent().getBooleanExtra(EXTRA_ANONYMOUS_LOGIN_WITH_ROOMID, false);
        //会议开始绑定会议退出状态监测管理类
        _MeetingStateManager.getInstance().bindActivity(this);

        SdkUtil.getAudioManager().initAudioRes(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        meetingManager = SdkUtil.getMeetingManager();
        shareModel = SdkUtil.getShareManager();
        userModel = SdkUtil.getUserManager();
        audioModel = SdkUtil.getAudioManager();
        proxy = meetingManager.getMeetingModule();
        audioModel.setAudioParam(AudioParam.getDefault(Platform.ANDROID));
        MeetingInfo meetingInfo = proxy.getMeetingInfo();
        rootView = findViewById(R.id.activity_root_view);
        variableLayout = findViewById(R.id.variableLayout);
        variableLayout.subscribe();
        variableLayout.onLayoutChanged(meetingInfo);
        initBottomAndTopMenu();
        MeetingSettingsModel.getInstance().addListener(this);
        proxy.setup();
        FileUtils.deleteFilesInDirWithFilter(getExternalFilesDir(null), pathname -> pathname.toString().endsWith(".pcm"));
        RoomInfo roomInfo = SdkUtil.getMeetingManager().getRoomInfo();
        String timeFormat = TimeUtils.date2String(TimeUtils.getNowDate());
        currentAudioDataFile = new File(getExternalFilesDir(null) + File.separator + roomInfo.dwRoomID + "_" + timeFormat + ".pcm");
        Log.i(TAG, "onCreate: AudioDataFile is " + currentAudioDataFile);
        if (savedInstanceState != null) {
            quitRoom();
            finish();
        }
        return rootView;
    }

    @Override
    protected MobileMeetingPresenter createPresenter() {
        return new MobileMeetingPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_mobile_meeting;
    }

    private boolean validateMicAvailability(){
        Boolean available = true;
        @SuppressLint("MissingPermission")
        AudioRecord recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT, 44100);
        try{
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED ){
                available = false;
            }

            recorder.startRecording();
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
                recorder.stop();
                available = false;
            }
            recorder.stop();
        } finally{
            recorder.release();
            recorder = null;
        }
        return available;
    }

    private void initBottomAndTopMenu() {
        RelativeLayout.LayoutParams topLayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        meetingTopTitleView = new MeetingTopTitleView(this);
        meetingTopTitleView.setLayoutParams(topLayoutParams);
        RelativeLayout.LayoutParams bottomLayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        bottomLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        meetingBottomMenuView = new MeetingBottomMenuView(this, channelCode);
        meetingBottomMenuView.setLayoutParams(bottomLayoutParams);
        RelativeLayout rootRelativeLayout = findViewById(R.id.activity_root_view);
        rootRelativeLayout.addView(meetingTopTitleView);
        rootRelativeLayout.addView(meetingBottomMenuView);
        initEvent();
    }

    private void initEvent() {
        //会议室事件监听
        meetingManager.addEventListener(this);
        userModel.addEventListener(userModelListener);
        audioModel.addEventListener(this);
        UiEntrance.getInstance().setMenuHelper(() ->
                meetingBottomAndTopMenuContainer.bottomAndTopMenuShowControl());
    }

    @Override
    public void initData() {
        popupWindowBuilder = new PopupWindowBuilder(this);
        meetingBottomMenuView.setBottomMenuLocationUpdateListener(this);
        meetingBottomAndTopMenuContainer = new MeetingBottomAndTopMenuContainer(this);
        meetingBottomAndTopMenuContainer.addMeetingMenuEventManagerListener(this);
        meetingBottomAndTopMenuContainer.correlationMeetingTopMenu(meetingTopTitleView);
        meetingBottomAndTopMenuContainer.correlationMeetingBottomMenu(meetingBottomMenuView,
                variableLayout.isDataLayoutShowing());
//        mPresenter.getChannelByCode(this, channelCode);
    }


    /**
     * 用户状态回调
     */
    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.AUDIO_STATE
                    | UserModelListenerImpl.VIDEO_STATE
                    | UserModelListenerImpl.BROADCAST_OWN_AUDIO_STATE
                    | UserModelListenerImpl.BROADCAST_OWN_VIDEO_STATE
                    | UserModelListenerImpl.MANAGER_CHANGED_STATE
                    | UserModelListenerImpl.ONLINE_INVITATION_STATE
                    | UserModelListenerImpl.MAIN_SPEAKER
                    | UserModelListenerImpl.USER_RIGHT, UserModelListenerImpl.ThreadMode.MAIN) {
        @Override
        public void onBatchUserChanged(int type, BaseUser[] batchUsers) {
            if (batchUsers == null || batchUsers.length == 0) {
                return;
            }
            for (BaseUser user : batchUsers) {
                if (user.isLocalUser()) {
                    onUserJurisdictionStateChanged(user);
                    break;
                }
            }
        }

        @Override
        public void onUserChanged(int type, BaseUser user) {
            Log.i(TAG, "onUserChanged:" + type);
            super.onUserChanged(type, user);
            long targetUserId = user.getUserId();
            long currentUserId = SdkUtil.getUserManager().getLocalUser().getUserId();
            switch (type) {
                case UserModelListenerImpl.AUDIO_STATE:
                    if (currentUserId == targetUserId) {
                        onUserAudioStateChanged(user);
                    }
                    break;
                case UserModelListenerImpl.VIDEO_STATE:
                    if (currentUserId == targetUserId) {
                        onUserVideoStateChanged(user);
                    }
                    break;

                case UserModelListenerImpl.BROADCAST_OWN_AUDIO_STATE:
                    if (currentUserId == targetUserId) {
                        onUserAudioStateChanged(user, false);
                    }
                    break;
                case UserModelListenerImpl.BROADCAST_OWN_VIDEO_STATE:
                    if (currentUserId == targetUserId) {
                        onUserVideoStateChanged(user, false);
                    }
                    break;
                case UserModelListenerImpl.MANAGER_CHANGED_STATE:
                    Log.i(TAG, "管理员状态发生变化");
                    onUserVideoStateChanged(user);
                    onUserAudioStateChanged(user);
                    onUserJurisdictionStateChanged(user);
                    break;
                case UserModelListenerImpl.ONLINE_INVITATION_STATE:
                case UserModelListenerImpl.MAIN_SPEAKER:
                case UserModelListenerImpl.USER_RIGHT:
                    Log.i("sdfdsfdsfds", "" + type);
                    onUserJurisdictionStateChanged(user);
                    break;
                default:
            }
        }
    };

    /**
     * 顶部“共享工具条位置发送变化时回调该方法”
     *
     * @param marginBottom 距离父容器顶部的margin值
     */
    @Override
    public void onUpdateShareBarLocationListener(float marginBottom) {

    }


    /**
     * 用戶音頻状态变更
     *
     * @param localUser 本地用户
     */
    private void onUserAudioStateChanged(BaseUser localUser) {
        onUserAudioStateChanged(localUser, true);
    }


    /**
     * 用戶音頻状态变更
     *
     * @param localUser 本地用户
     */
    private void onUserAudioStateChanged(BaseUser localUser, boolean isToast) {
        meetingBottomAndTopMenuContainer.updateBottomMenuMicIconState(localUser,
                AppCache.getInstance().isMicDisable(), isToast);
        registAudioDataCallBack(localUser, new MeetingSettings(this).isWriteAudioData());
    }

    /**
     * 权限状态变更 会议信息显示
     *
     * @param localUser 本地用户
     */
    private void onUserJurisdictionStateChanged(BaseUser localUser) {
        meetingBottomAndTopMenuContainer.onUserJurisdictionStateChanged(localUser);
    }

    /**
     * 用户视频状态变更
     *
     * @param localUser 用户参数
     */
    private void onUserVideoStateChanged(BaseUser localUser) {
        onUserVideoStateChanged(localUser, true);
    }

    /**
     * 用户视频状态变更
     *
     * @param localUser 用户参数
     */
    private void onUserVideoStateChanged(BaseUser localUser, boolean isToast) {
        meetingBottomAndTopMenuContainer.updateBottomMenuCameraIconState(localUser,
                !AppCache.getInstance().isCameraEnabled(), isToast);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "MobileMeetingActivity::onRestart()");
    }

    @Override
    public void onUserEnter(List<Long> userId) {
        // 防止AbstractMethodError...
    }

    /**
     * 会议室被重命名回调
     *
     * @param newName 新的会议室名称
     */
    @Override
    public void onMeetingRename(String newName) {
        meetingBottomAndTopMenuContainer.updateTopTitle(newName);

    }

    /**
     * 主讲状态发生改变
     *
     * @param user 状态变更的用户
     */
    @Override
    public void onMainSpeakerChanged(BaseUser user) {
        Log.i(TAG, "onMainSpeakerChanged");
        BaseUser localUser = userModel.getLocalUser();
        if (localUser.getUserId() == user.getUserId()) {
            onUserAudioStateChanged(localUser);
            onUserVideoStateChanged(localUser);
            onUserJurisdictionStateChanged(localUser);
        }
    }

    /**
     * 主持人关闭会议室回调
     *
     * @param reason 原因
     * @see MeetingModelListener
     */
    @Override
    public void onMeetingRoomClosed(int reason) {
        //主持人关闭会议室
        Map<String,Object> reason_map = new HashMap();
        ((HashMap)reason_map).put("code",1);
        ((HashMap)reason_map).put("type",2);
        _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);

        GlobalPopupView globalPopupView = new GlobalPopupView(this);
        globalPopupView.onlyOneButton();
        globalPopupView.setGlobalPopupListener(this::finish);
        popupWindowBuilder
                .setContentView(globalPopupView)
                .setSuspendWindow(true)
                .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
                .setFocusable(false)
                .setOutsideTouchable(false)
                .show();

        if (countdownRunnable == null) {
            countdownRunnable = createPopupWindowCountdownTask(globalPopupView, R.string.meetingui_room_closed);
        }
        ThreadUtils.getMainHandler().post(countdownRunnable);
    }

    /**
     * 被踢出会议室回调
     *
     * @param userId 踢你的人的ID
     * @see MeetingModelListener
     */
    @Override
    public void onUserKicked(long userId) {

        //被踢出会议
        Map<String,Object> reason_map = new HashMap();
        reason_map.put("code",3);
        reason_map.put("userId",userId);
        _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);

        GlobalPopupView globalPopupView = new GlobalPopupView(this);
        globalPopupView.onlyOneButton();
        globalPopupView.setGlobalPopupListener(() -> {
            meetingBottomAndTopMenuContainer.recycle();
            popupWindowBuilder.dismissDialog();
            finish();
        });
        popupWindowBuilder
                .setContentView(globalPopupView)
                .setSuspendWindow(true)
                .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
                .setFocusable(false)
                .setOutsideTouchable(false)
                .show();

        //开始倒计时
        if (countdownRunnable == null) {
            countdownRunnable = createPopupWindowCountdownTask(globalPopupView, R.string.meetingui_to_drive_out);
        }
        ThreadUtils.getMainHandler().post(countdownRunnable);
    }

    private Runnable createPopupWindowCountdownTask(GlobalPopupView globalPopupView, @StringRes int strId) {
        countdownRunnable = new Runnable() {
            int time;
            private static final int MAX_TIME = 5;

            @Override
            public void run() {
                int currentTime = MAX_TIME - time;
                if (currentTime <= 0) {
                    ThreadUtils.getMainHandler().removeCallbacks(this);
                    meetingBottomAndTopMenuContainer.recycle();
                    finish();
                    return;
                }
                ++time;
                String format = String.format(getResources()
                        .getString(strId), currentTime);
                globalPopupView.setContentText(format);
                ThreadUtils.getMainHandler().postDelayed(this, 1000);
            }
        };

        return countdownRunnable;

    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onVoiceIncentiveStateChanged(boolean enable) {
        Log.i(TAG, "enable:" + enable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        meetingBottomAndTopMenuContainer.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        meetingBottomAndTopMenuContainer.onResume();
    }

    /**
     * 横竖屏切换回调
     *
     * @param newConfig 当前Configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MutableLiveData<Configuration> liveData = LiveDataBus.getInstance().
                getLiveData(LiveDataBus.KEY_MEETING_ACTIVITY_CONFIG);
        liveData.postValue(newConfig);
        Window window = getWindow();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            meetingBottomAndTopMenuContainer.onLandscapeStateNotification();
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            meetingBottomAndTopMenuContainer.onPortraitStateNotification();
        }
    }

    /**
     * 点击顶部 切换相机 按钮 回调
     */
    @Override
    public void onMenuManagerChangeCameraListener() {
        cameraObserver.switchCamera();
    }

    /**
     * 所有的菜单需要关闭Activity的都回调到这里
     */
    @Override
    public void onMenuManagerFinishActivityListener() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, PosService.class);
        stopService(intent);
        Log.i(TAG, "onDestroy()");
        quitRoom();
        if (BrandUtil.checkoutHW() && headsetMonitorUtil != null) {
            headsetMonitorUtil.unregisterHeadsetPlugReceiver();
        }
        proxy.exitRoom();

    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*
        if( requestCode == 999 )
        {
            int cameraPermission = grantResults[0];
            if (PackageManager.PERMISSION_GRANTED == cameraPermission) {
                EventBus.getDefault().post(new CameraEventOnWrap());
            }
        }
        else {
            cameraObserver.onActivityResult(requestCode, permissions, grantResults);
        }
        */
        if( requestCode == 59 || requestCode == 60 ) //摄像头
        {
            this.cameraObserver.onActivityResult(requestCode, permissions, grantResults);
        }
        else if( requestCode == 61 || requestCode == 62 || requestCode == 64 ) //麦克风
        {
            AudioEventOnWrap wrap = new AudioEventOnWrap();

            int cameraPermission = grantResults[0];
            if (PackageManager.PERMISSION_GRANTED != cameraPermission) {
                wrap.flag = false;
            }
            else
            {
                wrap.flag = true;
            }

            if( requestCode == 62 )
            {
                wrap.description = "updateBottomMenuMicIconState";
            }
            else if( requestCode == 64 )
            {
                wrap.broadcast = true;
            }

            EventBus.getDefault().post(wrap);
        }
        else if( requestCode == 63 ) //存储
        {
            StorageEventOnWrap wrap = new StorageEventOnWrap();

            int cameraPermission_0 = grantResults[0];
            int cameraPermission_1 = grantResults[1];
            if (PackageManager.PERMISSION_GRANTED != cameraPermission_0 || PackageManager.PERMISSION_GRANTED != cameraPermission_1 ) {
                wrap.flag = false;
            }
            else
            {
                wrap.flag = true;
            }

            EventBus.getDefault().post(wrap);
        }
        else if( requestCode == 65 ) //麦克风+摄像头
        {
            CameraAndAudioEventOnWrap wrap = new CameraAndAudioEventOnWrap();

            int cameraPermission = grantResults[0];
            if (PackageManager.PERMISSION_GRANTED != cameraPermission) {
                wrap.flag_camera = false;
            }
            else
            {
                wrap.flag_camera = true;
                this.cameraObserver.reopen_camera();
            }

            cameraPermission = grantResults[1];
            if (PackageManager.PERMISSION_GRANTED != cameraPermission) {
                wrap.flag_audio = false;
            }
            else
            {
                wrap.flag_audio = true;
            }

            EventBus.getDefault().post(wrap);
        }
    }

    private void openWbFromLocalFile(String path) {
        SdkUtil.getWbShareManager().openLocalWb(path, new WbCreateListener() {
            @Override
            public void onWbCreated(WhiteBoard whiteBoard) {
                if (proxy.getMeetingInfo().layoutType == LayoutType.VIDEO_LAYOUT
                        || SdkUtil.getVideoManager().hasFullScreenVideo()) {
                    meetingManager.setMeetingLayoutType(LayoutType.STANDARD_LAYOUT,
                            RoomWndState.SplitStyle.SPLIT_STYLE_4);
                }
            }

            @Override
            public void onWbCreateFailed(int code) {
                if (code == WbCreateListener.PERMISSION_DENIED) {
                    ToastUtils.showShort(
                            R.string.meetingui_permission_not_permitted_admin);
                } else if (code == WbCreateListener.PERMISSION_OCCUPIED) {
                    ToastUtils.showShort( R.string.meetingui_wb_count_limit_tips);
                } else if (code == WbCreateListener.SOMEONE_ALREADY_SHARING) {
                    ToastUtils.showShort( R.string.meetingui_share_limit_tip);
                } else {
                    ToastUtils.showShort( R.string.meetingui_open_wb_failed);
                }
            }
        });
    }

    private final ScreenSharingCreateListener sharingCreateListener = new ScreenSharingCreateListener() {
        @Override
        public void onShareScreenSuccessfully() {
            ToastUtils.showShort(R.string.meetingui_screen_share_success);
            meetingManager.setMeetingLayoutType(LayoutType.STANDARD_LAYOUT,
                    RoomWndState.SplitStyle.SPLIT_STYLE_4);
            // 跳转到桌面
            try { //线上BUG-22157
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
            } catch (ActivityNotFoundException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onShareScreenFailed(int code, Object... objects) {
            if (code == ScreenShareResultCode.PERMISSION_DENIED) {
                int result = (int) objects[0];
                if (result == RolePermissionEngine.PERMISSION_OCCUPIED) {
                    Long userId = (Long) objects[1];
                    String nickName = "";
                    if (userId != null) {
                        BaseUser user = userModel.getUserInfo(userId);
                        if (user != null) {
                            nickName = user.getNickName();
                        }
                    }
                    String tip = getString(R.string.meetingui_other_user_share_screen, nickName);
                    ToastUtils.showShort(tip);
                } else if (result == RolePermissionEngine.SHARE_LIMIT) {
                    ToastUtils.showShort(R.string.meetingui_share_limit_tip);
                } else {
                    ToastUtils.showShort(
                            R.string.meetingui_permission_not_permitted_admin);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            if (requestCode == Constant.SCREEN_SHARE_REQUEST_CODE) {
                String msg = getString(R.string.meetingui_has_no_share_permission,
                        AppUtils.getAppName());
                //部分手机出现Toast异常显示，加上主线程抛出就好了
                ThreadUtils.getMainHandler().post(() -> ToastUtils.showShort(msg));
            }
            return;
        }

        switch (requestCode) {
            case Constant.SELECT_PIC_BY_PICK_PHOTO:
                if (data != null) {
                    // 不再判断主讲权限
                    String path = MediaUtils.getImagePath(this, data.getData());
                    openWbFromLocalFile(path);
                }
                break;
            case Constant.SELECT_PIC_BY_TACK_PHOTO:
                String takePhotoUri = MediaUtils.getTakePhotoUri().getPath();
                openWbFromLocalFile(takePhotoUri);
                break;
            case Constant.SCREEN_SHARE_REQUEST_CODE:
                PermissionManager manager = SdkUtil.getPermissionManager();
                long localUserId = userModel.getLocalUser().getUserId();
                boolean hasPermission = manager.checkUserPermission(localUserId, true,
                        RolePermission.CREATE_APPSHARE);
                if (!hasPermission) {
                    ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
                    return;
                }
                ScreenShareOptions screenShareOptions = new ScreenShareOptions();
                screenShareOptions.title = getString(R.string.meetingui_screen_shared);
                screenShareOptions.sharpness = VideoQuality.HD;
                shareModel.setScreenShareQualityBias(screenShareOptions);
                String appName = AppUtils.getAppName();
                Notification notification = NotificationUtil.buildNotify(this,
                        R.mipmap.tb_share_open, R.mipmap.tb_share_open,
                        getString(R.string.meetingui_share_screen_notification, appName),
                        getString(R.string.meetingui_sharing_screen_if_stop, appName));
                shareModel.addEventListener(sharingCreateListener);
                shareModel.startScreenSharing(resultCode, data, notification);
                break;

            default:
                break;
        }
    }

    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (popupWindowBuilder != null && popupWindowBuilder.isSuspendWindow()) {
                return true;
            }
            showQuitConfirmView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void showQuitConfirmView() {
        MeetingQuitContainer meetingQuitContainer = new MeetingQuitContainer(this);
        meetingQuitContainer.onlyShowQuitMeetingView();
        meetingQuitContainer
                .setMeetingQuitContainerListener(this::finish);
        popupWindowBuilder
                .setContentView(meetingQuitContainer.getView())
                .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(TAG, "onCreateOptionsMenuonCreateOptionsMenu");
        return true;
    }

    /**
     * PC端 控制扬声器音量时回调该方法
     *
     * @param param 音频参数
     */
    @Override
    public void onAudioParamChanged(AudioParam param) {
        if (param != null) {
            int playVolume = param.playVolume;
            meetingTopTitleView.setVolumeSwitchState(playVolume > 0);
        }
    }

    public void quitRoom() {
        if (exit) {
            return;
        }
        if (isAnonymousLogin || isAnonymousLoginWithRoomId) {
            PlatformConfig.getInstance().setLoginStatus(false);
        }
        Log.i(TAG, "exitRoom()");
        exit = true;
        if (objId != -1) {
            audioModel.removeAudioCaptureCallback(objId);
        }
        MeetingSettingsModel.getInstance().removeListener(this);
        UiEntrance.getInstance().setMenuHelper(null);
        meetingBottomAndTopMenuContainer.recycle();
        variableLayout.unSubscribe();
        meetingManager.updateAudioEnergyState(false);
        currentAudioDataFile = null;
        MeetingTempDataUtils.cleanTempData();
    }

    @Override
    public void onRawCapDataSink(byte[] bytes, int i, int i1) {
        if (currentAudioDataFile != null) {
            FileIOUtils.writeFileFromBytesByStream(currentAudioDataFile, bytes, true);
        }
    }

    @Override
    public void onSettingsChanged(String key, Object value) {
        if (TextUtils.equals(MeetingSettingsKey.KEY_AUDIO_DATA, key)) {
            boolean writeAudioData = (boolean) value;
            registAudioDataCallBack(userModel.getLocalUser(), writeAudioData);
        }
    }

    private File currentAudioDataFile;

    private void registAudioDataCallBack(BaseUser user, boolean isChecked) {
        if (user != null && user.isSpeechDone() && isChecked) {
            //此时传0是代表本地用户
            FileUtils.createOrExistsFile(currentAudioDataFile);
            objId = audioModel.addAudioCaptureCallback(this);
        } else if (objId != -1) {
            audioModel.removeAudioCaptureCallback(objId);
        }
    }

    @Override
    public void showData(ChannelBean o) {

    }
}
