package com.sdcz.endpass.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.MeetingModule;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.VideoInfo;
import com.comix.meeting.listeners.AudioModelListener;
import com.comix.meeting.listeners.MeetingModelListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.comix.meeting.listeners.VideoModelListener;
import com.inpor.base.sdk.SdkManager;
import com.inpor.base.sdk.roomlist.ContactManager;
import com.inpor.base.sdk.video.VideoManager;
import com.inpor.nativeapi.adaptor.ChatMsgInfo;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.inpor.sdk.online.PaasOnlineManager;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.LiveDataBus;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.TaskUserListAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.AudioEventOnWrap;
import com.sdcz.endpass.bean.CameraAndAudioEventOnWrap;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannelTypeBean;
import com.sdcz.endpass.bean.EventBusMode;
import com.sdcz.endpass.bean.MeetingSettingsKey;
import com.sdcz.endpass.bean.StorageEventOnWrap;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.callback.BottomMenuLocationUpdateListener;
import com.sdcz.endpass.callback.MeetingMenuEventManagerListener;
import com.sdcz.endpass.callback.MeetingRoomControl;
import com.sdcz.endpass.callback.OnSettingsChangedListener;
import com.sdcz.endpass.custommade.meetingover._manager._MeetingStateManager;
import com.sdcz.endpass.dialog.GlobalPopupView;
import com.sdcz.endpass.gps.PosService;
import com.sdcz.endpass.model.AppCache;
import com.sdcz.endpass.model.CameraObserver;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.model.MeetingLifecycleObserver;
import com.sdcz.endpass.model.MeetingSettings;
import com.sdcz.endpass.model.MeetingSettingsModel;
import com.sdcz.endpass.model.UiEntrance;
import com.sdcz.endpass.presenter.MeetingBottomAndTopMenuContainer;
import com.sdcz.endpass.presenter.MeetingQuitContainer;
import com.sdcz.endpass.presenter.MobileMeetingPresenter;
import com.sdcz.endpass.ui.activity.MailListActivity;
import com.sdcz.endpass.ui.activity.SelectUserActivity;
import com.sdcz.endpass.util.BrandUtil;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.HeadsetMonitorUtil;
import com.sdcz.endpass.util.MeetingTempDataUtils;
import com.sdcz.endpass.util.PermissionUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.util.UiHelper;

import com.sdcz.endpass.view.IMobileMeetingView;
import com.sdcz.endpass.widget.CustomDialog;
import com.sdcz.endpass.widget.MeetingBottomMenuView;
import com.sdcz.endpass.widget.MeetingTopTitleView;
import com.sdcz.endpass.widget.PopupWindowBuilder;

import com.inpor.base.sdk.audio.AudioManager;
import com.inpor.base.sdk.audio.RawCapDataSinkCallback;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.share.ScreenShareManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.nativeapi.adaptor.AudioParam;
import com.inpor.nativeapi.adaptor.Platform;
import com.inpor.nativeapi.adaptor.RoomInfo;
import com.inpor.sdk.PlatformConfig;
import com.sdcz.endpass.widget.PopupWindowToCall;
import com.sdcz.endpass.widget.VideoScreenView;
import com.universal.clientcommon.beans.CompanyUserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileMeetingActivity extends BaseActivity<MobileMeetingPresenter> implements IMobileMeetingView, MeetingModelListener, ChatManager.ChatMessageListener,
        MeetingMenuEventManagerListener, BottomMenuLocationUpdateListener, AudioModelListener, MeetingRoomControl, RawCapDataSinkCallback, OnSettingsChangedListener, VideoModelListener, View.OnClickListener {

    private static final String TAG = "MobileMeetingActivity";
    public static final String EXTRA_ANONYMOUS_LOGIN = "EXTRA_ANONYMOUS_LOGIN";
    public static final String EXTRA_ANONYMOUS_LOGIN_WITH_ROOMID = "EXTRA_ANONYMOUS_LOGIN_WITH_ROOMID";
    public static final String MEETIING_TYPE = "MEETIING_TYPE";
    private RelativeLayout rootView;
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
    private ChatManager chatManager;
    private VideoManager videoManager;
    private ScreenShareManager shareModel;
    private int objId = -1;
    private boolean isAnonymousLogin;
    private boolean isAnonymousLoginWithRoomId;
    private int meetingType = 3;
    private String channelCode = "";
    public static boolean isAdmin = false;

    private VideoScreenView vsVeuneUser;
    private VideoScreenView vsLocalUser;
    private LinearLayout llRoot;
    private List<VideoInfo> videoInfoList = new ArrayList<>();
    private long idid = 0;

    private final UserModelListenerImpl userModelListener2 =
            new UserModelListenerImpl(UserModelListenerImpl.USER_INFO
                    | UserModelListenerImpl.AUDIO_STATE, UserModelListenerImpl.ThreadMode.MAIN) {
                @Override
                public void onUserChanged(int type, BaseUser user) {

                    if (null != vsLocalUser.getVideoInfo()){
                        if (user.equals(vsLocalUser.getVideoInfo().getVideoUser())){
                            if (type == UserModelListenerImpl.USER_INFO) {
                                vsLocalUser.refreshUserInfo(user);
                            } else if (type == UserModelListenerImpl.AUDIO_STATE) {
                                vsLocalUser.refreshUserAudioState(user);
                            }
                        }
                    } else if (null != vsVeuneUser.getVideoInfo()){
                        if (idid == vsVeuneUser.getVideoInfo().getVideoUser().getUserId()){
                            if (type == UserModelListenerImpl.USER_INFO) {
                                vsVeuneUser.refreshUserInfo(user);
                            } else if (type == UserModelListenerImpl.AUDIO_STATE) {
                                vsVeuneUser.refreshUserAudioState(user);
                            }
                        }
                    }
                }
            };

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
        meetingType = getIntent().getIntExtra(MEETIING_TYPE, 3);
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
        chatManager = ChatManager.getInstance();
        chatManager.setChatMessageListener(this);
        videoManager = SdkUtil.getVideoManager();
        //监听VideoModel
        videoManager.addEventListener(this);
        proxy = meetingManager.getMeetingModule();
        audioModel.setAudioParam(AudioParam.getDefault(Platform.ANDROID));
//        MeetingInfo meetingInfo = proxy.getMeetingInfo();
        rootView = findViewById(R.id.activity_root_view);
        vsVeuneUser = findViewById(R.id.vsVeuneUser);
        vsLocalUser = findViewById(R.id.vsLocalUser);
        llRoot = findViewById(R.id.llRoot);
//        variableLayout = findViewById(R.id.variableLayout);
//        variableLayout.subscribe();
//        variableLayout.onLayoutChanged(meetingInfo);
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
        return R.layout.activity_metting_main;
    }

    @Override
    public void initListener() {
        super.initListener();
        vsVeuneUser.setOnClickListener(this);
        vsLocalUser.setOnClickListener(this);
        llRoot.setOnClickListener(this);
    }

    private void initBottomAndTopMenu() {
        RelativeLayout.LayoutParams topLayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        meetingTopTitleView = new MeetingTopTitleView(this, channelCode);
        meetingTopTitleView.setLayoutParams(topLayoutParams);
        RelativeLayout.LayoutParams bottomLayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        bottomLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        meetingBottomMenuView = new MeetingBottomMenuView(this);
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
        userModel.addEventListener(userModelListener2);
        audioModel.addEventListener(this);
        UiEntrance.getInstance().setMenuHelper(() ->
                meetingBottomAndTopMenuContainer.bottomAndTopMenuShowControl());
    }

    @Override
    public void initData() {
        EventBus.getDefault().register(this);
        PaasOnlineManager.getInstance().setBusy(true);
        PaasOnlineManager.getInstance().reportMeetingState(true);
        popupWindowBuilder = new PopupWindowBuilder(this);
        meetingBottomMenuView.setBottomMenuLocationUpdateListener(this);
        meetingBottomAndTopMenuContainer = new MeetingBottomAndTopMenuContainer(this ,channelCode, meetingType);
        meetingBottomAndTopMenuContainer.addMeetingMenuEventManagerListener(this);
        meetingBottomAndTopMenuContainer.correlationMeetingTopMenu(meetingTopTitleView);
        meetingBottomAndTopMenuContainer.correlationMeetingBottomMenu(meetingBottomMenuView,
                true);
        if (null != channelCode && (meetingType == 2 || meetingType == 3)){
            mPresenter.checkAdmin(channelCode);
            mPresenter.getChannelUser(channelCode);
        }
        if (meetingType == 1){
            meetingBottomAndTopMenuContainer.onClickMicListener();
            meetingBottomAndTopMenuContainer.onClickCameraListener();
        }else if (meetingType == 0){
            meetingBottomAndTopMenuContainer.onClickMicListener();
        }
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
//        Map<String,Object> reason_map = new HashMap();
//        ((HashMap)reason_map).put("code",1);
//        ((HashMap)reason_map).put("type",2);
//        _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);

        Map<String,Object> reason_map = new HashMap();
        reason_map.put("code",1);
        reason_map.put("type",1);
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

    @Override
    public void onUserLeave(BaseUser user) {
        if (meetingType != 3) leaveRoom();


    }

    private void leaveRoom(){
        //本地管理员结束会议
        Map<String,Object> reason_map = new HashMap();
        reason_map.put("code",1);
        reason_map.put("type",1);
        _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);
//        SdkUtil.getMeetingManager().closeMeeting(0, "");
        finish();
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

    @Subscribe
    public void onEvent(EventBusMode event) {/* Do something */
        switch (event.getType()){
            case "777":
                if (meetingManager.getMeetingModule().getMeetingInfo().roomId == SharedPrefsUtil.getUserInfo().getRoomId()){
                    leaveRoom();
                }else {
                    mPresenter.getChannelTypeByCode(MobileMeetingActivity.this,meetingManager.getMeetingModule().getMeetingInfo().roomId);
                }
            break;
            case "TemporaryUserLeave":
                leaveRoom();
            break;
        }
    };

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
        EventBus.getDefault().unregister(this);
        PaasOnlineManager.getInstance().setBusy(false);
        PaasOnlineManager.getInstance().reportMeetingState(false);
//        Intent intent = new Intent(this, PosService.class);
//        stopService(intent);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SharedPreKey.REQUEST_CODE_2){
            if (resultCode == Constants.HttpKey.RESPONSE_200){
                if (SharedPrefsUtil.getListUserInfo().size() > 0){
                    List<Integer> selectUsers = new ArrayList<>();
                    List<CompanyUserInfo> CompanyUserInfos = InstantMeetingOperation.getInstance().getCompanyUserData();
                    SharedPrefsUtil.getListUserInfo().forEach( userEntity-> {
                        selectUsers.add(userEntity.getUserId());
                        long userHstId = userEntity.getMdtUserId();
                        CompanyUserInfos.forEach(companyUserInfo->{
                            if (userHstId == companyUserInfo.getUserId() && companyUserInfo.isMeetingState() != 0){
                                InstantMeetingOperation.getInstance().addSelectUserData(companyUserInfo);
                            }
                        });
                    });

                    Integer[] usersId = selectUsers.toArray(new Integer[selectUsers.size()]);
                    if (usersId.length > 0){
                        mPresenter.addChannelUser(this, channelCode, usersId);
                    }
                }
            }else if (resultCode == Constants.SharedPreKey.REQUEST_CODE_201){
//                tvDept.setText(data.getStringExtra(KeyStore.DEPTNAME));
//                deptId = data.getStringExtra(KeyStore.DEPTID);
            }
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
        //todo
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
        chatManager.recycle();
        MeetingSettingsModel.getInstance().removeListener(this);
        UiEntrance.getInstance().setMenuHelper(null);
        meetingBottomAndTopMenuContainer.recycle();
//        variableLayout.unSubscribe();
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
    public void showData(Boolean o) {
        isAdmin = o;
    }

    @Override
    public void venueId(long id) {
       setUserId(id);
    }

    @Override
    public void showChannelInfo(ChannelBean channelBean) {
    }

    @Override
    public void inviteSuccess(Object o) {
        ToastUtils.showShort("邀请成功");

        if(null != meetingBottomAndTopMenuContainer.getUserPopWidget()) {
            meetingBottomAndTopMenuContainer.getUserPopWidget().initData();
        }
        SdkUtil.getContactManager().inviteUsers(meetingManager.getMeetingModule().getMeetingInfo().inviteCode, InstantMeetingOperation.getInstance().getSelectUserData(), new ContactManager.OnInviteUserCallback() {
            @Override
            public void inviteResult(int i, String s) {
                InstantMeetingOperation.getInstance().clearSelectUserData();
            }
        });
    }

    @Override
    public void showRoomInfo(ChannelTypeBean o) {
        if (o.getRoomType() == 3){
            if (o.getRoomId().equals(SharedPrefsUtil.getUserInfo().getRoomId())){
                leaveRoom();
            }
        }
    }

    @Override
    public void onChatMessage(ChatMsgInfo message) {
        String res = new String(message.msg);
            Log.d("====Msg====",res);
        String[] strarray = res.split("\\*");
        switch (strarray[0]){
            case "OPEN_AUDIO":
            case "OFF_AUDIO":
                if (strarray[1].equals("ALL") || String.valueOf(SdkUtil.getUserManager().getLocalUser().getUserId()).equals(strarray[1])){
                    meetingBottomAndTopMenuContainer.onClickMicListener();
                }
                break;
            case "OPEN_VIDEO":
            case "OFF_VIDEO":
                if(String.valueOf(SdkUtil.getUserManager().getLocalUser().getUserId()).equals(strarray[1])){
                    meetingBottomAndTopMenuContainer.onClickCameraListener();
                }
                break;
            case "SWITCH_VIDEO":
                if(String.valueOf(SdkUtil.getUserManager().getLocalUser().getUserId()).equals(strarray[1])){
                    cameraObserver.switchCamera();
                }
                break;
            case "MAIN_VENUE":
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setUserId(Long.valueOf(strarray[1]));
                        if(null != meetingBottomAndTopMenuContainer.getUserPopWidget()) {
                            meetingBottomAndTopMenuContainer.getUserPopWidget().onMassageEvent("MAIN_VENUE",strarray[1]);
                        }
                    }
                });
                break;
            case "ON_LISTEN":
                if(null != meetingBottomAndTopMenuContainer.getUserPopWidget()){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            meetingBottomAndTopMenuContainer.getUserPopWidget().onMassageEvent("ON_LISTEN",strarray[1]);
                        }
                    });
                }
                if (strarray[1].equals("ALL") || SharedPrefsUtil.getUserIdString().equals(strarray[1])){
                    meetingBottomAndTopMenuContainer.onClickOpenAudioListener();
                }
                break;
            case "OFF_LISTEN":
                if(null != meetingBottomAndTopMenuContainer.getUserPopWidget()){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            meetingBottomAndTopMenuContainer.getUserPopWidget().onMassageEvent("OFF_LISTEN",strarray[1]);
                        }
                    });
                }
                if (strarray[1].equals("ALL") || SharedPrefsUtil.getUserIdString().equals(strarray[1])){
                    meetingBottomAndTopMenuContainer.onClickCloseAudioListener();
                }
                break;
            case "PLEASE_LEAVE":
                if(null != meetingBottomAndTopMenuContainer.getUserPopWidget()){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            meetingBottomAndTopMenuContainer.getUserPopWidget().initData();
                        }
                    });
                }
                if (strarray[1].equals("ALL") || SharedPrefsUtil.getUserIdString().equals(strarray[1])){
                    leaveRoom();
                }
                break;
            case "ADD_CHANNEL_USER":
                if(null != meetingBottomAndTopMenuContainer.getUserPopWidget()){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            meetingBottomAndTopMenuContainer.getUserPopWidget().initData();
                        }
                    });
                }
                break;
            case "APPLY_LEAVE":
                if (strarray[1].equals(SharedPrefsUtil.getUserId())){
                    try {
                        showPleaceLeave(String.valueOf(message.srcUserId), strarray[2]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onVideoAdded(List<VideoInfo> list, VideoInfo changeInfo) {
        boolean use_local_camera = videoManager.get_use_local_camera();//SharedPreferencesUtils.getBoolean("use_local_camera",true);

        if( changeInfo.isLocalUser() )
        {
            List<String> permissionList = PermissionUtils.requestMeetingPermission();
            if ( use_local_camera == true && permissionList != null && (permissionList.contains(Manifest.permission.CAMERA))) {
                //没有权限还能创建视频流，纠正底层bug吧
                if( EventBus.getDefault().isRegistered(this) == false ) {
                    EventBus.getDefault().register(this);
                }
                String[] toBeStored = permissionList.toArray(new String[permissionList.size()]);
                int requestCode = 61;//只申请视频
                if( toBeStored.length == 2 )//音视频一起申请
                {
                    requestCode = 65;
                }
                ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                        toBeStored, requestCode);
                videoManager.broadcastVideo(changeInfo.getVideoUser());
                return;
            }
        }
            addVideoInfos(list);

            if (changeInfo.isLocalUser()){
                vsLocalUser.attachVideoInfo(changeInfo);
            }else {
                if (meetingType == 3){
                    if (changeInfo.getVideoUser().getUserId() == idid){
                        if (vsVeuneUser.getVideoInfo() != null) vsVeuneUser.detachVideoInfo();
                        vsVeuneUser.attachVideoInfo(changeInfo);
                    }
                }else {
                    vsVeuneUser.attachVideoInfo(changeInfo);

                }
            }
        }

    @Override
    public void onVideoRemoved(List<VideoInfo> list, VideoInfo changeInfo) {
        addVideoInfos(list);
        if (changeInfo.isLocalUser()){
            vsLocalUser.detachVideoInfo();
        }else {
            if (meetingType == 3){
                if (changeInfo.getVideoUser().getUserId() == idid){
                    vsVeuneUser.detachVideoInfo();
                }
            } else {
                if (changeInfo.isLocalUser()){
                    vsLocalUser.detachVideoInfo();
                }else {
                    vsVeuneUser.detachVideoInfo();
                }
            }
        }
    }

    @Override
    public void onVideoPositionChanged(List<VideoInfo> list) {
        addVideoInfos(list);

    }

    @Override
    public void onVideoFullStateChanged(VideoInfo changeInfo) {

    }

    private void addVideoInfos(List<VideoInfo> list){
        videoInfoList.clear();
        videoInfoList.addAll(list);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llRoot:
            case R.id.vsLocalUser:
            case R.id.vsVeuneUser:
                meetingBottomAndTopMenuContainer.bottomAndTopMenuShowControl();
                break;
        }
    }

    public void setUserId(long userId){
        try {
            idid = SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserId).getJSONObject(String.valueOf(userId)).getLong("mdtUserId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        vsVeuneUser.detachVideoInfo();
        if (userId == SharedPrefsUtil.getUserId() || userId == 0) return;
        for (VideoInfo info : videoInfoList){
            if (info.getVideoUser().getUserId() == idid ){
                vsVeuneUser.attachVideoInfo(info);
                break;
            }
        }
    }

    public void showPleaceLeave(String leaveId, String txt) throws JSONException {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setMessage(SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserName).getJSONObject(String.valueOf(leaveId)).getString("nickName") + "申请离开\\n原因:" + txt);
        builder.setTitle("申请退出");
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置你的操作事项
                try {
                    ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.YES_APPLY_LEAVE + SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserName).getJSONObject(String.valueOf(leaveId)).getLong("userId"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("拒绝",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.NO_APPLY_LEAVE + SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserName).getJSONObject(String.valueOf(leaveId)).getLong("userId"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        builder.create().show();
    }



}
