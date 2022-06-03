package com.sdcz.endpass.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.jaeger.library.StatusBarUtil;
import com.sdcz.endpass.LoginActivity;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.presenter.ChangePassPersenter;
import com.sdcz.endpass.util.ActivityUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IChangePassView;

import org.greenrobot.eventbus.EventBus;

import java.security.KeyStore;

public class ChangePassActivity extends BaseActivity<ChangePassPersenter> implements IChangePassView, View.OnClickListener {

    private ImageView ivBack;
    private TextView tvTitle;
    private LinearLayout llRoot;
    private EditText edOldPassw;
    private EditText edNewPassw;
    private EditText edNewPasswAgain;
    private Button btnOk;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_change_pass;
    }

    @Override
    protected ChangePassPersenter createPresenter() {
        return new ChangePassPersenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        llRoot = findViewById(R.id.llRoot);
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        edOldPassw = findViewById(R.id.ed_old_passw);
        edNewPassw = findViewById(R.id.ed_new_passw);
        edNewPasswAgain = findViewById(R.id.ed_new_passw_again);
        btnOk = findViewById(R.id.btn_ok);
        tvTitle.setText("修改密码");
        ivBack.setVisibility(View.VISIBLE);
        return llRoot;
    }

    @Override
    public void initListener() {
        super.initListener();
        ivBack.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                finish();
                break;
            case R.id.btn_ok:
                checkPass();
                break;
            default:
                break;
        }
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return super.registerReceiver(receiver, filter);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColorNoTranslucent(this, Color.WHITE);
    }

    private void checkPass() {

        String oldPassw = edOldPassw.getText().toString();
        String newPassw = edNewPassw.getText().toString();
        String newPasswAgain = edNewPasswAgain.getText().toString();

        if (TextUtils.isEmpty(oldPassw)) {
            ToastUtils.showLong("旧密码不能为空");
            return;
        }
        if (TextUtils.isEmpty(newPassw)) {
            ToastUtils.showLong("新密码不能为空");
            return;
        }
        if (TextUtils.isEmpty(newPasswAgain)) {
            ToastUtils.showLong( "请在次输入新密码");
            return;
        }
        if (!newPassw.equals(newPasswAgain)) {
            ToastUtils.showLong("两次输入的新密码不一致");
            return;
        }
        mPresenter.changePassWord(this, oldPassw, newPassw);
    }

    @Override
    public void showData(Object o) {
        ToastUtils.showLong("操作成功");
//        EventBus.getDefault().post(new EventStopHandler(true));
//        SharedPrefsUtil.putValue(getContext(), KeyStore.IS_LOGIN,false);
//        FspManager.getInstance().loginOut();
        SharedPrefsUtil.clean(this);
        Intent intent = new Intent(this, LoginActivityApp.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        ActivityUtils.getInstance().finishAll();
    }


}