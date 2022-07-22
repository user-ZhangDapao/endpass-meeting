package com.sdcz.endpass.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.inpor.manager.util.HandlerUtils;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.MailUserAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.LikePresenter;
import com.sdcz.endpass.view.ILikeView;
import com.sdcz.endpass.widget.TitleBarView;
import com.universal.clientcommon.beans.CompanyUserInfo;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class DemoActivity extends BaseActivity<LikePresenter> implements ILikeView {
    private List<UserEntity> userEntityList;
    private TitleBarView rltitlebar;
    private RecyclerView rvRecyclerview;
    private MailUserAdapter adapter;
    private ImageView isBack;

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
    public View initView(Bundle savedInstanceState) {
        rltitlebar = findViewById(R.id.rlTitleBar);
        rvRecyclerview = findViewById(R.id.rvRecyclerView);
        rltitlebar.setLeftText("测试测试");
        isBack = findViewById(R.id.ivBack);
        return rvRecyclerview;
    }

    @Override
    protected LikePresenter createPresenter() {
        return new LikePresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.acrtivity_demo;
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
        if (data!=null){
            this.userEntityList = data;
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

                rvRecyclerview.setLayoutManager(new LinearLayoutManager(this));
                adapter = new MailUserAdapter(R.layout.item_maillist_user, data, new MailUserAdapter.onItemClick() {
                    @Override
                    public void onClick(UserEntity item) {
                        ToastUtils.showLong("您点击了一下");
                    }
                });
                rvRecyclerview.setAdapter(adapter);
            }else {
                rvRecyclerview.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showCollectStatus(Integer data) {

    }

    @Override
    public void creatRecordSuccess(String channelCode, String collectUserId, int recordType, Long inviteCode) {

    }

    @Override
    public void collectUserSuccess(Object data) {

    }

    @Override
    public void initListener() {
        super.initListener();
        isBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    protected void onUserStateChange(CompanyUserInfo info) {
        for (UserEntity userEntity : userEntityList){
            if (userEntity.getMdtUserId() == info.getUserId()){
                userEntity.setIsOnline(info.isMeetingState());
                HandlerUtils.postToMain(() -> adapter.notifyDataSetChanged());
            }
        }
    }
}
