package com.sdcz.endpass.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.dialog.InputPasswordDialog;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginStateUtil;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class JoinMeetingActivity extends AppCompatActivity {

    private Button btJoinMeeting;
    private EditText etRoomPassword;
    private EditText etRoomNum;
    private TextView tvTitle;
    private ImageView ivBack;
    private LoadingDialog loadingDialog;
    private final TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String roomNum = etRoomNum.getText().toString().trim();
            btJoinMeeting.setEnabled(!TextUtils.isEmpty(roomNum));
        }
    };
    private InputPasswordDialog inputPasswordDialog;
    private int errorPwdCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_meeting);
        initView();
        initListener();
        tvTitle.setText(R.string.join_meeting);
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        etRoomNum = findViewById(R.id.et_room_num);
        etRoomPassword = findViewById(R.id.et_password);
        btJoinMeeting = findViewById(R.id.btn_join_meeting);
    }

    private void initListener() {
        ivBack.setOnClickListener(this::onClick);
        btJoinMeeting.setOnClickListener(this::onClick);
        etRoomNum.addTextChangedListener(editTextWatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_join_meeting) {
            /*
            List<String> permissionList = PermissionUtils.requestMeetingPermission();
            if (permissionList != null && !permissionList.isEmpty()) {
                PermissionUtils.requestPermission(this,permissionList);
                return;
            }*/
            joinMeeting();
        } else if (id == R.id.iv_back) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            JoinMeetingManager.getInstance().initLogger(this);
            joinMeeting();
        }
    }

    private void joinMeeting() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(JoinMeetingActivity.this, R.string.LOGIN);
        }
        loadingDialog.show();
        String roomNum = etRoomNum.getText().toString().trim();
        String roomPwd = etRoomPassword.getText().toString().trim();
        JoinMeetingManager.getInstance().loginRoomId(roomNum, "", roomPwd, false, new JoinMeetingCallback() {

            @Override
            public void onStart(Procedure procedure) {
                loadingDialog.show();
            }

            @Override
            public void onState(int i, String s) {
                if (loadingDialog != null) {
                    loadingDialog.updateText(LoginStateUtil.convertStateToString(i));
                }
            }

            @Override
            public void onBlockFailed(ProcessStep processStep, int i, String s) {
                ToastUtils.showShort(LoginErrorUtil.getErrorSting(i));
                loadingDialog.dismiss();
            }

            @Override
            public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
                showInputPasswordDialog(isFrontVerify, inputPassword);
            }

            @Override
            public void onFailed() {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess() {
                loadingDialog.dismiss();
                Intent intent = new Intent(JoinMeetingActivity.this, MobileMeetingActivity.class);
                intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN,true);
                startActivity(intent);
                finish();
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
                    inputPasswordDialog.update(isFrontVerify,inputPassword);
                    if (errorPwdCount > 0) {
                        ToastUtils.showShort(com.sdcz.endpass.R.string.check_password);
                    }
                    this.inputPasswordDialog.show();
                    errorPwdCount++;
                });
    }
}