package com.sdcz.endpass.dialog;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseFragmentDialog;

/**
 * @Description: 通用提示对话框（标题+提示+一个按钮）
 */
public class SimpleTipsDialog3 extends BaseFragmentDialog implements View.OnClickListener {

    public interface InteractionListener {
        void onRightBtnClick(DialogFragment dialog);
    }

    private TextView tvTitle;
    private TextView tvSure;
    private TextView tvTips;

    private int orientation = -1;

    private String title;
    private String btnRight;
    private String tips;
    private boolean cancelOnTouchOutside = true;

    private final InteractionListener interactionListener;

    /**
     * 构造函数
     */
    public SimpleTipsDialog3(boolean cancelOnTouchOutside, String title, String sure,
                             String tips, InteractionListener listener) {
        this.cancelOnTouchOutside = cancelOnTouchOutside;
        this.title = title;
        this.btnRight = sure;
        this.tips = tips;
        this.interactionListener = listener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setGravity(Gravity.CENTER);
        getDialog().setCanceledOnTouchOutside(cancelOnTouchOutside);
        getDialog().getWindow().setWindowAnimations(R.style.bottom_dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return createContentView(inflater);
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
        if (view.getId() == R.id.tvSure) {
            if (interactionListener != null) {
                interactionListener.onRightBtnClick(this);
            }
        }
    }

    private View createContentView(LayoutInflater inflater) {
        View content = inflater.inflate(R.layout.dialog_simple_tips3, null);
        tvTitle = content.findViewById(R.id.tvTitle);
        tvSure = content.findViewById(R.id.tvSure);
        tvTips = content.findViewById(R.id.tvTips);
        tvSure.setOnClickListener(this);
        initValue();
        return content;
    }

    private void initValue() {
        if (TextUtils.isEmpty(title)) {
            title = "";
        }
        if (TextUtils.isEmpty(btnRight)) {
            btnRight = getString(R.string.meetingui_confirm);
        }
        if (TextUtils.isEmpty(tips)) {
            tips = "";
        }
        tvTitle.setText(title);
        tvTips.setText(tips);
        tvSure.setText(btnRight);
    }

    /**
     * Builder
     */
    public static class Builder {
        private boolean cancelOnTouchOutside = true;
        private String title;
        private String btnRight;
        private String tips;
        private InteractionListener interactionListener;

        /**
         * 点击外部是否取消
         * @param cancelOnTouchOutside true 关闭
         * @return Builder
         */
        public Builder cancelOnTouchOutside(boolean cancelOnTouchOutside) {
            this.cancelOnTouchOutside = cancelOnTouchOutside;
            return this;
        }

        /**
         * title
         */
        public Builder title(String title) {
            this.title = title;
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
        public Builder tips(String defaultHint) {
            this.tips = defaultHint;
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
        public SimpleTipsDialog3 build() {
            SimpleTipsDialog3 dialog = new SimpleTipsDialog3(cancelOnTouchOutside,
                    title, btnRight, tips, interactionListener);
            return dialog;
        }
    }
}
