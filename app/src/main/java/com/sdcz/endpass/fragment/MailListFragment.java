package com.sdcz.endpass.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.inpor.base.sdk.roomlist.ContactManager;
import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.base.sdk.roomlist.RoomListManager;
import com.inpor.manager.beans.CompanyUserDto;
import com.inpor.manager.util.HandlerUtils;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.inpor.sdk.online.PaasOnlineManager;
import com.inpor.sdk.repository.BaseResponse;
import com.inpor.sdk.repository.bean.InstantMeetingInfo;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.MailListAdapter;
import com.sdcz.endpass.adapter.MailUserAdapter;
import com.sdcz.endpass.base.BaseFragment;
import com.sdcz.endpass.bean.EventBusMode;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.network.QueryCompanyUsersHttp;
import com.sdcz.endpass.presenter.MailListPresenter;
import com.sdcz.endpass.ui.activity.LikeActivity;
import com.sdcz.endpass.ui.activity.MailListActivity;
import com.sdcz.endpass.ui.activity.MainActivityApp;
import com.sdcz.endpass.ui.activity.SearchActivity;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.util.StatusBarUtils;
import com.sdcz.endpass.view.IMailListView;
import com.sdcz.endpass.widget.PopupWindowToCall;
import com.sdcz.endpass.widget.PopupWindowToUserData;
import com.universal.clientcommon.beans.CompanyUserInfo;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Author: Administrator
 * CreateDate: 2021/6/28 14:21
 * Description: @
 */
@RuntimePermissions
public class MailListFragment extends BaseFragment<MailListPresenter> implements IMailListView, View.OnClickListener, OnRefreshListener, QueryCompanyUsersHttp.QueryCompanyUsersHttpCallck {

    private TextView tvTitle;
    private ImageView ivHead;
    private ImageView ivRight;
    private RelativeLayout rlLike;
    private RelativeLayout layoutFind;
    private RelativeLayout rlUser;
    private RecyclerView recyclerUser;
    private RecyclerView recyclerList;
    private UserEntity info;
    private MailUserAdapter userAdapter;
    private MailListAdapter taskAdapter;
    private SmartRefreshLayout refreshLayout;
    private MailListBean mData;



    private Observer userStateObserver = new Observer() {
        @Override
        public void update(Observable observable, Object arg) {
            if (arg instanceof CompanyUserInfo) {
                Log.e("navi", "userStateObserver");
                onUserStateChange((CompanyUserInfo) arg);
            }
        }
    };

    @Override
    protected MailListPresenter createPresenter() {
        return new MailListPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_maillist;
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        recyclerUser = rootView.findViewById(R.id.recycler_User);
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        recyclerList = rootView.findViewById(R.id.recycler_List);
        tvTitle = rootView.findViewById(R.id.tvTitle);
        ivHead = rootView.findViewById(R.id.ivLeft);
        ivRight = rootView.findViewById(R.id.ivRight);
        rlUser = rootView.findViewById(R.id.rlUser);
        rlLike = rootView.findViewById(R.id.rlLike);
        layoutFind = rootView.findViewById(R.id.layout_find);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        StatusBarUtils.setTranslucentBar(getActivity());
    }


    @Override
    public void initData() {
        super.initData();
        ivHead.setImageResource(R.drawable.icon_head);
        showLoading();
        mPresenter.getUserInfo(getActivity());
    }

    @Override
    public void initListener() {
        super.initListener();
        ivHead.setOnClickListener(this);
        rlLike.setOnClickListener(this);
        ivRight.setOnClickListener(this);
        layoutFind.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivLeft:
                MainActivityApp MainActivityApp = (MainActivityApp)getActivity();
                MainActivityApp.setTab(2);
                break;
            case R.id.layout_find:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.rlLike:
                startActivity(new Intent(getActivity(), LikeActivity.class));
                break;
            case R.id.ivRight:
                showLoading();
                mPresenter.getContactList(getActivity());
                break;
            default:
                break;
        }
    }


    protected void onUserStateChange(CompanyUserInfo info) {
        for (UserEntity userEntity : mData.getUserList()){
            if (userEntity.getMdtUserId() == info.getUserId()){
                userEntity.setIsOnline(info.isMeetingState());
                HandlerUtils.postToMain(() -> userAdapter.notifyDataSetChanged());
            }
        }
    }


    @Override
    public void showUserInfo(UserEntity entity) {
        showLoading();
        mPresenter.getContactList(getActivity());
        if (null != entity.getRoomId() && null != entity.getChannelCode()){
            ContactEnterUtils.getInstance(getContext())
                    .joinForCode(String.valueOf(entity.getRoomId()),3,entity.getChannelCode(), getActivity() );
        }
    }

    @Override
    public void showData(MailListBean data) {
        if (data != null) {
            mData = data;
            if (data.getDeptList().size() > 0 || data.getUserList().size() > 0){
                List<CompanyUserInfo> list =  InstantMeetingOperation.getInstance().getCompanyUserData();
                for (UserEntity entity : data.getUserList()){
                    long userId = entity.getMdtUserId();
                    for (CompanyUserInfo info : list){
                        if (userId == info.getUserId()){
                            entity.setIsOnline(info.isMeetingState());
                            Log.d("****", info.getUserName() + ":" + info.isMeetingState());
                            break;
                        }
                    }
                }

                recyclerUser.setLayoutManager(initLayoutManager(getContext()));
                recyclerList.setLayoutManager(initLayoutManager(getContext()));
                userAdapter = new MailUserAdapter(R.layout.item_maillist_user, data.getUserList(), new MailUserAdapter.onItemClick() {
                    @Override
                    public void onClick(UserEntity item) {
                        info = item;
                        mPresenter.postCollectStatus(getActivity(), item.getUserId() + "");
                    }
                });
                recyclerUser.setAdapter(userAdapter);

                taskAdapter = new MailListAdapter(R.layout.item_my_group_list, data.getDeptList(), new MailListAdapter.onItemClick() {
                    @Override
                    public void onClick(String deptId, String groupName) {
                        startActivity(new Intent(getActivity(), MailListActivity.class).putExtra(Constants.SharedPreKey.DEPTID, deptId).putExtra(Constants.SharedPreKey.DEPTNAME, groupName));
                    }
                });
                recyclerList.setAdapter(taskAdapter);
            }else {
                showEmpty();
            }

            if(null == data.getUserList() || data.getUserList().size() == 0){
                rlUser.setVisibility(View.GONE);
            }
            hideLoading();
        }

    }


    @Override
    public void showStatus(Integer data) {
        if (data != null) {
            PopupWindowToUserData popuWin = new PopupWindowToUserData(getActivity(), data, info,"",
                    new PopupWindowToUserData.OnPopWindowClickListener() {
                        @Override
                        public void onCreatRecord(String userId, String collectUserId, int recordType) {
                            //创建临时会话
                            creteHstGroup(collectUserId, recordType);
                        }

                        @Override
                        public void onCollectUser(String userId, String collectUserId) {
                            //取消收藏
                            mPresenter.collectUser(getActivity(), collectUserId);
                        }

                        @Override
                        public void onCallPhone(String phoneNum) {
                            new PopupWindowToCall(getActivity(), new PopupWindowToCall.OnPopWindowClickListener() {
                                @Override
                                public void onPopWindowClickListener(View view) {
//                                    .CallWithPermissionCheck(getActivity(), phoneNum);
                                }
                            }, info.getPhonenumber()).show();
                        }
                    });
            popuWin.show();
        }
    }

    @Override
    public void cancelLikeSuccess(Object data) {
        ToastUtils.showLong("操作成功");
    }

    @Override
    public void creatRecordSuccess(String channelCode, String collectUserId, int recordType, Long inviteCode) {
        boolean isHaveUser = false;
        try {
            long id = SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserId).getJSONObject(collectUserId).getLong("mdtUserId");
            for (CompanyUserInfo userInfo : InstantMeetingOperation.getInstance().getCompanyUserData()){
                if (id == userInfo.getUserId()){
                    InstantMeetingOperation.getInstance().addSelectUserData(userInfo);
                    isHaveUser = true;
                    break;
                }
            }

            if (isHaveUser == false) return;
            SdkUtil.getContactManager().inviteUsers(inviteCode.toString(), InstantMeetingOperation.getInstance().getSelectUserData(), new ContactManager.OnInviteUserCallback() {
                @Override
                public void inviteResult(int i, String s) {
                    InstantMeetingOperation.getInstance().clearSelectUserData();
                    if(i == 0){
                        ToastUtils.showShort("呼叫失败,请稍后再试");
                        EventBus.getDefault().post(new EventBusMode("TemporaryUserLeave"));
                        return;
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ContactEnterUtils.getInstance(getContext()).joinForCode(inviteCode.toString(),recordType, channelCode,getActivity());
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        mPresenter.getContactList(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getContactList(getActivity());
    }

    @NeedsPermission({Manifest.permission.CALL_PHONE})
    public void Call(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

    private LinearLayoutManager initLayoutManager(Context context){
        LinearLayoutManager layoutManager = new LinearLayoutManager(context){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        return layoutManager;
    }

    @Override
    public void onFail() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onNoPermission() {

    }

    @Override
    public void onSuccess(CompanyUserDto companyUserDto) {
        for (UserEntity entity : mData.getUserList()){
            long userId = entity.getMdtUserId();
            for (CompanyUserInfo info : companyUserDto.getResult().getItems()){
                if (userId == info.getUserId()){
                    entity.setIsOnline(info.isMeetingState());
                    break;
                }
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideLoading();
                recyclerUser.setLayoutManager(initLayoutManager(getActivity()));
                recyclerList.setLayoutManager(initLayoutManager(getActivity()));
                userAdapter = new MailUserAdapter(R.layout.item_maillist_user, mData.getUserList(), new MailUserAdapter.onItemClick() {
                    @Override
                    public void onClick(UserEntity item) {
                        info = item;
                        mPresenter.postCollectStatus(getActivity(), item.getUserId()+"");
                    }
                });
                recyclerUser.setAdapter(userAdapter);

                taskAdapter = new MailListAdapter(R.layout.item_my_group_list, mData.getDeptList(), new MailListAdapter.onItemClick() {
                    @Override
                    public void onClick(String deptId, String groupName) {
                        startActivity(new Intent(getActivity(), MailListActivity.class).putExtra(Constants.SharedPreKey.DEPTID,deptId).putExtra(Constants.SharedPreKey.DEPTNAME,groupName));
                    }
                });
                recyclerList.setAdapter(taskAdapter);

                if (mData.getUserList().size() == 0){
                    rlUser.setVisibility(View.GONE);
                }
                taskAdapter.notifyDataSetChanged();
            }
        });
    }

    private void creteHstGroup(String collectUserId, int recordType){
        RoomListManager manager = SdkUtil.getRoomListManager();
        String meetingName = String.format(getString(R.string.create_instant_meeting_format), SharedPrefsUtil.getUserInfo().getNickName());
        manager.createInstantMeeting(meetingName, Collections.emptyList(), 2,
                30, "", "", new IRoomListResultInterface<BaseResponse<InstantMeetingInfo>>() {
                    @Override
                    public void failed(int code, String errorMsg) {
                        Log.i("TAG", "failed: code is " + code);
                        Log.i("TAG", "failed: errorMsg is " + errorMsg);
                        ToastUtils.showShort(R.string.instant_meeting_create_fail);
                        PaasOnlineManager.getInstance().setBusy(false);
                    }

                    @Override
                    public void succeed(BaseResponse<InstantMeetingInfo> result) {
                        Log.i("TAG", "succeed: result is " + new Gson().toJson(result));
                        if (result.getResCode() != 1) {
                            PaasOnlineManager.getInstance().setBusy(false);
                            ToastUtils.showShort(result.getResMessage());
                            return;
                        }
                        mPresenter.creatRecord(getActivity(), collectUserId, recordType, Long.parseLong(result.getResult().getInviteCode()));
                    }
                });
    }

}
