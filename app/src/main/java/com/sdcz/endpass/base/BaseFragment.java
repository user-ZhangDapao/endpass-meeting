package com.sdcz.endpass.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.widget.stateview.StateView;
import com.trello.rxlifecycle2.components.support.RxFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.security.KeyStore;

import io.reactivex.annotations.Nullable;

public abstract class BaseFragment<T extends BasePresenter> extends RxFragment implements IBaseView {

    protected T mPresenter;
    private View rootView;
    protected Activity mActivity;
    protected StateView stateView;
    private AlertDialog progressDialog; //提交数据展示进度条
    private TextView tvProgressMsg;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
    }

    private void initProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = View
                .inflate(getContext(), R.layout.progress_dialog, null);
        builder.setView(view);
        builder.setCancelable(true);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(provideContentViewId(), container, false);
            initView(rootView);
            if (canEventBus()){
//                EventBus.getDefault().register(this);
            }
            stateView = StateView.inject(getStateViewRoot());
            if (stateView != null) {
                /**设置数据为空时布局*/
                stateView.setEmptyResource(R.layout.base_empty);
                /**设置加载布局*/
                stateView.setLoadingResource(R.layout.base_loading);
                /**设置点击重试布局*/
                stateView.setRetryResource(R.layout.base_retry);
            }
            initData(); //放在此处是为了防止fragment切换时View销毁重建后多次执行
            initListener();
            setStatusBar();
        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
            onResumeFragment();
        }
        return rootView;
    }

    /**
     * 初始化一些view
     *
     * @param rootView
     */
    public void initView(View rootView) {

    }

    /**
     * 初始化数据
     */
    public void initData() {

    }

    /**
     * 设置listener的操作
     */
    public void initListener() {

    }

    /**
     *
     */
    protected void setStatusBar() {

    }

    public void onResumeFragment() {
    }

    /**
     * StateView的根布局，默认是整个界面，如果需要变换可以重写此方法
     */
    public View getStateViewRoot() {
        return rootView;
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
    public void showToast(String msg) {

    }

    @Override
    public void showEmpty() {
        stateView.showEmpty();
    }


    @Override
    public void showOnFailure(String status, String msg) {
        hideLoading();
        ToastUtils.showLong(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
        if (canEventBus()) EventBus.getDefault().unregister(this);
        rootView = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    /**
     * 检查activity连接情况
     */
    public void checkActivityAttached() {
        if (getActivity() == null) {
            throw new ActivityNotAttachedException();
        }
    }

    public static class ActivityNotAttachedException extends RuntimeException {
        public ActivityNotAttachedException() {
            super("Fragment has disconnected from Activity ! - -.");
        }
    }

    protected boolean canEventBus() {
        return true;
    }


}
