package com.sdcz.endpass.ui.activity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.MeetingModelListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.google.firebase.auth.UserInfo;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.user.UserManager;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.TaskUserListAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.MobileMeetingPresenter;
import com.sdcz.endpass.view.IMobileMeetingView;

import java.util.List;

public class UserPopActivity extends BaseActivity<MobileMeetingPresenter> implements IMobileMeetingView {

    private LinearLayout llRoot;
    private RecyclerView rvRoot;
    private List<ChannerUser> userInfoList;
    private TaskUserListAdapter taskUserAdapter;
    private Handler m_handler = new Handler();


    private MeetingManager meetingModel;
    private UserManager userModel;
    private String channelCode;

    private final MeetingModelListener meetingModelListener = new MeetingModelListener() {

        @Override
        public void onUserEnter(List<Long> userIds) {
            if (userIds == null || userIds.isEmpty()) {
                return;
            }
            // 有用户进入会议室重新计算各个类别的总数
//            Log.i(TAG, "count user on user enter");
//            presenter.countUser();
        }

        @Override
        public void onUserLeave(BaseUser user) {
            if (user == null) {
                return;
            }
//            // 用户离开会议室重新计算各个类别的总数
//            Log.i(TAG, "count user on user leave");
//            presenter.countUser();
        }

        public void onSetRoomMute(byte mute) {
//            setAllAudioOffState(mute);
        }

        @Override
        public void onMainSpeakerChanged(BaseUser user) {
//            if (user.isLocalUser()) {
//                updateHost();
//                updateMute(user);
//            }
//            presenter.countUser();
        }
    };

    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.MAIN_SPEAKER,
            UserModelListenerImpl.ThreadMode.MAIN) {

        @Override
        public void onUserChanged(int type, BaseUser user) {
//            if (user.isLocalUser() && (UserModelListenerImpl.MAIN_SPEAKER == type
//                    || UserModelListenerImpl.USER_RIGHT == type)) {
//                updateHost();
//                updateMute(user);
//            }
//            Log.i(TAG, "count user on user changed");
//            presenter.countUser();
        }

        @Override
        public void onBatchUserChanged(int type, BaseUser[] batchUsers) {
//            if (batchUsers == null || batchUsers.length == 0) {
//                return;
//            }
//            Log.i(TAG, "count user on batch user state changed");
//            presenter.countUser();
//            for (BaseUser user : batchUsers) {
//                if (user.isLocalUser()) {
//                    updateHost();
//                    updateMute(user);
//                    break;
//                }
//            }
        }

    };

    private Runnable m_OneSecondRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPause()) return;
            try {
                m_handler.postDelayed(this, 1000);
                refashChannelUser();
            } catch (Exception e) {
//                //e.printStackTrace();
            }
        }
    };


    @Override
    protected void requestWindowSet(Bundle savedInstanceState) {
        super.requestWindowSet(savedInstanceState);
        //设置宽度
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Point size = new Point();
        d.getSize(size);
        p.width = (int)(size.x * 1);//设置dialog的宽度为当前手机屏幕的宽度*0.8
        getWindow().setAttributes(p);
        getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        llRoot= findViewById(R.id.llRoot);
        rvRoot= findViewById(R.id.rvRoot);
        return llRoot;
    }

    @Override
    protected MobileMeetingPresenter createPresenter() {
        return new MobileMeetingPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_user_pop;
    }

    @Override
    public void initData() {
        super.initData();
        taskUserAdapter = new TaskUserListAdapter(this);
        rvRoot.setLayoutManager(new LinearLayoutManager(this));
        rvRoot.setAdapter(taskUserAdapter);
        channelCode = getIntent().getStringExtra(Constants.SharedPreKey.CHANNEL_CODE);
//        mPresenter.getChannelByCode(this, channelCode);
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                refashChannelUser();
            }
        });
    }

    @Override
    public void initListener() {
        super.initListener();
        meetingModel = SdkUtil.getMeetingManager();
        meetingModel.addEventListener(meetingModelListener);
        userModel = SdkUtil.getUserManager();
        userModel.addEventListener(userModelListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_handler.removeCallbacks(m_OneSecondRunnable);
        m_handler.postDelayed(m_OneSecondRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_handler != null) {
            m_handler.removeCallbacks(m_OneSecondRunnable);
        }
    }

    //
//    /**
//     * 物理/现实，组内成员合并 后 刷新
//     */
//    private void andOneUsersList() {
//        List<BaseUser> inGroupUsers = userModel.getAllUsers();
//
//        for (int i = 0; i < inGroupUsers.size(); i++) {
//            String fspId = inGroupUsers.get(i).getNickName();
//            boolean isIn = false;
//            for (int j = 0; j < userInfoList.size(); j++) {
//                if (fspId.equals(userInfoList.get(j).getUserId())) {
//                    userInfoList.get(j).setIsOnline(1);
//                    isIn = true;
//                    break;
//                }
//            }
//            if (isIn == false) {
//                UserInfo user = new UserInfo();
//                user.setUserId(fspId);
//                user.setIsOnline(1);
//                userInfoList.add(user);
//            }
//        }
//
//        if (userInfoList != null) {
//            taskUserAdapter.setData(userInfoList);
//            taskUserAdapter2.setData(userInfoList);
//
//        }
//        taskUserAdapter.setVonueState(mainVenue);
//        taskUserAdapter2.setVonueState(mainVenue);
//
//        setVisible();
//
//    }
//

    @Override
    public void showData(Boolean o) {

//        userInfoList = o.getChannelUserList();
//        refashChannelUser();
    }


    private void refashChannelUser() {
        if (null == userInfoList) return;
        Log.d("----------------","");
        //当前会议室所有的人
        List<BaseUser> inGroupUsers = userModel.getAllUsers();

        for (BaseUser userPass : inGroupUsers) {
            String fspName = userPass.getNickName();
            boolean isIn = false;

            for (ChannerUser user : userInfoList){
                if (fspName.equals(user.getNickName())){
                    user.setBaseUser(userPass);
                    isIn = true;
                    break;
                }
            }

            if (isIn == false) {
                ChannerUser user = new ChannerUser();
                user.setUserId((int)userPass.getUserId());
                user.setNickName(userPass.getNickName());
                user.setBaseUser(userPass);
                userInfoList.add(user);
            }

        }

        taskUserAdapter.setData(userInfoList);
    }
}