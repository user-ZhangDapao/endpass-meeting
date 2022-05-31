package com.sdcz.endpass;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.databinding.ActivityLoginBinding;
import com.sdcz.endpass.dialog.DoubleButtonDialog;
import com.sdcz.endpass.dialog.InputPasswordDialog;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginMeetingCallBack;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.ui.RoomListActivity;
import com.sdcz.endpass.util.NetUtil;
import com.sdcz.endpass.util.PermissionUtils;
import com.sdcz.endpass.util.PermissionsPageUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import permissions.dispatcher.RuntimePermissions;

public class LoginActivity extends AppCompatActivity implements DoubleButtonDialog.IOnClickListener {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding mBinding;
    private DoubleButtonDialog permissionDialog;
    //匿名登录
    private boolean isAnonymousLogin = false;
    private LoadingDialog loadingDialog;
    private InputPasswordDialog inputPasswordDialog;
    private int errorPwdCount;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                .init();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mBinding.setOnClickListener(this::onClick);
        loadingDialog = new LoadingDialog(LoginActivity.this,R.string.LOGIN);

    }

    @Override
    protected void onStart() {
        super.onStart();
        String accountName = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_ACCOUNT_NAME);
        String accountPwd = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_ACCOUNT_PWD);
        String roomId = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_ROOM_ID);
        String nickName = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_NICK_NAME);
        String roomPwd = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_ROOM_PWD);
//        mBinding.etUserName.setText(accountName);
//        mBinding.etUserPwd.setText(accountPwd);
        mBinding.etRoomId.setText(roomId);
//        mBinding.etNickName.setText(nickName);
//        mBinding.etRoomPwd.setText(roomPwd);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkViewDisplay();
    }

    private void checkViewDisplay() {
        mBinding.groupRoom.setVisibility(isAnonymousLogin ? View.VISIBLE : View.GONE);
        mBinding.groupLogin.setVisibility(isAnonymousLogin ? View.GONE : View.VISIBLE);
        mBinding.tvTitle.setText(isAnonymousLogin ? R.string.meeting_room_login : R.string.user_name_login);
        mBinding.ctvRoomLogin.setText(isAnonymousLogin ? R.string.user_name_login : R.string.meeting_room_login);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (permissionDialog != null && permissionDialog.isShowing()) {
            permissionDialog.dismiss();
        }
    }

    private static final long DOUBLE_CLICK_INTERVAL_TIME = 1 * 1000;
    private int lastClickId;
    private long lastClickTime;

    private void onClick(View view) {
        int id = view.getId();
        long currentClickTime = System.currentTimeMillis();
        if (lastClickId == id && currentClickTime - lastClickTime < DOUBLE_CLICK_INTERVAL_TIME) {
            return;
        }
        if (id == R.id.bt_login) {
            loginMeeting();
        } else if (id == R.id.ctv_room_login) {
            mBinding.ctvRoomLogin.setChecked(!isAnonymousLogin);
            isAnonymousLogin = mBinding.ctvRoomLogin.isChecked();
            checkViewDisplay();
        } else if (id == R.id.iv_setting) {
            Intent intent = new Intent(this, com.sdcz.endpass.LoginSettingActivity.class);
            startActivity(intent);
        }
        lastClickTime = currentClickTime;
        lastClickId = id;
    }

    private void loginMeeting() {
        if (!NetUtil.isNetworkConnected()) {
            ToastUtils.showShort(R.string.netError);
            return;
        }
        String serverAddress = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_SERVER_ADDRESS);
        String serverPort = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_SERVER_PORT);
        String clientId = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_CLIENT_ID);
        String clientSecret = SharedPrefsUtil.getString(com.sdcz.endpass.Constants.SP_KEY_CLIENT_SECRET);
        if (TextUtils.isEmpty(serverAddress) || TextUtils.isEmpty(serverPort) ||
                TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)) {
            ToastUtils.showShort(R.string.check_service_config_info);
            return;
        }
        //更新clientId和clientSecret
        JoinMeetingManager.getInstance().setClientIdInfo(clientId, clientSecret);
/*
        List<String> permissionList = PermissionUtils.requestBeforeMeetingPermission(this);
        if (permissionList == null || permissionList.isEmpty()) {
            loginAndJoinMeeting();
            return;
        }
        PermissionUtils.requestPermission(this,permissionList);*/
        loginAndJoinMeeting();
    }

    private void loginAndJoinMeeting() {
        final String userName = mBinding.etUserName.getText().toString();//"17700007781";
        final String userPwd = mBinding.etUserPwd.getText().toString();//"123456";
        final String roomId = mBinding.etRoomId.getText().toString();
        final String nickName = mBinding.etNickName.getText().toString();
        final String roomPwd = mBinding.etRoomPwd.getText().toString();
        SharedPrefsUtil.putString(com.sdcz.endpass.Constants.SP_KEY_ACCOUNT_NAME, userName);
        SharedPrefsUtil.putString(com.sdcz.endpass.Constants.SP_KEY_ACCOUNT_PWD, userPwd);
        SharedPrefsUtil.putString(com.sdcz.endpass.Constants.SP_KEY_ROOM_ID, roomId);
        SharedPrefsUtil.putString(com.sdcz.endpass.Constants.SP_KEY_NICK_NAME, nickName);
        SharedPrefsUtil.putString(com.sdcz.endpass.Constants.SP_KEY_ROOM_PWD, roomPwd);
        //检查输入
        if (isAnonymousLogin) {
            if (TextUtils.isEmpty(roomId)) {
                ToastUtils.showShort(R.string.need_input_room_num);
                return;
            }
            if (TextUtils.isEmpty(nickName)) {
                ToastUtils.showShort(R.string.need_input_nick_name);
                return;
            }
        } else {
            if (TextUtils.isEmpty(userName)) {
                ToastUtils.showShort(R.string.need_input_user_name);
                return;
            }
            if (TextUtils.isEmpty(userPwd)) {
                ToastUtils.showShort(R.string.need_input_user_pwd);
                return;
            }
        }
        //匿名登录
        if (isAnonymousLogin) {
            //检查入会权限
            /*
            List<String> permissionList = PermissionUtils.requestMeetingPermission();
            if (permissionList != null && !permissionList.isEmpty()) {
                PermissionUtils.requestPermission(permissionList,2);
                return;
            }*/
            anonymousLogin();
        }
        //用户登录
        else {
            //没有roomId，会议列表
            if (TextUtils.isEmpty(roomId)) {
                toRoomList(userName, userPwd);
                return;
            }
            /*
            List<String> permissionList = PermissionUtils.requestMeetingPermission();
            if (permissionList != null && !permissionList.isEmpty()) {
                PermissionUtils.requestPermission(permissionList,2);
                return;
            }*/
            realNameLogin();
        }
    }

    private void realNameLogin() {
        final String userName = mBinding.etUserName.getText().toString();
        final String userPwd = mBinding.etUserPwd.getText().toString();
        final String roomId = mBinding.etRoomId.getText().toString();
        loadingDialog.show();
        JoinMeetingManager.getInstance().loginAccount(roomId,userName, userPwd, new LoginMeetingCallBack() {
            @Override
            public void onConflict(boolean isMeeting) {
                Log.i(TAG, "onConflict: ");
                loadingDialog.dismiss();
            }

            @Override
            public void onStart(Procedure procedure) {
                errorPwdCount = 0;
                Log.i(TAG, "LoginMeeting onStart: ");
                loadingDialog.updateText(R.string.logging);
                loadingDialog.show();
            }

            @Override
            public void onState(int state) {
                Log.i(TAG, "onState: state is " + state);
                loadingDialog.updateText(LoginStateUtil.convertStateToString(state));
            }

            @Override
            public void onBlockFailed(ProcessStep step, int code, String msg) {
                Log.i(TAG, "onState: onBlockFailed is " + step);
                ToastUtils.showShort(LoginErrorUtil.getErrorSting(code));
                loadingDialog.dismiss();
            }

            @Override
            public void onFailed() {
                Log.i(TAG, "onFailed: ");
                loadingDialog.dismiss();
            }

            @Override
            public void onJoinMeetingSuccess() {
                Log.i(TAG, "onJoinMeetingSuccess: ");
                Intent intent = new Intent(LoginActivity.this, MobileMeetingActivity.class);
                intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN,false);
                intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN_WITH_ROOMID, !TextUtils.isEmpty(roomId));
                startActivity(intent);
                loadingDialog.dismiss();
            }

            @Override
            public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
                showInputPasswordDialog(isFrontVerify, inputPassword);
            }
        });
    }

    private void showInputPasswordDialog(boolean isFrontVerify, InputPassword inputPassword) {
        Disposable disposable = Flowable.just(inputPassword)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(input -> {
                    if (this.inputPasswordDialog == null) {
                        inputPasswordDialog = InputPasswordDialog.showInputPwdDialog(this);
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

    private void anonymousLogin() {
        final String roomId = mBinding.etRoomId.getText().toString();
        final String nickName = mBinding.etNickName.getText().toString();
        final String roomPwd = mBinding.etRoomPwd.getText().toString();

        loadingDialog.updateText(R.string.logging);
        loadingDialog.show();
        // isAnonymous是否是匿名入会
        JoinMeetingManager.getInstance().loginRoomId(roomId, nickName, roomPwd, true, new JoinMeetingCallback() {
            @Override
            public void onStart(Procedure procedure) {
                errorPwdCount = 0;
                Log.i(TAG, "onStart: ");
                loadingDialog.show();
            }

            @Override
            public void onState(int i, String s) {
                Log.i(TAG, "onState: i is " + i);
                loadingDialog.updateText(LoginStateUtil.convertStateToString(i));
            }

            @Override
            public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
                showInputPasswordDialog(isFrontVerify, inputPassword);
            }

            @Override
            public void onBlockFailed(ProcessStep processStep, int i, String s) {
                Log.i(TAG, "onBlockFailed: processStep is " + processStep);
                ToastUtils.showShort(LoginErrorUtil.getErrorSting(i));
                loadingDialog.dismiss();
            }

            @Override
            public void onFailed() {
                Log.i(TAG, "onFailed: ");
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "onSuccess: ");
                Intent intent = new Intent(LoginActivity.this, MobileMeetingActivity.class);
                intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN,true);
                startActivity(intent);
                loadingDialog.dismiss();
            }
        });
    }

    private void toRoomList(String userName, String userPwd) {
        loadingDialog.show();
        JoinMeetingManager.getInstance().loginAccount("", userName, userPwd, new LoginMeetingCallBack() {

            @Override
            public void onConflict(boolean isMeeting) {
                loadingDialog.dismiss();
            }

            @Override
            public void onStart(Procedure procedure) {
                loadingDialog.show();
            }

            @Override
            public void onState(int state) {
                Log.i(TAG, "onState: state is " + state);
                loadingDialog.updateText(LoginStateUtil.convertStateToString(state));
            }

            @Override
            public void onBlockFailed(ProcessStep step, int code, String msg) {
                Log.i(TAG, "onBlockFailed: code is " + code);
                ToastUtils.showShort(LoginErrorUtil.getErrorSting(code));
                loadingDialog.dismiss();
            }

            @Override
            public void onFailed() {
                loadingDialog.dismiss();
            }

            @Override
            public boolean onLoginSuccess() {
                loadingDialog.dismiss();
                Intent intent = new Intent(LoginActivity.this, RoomListActivity.class);
                intent.putExtra(RoomListActivity.EXTRA_PWD, userPwd);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (PermissionUtils.checkPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                JoinMeetingManager.getInstance().initLogger(getApplicationContext());
                loginAndJoinMeeting();
            } else {
                showPermissionDialog();
            }
        } else if (requestCode == 2){
            boolean callPhonePerResult = PermissionUtils.checkPermissions(this, Manifest.permission.CALL_PHONE);
            boolean cameraPerResult = PermissionUtils.checkPermissions(this, Manifest.permission.CAMERA);
            boolean audioPerResult = PermissionUtils.checkPermissions(this, Manifest.permission.RECORD_AUDIO);
            Log.i(TAG, "onRequestPermissionsResult: callPhonePerResult is " + callPhonePerResult);
            Log.i(TAG, "onRequestPermissionsResult: cameraPerResult is " + cameraPerResult);
            Log.i(TAG, "onRequestPermissionsResult: audioPerResult is " + audioPerResult);
            if (isAnonymousLogin) {
                anonymousLogin();
            }else {
                realNameLogin();
            }
        }
    }

    private void showPermissionDialog() {
        if (permissionDialog == null) {
            permissionDialog = new DoubleButtonDialog(LoginActivity.this, R.string.login_tip, R.string.permission_fail,
                    R.string.cancel, R.string.to_android_setting);
            permissionDialog.setOnClickListener(this);
        }
        permissionDialog.show();
    }

    @Override
    public void leftButtonClick(Dialog dialog) {
        finish();
    }

    @Override
    public void rightButtonClick(Dialog dialog) {
        PermissionsPageUtils pageUtils = new PermissionsPageUtils();
        pageUtils.jumpPermissionPage();
    }
}