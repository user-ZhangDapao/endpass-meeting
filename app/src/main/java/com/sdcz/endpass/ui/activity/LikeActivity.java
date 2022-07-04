package com.sdcz.endpass.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.inpor.manager.util.HandlerUtils;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.jaeger.library.StatusBarUtil;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.MailUserAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.LikePresenter;
import com.sdcz.endpass.view.ILikeView;
import com.sdcz.endpass.widget.PopupWindowToCall;
import com.sdcz.endpass.widget.PopupWindowToUserData;
import com.sdcz.endpass.widget.TitleBarView;
import com.universal.clientcommon.beans.CompanyUserInfo;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 收藏
 */
//@RuntimePermissions
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
            if (data.size() > 0){
                ivNoData.setVisibility(View.GONE);
                rvRoot.setVisibility(View.VISIBLE);
                rvRoot.setLayoutManager(new LinearLayoutManager(this));
                adapter = new MailUserAdapter(R.layout.item_maillist_user, data, new MailUserAdapter.onItemClick() {
                    @Override
                    public void onClick(UserEntity item) {
                        LikeActivity.this.info = item;
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
            PopupWindowToUserData popuWin = new PopupWindowToUserData(this, data, info,"",
                    new PopupWindowToUserData.OnPopWindowClickListener() {
                        @Override
                        public void onCreatRecord(String userId, String collectUserId, String recordType) {
                            //创建临时会话
                            mPresenter.creatRecord(LikeActivity.this, collectUserId, recordType);
                        }

                        @Override
                        public void onCollectUser(String userId, String collectUserId) {
                            //取消收藏
                            mPresenter.collectUser(LikeActivity.this, userId, collectUserId);
                        }

                        @Override
                        public void onCallPhone(String phoneNum) {
                            new PopupWindowToCall(LikeActivity.this, new PopupWindowToCall.OnPopWindowClickListener() {
                                @Override
                                public void onPopWindowClickListener(View view) {
//                                    LikeActivityPermissionsDispatcher.CallWithPermissionCheck(LikeActivity.this, phoneNum);
                                }
                            }, info.getPhonenumber()).show();
                        }
                    });
            popuWin.show();
        }
    }

    /**
     * 创建临时任务
     * @param data
     */
    @Override
    public void creatRecordSuccess(String data, String collectUserId, String recordType) {
//        SharedPrefsUtil.putValue(this, KeyStore.RECORDCODE,data);
//        String[] array = {collectUserId};
//        joinGroupVoiceUser(array,data,recordType);
        ToastUtils.showShort("创建成功");
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

//    @NeedsPermission({Manifest.permission.CALL_PHONE})
//    public void Call(String phoneNum) {
//        Intent intent = new Intent(Intent.ACTION_CALL);
//        Uri data = Uri.parse("tel:" + phoneNum);
//        intent.setData(data);
//        startActivity(intent);
//    }

}