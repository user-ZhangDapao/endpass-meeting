package com.sdcz.endpass.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.comix.meeting.MeetingModule;
import com.comix.meeting.NetworkUtils;
import com.comix.meeting.annotation.NetworkType;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.SessionState;
import com.comix.meeting.listeners.IConnectStateListener;
import com.comix.meeting.listeners.MeetingModelListener;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.callback.MeetingRoomControl;
import com.sdcz.endpass.custommade.meetingover._manager._MeetingStateManager;
import com.sdcz.endpass.dialog.SimpleTipsDialog2;
import com.sdcz.endpass.dialog.SimpleTipsDialog3;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.nativeapi.adaptor.RoomUserInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 绑定MeetingActivity，同步一些数据的管理
 */
public class MeetingLifecycleObserver implements LifecycleObserver, IConnectStateListener, MeetingModelListener {
    private final MeetingRoomControl meetingRoomControl;
    private MeetingManager meetingModel;
    private SimpleTipsDialog2 reconnectDialog;
    private SimpleTipsDialog2 sessionCloseDialogWhenNetworkDisable;
    private SimpleTipsDialog3 sessionCloseDialog;
    /**
     * 与app模块的约定参数
     */
    public static final String ARG_ROOM_LOGIN_TYPE = "roomLoginType";
    /**
     * 默认登录方式，即是从自己的APP发起会议室登录的
     */
    public static final int LOGIN_ROOM_DEFAULT = 0;
    private boolean background;
    private SessionState sessionState;
    private byte lastMainSpeakerState;
    private ScreenSwitchHelper helper;

    public MeetingLifecycleObserver(@NonNull MeetingRoomControl control) {
        meetingRoomControl = control;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        helper = new ScreenSwitchHelper();
        helper.startScreenSwitchListener();
        MeetingModule proxy = SdkUtil.getMeetingManager().getMeetingModule();
        meetingModel = SdkUtil.getMeetingManager();
        proxy.setConnectStateListener(this);
        MicEnergyMonitor.getInstance().startAudioEnergyMonitor();
        linkOrThirdEnterRoomReceiver = new LinkOrThirdEnterRoomReceiver();
        Utils.getApp().registerReceiver(linkOrThirdEnterRoomReceiver,
                new IntentFilter(BROADCAST_INTENT_ENTER_MEETING));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        background = false;
        MicEnergyMonitor.getInstance().setEnable(true);
        meetingModel.addEventListener(this);
        // 如果已经断开连接，则显示对话框，然后点击退出会议室
        if (SessionState.CLOSED == sessionState) {
            if (NetworkUtils.getNetType(Utils.getApp()) == NetworkType.NONE) {
                showSessionCloseDialogWhenNetworkDisable();
            } else {
                showSessionCloseDialog();
            }
            return;
        }
        // 如果重连，则显示重连对话框
        if (sessionState == SessionState.RECONNECTING) {
            showReconnectDialog();
            return;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        MicEnergyMonitor.getInstance().stopAudioEnergyMonitor();
        MeetingModule.getInstance().setConnectStateListener(null);
        helper.stopScreenSwitchListener();
        MicEnergyMonitor.getInstance().release();
        // 等MeetingActivity销毁后再调用会前的方法进入邀请会议室，否则会导致各Model空指针
        if (linkOrThirdEnterRoomReceiver != null) {
            Utils.getApp().unregisterReceiver(linkOrThirdEnterRoomReceiver);
        }
    }

    @Override
    public void onMainSpeakerChanged(BaseUser user) {
        if (!user.isLocalUser()) {
            return;
        }
        if (user.isMainSpeakerDone()) {
            ToastUtils.showShort(R.string.meetingui_have_main_speaker_right);
        } else if (user.isMainSpeakerWait()) {
            ToastUtils.showShort(R.string.meetingui_applying_main_speaker_tips);
        } else {
            boolean lastStateApplying = lastMainSpeakerState == RoomUserInfo.STATE_WAITING;
            if (lastStateApplying) {
                ToastUtils.showShort(R.string.meetingui_give_up_applying_main_speaker_tips);
            } else {
                ToastUtils.showShort(R.string.meetingui_give_up_main_speaker_tips);
            }
        }
        lastMainSpeakerState = user.getRoomUserInfo().dataState;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        MicEnergyMonitor.getInstance().setEnable(false);
        background = true;
        meetingModel.removeEventListener(this);
        // 先隐藏对话框，onResume如果还在呼叫则再次显示
        hideReconnectDialog();
    }
    @Override
    public void onSessionStateChanged(SessionState sessionState, long l) {
        this.sessionState = sessionState;
        if (background) {
            return;
        }

        if (sessionState == SessionState.RECONNECTED) {
            hideReconnectDialog();
            ToastUtils.showShort(R.string.meetingui_session_reconnected);
        } else if (sessionState == SessionState.RECONNECTING) {
            showReconnectDialog();
        } else {

            //网络异常退出会议
            Map<String,Object> reason_map = new HashMap();
            ((HashMap)reason_map).put("code",2);
            ((HashMap)reason_map).put("type",2);
            _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);

            hideReconnectDialog();
            if (NetworkUtils.getNetType(Utils.getApp()) == NetworkType.NONE) {
                showSessionCloseDialogWhenNetworkDisable();
            } else {
                showSessionCloseDialog();
            }
        }
    }

    private void showSessionCloseDialog() {
        if (sessionCloseDialog != null) {
            sessionCloseDialog.dismiss();
        }
        SimpleTipsDialog3.Builder builder = new SimpleTipsDialog3.Builder();
        sessionCloseDialog = builder.title(Utils.getApp().getString(R.string.meetingui_remind))
                .btnRight(Utils.getApp().getString(R.string.meetingui_confirm))
                .tips(Utils.getApp().getString(R.string.meetingui_session_close))
                .cancelOnTouchOutside(false)
                .interactionListener(dialog -> {
                    dialog.dismiss();
                    meetingRoomControl.quitRoom();
                }).build();
        sessionCloseDialog.show(
                ((FragmentActivity) ActivityUtils.getTopActivity()).getSupportFragmentManager(),
                "room_close_dialog");
    }

    private void showSessionCloseDialogWhenNetworkDisable() {
        if (sessionCloseDialogWhenNetworkDisable != null) {
            sessionCloseDialogWhenNetworkDisable.dismiss();
        }
        SimpleTipsDialog2.Builder builder = new SimpleTipsDialog2.Builder();
        sessionCloseDialogWhenNetworkDisable = builder.title(Utils.getApp().getString(R.string.meetingui_remind))
                .btnLeft(Utils.getApp().getString(R.string.meetingui_check_net))
                .btnRight(Utils.getApp().getString(R.string.meetingui_quit_room))
                .tips(Utils.getApp().getString(R.string.meetingui_no_net_tips))
                .cancelOnTouchOutside(false)
                .interactionListener(new SimpleTipsDialog2.InteractionListener() {
                    @Override
                    public void onLeftBtnClick(DialogFragment dialog) {
                        Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                        ActivityUtils.getTopActivity().startActivity(intent);
                        meetingRoomControl.quitRoom();
                    }

                    @Override
                    public void onRightBtnClick(DialogFragment dialog) {
                        dialog.dismiss();
                        meetingRoomControl.quitRoom();
                    }
                }).build();
        sessionCloseDialogWhenNetworkDisable.show(
                ((FragmentActivity) ActivityUtils.getTopActivity()).getSupportFragmentManager(),
                "reconnect_dialog");
    }

    private void showReconnectDialog() {
        hideReconnectDialog();
        SimpleTipsDialog2.Builder builder = new SimpleTipsDialog2.Builder();
        reconnectDialog = builder.title(Utils.getApp().getString(R.string.meetingui_remind))
                .btnLeft(Utils.getApp().getString(R.string.meetingui_cancel))
                .btnRight(Utils.getApp().getString(R.string.meetingui_confirm))
                .tips(Utils.getApp().getString(R.string.meetingui_session_reconnecting))
                .interactionListener(new SimpleTipsDialog2.InteractionListener() {
                    @Override
                    public void onLeftBtnClick(DialogFragment dialog) {

                        //modified by baodian
                        MobileMeetingActivity activity = (MobileMeetingActivity) ActivityUtils.getTopActivity();
                        activity.quitRoom();
                        activity.finish();
                        //meetingRoomControl.quitRoom();
                    }

                    @Override
                    public void onRightBtnClick(DialogFragment dialog) {
                        dialog.dismiss();
                    }
                }).build();
        reconnectDialog.show(
                ((FragmentActivity) ActivityUtils.getTopActivity()).getSupportFragmentManager(),
                "reconnect_dialog");
    }

    private void hideReconnectDialog() {
        if (reconnectDialog == null) {
            return;
        }
        reconnectDialog.dismiss();
        reconnectDialog = null;
    }

    public static final String BROADCAST_INTENT_ENTER_MEETING = "BROADCAST_INTENT_ENTER_MEETING";

    private LinkOrThirdEnterRoomReceiver linkOrThirdEnterRoomReceiver;

    /**
     * 监听通过链接/第三方应用拉起进入会议室的广播，如果收到，则退出原有的会议室
     */
    private class LinkOrThirdEnterRoomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_INTENT_ENTER_MEETING)) {
                Log.i("MobileMeetingAct", "receive the link start meeting, so quit out the old meeting");
                meetingRoomControl.quitRoom();
            }
        }
    }
}
