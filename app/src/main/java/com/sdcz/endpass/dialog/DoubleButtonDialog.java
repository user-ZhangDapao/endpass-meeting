package com.sdcz.endpass.dialog;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseDialog;

/**
 * 自定义带有底部两按钮的Dialog
 */

public class DoubleButtonDialog extends BaseDialog implements View.OnClickListener {

    TextView titleTextView;
    TextView contentTextView;
    Button leftButton;
    Button rightButton;
    CheckBox cancelTipCheckbox;
    LinearLayout cancelTipLinearlayout;

    private String title;
    private String content;
    private String leftButtonText;
    private String rightButtonText;
    private IOnClickListener listener;
    private Boolean isOpenVibrator = false;
    private Vibrator vibrator;
    private Boolean isOpenTimer = false;
    /**
     * 超时时间65s，定义成65s是为了避免主叫60s超时取消呼叫之后，被叫生成一次未接听记录
     * 但是被叫的自己的超时操作又会记录一次未接听，可能会出现2次未接听记录
     */
    CountDownTimer countDownTimer = new CountDownTimer(65000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            // 群组呼叫时，只要有一个被叫接听，主叫就会取消超时定时，那么没有接听的被叫也就不会
            // 收到取消呼叫的消息，这时生成的通话记录状态不对
            dismiss();
        }
    };

    public DoubleButtonDialog(Context context) {
        super(context, R.style.NormalDialog);
        setContentView(R.layout.dialog_double_button);
    }

    public static DoubleButtonDialog initInstance(Context context) {
        return new DoubleButtonDialog(context);
    }

    /**
     * 构造函数
     *
     * @param context           上下文
     * @param titleId           标题
     * @param contentId         内容
     * @param leftButtonTextId  左按钮文案
     * @param rightButtonTextId 右按钮文案
     */
    public DoubleButtonDialog(Context context, int titleId, int contentId, int leftButtonTextId,
                              int rightButtonTextId) {
        this(context, context.getString(titleId), context.getString(contentId),
                context.getString(leftButtonTextId), context.getString(rightButtonTextId));
    }

    /**
     * 构造函数
     *
     * @param context         上下文
     * @param title           标题
     * @param content         内容
     * @param leftButtonText  左按钮文案
     * @param rightButtonText 右按钮文案
     */
    public DoubleButtonDialog(Context context, @NonNull String title, @NonNull String content,
                              @NonNull String leftButtonText, @NonNull String rightButtonText) {
        super(context, R.style.NormalDialog);
        setContentView(R.layout.dialog_double_button);

        this.title = title;
        this.content = content;
        this.leftButtonText = leftButtonText;
        this.rightButtonText = rightButtonText;

        initViews();
        initValues();
        initListener();
    }

    public DoubleButtonDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public DoubleButtonDialog setContentData(String content) {
        this.content = content;
        return this;
    }

    public DoubleButtonDialog setLeftButtonText(String leftButtonText) {
        this.leftButtonText = leftButtonText;
        return this;
    }

    public DoubleButtonDialog setLeftButtonColor(int colorId) {
        leftButton.setTextColor(colorId);
        return this;
    }

    public DoubleButtonDialog setRightButtonText(String rightButtonText) {
        this.rightButtonText = rightButtonText;
        return this;
    }

    public DoubleButtonDialog setRightButtonColor(int colorId) {
        rightButton.setTextColor(colorId);
        return this;
    }

    public DoubleButtonDialog setListener(@NonNull IOnClickListener listener) {
        this.listener = listener;
        return this;
    }

    public DoubleButtonDialog openVibrator(Boolean isOpenVibrator) {
        this.isOpenVibrator = isOpenVibrator;
        return this;
    }

    /**
     * 设置复选框按钮不可点击，由layout触发点击事件
     *
     * @param listener 点击事件
     * @return 自身
     */
    public DoubleButtonDialog setCheckListener(@Nullable View.OnClickListener listener) {
        this.cancelTipLinearlayout.setOnClickListener(listener);
        this.cancelTipCheckbox.setEnabled(false);
        return this;
    }

    public DoubleButtonDialog setCancelViewVisibility(int visibility) {
        cancelTipLinearlayout.setVisibility(visibility);
        return this;
    }

    public boolean isCheck() {
        return cancelTipCheckbox.isChecked();
    }

    public void setCheckState(boolean isCheck) {
        cancelTipCheckbox.setChecked(isCheck);
    }

    public DoubleButtonDialog openTimer(Boolean openTimer) {
        this.isOpenTimer = openTimer;
        return this;
    }

    /**
     * 初始化数据并显示
     */
    public void initShow() {
        initViews();
        initValues();
        initListener();
        show();
    }

    /**
     * 隐藏左边按钮
     */
    public DoubleButtonDialog hideLeftButton() {
        leftButton.setVisibility(View.GONE);
        rightButton.setBackground(getContext().getResources().getDrawable(R
                .drawable.rect_end_button));
        return this;
    }

    /**
     * 设置是否开启振动器
     *
     * @param isOpenVibrator 是否开启震动
     */
    public void setIsOpenVibrator(Boolean isOpenVibrator) {
        this.isOpenVibrator = isOpenVibrator;
    }

    public void setOpenTimer(Boolean openTimer) {
        isOpenTimer = openTimer;
    }

    protected void initViews() {
        titleTextView = findViewById(R.id.title_textview);
        contentTextView = findViewById(R.id.content_textview);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        cancelTipCheckbox = findViewById(R.id.cancel_tip_checkbox);
        cancelTipLinearlayout = findViewById(R.id.cancel_tip_linearlayout);
    }

    protected void initValues() {
        setCancelable(false);
        contentTextView.setText(content);
        titleTextView.setText(title);
        leftButton.setText(leftButtonText);
        rightButton.setText(rightButtonText);
    }

    protected void initListener() {
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.left_button) {
            if (listener != null) {
                listener.leftButtonClick(this);
            }
        } else if (id == R.id.right_button) {
            if (listener != null) {
                listener.rightButtonClick(this);
            }
        }
    }

    @Override
    public void show() {
        super.show();
        if (isOpenTimer) {
            if (isOpenVibrator) {
                vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
                if (vibrator.hasVibrator()) {
                    vibrator.cancel();
                    vibrator.vibrate(new long[]{800, 200, 800, 200, 800, 200}, 0);
                }
            }
            countDownTimer.start();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (isOpenTimer && isOpenVibrator) {
            countDownTimer.cancel();
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.cancel();
            }
        }
    }

    /**
     * 设置按钮监听事件，将点击事件回调至使用者
     *
     * @param listener 事件回调监听器
     */
    public void setOnClickListener(@NonNull IOnClickListener listener) {
        this.listener = listener;
    }

    public interface IOnClickListener {
        void leftButtonClick(Dialog dialog);

        void rightButtonClick(Dialog dialog);
    }
}
