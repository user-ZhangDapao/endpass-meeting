package com.sdcz.endpass.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseDialog;
import com.inpor.sdk.open.pojo.InputPassword;

import org.jetbrains.annotations.NotNull;

/**
 * 入会时，提示输入密码的Dialog
 */
public class InputPasswordDialog extends BaseDialog implements TextWatcher {

    private EditText edtTxtPassword;
    private Button banCancel;
    private Button btnOK;
    private DialogInterface.OnClickListener onClickListener;
    private boolean isFrontVerify;
    private InputPassword inputPassword;

    public InputPasswordDialog(@NonNull @NotNull Context context) {
        super(context);
    }

    public InputPasswordDialog(@NonNull @NotNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setButtonCallback(DialogInterface.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input_room_password);
        initViews();
        initListener();
    }

    private void initViews() {
        edtTxtPassword = findViewById(R.id.edtTxt_room_password);
        banCancel = findViewById(R.id.btn_cancle);
        btnOK = findViewById(R.id.btn_ok);
        setCanceledOnTouchOutside(false);
    }

    protected void initListener() {
        banCancel.setOnClickListener(this::onClick);
        btnOK.setOnClickListener(this::onClick);
        btnOK.setEnabled(edtTxtPassword.length() >= 1);
        edtTxtPassword.addTextChangedListener(this);
    }

    public static InputPasswordDialog showInputPwdDialog(Context context) {
        InputPasswordDialog inputPasswordDialog =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                        ? new InputPasswordDialog(context, R.style.inputRoomPasswordDialog)
                        : new InputPasswordDialog(context, R.style.NormalDialog);
        inputPasswordDialog.show();
        return inputPasswordDialog;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ToastUtils.showShort(R.string.check_password);
        dismiss();
        if (onClickListener != null) {
            onClickListener.onClick(this, DialogInterface.BUTTON_NEGATIVE);
        }
    }

    private void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_cancle) {
            ToastUtils.showShort(R.string.check_password);
            dismiss();
            if (onClickListener != null) {
                onClickListener.onClick(this, DialogInterface.BUTTON_NEGATIVE);
            }
        } else if (id == R.id.btn_ok) {
            dismiss();
            if (inputPassword != null) {
                inputPassword.inputPassword(isFrontVerify, getInputPassword());
            }
            if (onClickListener != null) {
                onClickListener.onClick(this, DialogInterface.BUTTON_POSITIVE);
            }
        }
    }

    public String getInputPassword() {
        return edtTxtPassword.getText().toString();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        btnOK.setEnabled(edtTxtPassword.length() >= 1);
    }

    @Override
    public void show() {
        super.show();
        if (edtTxtPassword != null) {
            edtTxtPassword.setText("");
        }
    }

    public void update(boolean isFrontVerify, InputPassword inputPassword) {
        this.isFrontVerify = isFrontVerify;
        this.inputPassword = inputPassword;
    }
}
