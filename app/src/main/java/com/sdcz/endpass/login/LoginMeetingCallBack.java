package com.sdcz.endpass.login;

import android.util.Log;

import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;

public interface LoginMeetingCallBack {

    String TAG = "LoginMeetingCallBack";

    /**
     * 如果需要自定义登录成功的逻辑，返回值需为false
     *
     * @return
     */
    default boolean onLoginSuccess() {
        return true;
    }

    void onConflict(boolean isMeeting);

    void onStart(Procedure procedure);

    void onState(int state);

    default void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
        Log.i(TAG, "NeedInputPassword: ");
    }

    void onBlockFailed(ProcessStep step, int code, String msg);

    void onFailed();

    default void onJoinMeetingSuccess(){}
}
