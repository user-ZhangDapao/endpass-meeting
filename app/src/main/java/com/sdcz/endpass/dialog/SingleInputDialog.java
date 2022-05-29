package com.sdcz.endpass.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseFragmentDialog;
import com.sdcz.endpass.util.SoftKeyboardHelper;

/**
 * @Description: 会中输入一个文本的对话框
 */
public class SingleInputDialog extends BaseFragmentDialog implements View.OnClickListener {

    public interface InteractionListener {
        void onLeftBtnClick(DialogFragment dialog);

        void onRightBtnClick(DialogFragment dialog, String input);

        default void onInputTextChanged(String text) {}
    }

    private TextView tvTitle;
    private EditText edtInput;
    private ImageView imgClear;
    private TextView tvCancel;
    private TextView tvSure;

    private int orientation = -1;

    private String title;
    private String btnLeft;
    private String btnRight;
    private String defaultHint;

    private final InteractionListener interactionListener;
    private SoftKeyboardHelper helper;

    /**
     * 构造函数
     */
    public SingleInputDialog(String title, String btnLeft, String btnRight,
                             String defaultHint, InteractionListener listener) {
        this.title = title;
        this.btnLeft = btnLeft;
        this.btnRight = btnRight;
        this.defaultHint = defaultHint;
        this.interactionListener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        helper = new SoftKeyboardHelper(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return createContentView(inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        edtInput.post(() -> helper.showSoftKeyboard(edtInput));
    }

    @Override
    public void onStop() {
        helper.hideSoftKeyboard(edtInput);
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (orientation != newConfig.orientation) {
            orientation = newConfig.orientation;
            View view = createContentView(LayoutInflater.from(getContext()));
            getDialog().setContentView(view);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvCancel) {
            if (interactionListener != null) {
                interactionListener.onLeftBtnClick(this);
            }
        } else if (view.getId() == R.id.tvSure) {
            if (interactionListener != null) {
                interactionListener.onRightBtnClick(this, edtInput.getText().toString());
            }
        } else if (view.getId() == R.id.imgClear) {
            edtInput.setText("");
        }
    }

    private View createContentView(LayoutInflater inflater) {
        View content = inflater.inflate(R.layout.dialog_single_input, null);
        tvTitle = content.findViewById(R.id.tvTitle);
        edtInput = content.findViewById(R.id.edtInput);
        imgClear = content.findViewById(R.id.imgClear);
        tvCancel = content.findViewById(R.id.tvCancel);
        tvSure = content.findViewById(R.id.tvSure);
        tvSure.setOnClickListener(this);
        tvSure.setEnabled(false);
        tvCancel.setOnClickListener(this);
        imgClear.setOnClickListener(this);
        edtInput.addTextChangedListener(onTextChangedListener);
        initValue();
        return content;
    }

    private void initValue() {
        if (TextUtils.isEmpty(title)) {
            title = "";
        }
        if (TextUtils.isEmpty(btnLeft)) {
            btnLeft = getString(R.string.meetingui_cancel);
        }
        if (TextUtils.isEmpty(btnRight)) {
            btnRight = getString(R.string.meetingui_confirm);
        }
        if (TextUtils.isEmpty(defaultHint)) {
            defaultHint = "";
        }
        tvTitle.setText(title);
        tvCancel.setText(btnLeft);
        tvSure.setText(btnRight);
        edtInput.setHint(defaultHint);
    }

    private final TextWatcher onTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence sequence, int start, int before, int count) {
            String newName = sequence.toString();
            tvSure.setEnabled(!TextUtils.isEmpty(newName));
            if (interactionListener != null) {
                interactionListener.onInputTextChanged(newName);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    /**
     * Builder
     */
    public static class Builder {
        private String title;
        private String btnLeft;
        private String btnRight;
        private String defaultHint;
        private InteractionListener interactionListener;

        /**
         * title
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * 左边按钮文本
         */
        public Builder btnLeft(String btnLeft) {
            this.btnLeft = btnLeft;
            return this;
        }

        /**
         * 右边按钮文本
         */
        public Builder btnRight(String btnRight) {
            this.btnRight = btnRight;
            return this;
        }

        /**
         * 输入框的提示文本
         */
        public Builder defaultHint(String defaultHint) {
            this.defaultHint = defaultHint;
            return this;
        }

        /**
         * 交互接口
         */
        public Builder interactionListener(InteractionListener listener) {
            this.interactionListener = listener;
            return this;
        }

        /**
         * 构建 SingleInputDialog
         */
        public SingleInputDialog build() {
            SingleInputDialog dialog = new SingleInputDialog(title,
                    btnLeft, btnRight, defaultHint, interactionListener);
            return dialog;
        }
    }
}
