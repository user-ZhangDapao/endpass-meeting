package com.sdcz.endpass.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.MeetingModule;
import com.inpor.base.sdk.SdkManager;
import com.inpor.log.Logger;
import com.inpor.manager.application.ApplicationInstance;
import com.inpor.manager.model.Instantmeeting.GlobalPaasCode;
import com.inpor.manager.model.MeetingInfo;
import com.inpor.manager.model.MeetingModel;
import com.inpor.manager.util.LogUtil;
import com.inpor.manager.util.ScreenDeskUtil;
import com.inpor.nativeapi.adaptor.InviteData;
import com.inpor.sdk.online.InviteStateListener;
import com.inpor.sdk.online.PaasOnlineManager;
import com.inpor.sdk.repository.bean.CompanyUserInfo;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.MainActivity;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.ChannelTypeBean;
import com.sdcz.endpass.bean.EventBusMode;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.constant.Constant;
import com.sdcz.endpass.custommade.meetingover._manager._MeetingStateManager;
import com.sdcz.endpass.dialog.CallInDialog;
import com.sdcz.endpass.model.AppCache;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import cn.robotpen.utils.StringUtil;

/**
 * @author: HST
 * @date: 2022/5/26
 */
public class SdkBaseActivity extends AppCompatActivity implements InviteStateListener {
    protected CallInDialog callInDialog;
    private long callUserId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PaasOnlineManager.getInstance().addInviteStateCallback(this);
    }

    @Override
    public void onInviteAccepted(long remoteId, long inviteId) {
        Log.e("navi", "onInviteAccepted");
    }

    @Override
    public void onInviteRejected(long remoteId, long inviteId, int reason) {
        ///拒接 如果临时会话 退出
        Log.e("navi", "onInviteRejected");
    }

    @Override
    public void onInviteIncome(long inviter, long inviteId, InviteData inviteData) {

        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        am.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);

        Log.e("navi", "onInviteIncome");

        if (ScreenDeskUtil.isBackDesk) {
            ScreenDeskUtil.returnApp(this, this.getClass().getName());
        }
        if (inviteData.getInviteCode() == 666 ) return;
        if (inviteData.getInviteCode() == 777 ) {
            EventBus.getDefault().post(new EventBusMode("777"));
            return;
        }

        getChannelTypeByCode(inviter, inviteId, inviteData);
//
//        if (ApplicationInstance.getInstance().isSpecifiedActivity(MobileMeetingActivity.class)) {
//            //当前已在该会议中
//            MeetingInfo meetingInfo = MeetingModel.getInstance().getMeetingInfo();
//
//
//            if (meetingInfo.inviteCode != null) {
//                if ( null != SharedPrefsUtil.getUserInfo().getRoomId()
//                        && !SharedPrefsUtil.getUserInfo().getRoomId().equals(inviteData.getInviteCode())){
//                    return;
//                }
//
//                    if (meetingInfo.roomID == SharedPrefsUtil.getUserInfo().getRoomId()) return;
//                    Map<String, Object> reason_map = new HashMap();
//                    reason_map.put("code", 1);
//                    reason_map.put("type", 1);
//                    _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);
//                    SdkUtil.getMeetingManager().closeMeeting(0, "");
//                    if (null == SharedPrefsUtil.getUserInfo().getRoomId()){
//                        getChannelTypeByCode(inviteData.getInviteCode());
//                    } else {
//                        getChannelTypeByCode(SharedPrefsUtil.getUserInfo().getRoomId());
//                    }
//                return;
//            }
//        }
//
//        if(AppCache.getInstance().isFromMeeting()){
//            return;
//        }
//
////        //会中免打扰：拒绝
//        if (isBusy() || inviteData.getInviteCode() == 666) {
//            Log.e("navi", "isBusy");
//            return;
//        }

    }


    /**
     * 显示会前呼叫弹框
     *
     * @param userId     邀请用户id
     * @param inviteId   邀请id
     * @param inviteData 邀请信息
     */
    protected void showRoomListRecvDialog(long userId, long inviteId, InviteData inviteData,ChannelTypeBean result) {
        this.callUserId = userId;
        CompanyUserInfo companyUserInfo = new CompanyUserInfo();
        companyUserInfo.setUserId((int) inviteData.getInviter().getUserId());
        try {
            String name = SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserName).getJSONObject(String.valueOf(inviteData.getInviter().getUserId())).getString("nickName");
            companyUserInfo.setDisplayName(name);
        } catch (JSONException e) {
            companyUserInfo.setDisplayName(inviteData.getInviter().getUserName());
        }
        callInDialog = new CallInDialog(ApplicationInstance.getInstance().getCurrentActivity(), companyUserInfo);
        callInDialog.setCallName(companyUserInfo.getDisplayName());
        callInDialog.setWayChangeListener(new CallInDialog.CallWayChangeListener() {
            @Override
            public void refuse() {
                LogUtil.i("Online", "refuse button click");
                PaasOnlineManager.getInstance().setInviteId(-1);
                SdkUtil.getContactManager().AcceptRejectInvite(userId, inviteId, false);
//                PaasOnlineManager.getInstance().acceptInvite(userId, inviteId, false);
                callInDialog.dismiss();
            }

            @Override
            public void answer() {
                LogUtil.i("Online", "accept button click");
                callInDialog.dismiss();
                PaasOnlineManager.getInstance().setInviteId(inviteId);
                SdkUtil.getContactManager().AcceptRejectInvite(userId, inviteId, true);
                ContactEnterUtils.getInstance(ApplicationInstance.getInstance().getCurrentActivity())
                        .joinForCode(String.valueOf(inviteData.getInviteCode()),result.getRoomType(), result.getChannelCode(),ApplicationInstance.getInstance().getCurrentActivity());
            }
        });
        callInDialog.setOnDismissListener(dialog -> PaasOnlineManager.getInstance().setBusy(false));
        callInDialog.show();
        PaasOnlineManager.getInstance().setBusy(true);
    }

    private void getChannelTypeByCode(long inviter, long inviteId, InviteData inviteData){
        RequestUtils.getChannelTypeByCode(inviteData.getInviteCode(), new MyObserver<ChannelTypeBean>(SdkBaseActivity.this) {
            @Override
            public void onSuccess(ChannelTypeBean result) {
                MeetingModule meetingModule =  SdkUtil.getMeetingManager().getMeetingModule();
                Long taskRoomId = SharedPrefsUtil.getUserInfo().getRoomId() == null ? 0 : SharedPrefsUtil.getUserInfo().getRoomId();
                if (result.getRoomType() == 1 || result.getRoomType() == 0){
                    showRoomListRecvDialog(inviter, inviteId, inviteData, result);
                }else if (result.getRoomType() == 2){ //地图
                    if (taskRoomId.equals(0)){ ///无任务
                        if (meetingModule.isInMeeting()){
                            EventBus.getDefault().post(new EventBusMode("TemporaryUserLeave"));
                        }
                        joinRoom(String.valueOf(inviteData.getInviteCode()), result.getRoomType(), result.getChannelCode());
                    }else {
                        if (meetingModule.isInMeeting()){
                            if (meetingModule.getMeetingInfo().inviteCode.equals(result.getInviteCode()) || meetingModule.getMeetingInfo().roomId == result.getRoomId())return;
                            EventBus.getDefault().post(new EventBusMode("TemporaryUserLeave"));
                        }
                        joinRoom(String.valueOf(SharedPrefsUtil.getUserInfo().getRoomId()),result.getRoomType(), result.getChannelCode());
                    }
                }else if (result.getRoomType() == 3){
                    if (meetingModule.isInMeeting()){
                        if (meetingModule.getMeetingInfo().inviteCode.equals(result.getInviteCode()) || meetingModule.getMeetingInfo().roomId == result.getRoomId())return;
                        EventBus.getDefault().post(new EventBusMode("TemporaryUserLeave"));
                    }
                    UserEntity userEntity = SharedPrefsUtil.getUserInfo();
                    userEntity.setRoomId(result.getRoomId());
                    SharedPrefsUtil.putUserInfo(userEntity);
                    joinRoom(String.valueOf(SharedPrefsUtil.getUserInfo().getRoomId()),result.getRoomType(), result.getChannelCode());
                }
            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });

    }

    private void joinRoom(String inviteCode, int roomType, String channelCode){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ContactEnterUtils.getInstance(ApplicationInstance.getInstance().getCurrentActivity())
                        .joinForCode(inviteCode, roomType, channelCode, ApplicationInstance.getInstance().getCurrentActivity());
            }
        }, 1000);
    }


    @Override
    public void onInviteCanceled(long inviter, long inviteId, int reason) {

        if (ApplicationInstance.getInstance().isSpecifiedActivity(MobileMeetingActivity.class)) {
            // 会中不提示
            if (callUserId == inviter) {
                toastByReason(inviter, reason);
            }
            return;
        }
        if (callUserId == inviter && callInDialog != null && callInDialog.isShowing()) {
            callInDialog.dismiss();
            toastByReason(inviter, reason);
        }
    }


    private void toastByReason(long remoteId, int reason) {
        switch (reason) {
            case GlobalPaasCode.NORMAL_BYUSER:
//                ToastUtils.showShort(R.string.hst_main_call_user_call_finsh);
                break;
            case GlobalPaasCode.NORMAL_CLEAR:
                if (remoteId != 0) {
                    ToastUtils.showShort("您已在其他终端进行操作");
                }
                break;
            case GlobalPaasCode.NORMAL_LOCAL_TIMEOUT:
                Logger.info("navi", "NORMAL_LOCAL_TIMEOUT");
                break;
            default:
                ToastUtils.showShort("未知错误！");
                break;
        }
    }


//    private boolean isBusy() {
//        return ApplicationInstance.getInstance().isSpecifiedActivity(MobileMeetingActivity.class)
//                || InstantMeetingModel.getInstance().isBusy();
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PaasOnlineManager.getInstance().removeInviteStateCallback(this);
    }
}
