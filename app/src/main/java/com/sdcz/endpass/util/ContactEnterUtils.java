package com.sdcz.endpass.util;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.blankj.utilcode.util.ToastUtils;
import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.base.sdk.roomlist.RoomListManager;
import com.inpor.sdk.PlatformConfig;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;
import com.inpor.sdk.repository.BaseResponse;
import com.inpor.sdk.repository.bean.InstantMeetingInfo;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.dialog.InputPasswordDialog;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.ui.RoomListActivity;

import java.util.Collections;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * @author: HST
 * @date: 2022/5/30
 */
public class ContactEnterUtils {


    private static volatile ContactEnterUtils contactEnterUtils;
    private Context context;
    private LoadingDialog loadingDialog;
    private InstantMeetingInfo mRoomInfo;
    private InputPasswordDialog inputPasswordDialog;
    private int errorPwdCount = 0;

    private ContactEnterUtils(Context context) {
        this.context = context;
    }

    public static ContactEnterUtils getInstance(Context context) {
        if (contactEnterUtils == null) {
            synchronized (ContactEnterUtils.class) {
                if (contactEnterUtils == null) {
                    contactEnterUtils = new ContactEnterUtils(context);
                }
            }
        }
        return contactEnterUtils;
    }


    //自己拉人 自己创建会议并进会
    public void createInstantMeetingRoom(Activity activity) {
        loadingDialog = new LoadingDialog(activity);
        loadingDialog.show();
        String nickName = PlatformConfig.getInstance().getUserName();
        RoomListManager manager = SdkUtil.getRoomListManager();
        String meetingName = String.format(context.getString(R.string.create_instant_meeting_format), nickName);
        manager.createInstantMeeting(meetingName, Collections.emptyList(), 2,
                30, "", "", new IRoomListResultInterface<BaseResponse<InstantMeetingInfo>>() {
                    @Override
                    public void failed(int code, String errorMsg) {
                        ToastUtils.showShort(R.string.instant_meeting_create_fail);
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void succeed(BaseResponse<InstantMeetingInfo> result) {
                        if (result.getResCode() != 1) {
                            ToastUtils.showShort(result.getResMessage());
                            loadingDialog.dismiss();
                            return;
                        }
                        mRoomInfo = result.getResult();
                        joinMeeting(activity);
                    }
                });

    }

    private void joinMeeting(Activity activity) {
        if (mRoomInfo == null) {
            return;
        }
        String userName = PlatformConfig.getInstance().getUserName();
        JoinMeetingManager.getInstance().loginRoomId(String.valueOf(mRoomInfo.getInviteCode()), userName, "",
                false, new JoinMeetingCallback() {

                    @Override
                    public void onStart(Procedure procedure) {
                        loadingDialog.updateText(R.string.logging);
                    }

                    @Override
                    public void onState(int state, String msg) {
                        loadingDialog.updateText(LoginStateUtil.convertStateToString(state));
                    }

                    @Override
                    public void onBlockFailed(ProcessStep step, int code, String msg) {
                        ToastUtils.showShort(LoginErrorUtil.getErrorSting(code));
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailed() {
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
                        showInputPasswordDialog(isFrontVerify, inputPassword);
                    }

                    @Override
                    public void onSuccess() {
                        loadingDialog.dismiss();
                        Intent intent = new Intent(activity, MobileMeetingActivity.class);
                        intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN, false);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                });
    }

    private void showInputPasswordDialog(boolean isFrontVerify, InputPassword inputPassword) {
        Disposable disposable = Flowable.just(inputPassword)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(input -> {
                    if (this.inputPasswordDialog == null) {
                        inputPasswordDialog = InputPasswordDialog.showInputPwdDialog(context);
                        inputPasswordDialog.setButtonCallback((dialog, which) -> {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                loadingDialog.dismiss();
                            }
                        });
                    }
                    inputPasswordDialog.update(isFrontVerify, inputPassword);
                    if (errorPwdCount > 0) {
                        ToastUtils.showShort(R.string.check_password);
                    }
                    this.inputPasswordDialog.show();
                    errorPwdCount++;
                });
    }

    //被呼叫加入即时会议
    //roomType:0,    0语音通话  1视频通话 2地图查看 3固
    public void joinForCode(String inviteCode, int roomType, String channelCode, Activity activity ) {
//        if(loadingDialog == null){
        loadingDialog = new LoadingDialog(activity);
//        }
        loadingDialog.show();
        String userName = PlatformConfig.getInstance().getUserName();
        JoinMeetingManager.getInstance().loginRoomId(inviteCode, userName, "",
                false, new JoinMeetingCallback() {

                    @Override
                    public void onStart(Procedure procedure) {
                        loadingDialog.updateText(R.string.logging);
                    }

                    @Override
                    public void onState(int state, String msg) {
                        loadingDialog.updateText(LoginStateUtil.convertStateToString(state));
                    }

                    @Override
                    public void onBlockFailed(ProcessStep step, int code, String msg) {
                        ToastUtils.showShort(LoginErrorUtil.getErrorSting(code));
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailed() {
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
                        showInputPasswordDialog(isFrontVerify, inputPassword);
                    }

                    @Override
                    public void onSuccess() {
                        loadingDialog.dismiss();
//                        Intent intent = new Intent(activity, MobileMeetingActivity.class);
//                        intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN, false);
//                        activity.startActivity(intent);
////                        if(activity instanceof RoomListActivity){
////                        }else {
////                            activity.finish();
////                        }

                        Intent intent = new Intent(activity, MobileMeetingActivity.class);
                        intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN,false);
                        intent.putExtra(Constants.SharedPreKey.CHANNEL_CODE,channelCode);
                        intent.putExtra(MobileMeetingActivity.MEETIING_TYPE,roomType);
                        startActivity(intent);
                    }
                });


    }
}
