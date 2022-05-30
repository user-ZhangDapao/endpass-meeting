package com.sdcz.endpass.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.TaskListAdapter;
import com.sdcz.endpass.base.BaseFragment;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.presenter.TaskListPresenter;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ITaskListView;
import com.sdcz.endpass.widget.CustomItemDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.KeyStore;
import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:12
 * Description: @
 */
public class TaskListFragment extends BaseFragment<TaskListPresenter> implements ITaskListView, OnRefreshListener {

    private RecyclerView rvRoot;
    private TaskListAdapter adapter;
    private RelativeLayout rlError;
    private SmartRefreshLayout refreshLayout;

    @Override
    protected TaskListPresenter createPresenter() {
        return new TaskListPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_task_list;
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        rvRoot = rootView.findViewById(R.id.rvRoot);
        rlError = rootView.findViewById(R.id.rlError);
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
    }

    @Override
    public void initData() {
        super.initData();
        adapter = new TaskListAdapter(getContext());
        rvRoot.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRoot.setAdapter(adapter);
        mPresenter.getChannelList(getActivity());

    }

    @Override
    public void showData(List<ChannelBean> data) {
        refreshLayout.finishRefresh();
        if (data != null){
            if (data.size() <= 0){
                rlError.setVisibility(View.VISIBLE);
                rvRoot.setVisibility(View.GONE);
            }else {
                rlError.setVisibility(View.GONE);
                rvRoot.setVisibility(View.VISIBLE);
            }
            adapter.setData(data);
        }
    }

    @Override
    public void initListener() {
        super.initListener();
        refreshLayout.setOnRefreshListener(this);
        adapter.setOnItemClickListener(new TaskListAdapter.OnItemClickListener() {
            @Override
            public void onSelectedItem(ChannelBean data) {
                showWinMassage(data);
            }

            @Override
            public void onJoinItem(String Code) {
                JoinMeetingManager.getInstance().loginRoomId("10008", SharedPrefsUtil.getUserInfo().getNickName(),
                        "123456", false, new JoinMeetingCallback() {

                            @Override
                            public void onStart(Procedure procedure) {
//                                if (loadingDialog == null) {
//                                    loadingDialog = new LoadingDialog(requireContext(), R.string.logging);
//                                }
//                                loadingDialog.show();
                                showLoading();
                            }

                            @Override
                            public void onState(int state, String msg) {
//                                loadingDialog.updateText(LoginStateUtil.convertStateToString(state));
                            }

                            @Override
                            public void onBlockFailed(ProcessStep step, int code, String msg) {
                                ToastUtils.showShort(LoginErrorUtil.getErrorSting(code));
//                                loadingDialog.dismiss();
                                hideLoading();
                            }

                            @Override
                            public void onFailed() {
//                                loadingDialog.dismiss();
                                hideLoading();
                            }

                            @Override
                            public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
//                                showInputPasswordDialog(isFrontVerify, inputPassword);
                            }

                            @Override
                            public void onSuccess() {
//                                loadingDialog.dismiss();
                                hideLoading();
                                Intent intent = new Intent(requireActivity(), MobileMeetingActivity.class);
                                intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN,false);
                                startActivity(intent);
                            }
                        });
//                if (joinGroup(Code)){
//                    SharedPrefsUtil.putValue(getActivity(),KeyStore.RECORDCODE,Code);
//                    EventBus.getDefault().post(new EventManualJoin(true));
//                    EventBus.getDefault().post(new EventChannelType(Constants.MEETING_TASK));
//                }
            }
        });
    }

//    @Override
//    protected void sendRefrech() {
//        super.sendRefrech();
//        mPresenter.getChannelList(getActivity());
//    }

    //显示弹窗（任务信息）
    private void showWinMassage(ChannelBean data)  {
        CustomItemDialog.Builder builder = new CustomItemDialog.Builder(getActivity());
        builder.setMessage(data.getChannelName());
        builder.setMessage2(data.getCreateTime());
        builder.setMessage3(data.getPeopleCount()+"");
        builder.setMessage6(data.getDetails());
        builder.setTitle(data.getChannelName());
        builder.setPositiveButton("加入任务", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                if (joinGroup(data.getChannelCode())){
//                    SharedPrefsUtil.putValue(getActivity(), KeyStore.RECORDCODE,data.getChannelCode());
//                    EventBus.getDefault().post(new EventManualJoin(true));
//                    EventBus.getDefault().post(new EventChannelType(Constants.MEETING_TASK));
//                }
            }
        });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

//    /**
//     * 刷新
//     * @param event
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onRefach(EventRefach event) {
//        mPresenter.getChannelList(getActivity());
//    }

    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        mPresenter.getChannelList(getActivity());
    }

}
