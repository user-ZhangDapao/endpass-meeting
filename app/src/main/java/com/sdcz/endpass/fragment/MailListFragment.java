package com.sdcz.endpass.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.manager.beans.CompanyUserDto;
import com.inpor.nativeapi.adaptor.OnlineUserInfo;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.inpor.sdk.online.PaasOnlineManager;
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
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginMeetingCallBack;
import com.sdcz.endpass.network.QueryCompanyUsersHttp;
import com.sdcz.endpass.presenter.MailListPresenter;
import com.sdcz.endpass.ui.activity.LikeActivity;
import com.sdcz.endpass.ui.activity.LoginActivityApp;
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

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
//        tvTitle.setText(R.string.mail);
//        ivHead.setVisibility(View.VISIBLE);
//        ivRight.setVisibility(View.VISIBLE);
//        ivRight.setImageResource(R.drawable.video_refresh_press);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        StatusBarUtils.setTranslucentBar(getActivity());
//        StatusBarUtils.setStatusBarFontIconDark(true,getActivity());

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

    @Override
    public void showUserInfo(UserEntity entity) {
        showLoading();
        mPresenter.getContactList(getActivity());
        if (null != entity.getRoomId() && null != entity.getChannelCode()){
            ContactEnterUtils.getInstance(getContext())
                    .joinForCode(String.valueOf(entity.getRoomId()),entity.getChannelCode(),3, getActivity() );
        }
    }

    @Override
    public void showData(MailListBean data) {


//        HashMap<Long, OnlineUserInfo> map = SdkUtil.getContactManager().getOnlineDeviceInfo();
        refreshLayout.finishRefresh();
        if (data != null){
            mData = data;
            new QueryCompanyUsersHttp(1, 1000, this);
//            userInfoList = data.getUserList();
//            for (UserEntity entity : userInfoList){
//                if (map.containsKey(entity.getMdtUserId())){
//                    entity.setIsOnline(map.get(Long.getLong(entity.getMdtUserId() + "")).getUserState());
//                }else {
//                    entity.setIsOnline(0);
//                }
            }

//            FspManager.getInstance().refreshAllUserStatus();
//        }
    }


    @Override
    public void showStatus(Integer data) {
        if (data != null){
            PopupWindowToUserData popuWin = new PopupWindowToUserData(getActivity(), data, info, SharedPrefsUtil.getUserId() + "", new PopupWindowToUserData.OnPopWindowClickListener() {
                @Override
                public void onCreatRecord(String userId, String collectUserId, int recordType) {
                    //创建临时会话
//                    mPresenter.creatRecord(getActivity(),collectUserId, recordType);
                }

                @Override
                public void onCollectUser(String userId, String collectUserId) {
                    //取消收藏
//                    mPresenter.collectUser(getActivity(),userId,collectUserId);
                }

                @Override
                public void onCallPhone(String phoneNum) {
                    new PopupWindowToCall(getActivity(), new PopupWindowToCall.OnPopWindowClickListener() {
                        @Override
                        public void onPopWindowClickListener(View view) {
                            MailListFragmentPermissionsDispatcher.CallWithPermissionCheck(MailListFragment.this, phoneNum);
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
    public void creatRecordSuccess(String data, String collectUserId, int recordType) {
//        SharedPrefsUtil.putValue(getActivity(),KeyStore.RECORDCODE,data);
//        String[] array = {collectUserId};
//        joinGroupVoiceUser(array,data,recordType);
    }
//
//    @Override
//    protected void onRefreshAllUserStatusFinished(FspUserInfo[] infos) {
//        super.onRefreshAllUserStatusFinished(infos);
////        for (FspUserInfo info : infos) {
////            Log.e(TAG, "onEventRefreshUserStatusFinished -- onLineName --> " + info.getUserId());
////        }
//        //在线人员名单
//        List<FspUserInfo> m_data = new ArrayList();
//        m_data.addAll(Arrays.asList(infos));
//        if (userInfoList != null) {
//            for (int j = 0; j < userInfoList.size(); j++) {
//                String userId = userInfoList.get(j).getUserId() + "";
////                userInfoList.get(j).setIsOnline(0);
//                for (int i = 0; i < m_data.size(); i++) {
//                    String hstUser = m_data.get(i).getUserId();
//                    if (hstUser.equals(userId)) {
//                        userInfoList.get(j).setIsOnline(1);
//                    }
//                }
//            }
//            userAdapter.notifyDataSetChanged();
//            taskAdapter.setOnLinUserList(m_data);
//        }
//    }
//
//    @Override
//    protected void onUserStatusChangeResult(FspEvents.UserStatusChange userInfo) {
//        super.onUserStatusChangeResult(userInfo);
//        for (UserInfo info : userInfoList){
//            if (info.getUserId().equals(userInfo.userId)){
//                info.setIsOnline(userInfo.status);
//            }
//        }
//        userAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        mPresenter.getContactList(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getContactList(getActivity());
    }
//
//    @Override
//    protected void sendRefrech() {
//        super.sendRefrech();
//        String role = SharedPrefsUtil.getString(ROLE,"3");
//        if (role.equals("1")){
//            mPresenter.getContactList(getActivity(), "0");
//        }else {
//            mPresenter.getContactList(getActivity(), SharedPrefsUtil.getString(DEPTID,null));
//        }
//    }

    @NeedsPermission({Manifest.permission.CALL_PHONE})
    public void Call(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

//    /**
//     * 刷新
//     * @param event
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onRefach(EventRefach event) {
//        String role = SharedPrefsUtil.getString(ROLE,"3");
//        if (role.equals("1")){
//            mPresenter.getContactList(getActivity(), "0");
//        }else {
//            mPresenter.getContactList(getActivity(), SharedPrefsUtil.getString(DEPTID,null));
//        }
//    }

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
}
