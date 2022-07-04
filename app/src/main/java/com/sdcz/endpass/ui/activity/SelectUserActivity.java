package com.sdcz.endpass.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.UserInfo;
import com.inpor.manager.util.HandlerUtils;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.jaeger.library.StatusBarUtil;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.MailListAdapter;
import com.sdcz.endpass.adapter.SelectUserAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.SelectUserPresenter;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ISelectUserView;
import com.universal.clientcommon.beans.CompanyUserInfo;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SelectUserActivity extends BaseActivity<SelectUserPresenter> implements ISelectUserView, View.OnClickListener {

    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvRight;
    private LinearLayout llRoot;
    private RelativeLayout rlUser;
    private RecyclerView recyclerUser;
    private RecyclerView recyclerList;
    private List<UserEntity> userInfoList;
    private SelectUserAdapter adapter;

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
        return R.layout.activity_select_user;
    }

    @Override
    protected SelectUserPresenter createPresenter() {
        return new SelectUserPresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvRight = findViewById(R.id.tvRight);
        llRoot = findViewById(R.id.llRoot);
        rlUser = findViewById(R.id.rlUser);
        recyclerUser = findViewById(R.id.recycler_User);
        recyclerList = findViewById(R.id.recycler_List);
        ivBack.setVisibility(View.VISIBLE);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText("确认");
        return llRoot;
    }

    @Override
    public void initData() {
        super.initData();
        InstantMeetingOperation.getInstance().addObserver(userStateObserver);
        showLoading();
        String deptId = getIntent().getStringExtra(Constants.SharedPreKey.DEPTID);
        String grouName = getIntent().getStringExtra(Constants.SharedPreKey.GROUPNAME);
        if (!"".equals(grouName) && !"".equals(deptId)){
            tvTitle.setText(grouName);
            mPresenter.getContactList(this, deptId);
        }else {
            tvTitle.setText("选择成员");
            String role = SharedPrefsUtil.getRoleId();
            if (role.equals("1")){
                mPresenter.getContactList(this, "0");
            }else {
                mPresenter.getContactList(this, SharedPrefsUtil.getDeptId() + "");
            }
        }
    }

    @Override
    public void initListener() {
        super.initListener();
        ivBack.setOnClickListener(this);
        tvRight.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivBack:
                finish();
                break;
            case R.id.tvRight:
                setResult(Constants.HttpKey.RESPONSE_200,new Intent());
                finish();
                break;
            default:
                break;
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

//    @Override
//    protected void onUserStateChange(CompanyUserInfo info) {
//        super.onUserStateChange(info);
//        for (UserEntity userEntity : userInfoList){
//            if (userEntity.getMdtUserId() == info.getUserId()){
//                userEntity.setIsOnline(info.isMeetingState());
//                HandlerUtils.postToMain(() -> adapter.notifyDataSetChanged());
//            }
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
//        adapter.notifyDataSetChanged();
//    }

    @Override
    public void showData(MailListBean data) {
        hideLoading();
        if (data != null){
            userInfoList = data.getUserList();

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

            recyclerList.setLayoutManager(initLayoutManager(this));
            recyclerUser.setLayoutManager(initLayoutManager(this));

            if (data.getUserList().size() > 0){
                adapter = new SelectUserAdapter(R.layout.item_select_user, data.getUserList());
                recyclerUser.setAdapter(adapter);
            }else {
                rlUser.setVisibility(View.GONE);
            }

            if (data.getDeptList().size() > 0){
                recyclerList.setAdapter(new MailListAdapter(R.layout.item_my_group_list, data.getDeptList(), new MailListAdapter.onItemClick() {
                    @Override
                    public void onClick(String deptId, String groupName) {
                        Intent intent = new Intent(SelectUserActivity.this, SelectUserActivity.class);
                        intent.putExtra(Constants.SharedPreKey.DEPTID,deptId);
                        intent.putExtra(Constants.SharedPreKey.GROUPNAME,groupName);
                        startActivityForResult(intent,Constants.SharedPreKey.REQUEST_CODE_2);
                    }
                }));
            }

            if (data.getDeptList().size() == 0 && data.getUserList().size() == 0){
                showEmpty();
            }

//            FspManager.getInstance().refreshAllUserStatus();
        }
    }

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
//            adapter.notifyDataSetChanged();
//        }
//    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.color_4D6B4A));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Constants.HttpKey.RESPONSE_200){
                setResult(Constants.HttpKey.RESPONSE_200);
                finish();
            }
    }

    private LinearLayoutManager initLayoutManager(Context context) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        return layoutManager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}