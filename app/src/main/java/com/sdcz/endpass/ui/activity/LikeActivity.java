package com.sdcz.endpass.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.inpor.manager.util.HandlerUtils;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.inpor.sdk.online.PaasOnlineManager;
import com.inpor.sdk.repository.BaseResponse;
import com.inpor.sdk.repository.bean.InstantMeetingInfo;
import com.jaeger.library.StatusBarUtil;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.MailUserAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.EventBusMode;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.LikePresenter;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ILikeView;
import com.sdcz.endpass.widget.PopupWindowToCall;
import com.sdcz.endpass.widget.PopupWindowToUserData;
import com.sdcz.endpass.widget.TitleBarView;
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
 * 收藏
 */
@RuntimePermissions
public class LikeActivity extends BaseActivity<LikePresenter> implements ILikeView {

    private ImageView ivBack;
    private TitleBarView rlTitleBar;
    private TextView tvLeft;
    private RecyclerView rvRoot;
    private RelativeLayout rlRoot;
    private ImageView ivNoData;
    private UserEntity info;
    private List<UserEntity> userInfoList;
    private MailUserAdapter adapter;


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
    protected int provideContentViewId() {
        return R.layout.activity_like;
    }

    @Override
    protected LikePresenter createPresenter() {
        return new LikePresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        ivBack = findViewById(R.id.ivBack);
        rlRoot = findViewById(R.id.rlRoot);
        rlTitleBar =  findViewById(R.id.rlTitleBar);
        rvRoot =  findViewById(R.id.rvRoot);
        ivNoData =  findViewById(R.id.ivNoData);
        tvLeft =  findViewById(R.id.tvLeft);
        rlTitleBar.setLeftText("我的收藏");
        ivBack.setVisibility(View.VISIBLE);
        return rlRoot;
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
    public void initData() {
        super.initData();
        InstantMeetingOperation.getInstance().addObserver(userStateObserver);
        showLoading();
        mPresenter.getCollectList(this);
    }

    @Override
    public void showData(List<UserEntity> data) {
        hideLoading();
        if (data != null){
            this.userInfoList = data;
            if (data.size() > 0){
                List<CompanyUserInfo> list =  InstantMeetingOperation.getInstance().getCompanyUserData();
                for (UserEntity entity : data){
                    long userId = entity.getMdtUserId();
                    for (CompanyUserInfo info : list){
                        if (userId == info.getUserId()){
                            entity.setIsOnline(info.isMeetingState());
                            Log.d("****", info.getUserName() + ":" + info.isMeetingState());
                            break;
                        }
                    }
                }

                ivNoData.setVisibility(View.GONE);
                rvRoot.setVisibility(View.VISIBLE);
                rvRoot.setLayoutManager(new LinearLayoutManager(this));
                adapter = new MailUserAdapter(R.layout.item_maillist_user, data, new MailUserAdapter.onItemClick() {
                    @Override
                    public void onClick(UserEntity item) {
                        info = item;
                        mPresenter.postCollectStatus(LikeActivity.this, item.getUserId() + "");
                    }
                });
                rvRoot.setAdapter(adapter);
            }else {
                ivNoData.setVisibility(View.VISIBLE);
                rvRoot.setVisibility(View.GONE);
            }
        }
    }

    protected void onUserStateChange(CompanyUserInfo info) {
        for (UserEntity userEntity : userInfoList){
            if (userEntity.getMdtUserId() == info.getUserId()){
                userEntity.setIsOnline(info.isMeetingState());
                HandlerUtils.postToMain(() -> adapter.notifyDataSetChanged());
            }
        }
    }



//
//    @Override
//    protected void onRefreshAllUserStatusFinished(FspUserInfo[] infos) {
//        super.onRefreshAllUserStatusFinished(infos);
////        for (FspUserInfo info : infos) {
////            Log.e("TAG", "onEventRefreshUserStatusFinished -- onLineName --> " + info.getUserId());
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
//
//            if (adapter != null){
//                adapter.notifyDataSetChanged();
//            }
//        }
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
//        if (adapter != null){
//            adapter.notifyDataSetChanged();
//        }
//    }

    /**
     * 用户收藏状态
     *
     * @param data
     */
    @Override
    public void showCollectStatus(Integer data) {
        if (data != null) {

            PopupWindowToUserData popuWin = new PopupWindowToUserData(LikeActivity.this, data, info,"",
                    new PopupWindowToUserData.OnPopWindowClickListener() {
                        @Override
                        public void onCreatRecord(String userId, String collectUserId, int recordType) {
                            //创建临时会话
                            creteHstGroup(collectUserId, recordType);
                        }

                        @Override
                        public void onCollectUser(String userId, String collectUserId) {
                            //取消收藏
                            mPresenter.collectUser(LikeActivity.this, collectUserId);
                        }

                        @Override
                        public void onCallPhone(String phoneNum) {
                            new PopupWindowToCall(LikeActivity.this, new PopupWindowToCall.OnPopWindowClickListener() {
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
                        mPresenter.creatRecord(LikeActivity.this, collectUserId, recordType, Long.parseLong(result.getResult().getInviteCode()));
                    }
                });
    }


    /**
     * 创建临时任务
     * @param data
     */

    /**
     * 创建临时任务
     */
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
                            ContactEnterUtils.getInstance(getContext()).joinForCode(inviteCode.toString(),recordType, channelCode,LikeActivity.this);
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
     * @param data
     */
    @Override
    public void collectUserSuccess(Object data) {
        ToastUtils.showLong("操作成功");
        mPresenter.getCollectList(this);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.color_4D6B4A));
    }

    @NeedsPermission({Manifest.permission.CALL_PHONE})
    public void Call(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

}