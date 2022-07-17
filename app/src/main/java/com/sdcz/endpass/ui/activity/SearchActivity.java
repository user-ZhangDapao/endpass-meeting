package com.sdcz.endpass.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.auth.UserInfo;
import com.google.gson.Gson;
import com.inpor.base.sdk.roomlist.ContactManager;
import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.base.sdk.roomlist.RoomListManager;
import com.inpor.manager.util.HandlerUtils;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.inpor.sdk.online.PaasOnlineManager;
import com.inpor.sdk.repository.BaseResponse;
import com.inpor.sdk.repository.bean.InstantMeetingInfo;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.MailUserAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.EventBusMode;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.SearchPresenter;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.util.StatusBarUtils;
import com.sdcz.endpass.view.ISearchView;
import com.sdcz.endpass.widget.PopupWindowToCall;
import com.sdcz.endpass.widget.PopupWindowToUserData;
import com.universal.clientcommon.beans.CompanyUserInfo;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 搜索联系人
 */
@RuntimePermissions
public class SearchActivity extends BaseActivity<SearchPresenter> implements ISearchView, View.OnClickListener {

    private RecyclerView rlRoot;
    private TextView tvTitle;
    private ImageView ivBack;
    private ImageView ivClose;
    private EditText etSearch;
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


    protected void onUserStateChange(CompanyUserInfo info) {
        for (UserEntity userEntity : userInfoList){
            if (userEntity.getMdtUserId() == info.getUserId()){
                userEntity.setIsOnline(info.isMeetingState());
                HandlerUtils.postToMain(() -> adapter.notifyDataSetChanged());
            }
        }
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_search;
    }

    @Override
    protected SearchPresenter createPresenter() {
        return new SearchPresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        rlRoot = findViewById(R.id.rlRoot);
        tvTitle = findViewById(R.id.tvTitle);
        ivBack = findViewById(R.id.ivBack);
        ivClose = findViewById(R.id.ivClose);
        etSearch = findViewById(R.id.et_search);
        tvTitle.setText("搜索联系人");
        ivBack.setVisibility(View.VISIBLE);
        return rlRoot;
    }

    @Override
    public void initData() {
        super.initData();
        rlRoot.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void initListener() {
        super.initListener();
        ivBack.setOnClickListener(this);
        ivClose.setOnClickListener(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    ivClose.setVisibility(View.VISIBLE);
                    mPresenter.getUserByNameLike(SearchActivity.this,s.toString());
                } else {
                    ivClose.setVisibility(View.INVISIBLE);
                    mPresenter.getUserByNameLike(SearchActivity.this,"");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void setStatusBar() {
        StatusBarUtils.setTranslucentBar(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivBack:
                finish();
                break;
            case R.id.ivClose:
                etSearch.setText("");
                break;
        }
    }

    @Override
    public void showData(List<UserEntity> data) {
        if (null != data){
            userInfoList = data;
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

//            FspManager.getInstance().refreshAllUserStatus();
            adapter = new MailUserAdapter(R.layout.item_maillist_user, data, new MailUserAdapter.onItemClick() {
                @Override
                public void onClick(UserEntity item) {
                    info = item;
                    mPresenter.postCollectStatus(SearchActivity.this, item.getUserId() + "");
                }
            });
            rlRoot.setAdapter(adapter);


            if (data.size()>0){
                rlRoot.setVisibility(View.VISIBLE);
            }else {
                rlRoot.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void showStatus(Integer data) {
        if (data != null) {
            PopupWindowToUserData popuWin = new PopupWindowToUserData(this, data, info,"",
                    new PopupWindowToUserData.OnPopWindowClickListener() {
                        @Override
                        public void onCreatRecord(String userId, String collectUserId, int recordType) {
                            //创建临时会话
                            creteHstGroup(userId, collectUserId, recordType);
                        }

                        @Override
                        public void onCollectUser(String userId, String collectUserId) {
                            //取消收藏
                            mPresenter.collectUser(SearchActivity.this, collectUserId);
                        }

                        @Override
                        public void onCallPhone(String phoneNum) {
                            new PopupWindowToCall(SearchActivity.this, new PopupWindowToCall.OnPopWindowClickListener() {
                                @Override
                                public void onPopWindowClickListener(View view) {
                                    SearchActivityPermissionsDispatcher.CallWithPermissionCheck(SearchActivity.this, phoneNum);
                                }
                            }, info.getPhonenumber()).show();
                        }
                    });
            popuWin.show();
        }
    }


    /**
     * 修改是否收藏用户
     *
     * @param data
     */
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
                        EventBus.getDefault().post(new EventBusMode("TemporaryUserLeave"));
                        ToastUtils.showShort("呼叫失败,请稍后再试");
                        return;
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ContactEnterUtils.getInstance(getContext()).joinForCode(inviteCode.toString(),recordType, channelCode,SearchActivity.this);
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void creteHstGroup(String userId, String collectUserId, int recordType){
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
//                                loadingDialog.dismiss();
                            return;
                        }
                        mPresenter.creatRecord(SearchActivity.this, collectUserId, recordType, Long.parseLong(result.getResult().getInviteCode()));

//                            String inviteCode = recordType + result.getResult().getInviteCode();

//                        SdkUtil.getContactManager().inviteUsers(result.getResult().getInviteCode(), InstantMeetingOperation.getInstance().getSelectUserData(), new ContactManager.OnInviteUserCallback() {
//                            @Override
//                            public void inviteResult(int i, String s) {
//                                if(i == 0){
//                                    ToastUtils.showShort("呼叫失败,请稍后再试");
//                                    return;
//                                }
//                                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        ContactEnterUtils.getInstance(getContext()).joinForCode(String.valueOf(result.getResult().getRoomId()), data,recordType, MailListActivity.this);
//                                    }
//                                });
//                            }
//                        });
                    }
                });
    }
    @NeedsPermission({Manifest.permission.CALL_PHONE})
    public void Call(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

}