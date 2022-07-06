package com.sdcz.endpass.dialog;

import android.app.Activity;
import android.app.Service;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inpor.sdk.online.PaasOnlineManager;
import com.inpor.sdk.repository.bean.CompanyUserInfo;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseDialog2;
import com.sdcz.endpass.view.PreviewVideoView;

import butterknife.BindView;


/**
 * 语音呼入弹框
 */
public class CallInDialog extends BaseDialog2 implements View.OnClickListener {

    @BindView(R.id.ll_video_show)
    LinearLayout videoShowLinearLayout;
    @BindView(R.id.tv_call_name)
    TextView callNameTextView;
    @BindView(R.id.tv_call_tip)
    TextView callTipTextView;
    @BindView(R.id.tv_call_audio_recv)
    TextView callInAudioRecvTextView;
    @BindView(R.id.tv_call_in_close)
    TextView callInCloseTextView;
    @BindView(R.id.tv_call_video_recv)
    TextView callInVideoRecvTextView;
    @BindView(R.id.rl_video_in_show)
    RelativeLayout videoRecRelativeLayout;
    @BindView(R.id.tv_audio_call_name)
    TextView audioCallNameTextView;
    @BindView(R.id.tv_call_audio_close)
    TextView audioRefuseTextView;
    @BindView(R.id.tv_call_audio_in_recv)
    TextView audioAnswerTextView;
    @BindView(R.id.ll_audio_in_show)
    RelativeLayout audioRecLinearLayout;

    private CompanyUserInfo roomUserInfo;

    private Vibrator vibrator;
    private String userName;
    private PreviewVideoView previewVideoView;
    private Activity activity;
    private MediaPlayer mediaPlayer;
    private CallWayChangeListener wayChangeListener;
    /**
     * 超时时间65s，定义成65s是为了避免主叫60s超时取消呼叫之后，被叫生成一次未接听记录
     * 但是被叫的自己的超时操作又会记录一次未接听，可能会出现2次未接听记录
     */
    CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
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

    public void setWayChangeListener(CallWayChangeListener wayChangeListener) {
        this.wayChangeListener = wayChangeListener;
    }

    /**
     * 构造方法
     *
     * @param activity 上下文
     */
    public CallInDialog(Activity activity, CompanyUserInfo roomUserInfo) {
        super(activity, R.style.dialog_call_style);
        this.activity = activity;
        this.roomUserInfo = roomUserInfo;
            setContentView(R.layout.dialog_call_in);
        initValues();
        initListener();
        changeToAudio(true);
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected void initValues() {
        callNameTextView.setText(userName);
        vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        //初始化Call model
    }

    @Override
    protected void initListener() {
        callInCloseTextView.setOnClickListener(this);
        callInAudioRecvTextView.setOnClickListener(this);
        callInVideoRecvTextView.setOnClickListener(this);
        audioRefuseTextView.setOnClickListener(this);
        audioAnswerTextView.setOnClickListener(this);
    }

    /**
     * 改为音频接听
     *
     * @param isAudio 是否是音频模式
     */
    public void changeToAudio(boolean isAudio) {
        videoShowLinearLayout.setVisibility(isAudio ? View.GONE : View.VISIBLE);
        audioRecLinearLayout.setVisibility(isAudio ? View.VISIBLE : View.GONE);
        videoRecRelativeLayout.setVisibility(isAudio ? View.GONE : View.VISIBLE);
    }

    /**
     * 设置呼入用户名称
     *
     * @param userName 呼入用户名
     */
    public void setCallName(String userName) {
        this.userName = userName;
        callNameTextView.setText(userName);
        audioCallNameTextView.setText(userName);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_call_in_close || id == R.id.tv_call_audio_close) {
            if (wayChangeListener != null) {
                wayChangeListener.refuse();
            }
            dismiss();
        } else if (id == R.id.tv_call_audio_in_recv) {
            if (wayChangeListener != null) {
                wayChangeListener.answer();
            }
        }
    }

    /**
     * 视频预览
     */
    private void dealVideoShow() {
        previewVideoView = new PreviewVideoView(activity);
        previewVideoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.MATCH_PARENT));
        videoShowLinearLayout.addView(previewVideoView);
        previewVideoView.showVideoViewOnTop(false);
    }

    /**
     * 开启铃声
     */
    private void startRingPlay() {
        mediaPlayer = MediaPlayer.create(context, R.raw.ring);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.start());
    }

    /**
     * 停止扬声器检测
     */
    private void stopRingPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator.hasVibrator()) {
            vibrator.cancel();
        }
    }

    @Override
    public void show() {
        super.show();
        setCancelable(false);
        callNameTextView.setText(roomUserInfo.getDisplayName());
        PaasOnlineManager.getInstance().setBusy(true);
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
        dealVideoShow();
        startRingPlay();
        if (vibrator.hasVibrator()) {
            vibrator.cancel();
            vibrator.vibrate(new long[]{800, 200, 800, 200, 800, 200}, 0);
        }
        countDownTimer.start();
    }

    @Override
    public void dismiss() {
        PaasOnlineManager.getInstance().setBusy(false);
        countDownTimer.cancel();
        stopRingPlay();
        if (previewVideoView != null) {
            previewVideoView.releaseVideoView();
            previewVideoView = null;
            videoShowLinearLayout.removeAllViews();
        }
        super.dismiss();
    }

    public interface CallWayChangeListener {

        void refuse();

        void answer();
    }
}