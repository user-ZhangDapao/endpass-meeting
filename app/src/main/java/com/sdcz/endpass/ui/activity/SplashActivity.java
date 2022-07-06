package com.sdcz.endpass.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.online.PaasOnlineManager;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.LoginActivity;
import com.sdcz.endpass.MainActivity;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginMeetingCallBack;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.util.StatusBarUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;

/**
 * Author: Administrator
 * CreateDate: 2021/6/28 10:09
 * Description: @
 */
public class SplashActivity extends BaseActivity {

    private RelativeLayout rlRoot;
    private TextView tvTime;
    private int i = 3;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_splash;
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        rlRoot = findViewById(R.id.rlRoot);
        tvTime = findViewById(R.id.tv_time);
        return rlRoot;
    }

    @Override
    protected void requestWindowSet(Bundle savedInstanceState) {
        super.requestWindowSet(savedInstanceState);
        StatusBarUtils.setTranslucentBar(this);
    }

    @Override
    public void initListener() {
        super.initListener();
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this, MainActivityApp.class));
                finish();
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        /**
         * 判断手机是否ROOT
         */
        if (isRoot()){
            ToastUtils.showShort("禁止使用");
            finish();
            return;
        }
        checkLogin();

    }
    private void checkLogin() {
        if (null == SharedPrefsUtil.getUserInfo()){
            startActivity(new Intent(getContext(), LoginActivityApp.class));
            return;
        }
        String userName = SharedPrefsUtil.getUserInfo().getUserName();
        PaasOnlineManager.getInstance().setBusy(true);
        JoinMeetingManager.getInstance().loginAccount(null, "mdt" + userName, "mdt0"+userName, new LoginMeetingCallBack() {

            @Override
            public void onConflict(boolean isMeeting) {
            }

            @Override
            public void onStart(Procedure procedure) {
            }

            @Override
            public void onState(int state) {
            }

            @Override
            public void onBlockFailed(ProcessStep step, int code, String msg) {
                SharedPrefsUtil.clean(getContext());
                finish();
                startActivity(new Intent(getContext(), LoginActivityApp.class));
            }

            @Override
            public void onFailed() {
                SharedPrefsUtil.clean(getContext());
                finish();
                startActivity(new Intent(getContext(), LoginActivityApp.class));
            }

            @Override
            public boolean onLoginSuccess() {
                handler.sendEmptyMessageDelayed(0,1000);
                return true;
            }
        });

    }

//
//    void HSTlogin(String userId, String realName) {
//        if (userId.isEmpty()) {
//            return;
//        }
//        if (FspManager.getInstance().checkAppConfigChange()) {
//            if (!FspManager.getInstance().init()) {
//                showToast("无初始化配置信息，请联系开发人员");
//                return;
//            }
//        } else {
//            showToast("请输入配置信息，请联系开发人员");
//            return;
//        }
//        boolean result = FspManager.getInstance().login(userId, realName);
//        if (!result) {
//            showToast("身份验证过期");
//            startActivity(new Intent(this, LoginActivity.class));
//        }
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//        } else {
//            audioManager.setMode(AudioManager.MODE_IN_CALL);
//        }
//        audioManager.setSpeakerphoneOn(true);
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onLoginResult(FspEvents.LoginResult result) {
//        if (result.isSuccess) {
//            handler.sendEmptyMessageDelayed(0,1000);
//        }else {
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//            ToastUtils.show(this,"服务器连接失败,请重新登录");
//        }
//    }

    //handler
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            i--;
            //判断i是否小于0
            //如果小于0就跳转页面，并结束当前页面
            if (i <= 0){
                String token = SharedPrefsUtil.getString(Constants.SharedPreKey.Token, "");
                if (token.isEmpty()){
                    startActivity(new Intent(SplashActivity.this, LoginActivityApp.class));
                }else {
                    startActivity(new Intent(SplashActivity.this, MainActivityApp.class));
                }
                finish();
            }else {
                //改变倒计时
                handler.sendEmptyMessageDelayed(0,1000);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void setStatusBar() {
    }

    //判断手机是否root
    public static boolean isRoot() {
        String binPath = "/system/bin/su";
        String xBinPath = "/system/xbin/su";

        if (new File(binPath).exists() && isCanExecute(binPath)) {
            return true;
        }
        if (new File(xBinPath).exists() && isCanExecute(xBinPath)) {
            return true;
        }
        return false;
    }

    private static boolean isCanExecute(String filePath) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("ls -l " + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = in.readLine();
            if (str != null && str.length() >= 4) {
                char flag = str.charAt(3);
                if (flag == 's' || flag == 'x')
                    return true;
            }
        } catch (IOException e) {
//            //e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }
}
