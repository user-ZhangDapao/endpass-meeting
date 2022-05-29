package com.sdcz.endpass;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.databinding.ActivityLoginSettingBinding;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.inpor.sdk.callback.SetServerCallback;

public class LoginSettingActivity extends AppCompatActivity {

    private ActivityLoginSettingBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                .init();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login_setting);
        mBinding.setOnClickListener(this::onClick);
        String serverAddress = SharedPrefsUtil.getString(Constants.SP_KEY_SERVER_ADDRESS);
        String serverPort = SharedPrefsUtil.getString(Constants.SP_KEY_SERVER_PORT);
        String clientId = SharedPrefsUtil.getString(Constants.SP_KEY_CLIENT_ID);
        String clientSecret = SharedPrefsUtil.getString(Constants.SP_KEY_CLIENT_SECRET);
//        mBinding.etIp.setText(serverAddress);
//        mBinding.etPort.setText(serverPort);
//        mBinding.etClientId.setText(clientId);
//        mBinding.etClientSecret.setText(clientSecret);
    }

    private void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.tv_save) {
            saveParam();
        }
    }

    private void saveParam() {
        String ip = mBinding.etIp.getText().toString().trim();
        String port = mBinding.etPort.getText().toString().trim();
        String clientId = mBinding.etClientId.getText().toString().trim();
        String clientSecret = mBinding.etClientSecret.getText().toString().trim();
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port) ||
                TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)) {
            ToastUtils.showShort(R.string.check_server_info_input);
            return;
        }
        SharedPrefsUtil.putString(Constants.SP_KEY_SERVER_ADDRESS, ip);
        SharedPrefsUtil.putString(Constants.SP_KEY_SERVER_PORT, port);
        SharedPrefsUtil.putString(Constants.SP_KEY_CLIENT_ID, clientId);
        SharedPrefsUtil.putString(Constants.SP_KEY_CLIENT_SECRET, clientSecret);
        //更新clientId和clientSecret
        JoinMeetingManager.getInstance().setClientIdInfo(clientId, clientSecret);
        LoadingDialog loadingDialog = new LoadingDialog(this, R.string.CONNECTING_SERVER);
        loadingDialog.show();
        JoinMeetingManager.getInstance().setServer(ip, Integer.parseInt(port), new SetServerCallback() {
            @Override
            public void onSuccess() {
                loadingDialog.dismiss();
                ToastUtils.showShort(R.string.configure_server_success);
            }

            @Override
            public void onFailed(int i, String s) {
                loadingDialog.dismiss();
                ToastUtils.showShort(R.string.configure_server_fail);
            }
        });
    }

}