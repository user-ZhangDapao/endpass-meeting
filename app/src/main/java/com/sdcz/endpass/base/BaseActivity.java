package com.sdcz.endpass.base;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.multidex.BuildConfig;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.util.ActivityUtils;
import com.sdcz.endpass.util.StatusBarUtils;
import com.sdcz.endpass.widget.stateview.StateView;

import org.greenrobot.eventbus.EventBus;
import io.reactivex.annotations.Nullable;

public abstract class BaseActivity <T extends BasePresenter> extends RxActivity implements IBaseView {

    protected T mPresenter;
    protected StateView stateView;
    //提交数据展示进度条
    private AlertDialog progressDialog;
    private TextView tvProgressMsg;
    private boolean m_isPause = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        requestWindowSet();
        super.onCreate(savedInstanceState);
        //设定为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ActivityUtils.getInstance().addActivity(this.getClass().getSimpleName(),this);
        mPresenter = createPresenter();
        //子类不再需要设置布局ID
        setContentView(provideContentViewId());
        if (canEventBus()){
//            EventBus.getDefault().register(this);
        }
        //黄油刀
        if (canButterKnife()) {
//            ButterKnife.bind(this);
        }

        stateView = StateView.inject(initView());
        if (stateView != null) {
            /**设置数据为空时布局*/
            stateView.setEmptyResource(R.layout.base_empty);
            /**设置加载布局*/
            stateView.setLoadingResource(R.layout.base_loading);
            /**设置点击重试布局*/
            stateView.setRetryResource(R.layout.base_retry);
        }

        initData();
        initListener();
        setStatusBar();

    }

    private void initProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
        View view = View
                .inflate(this, R.layout.progress_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCore, KeyEvent keyEvent) {
                if(keyCore == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }else {
                    // TODO: true拦截 false不拦截
                    return true;
                }
            }
        });
        tvProgressMsg = view.findViewById(R.id.tv_progress_msg);

        //取消或确定按钮监听事件处理
        progressDialog = builder.create();
    }

    protected void showProgressDialog() {
        if (progressDialog == null) {
            initProgressDialog();
        }
        progressDialog.show();
    }

    protected void showProgressDialog(String msg) {
        if (progressDialog == null) {
            initProgressDialog();
        }
        tvProgressMsg.setText(msg);
        progressDialog.show();
    }

    protected void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    protected void requestWindowSet() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    protected void setStatusBar() {
        StatusBarUtils.setTranslucentBar(this);
    }

    /**
     * 初始化控件
     */
    public abstract View initView();

    /**
     * 初始化数据
     */
    public void initData() {
    }

    /**
     * 初始化监听
     */
    public void initListener() {
    }

    /**
     * 用于创建Presenter和判断是否使用MVP模式(由子类实现)
     *
     * @return
     */
    protected abstract T createPresenter();

    /**
     * 得到当前界面的布局文件id(由子类实现)
     *
     * @return
     */
    protected abstract int provideContentViewId();

    //-------------

    @Override
    public void showLoading() {
        stateView.showLoading();
    }

    @Override
    public void hideLoading() {
        stateView.showContent();
    }

    @Override
    public void showError() {
        hideLoading();
        stateView.showRetry();
    }

    @Override
    public void showEmpty() {
        stateView.showEmpty();
    }

    @Override
    public void showToast(String msg) {

    }

    @Override
    public void showOnFailure(String status, String msg) {
        hideLoading();
        ToastUtils.showLong(msg);
    }

    //-------------

    @Override
    public Context getContext() {
        return BaseActivity.this;
    }

    public boolean isPause() {
        return m_isPause;
    }

    protected boolean canEventBus() {
        return true;
    }

    protected boolean canButterKnife() {
        return true;
    }

    //------------

    @Override
    protected void onResume() {
        super.onResume();
//        mCurrentActivity = this;
        m_isPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mCurrentActivity = null;
        m_isPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPresenter != null) {
            mPresenter.detachView();
        }

        if (canEventBus()) EventBus.getDefault().unregister(this);
        ActivityUtils.getInstance().moveAcitivityKey(this.getClass().getSimpleName());
    }

    public void wakeUpAndUnlock(Context context) {
        //屏锁管理器
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }


}