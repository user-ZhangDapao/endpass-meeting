package com.sdcz.endpass.base;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
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
import com.sdcz.endpass.MainActivity;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.dialog.CallInDialog;
import com.sdcz.endpass.model.AppCache;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.ContactEnterUtils;

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

        if (ApplicationInstance.getInstance().isSpecifiedActivity(MobileMeetingActivity.class)) {
            //当前已在该会议中
            MeetingInfo meetingInfo = MeetingModel.getInstance().getMeetingInfo();
            if (meetingInfo.inviteCode != null
                    && meetingInfo.inviteCode.equals(String.valueOf(inviteData.getInviteCode()))) {
                return;
            }
        }

        if(AppCache.getInstance().isFromMeeting()){
            return;
        }

//        //会中免打扰：拒绝
        if (isBusy()) {
            Log.e("navi", "isBusy");
            return;
        }

        if (!ApplicationInstance.getInstance().isSpecifiedActivity(MobileMeetingActivity.class)) {
            this.callUserId = inviter;
            showRoomListRecvDialog(inviter, inviteId, inviteData);
        }

    }

    private boolean isBusy() {
        return ApplicationInstance.getInstance().isSpecifiedActivity(MobileMeetingActivity.class)
                || PaasOnlineManager.getInstance().isBusy() ;
    }

    /**
     * 显示会前呼叫弹框
     *
     * @param userId     邀请用户id
     * @param inviteId   邀请id
     * @param inviteData 邀请信息
     */
    protected void showRoomListRecvDialog(long userId, long inviteId, InviteData inviteData) {
        CompanyUserInfo companyUserInfo = new CompanyUserInfo();
        companyUserInfo.setUserId((int) inviteData.getInviter().getUserId());
        companyUserInfo.setDisplayName(inviteData.getInviter().getUserName());
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
//                PaasOnlineManager.getInstance().acceptInvite(userId, inviteId, true);
                ContactEnterUtils.getInstance(ApplicationInstance.getInstance().getCurrentActivity())
                        .joinInstantMeetingRoom(String.valueOf(inviteData.getInviteCode()), ApplicationInstance.getInstance().getCurrentActivity());

            }
        });
        callInDialog.setOnDismissListener(dialog -> PaasOnlineManager.getInstance().setBusy(false));
        callInDialog.show();
        PaasOnlineManager.getInstance().setBusy(true);
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
                ToastUtils.showShort(R.string.hst_main_call_user_call_finsh);
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
