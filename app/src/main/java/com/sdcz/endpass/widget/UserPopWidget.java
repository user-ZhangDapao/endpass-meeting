package com.sdcz.endpass.widget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
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
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.gps.EasyPermissions;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.model.MicEnergyMonitor;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.ui.activity.SelectUserActivity;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import org.json.JSONException;
import java.util.List;


public class UserPopWidget extends BasePopupWindowContentView {

    private Activity context;
    private String channelCode;
    private int channelId;

    private List<ChannerUser> userInfoList;
    public TaskUserListAdapter taskUserAdapter;
    private Handler m_handler = new Handler();

    private MeetingManager meetingModel;
    private UserManager userModel;
    private RecyclerView rvRoot;
    private TextView tvAddUser;


    private final MeetingModelListener meetingModelListener = new MeetingModelListener() {

        @Override
        public void onUserEnter(List<Long> userIds) {
            if (userIds == null || userIds.isEmpty()) {
                return;
            }
            refashChannelUser();
        }

        @Override
        public void onUserLeave(BaseUser user) {
            if (user == null) {
                return;
            }
            initData();
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
            switch (type) {
                case UserModelListenerImpl.ADD_USER:
                case UserModelListenerImpl.REMOVE_USER:
                    initData();
                    break;
                default:
                    break;
            }

//            if (user.isLocalUser() && (UserModelListenerImpl.MAIN_SPEAKER == type
//                    || UserModelListenerImpl.USER_RIGHT == type)) {
//                updateHost();
//                updateMute(user);
//            }
//            Log.i(TAG, "count user on user changed");
//            presenter.countUser();
            initData();
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
                taskUserAdapter.notifyItemChanged2();
                m_handler.postDelayed(this, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void onMassageEvent(String type,String id) {/* Do something */
        if(null == taskUserAdapter) return;
        switch (type){
            case "MAIN_VENUE":
                taskUserAdapter.setVenueId(id);
                break;
            case "ON_LISTEN":
                if (id.equals("ALL")){
                    taskUserAdapter.removeAllMuteUserIds();
                }else {
                    taskUserAdapter.removeMuteUserIds(Long.valueOf(id));
                }
                break;
            case "OFF_LISTEN":
                if (id.equals("ALL")){
                    taskUserAdapter.addAllMuteUserIds();
                }else {
                    taskUserAdapter.addMuteUserIds(Long.valueOf(id));
                }
            case "REFASH":
                taskUserAdapter.notifyDataSetChanged();
                break;
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
        tvAddUser = findViewById(R.id.tvAddUser);

        meetingModel = SdkUtil.getMeetingManager();
        meetingModel.addEventListener(meetingModelListener);
        userModel = SdkUtil.getUserManager();
        userModel.addEventListener(userModelListener);
        taskUserAdapter = new TaskUserListAdapter(context);
        rvRoot.setLayoutManager(new LinearLayoutManager(context));
        rvRoot.setAdapter(taskUserAdapter);

        BaseUser localUser = userModel.getLocalUser();

        m_handler.removeCallbacks(m_OneSecondRunnable);
        m_handler.postDelayed(m_OneSecondRunnable, 1000);
    }

    public void initData() {
        if(null == channelCode) return;
        if (userInfoList != null) userInfoList.clear();
        getChannelByCode(channelCode);
    }

    private void initListener() {
        tvAddUser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivityForResult(new Intent(context, SelectUserActivity.class).putExtra(Constants.SharedPreKey.DEPTID,"").putExtra(Constants.SharedPreKey.GROUPNAME,""),Constants.SharedPreKey.REQUEST_CODE_2);
            }
        });
        taskUserAdapter.setClickListener(new TaskUserListAdapter.ItemClickEvent() {
            @Override
            public void clickCall(String mobile) {
            if (TextUtils.isEmpty(mobile)){
                ToastUtils.showLong("暂无联系方式！");
                return;
            }
                new PopupWindowToCall(context, new PopupWindowToCall.OnPopWindowClickListener() {
                    @Override
                    public void onPopWindowClickListener(View view) {
                        if (verifyPermissions()){
                            Call(mobile);
                        }
                    }
                },mobile).show();

            }

            @Override
            public void clickCallKickOut(String userId) {
                deleteChannelUser(channelCode, userId);
            }

            @Override
            public void clickVonue(long userId, boolean isVonue) {
                if (isVonue) {
                    setVenue(0);
                } else {
                    setVenue(userId);
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

        for (int i = 0; i < inGroupUsers.size(); i++) {
            Long fspId = inGroupUsers.get(i).getUserId();
            boolean isIn = false;
            for (int j = 0; j < userInfoList.size(); j++) {
                if (fspId.equals(userInfoList.get(j).getMdtUserId())) {
                    userInfoList.get(j).setIsOnline(1);
                    userInfoList.get(j).setBaseUser(inGroupUsers.get(i));
                    isIn = true;
                    break;
                }
            }
            if (isIn == false) {
                ChannerUser user = new ChannerUser();
                user.setMdtUserId(fspId);
                try {
                    user.setUserId(SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserName).getJSONObject(String.valueOf(fspId)).getLong("userId"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                user.setIsOnline(1);
                user.setBaseUser(inGroupUsers.get(i));
                userInfoList.add(user);
            }
        }

        if (userInfoList != null) {
            taskUserAdapter.setData(userInfoList);
        }
    }

    public void getChannelByCode(String channelCode){
        RequestUtils.getChannelByCode(channelCode, new MyObserver<ChannelBean>(context) {
            @Override
            public void onSuccess(ChannelBean result) {
                if (null == result) return;
                channelId = result.getId();
                userInfoList = result.getChannelUserList();
                refashChannelUser();
                taskUserAdapter.setMuteUserIds(result.getMuteUserIds());
                taskUserAdapter.setVenueId(String.valueOf(result.getVenue()));
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

    public void setVenue(long userId){
        RequestUtils.setVenue(channelId, userId, new MyObserver<Object>(context) {
            @Override
            public void onSuccess(Object result) {
                ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.MAIN_VENUE + userId);
                taskUserAdapter.setVenueId(String.valueOf(userId));
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
                ContactEnterUtils.getInstance(context).sendRefash();
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
            }
        });
    }

    public boolean verifyPermissions() {
        boolean hasPermission = true;
        if (!EasyPermissions.hasPermissions(context,
                Manifest.permission.CALL_PHONE)) {
            EasyPermissions.requestPermissions(context,
                    EasyPermissions.RC_CAMERA_PERM,
                    Manifest.permission.CALL_PHONE);
            hasPermission = false;
        }
        return hasPermission;
    }

    public void Call(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        context.startActivity(intent);
    }

}