package com.sdcz.endpass.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.MeetingModelListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.nativeapi.adaptor.ChatMsgInfo;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.TaskUserListAdapter;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.ActivityUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;

import org.json.JSONException;

import java.util.List;

public class UserPopWidget extends BasePopupWindowContentView {

    private Activity context;
    private String channelCode;

    private List<ChannerUser> userInfoList;
    public static TaskUserListAdapter taskUserAdapter;
    private Handler m_handler = new Handler();

    private MeetingManager meetingModel;
    private UserManager userModel;
    private RecyclerView rvRoot;
    private ImageView ivClose;

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
            try {
                m_handler.postDelayed(this, 1000);
                refashChannelUser();
            } catch (Exception e) {
//                //e.printStackTrace();
            }
        }
    };

    public UserPopWidget(@NonNull Activity context, String channelCode) {
        super(context);
        this.context = context;
        this.channelCode = channelCode;
        init(context);
        initListener();
        initData();
    }



    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.activity_user_pop, this);
        rvRoot = findViewById(R.id.rvRoot);
        ivClose = findViewById(R.id.ivClose);

        meetingModel = SdkUtil.getMeetingManager();
        meetingModel.addEventListener(meetingModelListener);
        userModel = SdkUtil.getUserManager();
        userModel.addEventListener(userModelListener);
        taskUserAdapter = new TaskUserListAdapter(context);
        rvRoot.setLayoutManager(new LinearLayoutManager(context));
        rvRoot.setAdapter(taskUserAdapter);

        m_handler.removeCallbacks(m_OneSecondRunnable);
        m_handler.postDelayed(m_OneSecondRunnable, 1000);
    }

    private void initData() {
        if(null == channelCode) return;
        getChannelByCode(channelCode);
    }

    private void initListener() {
        taskUserAdapter.setClickListener(new TaskUserListAdapter.ItemClickEvent() {
            @Override
            public void clickCall(String mobile) {
                Log.d("--mobile--",mobile);
            }

            @Override
            public void clickCallKickOut(String userId) {
                deleteChannelUser(channelCode, userId);
            }

            @Override
            public void clickVonue(String userId, boolean isVonue) {
                if (isVonue) {
//                    setMute(channelCode, userId);
                } else {
//                    cancelMute(channelCode, userId);
                }
            }

            @Override
            public void clickListen(long userId, boolean isListen) {
                if (isListen) {
                    cancelMute(channelCode, userId);
                } else {
                    setMute(channelCode, userId);
                }
            }
        });
    }

    @Override
    public void recycle() {
        super.recycle();
        if (m_handler != null) {
            m_handler.removeCallbacks(m_OneSecondRunnable);
        }
    }

    private void refashChannelUser() {
        if (null == userInfoList) return;
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
                try {
                    user.setUserId(SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserName).getJSONObject(userPass.getNickName()).getLong("userId"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                user.setNickName(userPass.getNickName());
                user.setBaseUser(userPass);
                userInfoList.add(user);
            }

        }

        taskUserAdapter.setData(userInfoList);
    }

    public void getChannelByCode(String channelCode){
        RequestUtils.getChannelByCode(channelCode, new MyObserver<ChannelBean>(context) {
            @Override
            public void onSuccess(ChannelBean result) {
                if (null == result) return;
                userInfoList = result.getChannelUserList();
                refashChannelUser();
                taskUserAdapter.setMuteUserIds(result.getMuteUserIds());
            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
    }


    public void setMute(String channelCode, long userId){
        RequestUtils.setMute(channelCode, userId, new MyObserver<Object>(context) {
            @Override
            public void onSuccess(Object result) {
                ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.OFF_LISTEN + userId);
                taskUserAdapter.addMuteUserIds(userId);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
    }


    public void cancelMute(String channelCode, long userId){
        RequestUtils.cancelMute(channelCode, userId, new MyObserver<Object>(context) {
            @Override
            public void onSuccess(Object result) {
                ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.ON_LISTEN + userId);
                taskUserAdapter.removeMuteUserIds(userId);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
    }


    public void deleteChannelUser(String channelCode, String userId){
        RequestUtils.deleteChannelUser(channelCode, userId, new MyObserver<Object>(context) {
            @Override
            public void onSuccess(Object result) {
                ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.PLEASE_LEAVE + userId);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
            }
        });
    }


}