package com.sdcz.endpass.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.DashPathEffect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.common.util.Base64Utils;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.SetServerCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.online.PaasOnlineManager;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.LoginActivity;
import com.sdcz.endpass.LoginSettingActivity;
import com.sdcz.endpass.MainActivity;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.DefaultJoinMeetingCallBack;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginMeetingCallBack;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.presenter.LoginPresenter;
import com.sdcz.endpass.ui.RoomListActivity;
import com.sdcz.endpass.util.PackageUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ILoginView;

import java.security.KeyStore;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Author: Administrator
 * CreateDate: 2021/6/28 10:56
 * Description: @
 */

@RuntimePermissions
public class LoginActivityApp extends BaseActivity<LoginPresenter> implements ILoginView, View.OnClickListener {

    private RelativeLayout rlRoot;
    private EditText etLoginName;
    private EditText etPassWord;
    private Button btnLogin;
    private TextView tvSetIP;
    private CheckBox checkbox;
    private ImageView ivClose;
    private TextView tvVersion;
    LoadingDialog loadingDialog;


    @Override
    protected int provideContentViewId() {
        return R.layout.activity_login_app;
    }

    @Override
    protected LoginPresenter createPresenter() {
        return new LoginPresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        rlRoot = findViewById(R.id.rlRoot);
        etLoginName = findViewById(R.id.etLoginName);
        etPassWord = findViewById(R.id.etPassWord);
        btnLogin = findViewById(R.id.btnLogin);
        tvSetIP = findViewById(R.id.tv_loginSetting);
        checkbox = findViewById(R.id.checkbox);
        ivClose = findViewById(R.id.ivClose);
        tvVersion = findViewById(R.id.tvVersion);
        return rlRoot;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void initData() {
        super.initData();
        loadingDialog = new LoadingDialog(this, R.string.CONNECTING_SERVER);
        tvVersion.setText("V" + PackageUtils.getVersionName(getApplicationContext()));
    }

    @Override
    public void initListener() {
        super.initListener();
        btnLogin.setOnClickListener(this);
        tvSetIP.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //选择状态 显示明文--设置为可见的密码
                    etPassWord.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etPassWord.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        etLoginName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    ivClose.setVisibility(View.VISIBLE);
                } else {
                    // 此处为失去焦点时的处理内容
                    ivClose.setVisibility(View.INVISIBLE);
                }
            }
        });

        etPassWord.setOnFocusChangeListener(new View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    checkbox.setVisibility(View.VISIBLE);
                } else {
                    // 此处为失去焦点时的处理内容
                    checkbox.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                LoginActivityAppPermissionsDispatcher.checkLoginWithPermissionCheck(this);
                break;
            case R.id.tv_loginSetting:
                startActivity(new Intent(this, LoginSettingActivity.class));
                break;
            case R.id.ivClose:
                etLoginName.setText("");
                break;
            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void showData() {
        mPresenter.getUserInfo(this);
        saveParam();
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    public void checkLogin() {
//        //这儿还需要判断手机号和密码
        String phone = etLoginName.getText().toString().trim();
        String pass = etPassWord.getText().toString().trim();
        if (null == phone || TextUtils.isEmpty(phone)) {
            ToastUtils.showLong("账号不能为空");
            hideLoading();
            return;
        }
        if (null == pass || TextUtils.isEmpty(pass)) {
            ToastUtils.showLong("密码不能为空");
            hideLoading();
            return;
        }
        mPresenter.doLogin(this, phone, pass);
    }

    private void saveParam() {

        String ip = "webapp.haoshitong.com";
        String port = "1089";
        String clientId = "7cce02b4-c715-47a9-91f4-8bc177525c1c";
        String clientSecret = "8593ab94-be6a-429d-803d-1808ea7bd8b5";

        SharedPrefsUtil.putString(Constants.SP_KEY_SERVER_ADDRESS, ip);
        SharedPrefsUtil.putString(Constants.SP_KEY_SERVER_PORT, port);
        SharedPrefsUtil.putString(Constants.SP_KEY_CLIENT_ID, clientId);
        SharedPrefsUtil.putString(Constants.SP_KEY_CLIENT_SECRET, clientSecret);
        //更新clientId和clientSecret
        JoinMeetingManager.getInstance().setClientIdInfo(clientId, clientSecret);
        loadingDialog.show();
        JoinMeetingManager.getInstance().setServer(ip, Integer.parseInt(port), new SetServerCallback() {
            @Override
            public void onSuccess() {
                ToastUtils.showShort(R.string.configure_server_success);
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


                toRoomList(etLoginName.getText().toString().trim(), etPassWord.getText().toString().trim());

            }

            @Override
            public void onFailed(int i, String s) {
                loadingDialog.dismiss();
                ToastUtils.showShort(R.string.configure_server_fail);
            }
        });
    }

    private void toRoomList(String userName, String userPwd) {
        PaasOnlineManager.getInstance().setBusy(true);
        loadingDialog.show();
        JoinMeetingManager.getInstance().loginAccount(null, "mdt"+userName, "mdt0"+userName, new LoginMeetingCallBack() {
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
                startActivity(new Intent(LoginActivityApp.this, MainActivityApp.class));
                finish();
                return true;
            }
        });
    }

}
