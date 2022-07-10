package com.sdcz.endpass.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.Opcode;
import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.WhiteBoard;
//import com.comix.meeting.listeners.CustomYUVModelListener;
import com.comix.meeting.listeners.ShareModelListener;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.AudioEventOnWrap;
import com.sdcz.endpass.bean.CameraEventOnWrap;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.StorageEventOnWrap;
import com.sdcz.endpass.callback.IMeetingMultimediaDisableListener;
import com.sdcz.endpass.callback.MeetingMenuEventManagerListener;
import com.sdcz.endpass.callback.PopupWindowStateListener;
import com.sdcz.endpass.custommade.meetingover._manager._MeetingStateManager;
import com.sdcz.endpass.dialog.GlobalPopupView;
import com.sdcz.endpass.dialog.MarkWhiteBoardDialog;
import com.sdcz.endpass.model.AppCache;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.model.MicEnergyMonitor;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.PermissionUtils;
import com.sdcz.endpass.util.PermissionsPageUtils;
import com.sdcz.endpass.widget.AttendeeView;
import com.sdcz.endpass.widget.CustomDialog;
import com.sdcz.endpass.widget.MeetLeftView;
import com.sdcz.endpass.widget.MeetingBottomMenuView;
import com.sdcz.endpass.widget.MeetingTopTitleView;
import com.sdcz.endpass.widget.PopupWindowBuilder;
import com.sdcz.endpass.widget.UserPopWidget;
import com.sdcz.endpass.widget.VariableLayout;
import com.inpor.base.sdk.audio.AudioManager;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.permission.PermissionManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.base.sdk.video.VideoManager;
import com.inpor.nativeapi.adaptor.AudioParam;
import com.inpor.nativeapi.adaptor.RolePermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Description     会中顶部、底部菜单的P层
 */
public class MeetingBottomAndTopMenuContainer implements
        MeetingTopTitleView.MeetingTopTitleListener, MeetingBottomMenuView.MeetingBottomMenuListener,
        MeetingQuitContainer.MeetingQuitContainerListener, IMeetingMultimediaDisableListener,
        PopupWindowStateListener, MeetingMoreMenuContainer.QuitMeetingListener,
        MicEnergyMonitor.AudioEnergyListener {

    private MeetingTopTitleView meetingTopTitleView;
    private MeetingBottomMenuView meetingBottomMenuView;
    private final Activity context;
    private long bottomAndTopMenuLastOnClickTime;
    private boolean isVideoApplyingState;
    private boolean isSpeechApplyingState;
    private MeetingMenuEventManagerListener meetingMenuEventManagerListener;
    private final PopupWindowBuilder popupWindowBuilder;
    private MeetingMoreMenuContainer meetingMoreMenuContainer;
    private final UserManager userModel;
    private final AudioManager audioManager;
    private final MeetingManager meetingModel;
    private static UserPopWidget attendeeView2;
    private String channelCode;
    //生命周期回调onPause 如果没有录音权限 则 下一次onResume 检查是否有此权限
    private boolean isBackgroupPermission = false;//true onPause之后没有权限

    private String meeting = "0";

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetingBottomAndTopMenuContainer(Activity context, String channelCode, String type) {
        this.context = context;
        this.channelCode = channelCode;
        popupWindowBuilder = new PopupWindowBuilder(context);
        popupWindowBuilder.setPopupWindowStateListener(this);
        userModel = SdkUtil.getUserManager();
        audioManager =   SdkUtil.getAudioManager();
        meetingModel = SdkUtil.getMeetingManager();
        BaseUser localUser = userModel.getLocalUser();
        MicEnergyMonitor.getInstance().addAudioEnergyListener(this, MicEnergyMonitor.MEETING_MENU_CONTAINER);
        MicEnergyMonitor.getInstance().addAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
        meeting = type;
        if (meeting.equals("0")){
            attendeeView2 = new UserPopWidget(context, channelCode);
        }
    }



    public static UserPopWidget getUserPopWidget (){
        if (null == attendeeView2) return null;
        return attendeeView2;
    }


    /**
     * 关联 MeetingTopTitleView
     *
     * @param meetingTopTitleView MeetingTopTitleView
     */
    public void correlationMeetingTopMenu(MeetingTopTitleView meetingTopTitleView) {
        this.meetingTopTitleView = meetingTopTitleView;
        this.meetingTopTitleView.addMeetingTopTitleListener(this);
        initTopMenuData();
    }


    /**
     * 关联 MeetingBottomMenuView
     *
     * @param meetingBottomMenuView MeetingBottomMenuView
     */
    public void correlationMeetingBottomMenu(MeetingBottomMenuView meetingBottomMenuView, boolean dataLayoutShow) {
        this.meetingBottomMenuView = meetingBottomMenuView;
        this.isDataLayoutShow = dataLayoutShow;
        this.meetingBottomMenuView.addMeetingBottomMenuListener(this);
        SdkUtil.getWbShareManager().addWhiteBoardListener(shareModelListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(VariableLayout.ACTION_DATA_LAYOUT_VISIBILITY);
        LocalBroadcastManager.getInstance(context).registerReceiver(dataLayoutReceiver, filter);
        updateShareBarState();
    }


    /**
     * 初始化顶部标题栏的一些数据
     */
    private void initTopMenuData() {
        //初始化会议室名称
        String strRoomName = meetingModel.getRoomInfo().strRoomName;
        updateTopTitle(strRoomName);
    }
    /**
     * 更新顶部标题栏名称
     *
     * @param strRoomName 会议室名称
     */
    public void updateTopTitle(String strRoomName) {
        meetingTopTitleView.setTitleText(strRoomName);
    }

    /**
     * 更新底部菜单“相机”的icon图标,没有Toast提示
     */
    public void updateBottomMenuCameraIconState(boolean isLocalDisable) {
        BaseUser localUser = userModel.getLocalUser();
        // 状态处理
        if (localUser.isVideoWait()) {
            isVideoApplyingState = true;
            meetingBottomMenuView
                    .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_WAITING, isLocalDisable);
        } else if (localUser.isVideoDone()) {
            isVideoApplyingState = false;
            meetingBottomMenuView
                    .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_DONE, isLocalDisable);
        } else {
            isVideoApplyingState = false;
            meetingBottomMenuView
                    .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_NONE, isLocalDisable);
        }
    }

    /**
     * 更新底部菜单“相机”的icon图标,有Toast提示
     *
     * @param localUser      本地用户
     * @param isLocalDisable 是否已经本地禁用
     */
    public void updateBottomMenuCameraIconState(BaseUser localUser, boolean isLocalDisable) {
        updateBottomMenuCameraIconState(localUser, isLocalDisable);
    }


    /**
     * 更新底部菜单“相机”的icon图标,有Toast提示
     *
     * @param localUser      本地用户
     * @param isLocalDisable 是否已经本地禁用
     */
    public void updateBottomMenuCameraIconState(BaseUser localUser, boolean isLocalDisable, boolean isToast) {
        // 状态处理
        MeetingBottomMenuView.CameraViewState currentCameraViewState = meetingBottomMenuView.getCurrentCameraViewState();
        if (localUser.isVideoWait()) {
            isVideoApplyingState = true;
            if (!isLocalDisable && isToast && currentCameraViewState != null) {
                ToastUtils.showShort(R.string.meetingui_apply_open_camera);
            }
            meetingBottomMenuView
                    .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_WAITING, isLocalDisable);
            meetingTopTitleView.setCameraIconState(false);
        } else if (localUser.isVideoDone()) {
            isVideoApplyingState = false;
            if (currentCameraViewState != null && !isLocalDisable
                    && currentCameraViewState != MeetingBottomMenuView.CameraViewState.CAMERA_STATE_DONE && isToast) {
                ToastUtils.showShort(R.string.meetingui_camera_opened);
            }
            meetingBottomMenuView
                    .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_DONE, isLocalDisable);
            meetingTopTitleView.setCameraIconState(true);
        } else {
            if (isToast && currentCameraViewState != null) {
                if (isVideoApplyingState) {
                    ToastUtils.showShort(R.string.meetingui_give_up_open);
                } else {
                    if (currentCameraViewState != MeetingBottomMenuView.CameraViewState.CAMERA_STATE_NONE) {
                        ToastUtils.showShort(R.string.meetingui_camera_closed);
                    }
                }
            }
            isVideoApplyingState = false;
            meetingBottomMenuView
                    .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_NONE, isLocalDisable);
            meetingTopTitleView.setCameraIconState(false);
        }
    }


    /**
     * 权限状态变更
     *
     * @param localUser 用户数据
     */
    public void onUserJurisdictionStateChanged(BaseUser localUser) {
    }

    /**
     * 更新底部菜单“麦克风”的Icon图标状态
     *
     * @param localUser 用户数据
     */
    public void updateBottomMenuMicIconState(BaseUser localUser, boolean isLocalDisable) {
        updateBottomMenuMicIconState(localUser, isLocalDisable, true);
    }

    /**
     * 更新底部菜单“麦克风”的Icon图标状态
     *
     * @param localUser 用户数据
     */
    public void updateBottomMenuMicIconState(BaseUser localUser, boolean isLocalDisable, boolean isToast) {
        MeetingBottomMenuView.MicViewState currentMicIconState = meetingBottomMenuView.getCurrentMicIconState();
        if (localUser.isSpeechWait()) {
            //移除对用户的语音能量值监听
            MicEnergyMonitor.getInstance().removeAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
            isSpeechApplyingState = true;
            if (isToast && currentMicIconState != null) {
                ToastUtils.showShort(R.string.meetingui_speech_applying);
            }
            meetingBottomMenuView
                    .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_WAITING, isLocalDisable);
        } else if (localUser.isSpeechDone()) {

            //音频权限判断
            AudioManager audioManager = SdkUtil.getAudioManager();
            List<String> permissionList = PermissionUtils.requestMeetingPermission();
            if (permissionList != null && (permissionList.contains(Manifest.permission.RECORD_AUDIO))) {

                //没有权限还能创建音频流，纠正底层bug吧
                if( permissionList.size() == 1 ) {
                    if( EventBus.getDefault().isRegistered(this) == false ) {
                        EventBus.getDefault().register(this);
                    }
                    ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                            new String[]{Manifest.permission.RECORD_AUDIO}, 64);
                }
                audioManager.broadcastAudio(localUser);
                return;

            }

            //添加对用户的语音能量值监听
            MicEnergyMonitor.getInstance().addAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
            isSpeechApplyingState = false;
            if (currentMicIconState != MeetingBottomMenuView.MicViewState.AUDIO_STATE_DONE) {
                if (isToast && currentMicIconState != null) {
                    ToastUtils.showShort(R.string.meetingui_mic_applied);
                }
                meetingBottomMenuView
                        .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_DONE, isLocalDisable);
            }

        } else {
            //移除对用户的语音能量值监听
            MicEnergyMonitor.getInstance().removeAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
            if (isToast && currentMicIconState != null) {
                if (isSpeechApplyingState) {
                    ToastUtils.showShort(R.string.meetingui_give_up_open);
                } else {
                    if (currentMicIconState != MeetingBottomMenuView.MicViewState.AUDIO_STATE_NONE) {
                        ToastUtils.showShort(R.string.meetingui_give_up_mic);
                    }
                }
            }
            isSpeechApplyingState = false;
            meetingBottomMenuView.setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_NONE, isLocalDisable);
        }
    }


    /**
     * 更新底部菜单“麦克风”的Icon图标状态,没有Toast提示
     *
     * @param isLocalDisable 是否本地已禁用
     */
    public void updateBottomMenuMicIconState(boolean isLocalDisable) {
        BaseUser localUser = userModel.getLocalUser();
        if (localUser.isSpeechWait()) {
            MicEnergyMonitor.getInstance().removeAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
            meetingBottomMenuView
                    .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_WAITING, isLocalDisable);
            isSpeechApplyingState = true;
        } else if (localUser.isSpeechDone()) {
            MicEnergyMonitor.getInstance().removeAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
            meetingBottomMenuView
                    .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_DONE, isLocalDisable);
        } else {
            //移除对用户的语音能量值监听
            MicEnergyMonitor.getInstance().removeAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
            isSpeechApplyingState = false;
            meetingBottomMenuView
                    .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_NONE, isLocalDisable);
        }
    }

    //-----------------------顶部菜单栏按钮点击事件回调监听 code start---------------------->

    /**
     * 顶部标题栏 切换相机按钮被点击回调
     *
     * @param view 当前被点击的View
     * @see MeetingTopTitleView.MeetingTopTitleListener
     */
    @Override
    public void onClickChangeCameraItemListener(View view) {
        //切换前后摄像头 依赖CameraObserver类  该类为protected 无法直接再本类中直接调用所以回调出去再Activity中处理
        if (meetingMenuEventManagerListener != null) {
            meetingMenuEventManagerListener.onMenuManagerChangeCameraListener();
        }
        //点击顶部标题栏 “切换摄像头”按钮，底部和顶部菜单重新计时，5秒后隐藏
        bottomAndTopMenuTimerControl(true);
    }

    /**
     * 点击顶部标题栏的 “音频”按钮 为打开音频状态时回调
     */
    @Override
    public void onClickOpenAudioListener() {
        //点击顶部标题栏 “静音”按钮，底部和顶部菜单重新计时，5秒后隐藏
        bottomAndTopMenuTimerControl(true);
        AudioManager audioManager = SdkUtil.getAudioManager();
        AudioParam audioParam = audioManager.getAudioParam();
        audioParam.aec = 1;
        audioParam.playVolume = 50;
        audioManager.setAudioParam(audioParam);
        audioManager.setAppMute(userModel.getLocalUser().getUserId(), (byte) 0);
    }

    /**shi wpo
     * 点击顶部标题栏的 “音频”按钮 为关闭音频状态时回调
     */
    @Override
    public void onClickCloseAudioListener() {
        //点击顶部标题栏 “静音”按钮，底部和顶部菜单重新计时，5秒后隐藏
        bottomAndTopMenuTimerControl(true);
        AudioManager audioManager = SdkUtil.getAudioManager();
        AudioParam audioParam = audioManager.getAudioParam();
        audioParam.aec = 1;
        audioParam.playVolume = 0;
        audioManager.setAudioParam(audioParam);
        SdkUtil.getAudioManager().setAppMute(userModel.getLocalUser().getUserId(), (byte) 1);
    }


    /**
     * 点击麦克风按钮回调
     *
     * @see MeetingBottomMenuView.MeetingBottomMenuListener
     */
    @Override
    public void onClickMicListener() {
        //点击“麦克风”按钮弹出popup时，底部和顶部菜单重新计时，5秒后隐藏
        bottomAndTopMenuTimerControl(true);
        List<String> permissionList = PermissionUtils.requestMeetingPermission();
        if (permissionList != null && (permissionList.contains(Manifest.permission.RECORD_AUDIO))) {
            //showSinglePermissionDialog(Manifest.permission.RECORD_AUDIO);
            if( EventBus.getDefault().isRegistered(this) == false ) {
                EventBus.getDefault().register(this);
            }
            ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO}, 61);
            return;
        }
        AudioEventOnWrap event_on = new AudioEventOnWrap();
        event_on.flag = true;
        on_audio_open(event_on);
        /*
        boolean disableLocalMic = AppCache.getInstance().isMicDisable();
        boolean videoDone = userModel.getLocalUser().isSpeechDone();
        if (disableLocalMic) {
            if (!videoDone) {
                ToastUtils.showShort(R.string.meeting_disable_mic);
                return;
            }
        }
        boolean hasPermission = SdkUtil.getPermissionManager().checkUserPermission(userModel.getLocalUser().getUserId()
                , false, RolePermission.AUDIO, RolePermission.AUDIO_CAN_BE_BROADCASTED);
        if (hasPermission) {
            audioManager.broadcastAudio(userModel.getLocalUser());
        }else {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
        }*/
    }

    public static byte[] InputStreamTOByte(InputStream in) throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        byte[] data = new byte[in.available()];
        int count = -1;
        while((count = in.read(data,0,in.available())) != -1)
            outStream.write(data, 0, count);

        data = null;
        return outStream.toByteArray();
    }

    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        int realcount = inStream.available();
        byte[] buff = new byte[realcount];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, realcount)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }


    //摄像头权限申请回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_camera_open(CameraEventOnWrap event_on) {

        if( event_on.description.isEmpty() == false ) {
            BaseUser localUser = userModel.getLocalUser();
            // 状态处理
            if (localUser.isVideoWait()) {
                isVideoApplyingState = true;
                meetingBottomMenuView
                        .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_WAITING, !event_on.flag);
            } else if (localUser.isVideoDone()) {
                isVideoApplyingState = false;
                meetingBottomMenuView
                        .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_DONE, !event_on.flag);
            } else {
                isVideoApplyingState = false;
                meetingBottomMenuView
                        .setCameraIconState(MeetingBottomMenuView.CameraViewState.CAMERA_STATE_NONE, !event_on.flag);
            }
        }
        else if( event_on.flag == true ) {


                boolean isEnableLocalCamera = AppCache.getInstance().isCameraEnabled();
                if (!isEnableLocalCamera) {
                    boolean videoDone = userModel.getLocalUser().isVideoDone();
                    if (!videoDone) {
                        ToastUtils.showShort(R.string.meeting_disable_camera);
                        return;
                    }
                }

                SdkUtil.getVideoManager().broadcastVideo(userModel.getLocalUser());


        }
        //纯属测试 读取YUV视频文件播放
        else
        {




            //MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            /*
            metadataRetriever.setDataSource(file);

            new Thread(new Runnable() {
                @Override
                public void run() {




                    String width = metadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String height = metadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

                    String duration = metadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);//时长(毫秒)


                    //VideoDevice.getInstance().writeVideoSample(System.currentTimeMillis() / 1000,file.createOutputStream(). file.getLength());

                }
            }).start();

*/



        }


        EventBus.getDefault().unregister(this);

    }

    //音频录制权限申请回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_audio_open(AudioEventOnWrap event_on) {

        if( event_on.broadcast == true )
        {
            //入会自动播放音频设置回调
            if( event_on.flag == true )
            {
                UserManager userModel = SdkUtil.getUserManager();
                BaseUser localUser = userModel.getLocalUser();
                AudioManager audioManager = SdkUtil.getAudioManager();
                audioManager.broadcastAudio(localUser);
            }
        }
        else {
            if (event_on.description.isEmpty() == false) {
                BaseUser localUser = userModel.getLocalUser();
                if (localUser.isSpeechWait()) {
                    MicEnergyMonitor.getInstance().removeAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
                    meetingBottomMenuView
                            .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_WAITING, !event_on.flag);
                    isSpeechApplyingState = true;
                } else if (localUser.isSpeechDone()) {
                    MicEnergyMonitor.getInstance().removeAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
                    meetingBottomMenuView
                            .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_DONE, !event_on.flag);
                } else {
                    //移除对用户的语音能量值监听
                    MicEnergyMonitor.getInstance().removeAudioSource(localUser, MicEnergyMonitor.MEETING_MENU_CONTAINER);
                    isSpeechApplyingState = false;
                    meetingBottomMenuView
                            .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_NONE, !event_on.flag);
                }
            } else if (event_on.flag == true) {

                boolean disableLocalMic = AppCache.getInstance().isMicDisable();
                boolean videoDone = userModel.getLocalUser().isSpeechDone();
                if (disableLocalMic) {
                    if (!videoDone) {
                        ToastUtils.showShort(R.string.meeting_disable_mic);
                        return;
                    }
                }
                boolean hasPermission = SdkUtil.getPermissionManager().checkUserPermission(userModel.getLocalUser().getUserId()
                        , false, RolePermission.AUDIO, RolePermission.AUDIO_CAN_BE_BROADCASTED);
                if (hasPermission) {
                    audioManager.broadcastAudio(userModel.getLocalUser());
                } else {
                    ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
                }

            }
        }

        EventBus.getDefault().unregister(this);

    }




    /**
     * 点击相机按钮回调
     *
     * @see MeetingBottomMenuView.MeetingBottomMenuListener
     */
    @Override
    public void onClickCameraListener(){
        bottomAndTopMenuTimerControl(true);
        List<String> permissionList = PermissionUtils.requestMeetingPermission();
        VideoManager videomanager = SdkUtil.getVideoManager();
        boolean use_local_camera = videomanager.get_use_local_camera();
        if (/*SharedPreferencesUtils.getBoolean("use_local_camera",true)*/use_local_camera == true && permissionList != null && (permissionList.contains(Manifest.permission.CAMERA))) {
            if( EventBus.getDefault().isRegistered(this) == false ) {
                EventBus.getDefault().register(this);
            }
            ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                    new String[]{Manifest.permission.CAMERA}, 60);
            return;
        }

        SdkUtil.getVideoManager().broadcastVideo(userModel.getLocalUser());
        /*
        if( SharedPreferencesUtils.getBoolean("use_local_camera",true) == true )
        {
            DeviceUtil.startup_local_capture(true);
        }
        else
        {
            DeviceUtil.startup_local_capture(false);
            YUVModel.getInstance().goon = true;
            YUVModel.getInstance().start_loop();
        }
        */
    }

    private void showSinglePermissionDialog(String permissionStr) {
        if (TextUtils.isEmpty(permissionStr)) {
            return;
        }
        GlobalPopupView globalPopupView = new GlobalPopupView(context);
        if (permissionStr.equals(Manifest.permission.CAMERA)) {
            globalPopupView.setContentText(R.string.meetingui_open_camera_permission);
            globalPopupView.setTitleText(R.string.meetingui_no_camera_permission);
        } else if (permissionStr.equals(Manifest.permission.RECORD_AUDIO)) {
            globalPopupView.setContentText(R.string.meetingui_open_mic_permission);
            globalPopupView.setTitleText(R.string.meetingui_no_mic_permission);
        }
        globalPopupView.setRightButtonText(R.string.meetingui_setting);
        globalPopupView.setGlobalPopupListener(() -> {
            PermissionsPageUtils permissionsPageUtils = new PermissionsPageUtils();
                permissionsPageUtils.jumpPermissionPage();
                globalPopupView.dismissPopupWindow();
        });
        popupWindowBuilder
                .setContentView(globalPopupView)
                .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
                .setOutsideTouchable(false)
                .show();
    }


    /**
     * 点击 共享 按钮回调
     *
     * @param sharedMenuView 当前被点击的View
     * @see MeetingBottomMenuView.MeetingBottomMenuListener
     */
    @Override
    public void onClickSharedListener(View sharedMenuView) {
        long userId = userModel.getLocalUser().getUserId();
        //权限检查
        boolean hasPermissions = SdkUtil.getPermissionManager().checkUserPermission(userId,false,
                RolePermission.CREATE_APPSHARE, RolePermission.CREATE_WHITEBOARD);

        if (hasPermissions) {
            MeetingSelectSharedContainer container = new MeetingSelectSharedContainer(context);
            popupWindowBuilder.setContentView(container.getView()).show();
            //点击“共享”按钮弹出popup时，取消底部和顶部菜单的定时隐藏，设置为一直显示
            bottomAndTopMenuTimerControl(false);
        } else {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
        }
    }

    /**
     * 点击 退出 回调
     *
     * @param quitMenuView 当前被点击的View
     * @see MeetingBottomMenuView.MeetingBottomMenuListener
     */
    @Override
    public void onClickQuitListener(View quitMenuView) {
        showOutLoding();
//        MeetingQuitContainer meetingQuitContainer = new MeetingQuitContainer(context);
//        meetingQuitContainer.onlyShowQuitMeetingView();
//        meetingQuitContainer.setMeetingQuitContainerListener(this);
//        popupWindowBuilder.setContentView(meetingQuitContainer.getView())
//                .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
//                .setDuration(100)
//                .show();
//        bottomAndTopMenuTimerControl(true);
    }

    @Override
    public void onClickMeetingInfoListener() {
        MeetingInfoContainer infoContainer = new MeetingInfoContainer(context);
        popupWindowBuilder.setContentView(infoContainer.getView())
                .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
                .setDuration(100)
                .show();
        bottomAndTopMenuTimerControl(true);
    }


    @Override
    public void onClickLeftOtherListener(String channelCode) {
        //左上角
        MeetLeftView attendeeView = new MeetLeftView(context, channelCode);
        popupWindowBuilder.setContentView(attendeeView)
                .setAnimationType(PopupWindowBuilder.AnimationType.FADE).show();
    }

    /**
     * 点击 更多 回调
     *
     * @param moreMenuView 当前被点击的View
     * @see MeetingBottomMenuView.MeetingBottomMenuListener
     */
    @Override
    public void onClickMoreListener(View moreMenuView) {
        meetingMoreMenuContainer = new MeetingMoreMenuContainer(context);
        meetingMoreMenuContainer.setMeetingMultimediaDisableListener(this);
        meetingMoreMenuContainer.setQuitMeetingListener(this);
        popupWindowBuilder.setContentView(meetingMoreMenuContainer.getView())
                .setAnimationType(PopupWindowBuilder.AnimationType.SLIDE)
                .show();
        bottomAndTopMenuTimerControl(true);
    }

    @Override
    public void onClickAttendeeListener() {
        if (meeting.equals("0")){
            popupWindowBuilder.setContentView(attendeeView2)
                    .setAnimationType(PopupWindowBuilder.AnimationType.SLIDE).show();
        }else {
            AttendeeView attendeeView = new AttendeeView(context);
            popupWindowBuilder.setContentView(attendeeView)
                    .setAnimationType(PopupWindowBuilder.AnimationType.SLIDE).show();
        }

    }

    @Override
    public void onClickShareBarLockListener(boolean isLock) {
        if (!isLock) {
            bottomAndTopMenuTimerControl(true);
        }
    }

    /**
     * 点击共享工具条中的“停止共享按钮”
     */
    @Override
    public void onClickStopShareItemListener() {
        if (currentShareBean == null) {
            return;
        }
        long localUserId = userModel.getLocalUser().getUserId();
        if (localUserId != currentShareBean.getUserId()) {
            PermissionManager manager = SdkUtil.getPermissionManager();
            RolePermission permission = null;
            switch (currentShareBean.getType()) {
                case DATA_TYPE_WB:
                    permission = RolePermission.CLOSE_OTHERS_WHITEBOARD;
                    break;
                case DATA_TYPE_APPSHARE:
                    permission = RolePermission.CLOSE_OTHERS_APPSHARE;
                    break;
            }
            if (permission != null) {
                boolean hasPermission = manager.checkUserPermission(localUserId, true, permission);
                if (!hasPermission) {
                    ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
                    return;
                }
            }
        }
        SdkUtil.getWbShareManager().closeWhiteBoard(currentShareBean.getId());
    }

    /**
     * 点击共享工具条中的“切换页签”按钮
     */
    @Override
    public void onClickChangeTabItemListener() {
        ShareSwitchContainer shareSwitchContainer = new ShareSwitchContainer(context, popupWindowBuilder);
        shareSwitchContainer.show();
    }

    /**
     * 点击共享工具条中的“标记”按钮
     */
    @Override
    public void onClickShareMarkItemListener() {
        if (currentShareBean instanceof WhiteBoard) {
            if (!SdkUtil.getWbShareManager().hasMarkWbRights((WhiteBoard) currentShareBean)) {
                ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
                return;
            }
            MarkWhiteBoardDialog markWhiteBoardDialog = new MarkWhiteBoardDialog(context,
                    (WhiteBoard) currentShareBean);
            markWhiteBoardDialog.show();
        } else {
            ToastUtils.showShort(R.string.meetingui_current_content_unable_to_mark);
        }


    }

    /**
     * 点击共享工具条中的“旋转”按钮
     */
    @Override
    public void onClickShareRotatingItemListener() {
        if (currentShareBean instanceof WhiteBoard) {
            WhiteBoard whiteBoard = (WhiteBoard) currentShareBean;
            SdkUtil.getWbShareManager().rotateByAngle(whiteBoard, -90);
        } else {
            ToastUtils.showShort(R.string.meetingui_current_content_unable_to_rotate);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_storage_open(StorageEventOnWrap event_on) {

        if( event_on.flag == true )
        {
            long localUserId = SdkUtil.getUserManager().getLocalUser().getUserId();
            PermissionManager permissionManager = SdkUtil.getPermissionManager();
            boolean hasPermission = permissionManager.checkUserPermission(localUserId, true,
                    RolePermission.SAVE_WHITEBOARD);
            if (!hasPermission) {
                ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
            } else {
                int code = SdkUtil.getWbShareManager().saveWb((WhiteBoard) currentShareBean);
                if (code == Opcode.SUCCESS) {
                    ToastUtils.showShort(R.string.meetingui_save_success);
                } else {
                    ToastUtils.showShort(R.string.meetingui_save_fail);
                }
            }
        }
        else
        {
            ToastUtils.showShort(R.string.meetingui_save_fail);
        }

        EventBus.getDefault().unregister(this);

    }


    /**
     * 点击共享工具条中的“保存”按钮
     */
    @Override
    public void onClickShareDownloadItemListener() {
        if (currentShareBean instanceof WhiteBoard) {

            List<String> permissionList = PermissionUtils.requestBeforeMeetingPermission(this.context);
            if (permissionList != null && permissionList.size()>0 ) {
                if( EventBus.getDefault().isRegistered(this) == false ) {
                    EventBus.getDefault().register(this);
                }
                ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 63);
                return;
            }

            long localUserId = SdkUtil.getUserManager().getLocalUser().getUserId();
            PermissionManager permissionManager = SdkUtil.getPermissionManager();
            boolean hasPermission = permissionManager.checkUserPermission(localUserId, true,
                    RolePermission.SAVE_WHITEBOARD);
            if (!hasPermission) {
                ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
            } else {
                int code = SdkUtil.getWbShareManager().saveWb((WhiteBoard) currentShareBean);
                if (code == Opcode.SUCCESS) {
                    ToastUtils.showShort(R.string.meetingui_save_success);
                } else {
                    ToastUtils.showShort(R.string.meetingui_save_fail);
                }
            }
        } else {
            ToastUtils.showShort(R.string.meetingui_current_content_can_not_be_save);
        }
    }

    /**
     * @Description: 参会人点击回调
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/18 15:28
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/18 15:28
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
//    @Override
//    public void onClickAttendeeListener(String channelCode, IMassageEvent massageEvent) {
//////        AttendeeView attendeeView = new AttendeeView(context);
//////        popupWindowBuilder.setContentView(attendeeView)
//////                .setAnimationType(PopupWindowBuilder.AnimationType.SLIDE).show();
////        popWidget = new UserPopWidget(context, channelCode);
////
////        //点击要弹出popupwindow时父控件显示为灰色
//////        WindowManager.LayoutParams lp = getWindow().getAttributes();
//////        lp.alpha = 0.3f;
//////        context.getWindow().setAttributes(lp);
////        popWidget.showAtLocation(context.getResources().,
////                Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
//////        context.startActivity(new Intent(context, UserPopActivity.class).putExtra(Constants.SharedPreKey.CHANNEL_CODE, channelCode));
//
//        popupWindowBuilder.setContentView(attendeeView2)
//                .setAnimationType(PopupWindowBuilder.AnimationType.SLIDE).show();
//    }

    //----------------------底部菜单栏按钮点击事件回调 code end----------------------||


    /**
     * 顶部标题和底部菜单栏释放定时隐藏
     *
     * @param isTimer true 定时隐藏（5秒误操作） false 取消定时隐藏（一直显示）
     */
    private void bottomAndTopMenuTimerControl(boolean isTimer) {
        if (isTimer) {
            meetingTopTitleView.restartTime();
            meetingBottomMenuView.restartTime();
        } else {
            meetingTopTitleView.cancelTimer();
            meetingBottomMenuView.cancelTimer();
        }
    }


    /**
     * 横竖屏状态改变时，回调通知各个组件,改变为横屏参数
     */
    public void onLandscapeStateNotification() {
        popupWindowBuilder.setSlideMode(Gravity.RIGHT);
        onLandTopAndBottomMenuState();

    }

    /**
     * 横竖屏状态改变时，回调通知各个组件,改变为竖屏参数
     */
    public void onPortraitStateNotification() {
        popupWindowBuilder.setSlideMode(Gravity.BOTTOM);
        onPortraitTopAndBottomMenuState();
    }


    /**
     * 底部菜单栏和顶部标题栏的显示/隐藏状态更新
     * 当“退出”、“共享”、“更多”弹窗为显示状态时 （竖屏状态下，只隐藏底部菜单栏;横屏状态下，底部和顶部都隐藏）
     */
    private void onLandTopAndBottomMenuState() {
        boolean isShow = popupWindowBuilder.isShowing();
        //横竖屏切换时，还要设置标题栏和底部菜单栏的显示和隐藏状态
        if (isShow) {
            meetingTopTitleView.fadeHide();
            meetingBottomMenuView.fadeHide();
        }

    }


    /**
     * 这个为透明度的隐藏方式
     * 底部菜单栏和顶部标题栏的显示/隐藏状态更新
     * 当“退出”、“共享”、“更多”弹窗为显示状态时 （竖屏状态下，只隐藏底部菜单栏;横屏状态下，底部和顶部都隐藏）
     */
    private void onPortraitTopAndBottomMenuState() {
        boolean isShowing = popupWindowBuilder.isShowing();
        if (isShowing) {
            meetingTopTitleView.fadeShow();
            meetingBottomMenuView.fadeShow();
        }
    }


    /**
     * 这个为上下滑动消失的隐藏方式
     * 顶部标题栏和顶部菜单栏的显示和隐藏控制
     */
    public void bottomAndTopMenuShowControl() {
        long defaultTime = 400;
        long currentTime = System.currentTimeMillis();
        //禁止频繁点击
        if (currentTime - bottomAndTopMenuLastOnClickTime < defaultTime) {
            return;
        }
        boolean show = meetingBottomMenuView.isShowMenuBar();
        if (show) {
            meetingTopTitleView.slideHide();
            meetingBottomMenuView.slideHide();
        } else {
            meetingTopTitleView.slideShow();
            meetingBottomMenuView.slideShow();
        }
        bottomAndTopMenuLastOnClickTime = currentTime;
    }

    /**
     * 添加 事件监听
     *
     * @param meetingMenuEventManagerListener 事件监听接口
     */
    public void addMeetingMenuEventManagerListener(MeetingMenuEventManagerListener meetingMenuEventManagerListener) {
        this.meetingMenuEventManagerListener = meetingMenuEventManagerListener;
    }

    /**
     * 隐藏共享工具条
     */
    public void hideSharedBar() {
        meetingBottomMenuView.hideSharedBar();
    }

    /**
     * 显示共享工具条
     */
    public void showSharedBar() {
        meetingBottomMenuView.showSharedBar();
    }

    /**
     * 资源回收
     */
    public void recycle() {
        if (meetingMoreMenuContainer != null) {
            meetingMoreMenuContainer.recycle();
            meetingMoreMenuContainer = null;
        }
        popupWindowBuilder.dismissDialog();
        meetingTopTitleView.recycle();
        meetingBottomMenuView.recycle();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(dataLayoutReceiver);
        MicEnergyMonitor.getInstance().removeAudioEnergyListener(this, MicEnergyMonitor.MEETING_MENU_CONTAINER);
    }


    @Override
    public void onOpenPopupWindowListener() {
        //如果是竖屏状态下，当“选择共享”菜单弹出时，只隐藏底部菜单，顶部标题栏保存显示状态
        if (ScreenUtils.isPortrait()) {
            meetingTopTitleView.fadeShow();
            meetingBottomMenuView.fadeShow();
        } else {
            meetingTopTitleView.fadeHide();
            meetingBottomMenuView.fadeHide();
        }
        bottomAndTopMenuTimerControl(false);
    }


    /**
     * popouWindow关闭时回调
     */
    @Override
    public void onClosePopupWindowListener() {
        meetingTopTitleView.fadeShow();
        meetingBottomMenuView.fadeShow();
        bottomAndTopMenuTimerControl(true);
        if (meetingMoreMenuContainer != null) {
            meetingMoreMenuContainer.recycle();
            meetingMoreMenuContainer = null;
        }
    }

    private void updateShareBarState() {
        if (currentShareBean != null && isDataLayoutShow) {
            meetingBottomMenuView.showSharedBar();
            if (currentShareBean instanceof WhiteBoard) {
                meetingBottomMenuView.setMarkViewEnabled(true);
                meetingBottomMenuView.setRotatingViewEnabled(true);
                meetingBottomMenuView.setSaveViewEnabled(true);
            } else {
                meetingBottomMenuView.setMarkViewEnabled(false);
                meetingBottomMenuView.setRotatingViewEnabled(false);
                meetingBottomMenuView.setSaveViewEnabled(false);
            }
        } else {
            meetingBottomMenuView.hideSharedBar();
        }
    }

    /**
     * “退出会议” 弹窗事件
     */
    @Override
    public void onQuitMeetingAndFinishActivityListener() {
        if (meetingMenuEventManagerListener != null) {
            meetingMenuEventManagerListener.onMenuManagerFinishActivityListener();
        }
    }

    /**
     * 当在“更多”菜单中 禁用/启用麦克风时回调该方法
     */
    @Override
    public void onDisableMicListener(boolean isDisable) {

        updateBottomMenuMicIconState(isDisable);
    }

    /**
     * 当在“更多”菜单中 禁用/启用摄像头时回调该方法
     */
    @Override
    public void onDisableCameraListener(boolean isDisable) {
        updateBottomMenuCameraIconState(isDisable);
    }


    /**
     * 视频预览 开/关 回调
     *
     * @param openVideoPreview true 打开视频预览 false 关闭视频预览
     */
    @Override
    public void onVideoPreviewSwitchChangeListener(boolean openVideoPreview) {
        if (meetingTopTitleView != null) {
            meetingTopTitleView.setCameraIconState(openVideoPreview);
        }
    }


    /**
     * 在“更多”菜单中点击“结束会议”按钮 弹出 退出会议回调
     */
    @Override
    public void onQuitMeetingListener() {
        if (meetingMenuEventManagerListener != null) {
            meetingMenuEventManagerListener.onMenuManagerFinishActivityListener();
        }
    }


    private BaseShareBean currentShareBean;
    private final ShareModelListener shareModelListener = new ShareModelListener() {

        @Override
        public void onShareTabChanged(int type, BaseShareBean shareBean) {
            if (type == SHARE_ACTIVE) {
                currentShareBean = shareBean;
                updateShareBarState();
            }
            meetingBottomMenuView.setTabNum(SdkUtil.getWbShareManager().getShareBeans().size());
        }

        @Override
        public void onNoSharing() {
            currentShareBean = null;
            updateShareBarState();
        }
    };


    /**
     * 数据区域是否显示
     */
    boolean isDataLayoutShow;

    private final BroadcastReceiver dataLayoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isDataLayoutShow = intent.getBooleanExtra("state", false);
            updateShareBarState();
        }
    };


    @Override
    public void onAudioEnergyChanged(List<BaseUser> sources) {
        BaseUser localUser = userModel.getLocalUser();
        for (BaseUser user : sources) {
            if (user.getUserId() == localUser.getUserId()
                    && user.isSpeechDone()) {
                ThreadUtils.runOnUiThread(() -> meetingBottomMenuView
                        .setMicIconState(MeetingBottomMenuView.MicViewState.AUDIO_STATE_DONE,
                                AppCache.getInstance().isMicDisable(), user.getSoundEnergy()));
                return;
            }
        }
    }

    /**
     * @Description: activity 生命周期回调
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/15 18:03
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/15 18:03
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void onPause() {
        if (!checkoutMicPermission()) {
            isBackgroupPermission = true;
        }
    }

    /**
     * @Description: activity 生命周期回调
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/15 18:03
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/15 18:03
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void onResume() {
        if (checkoutMicPermission() && isBackgroupPermission) {
            SdkUtil.getAudioManager().initAudioDevice(userModel.getLocalUser().getRoomUserInfo());
        }
        isBackgroupPermission = false;
    }

    /**
     * @Description: 判断录音权限
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/4/15 18:06
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/15 18:06
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public boolean checkoutMicPermission() {
        List<String> permissionList = PermissionUtils.requestMeetingPermission();
        return permissionList == null || (!permissionList.contains(Manifest.permission.RECORD_AUDIO));
    }


    /**
     * 离开或者退出
     */
    private void showOutLoding() {
        CustomDialog.Builder builder = new CustomDialog.Builder(context);

        if (MobileMeetingActivity.isAdmin) {
            builder.setMessage("您确认要退出当前任务吗？");
            builder.setTitle("退出任务");
            builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            builder.setNegativeButton("退出",
                    new android.content.DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            leaveOp();
                        }
                    });
        } else {
            builder.setMessage("退出任务,将会向管理员发送申请");
            builder.setTitle("申请退出");
            builder.setEtMessage("请填写申请事由");
            builder.setPositiveButton("申请", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //设置你的操作事项
                    getChannelByCode(context, channelCode, builder.getEtMessage());
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("取消",
                    new android.content.DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }

        builder.create().show();
    }

    private void leaveOp(){
        //本地管理员结束会议
        Map<String,Object> reason_map = new HashMap();
        reason_map.put("code",1);
        reason_map.put("type",1);
        _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);
        SdkUtil.getMeetingManager().closeMeeting(0, "");
        context.finish();
    }

    public void getChannelByCode(Activity activity,String channelCode, String txt){
        RequestUtils.getChannelByCode(channelCode, new MyObserver<ChannelBean>(activity) {
            @Override
            public void onSuccess(ChannelBean result) {
                if (null != result){
                    ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.APPLY_LEAVE + result.getCreateUser() + "*" + txt);
                }
            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
    }

}
