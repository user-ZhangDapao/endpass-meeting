package com.sdcz.endpass.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * @Description 底部菜单定时器
 */
public class MeetingMenuTimer extends Handler {

    public static final String TAG = "MeetingMenuTimer";
    private TimerListener timerListener;


    /**
     * 构造函数
     *
     * @param looper looper
     */
    public MeetingMenuTimer(@NonNull Looper looper) {
        super(looper);
    }


    /**
     * 消息接收回调该方法
     *
     * @param msg 消息
     */
    @Override
    public void handleMessage(@NonNull Message msg) {
        Log.e(TAG, "收到消息:" + Thread.currentThread().getName());
        if (msg.what == Builder.TIMER_FLAG && timerListener != null) {
            timerListener.onOverTimeListener();
        }
    }

    /**
     * 设置定时超时监听器
     *
     * @param timerListener 定时器监听
     */
    protected void addTimerListener(TimerListener timerListener) {
        this.timerListener = timerListener;
    }



    /**
     * 资源清理
     */
    protected void recycle() {
        this.timerListener = null;
        removeMessages(Builder.TIMER_FLAG);
    }

    public static class Builder {
        private static final int START_STATISTICS_TIMER = 2;
        private static final int STOP_STATISTICS_TIMER = 3;
        private final MeetingMenuTimer meetingMenuTimer;
        private int overTime = 5000;
        private static final int TIMER_FLAG = 1;

        public Builder() {
            Looper looper = Looper.getMainLooper();
            meetingMenuTimer = new MeetingMenuTimer(looper);
        }

        /**
         * 开始计时
         *
         * @param distanceTime 间隔时间
         */
        public void startStatisticsTime(long distanceTime) {
            Message message = meetingMenuTimer.obtainMessage();
            message.what = START_STATISTICS_TIMER;
            message.obj = distanceTime;
            meetingMenuTimer.sendMessage(message);
            Log.i(TAG, "startStatisticsTime");
        }


        /**
         * 停止统计时间
         */
        public void stopStatisticsTime() {
            Log.i(TAG, "stopStatisticsTime");
            Message message = meetingMenuTimer.obtainMessage();
            message.what = STOP_STATISTICS_TIMER;
            meetingMenuTimer.removeMessages(START_STATISTICS_TIMER);
            meetingMenuTimer.sendMessage(message);
        }


        /**
         * 设置超时时间
         *
         * @param overTime 超时时间
         * @return Builder
         */
        public Builder setOverTime(int overTime) {
            this.overTime = overTime;
            return this;
        }

        /**
         * 设置定时超时监听器
         *
         * @param timerListener 定时监听器
         * @return Builder
         */
        public Builder addTimerListener(TimerListener timerListener) {
            meetingMenuTimer.addTimerListener(timerListener);
            return this;
        }


        /**
         * 重新开始计算
         *
         * @return Builder
         */
        public Builder restartTime() {
            //先将消息队列中已有的延迟消息移除
            meetingMenuTimer.removeMessages(TIMER_FLAG);
            //重新添加延迟消息
            Message message = meetingMenuTimer.obtainMessage();
            message.what = TIMER_FLAG;
            meetingMenuTimer.sendMessageDelayed(message, overTime);
            return this;
        }


        /**
         * 取消定时计时
         *
         * @return Builder
         */
        public Builder cancelTimer() {
            meetingMenuTimer.removeMessages(TIMER_FLAG);
            return this;
        }

        /**
         * 清理工作
         */
        public void recycle() {
            stopStatisticsTime();
            meetingMenuTimer.removeMessages(TIMER_FLAG);
            meetingMenuTimer.removeMessages(START_STATISTICS_TIMER);
        }

    }


    /**
     * 定时器事件监听
     */
    public interface TimerListener {
        /**
         * 超过指定事件后回调该方法
         */
        void onOverTimeListener();
    }


}
