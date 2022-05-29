package com.sdcz.endpass.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseContainer;
import com.sdcz.endpass.callback.IMeetingMoreMenuListener;
import com.sdcz.endpass.callback.IMeetingMultimediaDisableListener;
import com.sdcz.endpass.dialog.SettingsFragmentDialog;
import com.sdcz.endpass.gps.PosService;
import com.sdcz.endpass.model.AppCache;
import com.sdcz.endpass.model.VideoController;
//import com.sdcz.endpass.util.YUVUtil;
import com.sdcz.endpass.widget.LayoutPickerView;
import com.sdcz.endpass.widget.ManagerPasswordView;
import com.sdcz.endpass.widget.MeetingMoreMenuView;
import com.sdcz.endpass.widget.PopupWindowBuilder;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.base.sdk.video.CustomImageOnOffEvent;
import com.inpor.base.sdk.video.VideoManager;

import org.greenrobot.eventbus.EventBus;


/**
 * @Description     会中  点击更多，弹窗更多窗口的P层
 */
public class MeetingMoreMenuContainer extends BaseContainer<MeetingMoreMenuView>
        implements IMeetingMoreMenuListener, MeetingQuitContainer.MeetingQuitContainerListener,
        ManagerPasswordView.ManagerPasswordViewListener {
    private static final String TAG = "MeetingMoreMenuContainer";
    private IMeetingMultimediaDisableListener meetingMultimediaDisableListener;
    private QuitMeetingListener quitMeetingListener;
    private MeetingChatContainer meetingChatContainer;
    private VideoManager videoModel;
    private UserManager userModel;



    /**
     * 构造函数
     *
     * @param context 上下文
     */
    @SuppressLint({"MissingPermission", "LongLogTag"})
    public MeetingMoreMenuContainer(Context context) {
        super(context);
        view = new MeetingMoreMenuView(context);
        view.setIMeetingMoreMenuContainerListener(this);
        initData();
    }


    /**
     * 用户状态回调
     */
    private final UserModelListenerImpl listener = new UserModelListenerImpl(
            UserModelListenerImpl.MANAGER_CHANGED_STATE
                    | UserModelListenerImpl.APPLY_MANAGER_FAILURE_STATE,
            UserModelListenerImpl.ThreadMode.MAIN) {
                static final int PASSWORD_ERROR_CODE = 8468;
                static final int UNALLOWED_ERROR_CODE = 8471;

                /**
                 * 用户信息、状态变更
                 *
                 * @param type 更新的数据类型
                 * @param user 变化的用户
                 */
                @SuppressLint("LongLogTag")
                @Override
                public void onUserChanged(int type, BaseUser user) {
                    if (type != UserModelListenerImpl.MANAGER_CHANGED_STATE || user == null) {
                        return;
                    }
                    if (userModel == null) {
                        return;
                    }
                    BaseUser localUser = userModel.getLocalUser();
                    long targetUserId = user.getUserId();
                    if (targetUserId == localUser.getUserId()) {
                        Log.i(TAG, "是否是管理员:" + localUser + "  -->" + localUser.isManager());
                        if (user.isManager()) {
                            ToastUtils.showShort(R.string.meetingui_manager_succeed);
                            view.setManagerViewText(R.string.meetingui_abandon_manager);
                            view.setExitMeetingViewShowState(true);
                        } else {
                            ToastUtils.showShort(R.string.meetingui_manager_cancel);
                            view.setManagerViewText(R.string.meetingui_apply_manager);
                            view.setExitMeetingViewShowState(false);
                        }
                    }

                }


                @SuppressLint("LongLogTag")
                @Override
                public void onApplyManagerDefeated(int type, int resultCode) {
                    Log.i(TAG, "申请管理员失败:" + type);
                    if (resultCode == UNALLOWED_ERROR_CODE) {
                        ToastUtils.showShort(R.string.meetingui_apply_manager_denied);
                        getView().getPopupWindowBuilder().dismissDialog();
                        return;
                    }

                    if (resultCode == PASSWORD_ERROR_CODE) {
                        ToastUtils.showShort(R.string.meetingui_manager_pwd_wrong);
                        return;
                    }
                }
            };

    @SuppressLint({"MissingPermission", "LongLogTag"})
    private void initData() {

        //初始化相机开启状态
        videoModel = SdkUtil.getVideoManager();
        userModel = SdkUtil.getUserManager();
        boolean isEnableLocalCamera = AppCache.getInstance().isCameraEnabled();
        view.setCameraCheckBoxState(isEnableLocalCamera);

        //初始化视频预览开启状态
        boolean enableVideoPreview = videoModel.isDisplayingLocalVideo();
        Log.i(TAG, "是否启用视频预览：" + enableVideoPreview);
        view.setVideoCheckBoxState(enableVideoPreview);

        //初始化音频开启状态
        boolean micDisable = AppCache.getInstance().isMicDisable();
        view.setMicCheckBoxState(!micDisable);
        //初始化管理员身份状态
        userModel.addEventListener(listener);

        BaseUser localUser = userModel.getLocalUser();
        boolean isManager = localUser.isManager();
        if (isManager) {
            view.setManagerViewText(R.string.meetingui_abandon_manager);
            view.setExitMeetingViewShowState(true);
        } else {
            view.setManagerViewText(R.string.meetingui_apply_manager);
            view.setExitMeetingViewShowState(false);
        }
    }
    @SuppressLint("LongLogTag")
    @Override
    public void onClickChangeLayoutItemListener() {
        Log.i(TAG, "onClickChangeLayoutItemListener");
        LayoutPickerView layoutPickerView = new LayoutPickerView(context);
        PopupWindowBuilder popupWindowBuilder = view.getPopupWindowBuilder();
        popupWindowBuilder.setContentView(layoutPickerView).show();
    }

    @Override
    public void onClickopenGPSListener() {
        //开启服务
        Intent intent = new Intent(context,PosService.class);
        context.startService(intent);
    }

    @Override
    public void onClickcancleGPSListener() {
        ToastUtils.showShort("关闭gps");
        Log.e("22222222","关闭gps");
        //关闭服务
        Intent intent = new Intent(context,PosService.class);
        context.stopService(intent);
    }


    @Override
    public void onClickChatItemListener() {
        meetingChatContainer = new MeetingChatContainer(context);
        PopupWindowBuilder popupWindowBuilder = view.getPopupWindowBuilder();
        popupWindowBuilder.setContentView(meetingChatContainer.getView()).show();
    }

    /**
     * 点击申请管理按钮
     */
    @Override
    public void onClickApplyManagerListener() {
        BaseUser localUser = userModel.getLocalUser();
        boolean isManager = localUser.isManager();
        if (isManager) {
            MeetingAbandonManagerContainer container = new MeetingAbandonManagerContainer(context);
            PopupWindowBuilder popupWindowBuilder = getView().getPopupWindowBuilder();
            popupWindowBuilder.setContentView(container.getView())
                    .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
                    .coverageShow();
        } else {
            ManagerPasswordView passwordView = new ManagerPasswordView(context);
            passwordView.setManagerPasswordViewListener(this);
            PopupWindowBuilder popupWindowBuilder = getView().getPopupWindowBuilder();
            popupWindowBuilder.setContentView(passwordView)
                    .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
                    .show();
        }
    }

    /**
     * 点击设置按钮
     */
    @Override
    public void onClickSettingListener() {
        SettingsFragmentDialog settings = new SettingsFragmentDialog();
        settings.show(((FragmentActivity) context).getSupportFragmentManager(), "settings");
    }

    @Override
    public void onClickFinishMeetingListener() {
        MeetingQuitContainer meetingQuitContainer = new MeetingQuitContainer(context);
        meetingQuitContainer.setMeetingQuitContainerListener(this);
        PopupWindowBuilder popupWindowBuilder = getView().getPopupWindowBuilder();
        popupWindowBuilder.setContentView(meetingQuitContainer.getView())
                .setAnimationType(PopupWindowBuilder.AnimationType.FADE)
                .setDuration(100)
                .show();
    }


    /**
     * 处理音频禁用事件
     */
    @Override
    public boolean dealDisableMicListener(boolean isDisableAudio) {
        AppCache.getInstance().setMicDisable(!isDisableAudio);
        if (meetingMultimediaDisableListener != null) {
            meetingMultimediaDisableListener.onDisableMicListener(!isDisableAudio);
        }
        return true;
    }

    @Override
    public boolean dealDisableCameraListener(boolean isDisableCamera) {

        if( AppCache.getInstance().isCameraEnabled() == isDisableCamera && isDisableCamera == true )
        {
            return false;
        }

        VideoManager videomanager = SdkUtil.getVideoManager();
        boolean use_local_camera = videomanager.get_use_local_camera();

        if( /*SharedPreferencesUtils.getBoolean("use_local_camera",true)*/use_local_camera == false && isDisableCamera == false)
        {
            CustomImageOnOffEvent event = new CustomImageOnOffEvent();
            event.on = false;
            event.show_camera_change_icon = false;
            EventBus.getDefault().post(event);
/*
            if( YUVUtil.getInstance().get_observe_send_data() != null )
            {
                YUVUtil.getInstance().get_observe_send_data().notify_to_draw(false);
                YUVUtil.getInstance().get_observe_send_data().stop_send_data();
                List<String> permissionList = PermissionUtils.requestMeetingPermission();
                if ( permissionList != null && (permissionList.contains(Manifest.permission.CAMERA))) {  //自己发送disable图片信息吧
                    YUVUtil.getInstance().get_observe_send_data().can_send_disable_data();
                }
            }*/
        }
        else if( /*SharedPreferencesUtils.getBoolean("use_local_camera",true)*/use_local_camera == false && isDisableCamera == true)
        {
            CustomImageOnOffEvent event = new CustomImageOnOffEvent();
            event.on = true;
            EventBus.getDefault().post(event);
/*
            if( YUVUtil.getInstance().get_observe_send_data() != null )
            {
                YUVUtil.getInstance().get_observe_send_data().notify_to_draw(true);

                List<String> permissionList = PermissionUtils.requestMeetingPermission();
                if ( permissionList != null && (permissionList.contains(Manifest.permission.CAMERA))) {  //结束发送disable图片信息
                    YUVUtil.getInstance().get_observe_send_data().stop_send_disable_data();
                }
                YUVUtil.getInstance().get_observe_send_data().can_send_data();
            }*/
        }

        AppCache.getInstance().setCameraEnabled(isDisableCamera);
        VideoController.getInstance().enableVideoStateChanged(isDisableCamera);
        if (meetingMultimediaDisableListener != null) {
            meetingMultimediaDisableListener.onDisableCameraListener(!isDisableCamera);
        }
        return false;
    }

    @Override
    public boolean isManager() {
        BaseUser localUser = userModel.getLocalUser();
        return localUser.isManager();
    }


    public void setMeetingMultimediaDisableListener(
            IMeetingMultimediaDisableListener meetingMultimediaDisableListener) {
        this.meetingMultimediaDisableListener = meetingMultimediaDisableListener;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void enableVideoPreviewListener(boolean isEnablePreview) {
        BaseUser localUser = userModel.getLocalUser();
        if (localUser.isVideoDone()) {
            ToastUtils.showShort(R.string.meeting_not_support_check_local);
            view.setVideoCheckBoxState(false);
            return;
        }

        if (isEnablePreview) {
            Log.i(TAG, "开始视频预览");
            videoModel.startPreviewLocalVideo();
        } else {
            Log.i(TAG, "停止视频预览");
            videoModel.closePreviewLocalVideo();
        }

        if (meetingMultimediaDisableListener != null) {
            meetingMultimediaDisableListener.onVideoPreviewSwitchChangeListener(isEnablePreview);
        }
    }

    @Override
    public void onQuitMeetingAndFinishActivityListener() {
        if (quitMeetingListener != null) {
            quitMeetingListener.onQuitMeetingListener();
        }
    }


    public void setQuitMeetingListener(QuitMeetingListener quitMeetingListener) {
        this.quitMeetingListener = quitMeetingListener;
    }


    /**
     * 管理员密码输入弹窗，点击取消事件回调
     */
    @Override
    public void onClickCancelManagerPasswordListener() {

    }

    /**
     * 管理员密码输入弹窗，点击确认事件回调
     */
    @Override
    public void onClickConfirmManagerPasswordListener(String passwordText) {
        if (TextUtils.isEmpty(passwordText)) {
            ToastUtils.showShort(R.string.meetingui_manager_not_empty);
            return;
        }
        userModel.applyToBeManager(passwordText);
        getView().dismissPopupWindow();
    }

    public interface QuitMeetingListener {
        void onQuitMeetingListener();
    }

    /**
     * 资源回收/清理
     */
    @SuppressLint("LongLogTag")
    @Override
    public void recycle() {
        Log.i(TAG, "移除监听");
        if (userModel != null) {
            userModel.removeEventListener(listener);
        }
        PopupWindowBuilder builder = view.getPopupWindowBuilder();
        builder.dismissDialog();
        if (meetingChatContainer != null) {
            meetingChatContainer.recycle();
            meetingChatContainer = null;
        }
        quitMeetingListener = null;
        meetingMultimediaDisableListener = null;
        view.recycle();
        view = null;

    }
}
