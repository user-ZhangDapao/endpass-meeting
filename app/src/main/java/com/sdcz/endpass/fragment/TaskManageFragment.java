package com.sdcz.endpass.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.TaskManageAdapter;
import com.sdcz.endpass.base.BaseFragment;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.presenter.TaskManagePresenter;
import com.sdcz.endpass.ui.activity.CreateTaskActivity;
import com.sdcz.endpass.ui.activity.TaskUserActivity;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.view.ITaskManageView;
import com.sdcz.endpass.widget.CustomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:12
 * Description: @
 */
public class TaskManageFragment extends BaseFragment<TaskManagePresenter> implements ITaskManageView, OnRefreshListener {

    private TextView tvCreate;
    private RecyclerView rvRoot;
    private SmartRefreshLayout refreshLayout;
    private TaskManageAdapter adapter;

    @Override
    protected TaskManagePresenter createPresenter() {
        return new TaskManagePresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_task_manage;
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        tvCreate = rootView.findViewById(R.id.tvCreate);
        rvRoot = rootView.findViewById(R.id.rvRoot);
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
    }

    @Override
    public void initData() {
        super.initData();
        adapter = new TaskManageAdapter(getContext());
        rvRoot.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRoot.setAdapter(adapter);
        mPresenter.getChannelList(getActivity());
    }

    @Override
    public void initListener() {
        super.initListener();
        refreshLayout.setOnRefreshListener(this);
        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity() , CreateTaskActivity.class), Constants.SharedPreKey.REQUEST_CODE_2);
            }
        });
        adapter.setListener(new TaskManageAdapter.ItemListener() {
            @Override
            public void deleteClick(String groupId) {
                showDeleteloding(groupId);
            }

            @Override
            public void InfoClick(String channelCode ,String channelName) {
                startActivity(new Intent(getActivity(), TaskUserActivity.class).putExtra(Constants.SharedPreKey.CHANNEL_NAME,channelName).putExtra(Constants.SharedPreKey.CHANNEL_CODE,channelCode));
            }
        });
    }

    @Override
    public void showData(List<ChannelBean> data) {
        refreshLayout.finishRefresh();
        if (data != null){
            adapter.setData(data);
            if (data.size() == 0){
                rvRoot.setVisibility(View.GONE);
            }else {
                rvRoot.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void deleteSuccess(Object data) {
        ToastUtils.showLong("解散成功");
        mPresenter.getChannelList(getActivity());
        ContactEnterUtils.getInstance(getContext()).sendRefash();
    }

    private void showDeleteloding(String groupId){
        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        builder.setMessage("您确认要解散当前任务吗？");
        builder.setTitle("解散任务");
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //设置你的操作事项
//                        if (FspManager.getInstance().kickOutGroupUser(groupId)){
                            mPresenter.deletChannel(getActivity(),groupId);
//                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.HttpKey.RESPONSE_200){
            mPresenter.getChannelList(getActivity());
        }
    }
//
//    @Override
//    protected void sendRefrech() {
//        super.sendRefrech();
//        mPresenter.getChannelList(getActivity());
//    }
//
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
