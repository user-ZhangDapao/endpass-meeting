package com.sdcz.endpass.login;

import android.util.Log;

import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;

public class DefaultJoinMeetingCallBack implements JoinMeetingCallback {

    private static final String TAG = "DefaultJoinMeeting";
    private final LoginMeetingCallBack mListener;

    public DefaultJoinMeetingCallBack(LoginMeetingCallBack listener) {
        mListener = listener;
    }

    @Override
    public void onStart(Procedure procedure) {
        Log.i(TAG, "onStart: ");
        mListener.onStart(procedure);
    }

    @Override
    public void onState(int i, String s) {
        Log.i(TAG, "onState: i is " + i + " -- s is " + s);
        mListener.onState(i);
    }

    @Override
    public void onBlockFailed(ProcessStep processStep, int i, String s) {
        Log.i(TAG, "onBlockFailed: processStep is " + processStep + " -- i is " + i
                + " -- s is " + s);
        mListener.onBlockFailed(processStep, i, s);
    }

    @Override
    public void onFailed() {
        Log.i(TAG, "onFailed: ");
        mListener.onFailed();
    }

    @Override
    public void onSuccess() {
        Log.i(TAG, "onSuccess: ");
        mListener.onJoinMeetingSuccess();
    }

    @Override
    public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
        mListener.onInputPassword(isFrontVerify, inputPassword);
    }
}
