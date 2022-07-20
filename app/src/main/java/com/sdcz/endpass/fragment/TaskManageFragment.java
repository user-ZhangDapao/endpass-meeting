package com.sdcz.endpass.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.inpor.manager.util.HandlerUtils;
import com.inpor.nativeapi.adaptor.ChatMsgInfo;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.TaskManageAdapter;
import com.sdcz.endpass.base.BaseFragment;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.EventBusMode;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.presenter.TaskManagePresenter;
import com.sdcz.endpass.ui.activity.CreateTaskActivity;
import com.sdcz.endpass.ui.activity.TaskUserActivity;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ITaskManageView;
import com.sdcz.endpass.widget.CustomDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:12
 * Description: @
 */
public class TaskManageFragment extends BaseFragment<TaskManagePresenter> implements ITaskManageView, OnRefreshListener,ChatManager.ChatMessageListener {

    private TextView tvCreate;
    private RecyclerView rvRoot;
    private SmartRefreshLayout refreshLayout;
    private TaskManageAdapter adapter;
    private ChatManager chatManager;


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != chatManager){
            chatManager.setChatMessageListener(null);
            chatManager.recycle();
        }
    }


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
            public void deleteClick(ChannelBean bean) {
                showDeleteloding(bean);


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
        EventBus.getDefault().post(new EventBusMode("deleteChannel"));
    }

    private void showDeleteloding(ChannelBean bean){

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
                        mPresenter.deletChannel(getActivity(),bean.getChannelCode());

//                        JoinMeetingManager.getInstance().loginRoomId(String.valueOf(bean.getRoomId()), SharedPrefsUtil.getUserInfo().getNickName(), "",
//                                false, new JoinMeetingCallback() {
//
//                                    @Override
//                                    public void onStart(Procedure procedure) {
////                                showProgressDialog("...");
//                                    }
//
//                                    @Override
//                                    public void onState(int state, String msg) {
////                                showProgressDialog("正在删除中，请稍后...");
//                                    }
//
//                                    @Override
//                                    public void onBlockFailed(ProcessStep step, int code, String msg) {
////                                hideProgressDialog();
//                                    }
//
//                                    @Override
//                                    public void onFailed() {
////                                hideProgressDialog();
//                                    }
//
//                                    @Override
//                                    public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
////                                hideProgressDialog();
//                                    }
//
//                                    @Override
//                                    public void onSuccess() {
////                                        mPresenter.deletChannel(getActivity(),bean.getChannelCode());
//
//                                        new Handler().postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
////                                              chatManager = new ChatManager();
//                                                SdkUtil.getChatManager().sendChatMessage(0,Constants.SharedPreKey.PLEASE_LEAVE_ALL);
//
////                                                chatManager = ChatManager.getInstance();
////                                                chatManager.setChatMessageListener(new ChatManager.ChatMessageListener() {
////                                                    @Override
////                                                    public void onChatMessage(ChatMsgInfo message) {
////
////                                                    }
////                                                });
//                                                /**
//                                                 * 延时执行的代码
//                                                 */
////                                                chatManager.sendMessage(0,Constants.SharedPreKey.PLEASE_LEAVE_ALL);
//
//                                            }
//                                        },1000); // 延时1秒
//
//
////
//
////                                        chatManager = ChatManager.getInstance();
////                                        chatManager.setChatMessageListener(TaskManageFragment.this);
////                                        ChatManager.getInstance().sendMessage(0, );
//                                        //本地管理员结束会议
////                                Map<String,Object> reason_map = new HashMap();
////                                reason_map.put("code",1);
////                                reason_map.put("type",1);
////                                _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);
////                                ChatManager.getInstance().recycle();
////                                hideProgressDialog();
//                                    }
//                                });
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

    @Override
    public void onChatMessage(ChatMsgInfo message) {

    }
}
