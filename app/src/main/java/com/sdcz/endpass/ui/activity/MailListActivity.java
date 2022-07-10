package com.sdcz.endpass.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.auth.UserInfo;
import com.google.gson.Gson;
import com.inpor.base.sdk.roomlist.ContactManager;
import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.base.sdk.roomlist.RoomListManager;
import com.inpor.manager.util.HandlerUtils;
import com.inpor.nativeapi.adaptor.OnlineUserInfo;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.inpor.sdk.online.PaasOnlineManager;
import com.inpor.sdk.repository.BaseResponse;
import com.inpor.sdk.repository.bean.InstantMeetingInfo;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.MailListAdapter;
import com.sdcz.endpass.adapter.MailUserAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.MailListPresenter;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMailListView;
import com.sdcz.endpass.widget.PopupWindowToCall;
import com.sdcz.endpass.widget.PopupWindowToUserData;
import com.sdcz.endpass.widget.TitleBarView;
import com.universal.clientcommon.beans.CompanyUserInfo;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MailListActivity extends BaseActivity<MailListPresenter> implements IMailListView {

    private ImageView ivBack;
    private LinearLayout llRoot;
    private RelativeLayout rlUser;
    private RecyclerView recyclerUser;
    private RecyclerView recyclerList;
    private UserEntity info;
    private List<UserEntity> userInfoList;
    private MailUserAdapter userAdapter;
    private MailListAdapter taskAdapter;
    TitleBarView rlTitleBar;


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
        return R.layout.activity_mail_list;
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        ivBack = findViewById(R.id.ivBack);
        recyclerUser = findViewById(R.id.recycler_User);
        recyclerList = findViewById(R.id.recycler_List);
        rlTitleBar = findViewById(R.id.rlTitleBar);
        rlUser = findViewById(R.id.rlUser);
        llRoot = findViewById(R.id.llRoot);
        ivBack.setVisibility(View.VISIBLE);
        return llRoot;
    }

    @Override
    public void initData() {
        super.initData();

        String deptId = getIntent().getStringExtra(Constants.SharedPreKey.DEPTID);
        String groupName = getIntent().getStringExtra(Constants.SharedPreKey.DEPTNAME);
        InstantMeetingOperation.getInstance().addObserver(userStateObserver);

        rlTitleBar.setLeftText(groupName);
        mPresenter.getContactList(this, deptId);
    }

    protected void onUserStateChange(CompanyUserInfo info) {
        for (UserEntity userEntity : userInfoList){
            if (userEntity.getMdtUserId() == info.getUserId()){
                userEntity.setIsOnline(info.isMeetingState());
                HandlerUtils.postToMain(() -> userAdapter.notifyDataSetChanged());
            }
        }
    }

    @Override
    public void initListener() {
        super.initListener();
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void showUserInfo(UserEntity entity) {
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void showData(MailListBean data) {
        if (data != null) {
            userInfoList = data.getUserList();
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

                recyclerUser.setLayoutManager(initLayoutManager(this));
                recyclerList.setLayoutManager(initLayoutManager(this));
                userAdapter = new MailUserAdapter(R.layout.item_maillist_user, data.getUserList(), new MailUserAdapter.onItemClick() {
                    @Override
                    public void onClick(UserEntity item) {
                        MailListActivity.this.info = item;
                        mPresenter.postCollectStatus(MailListActivity.this, item.getUserId() + "");
                    }
                });
                recyclerUser.setAdapter(userAdapter);

                taskAdapter = new MailListAdapter(R.layout.item_my_group_list, data.getDeptList(), new MailListAdapter.onItemClick() {
                    @Override
                    public void onClick(String deptId, String groupName) {
                        startActivity(new Intent(MailListActivity.this, MailListActivity.class).putExtra(Constants.SharedPreKey.DEPTID, deptId).putExtra(Constants.SharedPreKey.DEPTNAME, groupName));
                    }
                });
                recyclerList.setAdapter(taskAdapter);
//                FspManager.getInstance().refreshAllUserStatus();

            }else {
                showEmpty();
            }

            if(null == data.getUserList() || data.getUserList().size() == 0){
                rlUser.setVisibility(View.GONE);
            }
            hideLoading();
        }
    }

    /**
     * 用户收藏状态
     *
     * @param data
     */
    @Override
    public void showStatus(Integer data) {
        if (data != null) {
            PopupWindowToUserData popuWin = new PopupWindowToUserData(this, data, info,"",
                new PopupWindowToUserData.OnPopWindowClickListener() {
                    @Override
                    public void onCreatRecord(String userId, String collectUserId, String recordType) {
                        //创建临时会话
                        mPresenter.creatRecord(MailListActivity.this, collectUserId, recordType);
                    }

                    @Override
                    public void onCollectUser(String userId, String collectUserId) {
                        //取消收藏
                        mPresenter.collectUser(MailListActivity.this, collectUserId);
                    }

                    @Override
                    public void onCallPhone(String phoneNum) {
                        new PopupWindowToCall(MailListActivity.this, new PopupWindowToCall.OnPopWindowClickListener() {
                            @Override
                            public void onPopWindowClickListener(View view) {
                                MailListActivityPermissionsDispatcher.CallWithPermissionCheck(MailListActivity.this, phoneNum);
                            }
                        }, info.getPhonenumber()).show();
                    }
                });
            popuWin.show();
        }
    }

    /**
     * 创建临时任务
     *
     * @param data
     */
    @Override
    public void creatRecordSuccess(String data, String collectUserId, String recordType) {
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
            RoomListManager manager = SdkUtil.getRoomListManager();
            String meetingName = String.format(getString(R.string.create_instant_meeting_format), "临时会话");
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
//                                loadingDialog.dismiss();
                                return;
                            }

                            String inviteCode = recordType + result.getResult().getInviteCode();

                            SdkUtil.getContactManager().inviteUsers(inviteCode, InstantMeetingOperation.getInstance().getSelectUserData(), new ContactManager.OnInviteUserCallback() {
                                @Override
                                public void inviteResult(int i, String s) {
                                    if(i == 0){
                                        ToastUtils.showShort("呼叫失败,请稍后再试");
                                        return;
                                    }
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ContactEnterUtils.getInstance(getContext()).joinInstantMeetingRoom(String.valueOf(result.getResult().getRoomId()), MailListActivity.this);
                                        }
                                    });
                                }
                            });
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改是否收藏用户
     *
     * @param data
     */
    @Override
    public void cancelLikeSuccess(Object data) {
        ToastUtils.showLong( "操作成功");
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
//        }
//        taskAdapter.setOnLinUserList(m_data);
//    }
//
//    @Override
//    protected void onUserStatusChangeResult(FspEvents.UserStatusChange userInfo) {
////        super.onUserStatusChangeResult(userInfo);
//        for (UserInfo info : userInfoList){
//            if (info.getUserId().equals(userInfo.userId)){
//                info.setIsOnline(userInfo.status);
//            }
//        }
//        if (null != userAdapter){
//            userAdapter.notifyDataSetChanged();
//        }
//    }

    private LinearLayoutManager initLayoutManager(Context context) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        return layoutManager;
    }


    @NeedsPermission({Manifest.permission.CALL_PHONE})
    public void Call(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

}