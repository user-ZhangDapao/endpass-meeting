package com.sdcz.endpass.widget;

import android.Manifest;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.MeetingModelListener;
import com.comix.meeting.listeners.ShareModelListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.AttendeeAdapter;
import com.sdcz.endpass.adapter.HorLineItemDecoration;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.bean.AudioEventOnWrap;
import com.sdcz.endpass.bean.CameraEventOnWrap;
import com.sdcz.endpass.bean.DeviceItem;
import com.sdcz.endpass.callback.OnItemClickListener;
import com.sdcz.endpass.contract.AttendeeCategory;
import com.sdcz.endpass.contract.AttendeeContracts;
import com.sdcz.endpass.dialog.DeviceControlDialog;
import com.sdcz.endpass.dialog.OperatorAttendeeDialog;
import com.sdcz.endpass.model.AttendeeModel;
import com.sdcz.endpass.model.MicEnergyMonitor;
import com.sdcz.endpass.presenter.AttendeePresenter;
import com.sdcz.endpass.util.AttendeeComparator;
import com.sdcz.endpass.util.PermissionUtils;
import com.inpor.base.sdk.audio.AudioManager;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.permission.PermissionManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.base.sdk.video.VideoManager;
import com.inpor.nativeapi.adaptor.RolePermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * @Date: 2020/12/11
 * @Author: hugo
 * @Description: 参会人中的所有参会人，正在发言，正在申请广播音视频、成为主持人，离线参会人的四个列表
 */
public class AttendeeCategoryView extends FrameLayout implements OnItemClickListener,
        AttendeeContracts.IView, AttendeeAdapter.ItemListener, MicEnergyMonitor.AudioEnergyListener {
    private static final String TAG = "AttendeeCategoryView";
    private int category;
    private String channelCode;
    private RecyclerView recyclerView;
    private AttendeeAdapter attendeeAdapter;
    private LinearLayoutManager layoutManager;

    private AttendeePresenter presenter;
    private MeetingManager meetingModel;
    private UserManager userModel;
    private AudioManager audioModel;
    private VideoManager videoModel;

    private final MeetingModelListener meetingModelListener = new MeetingModelListener() {

        @Override
        public void onUserEnter(List<Long> userIds) {
            if (userIds == null || userIds.isEmpty()) {
                return;
            }
            switch (category) {
                case AttendeeCategory.ALL:
                    onUserEnterAll(userIds);
                    break;
                case AttendeeCategory.SPEAKING:
                    onUserEnterSpeaking(userIds);
                    break;
                case AttendeeCategory.REQUEST_SPEAKING:
                    onUserEnterRequestSpeaking(userIds);
                    break;
                case AttendeeCategory.OFFLINE:
                    onUserEnterOffline(userIds); // 当用户进入会议室时，需要把离线用户
                    break;
                default:
                    break;
            }
            post(() -> setSoundSource());
        }

        @Override
        public void onUserLeave(BaseUser user) {
            if (user == null) {
                return;
            }
            // 用户离开会议室不需要判断类别，直接移除掉即可
            if (category == AttendeeCategory.OFFLINE) {
                // 离线用户直接添加到列表
                // attendeeAdapter.add(user);
            } else {
                attendeeAdapter.remove(user);
            }
            attendeeAdapter.notifyDataSetChanged();
            if (sources.isEmpty()) {
                return;
            }
            BaseUser temp = null;
            for (BaseUser source : sources) {
                if (source.getUserId() == user.getUserId()) {
                    temp = source;
                    break;
                }
            }
            if (temp != null) {
                sources.remove(temp);
                StringBuffer group = new StringBuffer(MicEnergyMonitor.ATTENDEE_CATEGORY_VIEW);
                group.append(category);
                MicEnergyMonitor.getInstance().removeAudioSource(temp, group.toString());
            }
        }

        @Override
        public void onMainSpeakerChanged(BaseUser user) {
            if (AttendeeCategory.REQUEST_SPEAKING == category) {
                // 申请发言列表需要增删
                if (user.isSpeechWait() || user.isVideoWait() || user.isMainSpeakerWait()) {
                    if (!attendeeAdapter.getData().contains(user)) {
                        attendeeAdapter.add(user);
                    }
                } else {
                    attendeeAdapter.remove(user);
                }
                attendeeAdapter.notifyDataSetChanged();
            } else if (AttendeeCategory.OFFLINE == category) {
                // 离线列表不需要操作
            } else {
                attendeeAdapter.updateUserStateOnly(user);
            }
            sortList();
        }

        private void onUserEnterAll(List<Long> userIds) {
            BaseUser temp;
            for (Long userId : userIds) {
                temp = userModel.getUserInfo(userId);
                if (temp == null) {
                    continue;
                }
                attendeeAdapter.add(temp);
            }
            attendeeAdapter.notifyDataSetChanged();
            sortList();
        }

        private void onUserEnterSpeaking(List<Long> userIds) {
            BaseUser temp;
            boolean changed = false;
            for (Long userId : userIds) {
                temp = userModel.getUserInfo(userId);
                if (temp == null || !temp.isSpeechDone()) {
                    continue; // 不是正在发言的就不要管
                }
                if (!changed) {
                    changed = true;
                }
                attendeeAdapter.add(temp);
            }
            if (changed) {
                attendeeAdapter.notifyDataSetChanged();
                sortList();
            }
        }

        private void onUserEnterRequestSpeaking(List<Long> userIds) {
            BaseUser temp;
            boolean changed = false;
            for (Long userId : userIds) {
                temp = userModel.getUserInfo(userId);
                if (temp == null || !temp.isSpeechWait()
                        || !temp.isVideoWait() || !temp.isMainSpeakerWait()) {
                    continue;
                }
                if (!changed) {
                    changed = true;
                }
                attendeeAdapter.add(temp);
            }
            if (changed) {
                attendeeAdapter.notifyDataSetChanged();
                sortList();
            }
        }

        private void onUserEnterOffline(List<Long> userIds) {
            BaseUser temp;
            for (Long userId : userIds) {
                temp = attendeeAdapter.getUser(userId);
                if (temp == null) {
                    continue;
                }
                attendeeAdapter.remove(temp);
            }
            attendeeAdapter.notifyDataSetChanged();
            sortList();
        }
    };

    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.AVINFO_STATE
                    | UserModelListenerImpl.AUDIO_STATE
                    | UserModelListenerImpl.VIDEO_STATE
                    | UserModelListenerImpl.USER_RIGHT
                    | UserModelListenerImpl.USER_INFO
                    | UserModelListenerImpl.USER_OTHER_STATE,
            UserModelListenerImpl.ThreadMode.MAIN) {

        @Override
        public void onUserChanged(int type, BaseUser user) {
            if (user == null) {
                return;
            }
            switch (category) {
                case AttendeeCategory.ALL:
                    onUserChangedAll(type, user);
                    break;
                case AttendeeCategory.SPEAKING:
                    onUserChangedSpeaking(type, user);
                    break;
                case AttendeeCategory.REQUEST_SPEAKING:
                    onUserChangedRequestSpeaking(type, user);
                    break;
                case AttendeeCategory.OFFLINE:
                    // 离线用户没有什么需要处理的
                    break;
                default:
                    break;
            }
            if (UserModelListenerImpl.AUDIO_STATE == type) {
                setSoundSource();
            }
        }

        @Override
        public void onBatchUserChanged(int type, BaseUser[] batchUsers) {
            if (batchUsers == null || batchUsers.length == 0) {
                return;
            }
            for (BaseUser user : batchUsers) {
                onUserChanged(type, user);
            }
        }

        private void onUserChangedAll(int type, BaseUser user) {
            attendeeAdapter.updateUserStateOnly(user);
            if (type == UserModelListenerImpl.USER_INFO) {
                sortList();
            }
        }

        // 正在发言列表
        private void onUserChangedSpeaking(int type, BaseUser user) {
            if (UserModelListenerImpl.AUDIO_STATE == type) {
                if (user.isSpeechDone()) {
                    if (!attendeeAdapter.getData().contains(user)) {
                        attendeeAdapter.add(user);
                    }
                    sortList();
                } else {
                    attendeeAdapter.remove(attendeeAdapter.getUser(user.getUserId()));
                    attendeeAdapter.notifyDataSetChanged();
                }
            } else {
                attendeeAdapter.updateUserStateOnly(user);
                if (UserModelListenerImpl.MAIN_SPEAKER == type || UserModelListenerImpl.USER_INFO == type) {
                    // 如果是主持人变更则需要重排序
                    sortList();
                }
            }
        }

        // 举手发言列表
        private void onUserChangedRequestSpeaking(int type, BaseUser user) {
            if (UserModelListenerImpl.AUDIO_STATE == type
                    || UserModelListenerImpl.VIDEO_STATE == type
                    || UserModelListenerImpl.MAIN_SPEAKER == type) {
                if (user.isSpeechWait() || user.isVideoWait() || user.isMainSpeakerWait()) {
                    BaseUser temp = attendeeAdapter.getUser(user.getUserId());
                    if (temp == null) {
                        attendeeAdapter.add(user);
                    } else {
                        attendeeAdapter.updateUserStateOnly(user);
                    }
                    sortList();
                } else {
                    BaseUser temp = attendeeAdapter.getUser(user.getUserId());
                    if (temp != null) {
                        attendeeAdapter.remove(temp);
                        attendeeAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                attendeeAdapter.updateUserStateOnly(user);
            }
        }
    };

    private final ShareModelListener shareModelListener = new ShareModelListener() {
        @Override
        public void onShareTabChanged(int type, BaseShareBean shareBean) {
            attendeeAdapter.updateVisibleUser(layoutManager);
        }

        @Override
        public void onNoSharing() {
            attendeeAdapter.updateVisibleUser(layoutManager);
        }

        private void updateUserState(BaseShareBean shareBean) {
            if (shareBean == null) {
                return;
            }
            int first = layoutManager.findFirstVisibleItemPosition();
            if (first < 0) {
                return;
            }
            int last = layoutManager.findLastVisibleItemPosition();
            if (last > attendeeAdapter.getItemCount()) {
                last = attendeeAdapter.getItemCount();
            }
            BaseUser temp;
            for (int index = first; index < last; index++) {
                temp = attendeeAdapter.getItem(index);
                if (temp.getUserId() == shareBean.getUserId()) {
                    attendeeAdapter.updateUserStateOnly(temp);
                    break;
                }
            }
        }
    };

    public AttendeeCategoryView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public AttendeeCategoryView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AttendeeCategoryView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
        public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "AttendeeCategoryView#onAttachedToWindow()");
        AttendeeContracts.IModel model = new AttendeeModel();
        presenter = new AttendeePresenter();
        presenter.attachModel(model);
        presenter.attachView(this);
        presenter.queryUsers(category);
        meetingModel = SdkUtil.getMeetingManager();
        userModel = SdkUtil.getUserManager();
        audioModel = SdkUtil.getAudioManager();
        videoModel = SdkUtil.getVideoManager();
        meetingModel.addEventListener(meetingModelListener);
        userModel.addEventListener(userModelListener);
        SdkUtil.getWbShareManager().addWhiteBoardListener(shareModelListener);
        StringBuffer group = new StringBuffer(MicEnergyMonitor.ATTENDEE_CATEGORY_VIEW);
        group.append(category);
        MicEnergyMonitor.getInstance().addAudioEnergyListener(this, group.toString());
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "AttendeeCategoryView#onDetachedFromWindow()");
        presenter.detachView();
        meetingModel.removeEventListener(meetingModelListener);
        recyclerView.removeOnScrollListener(onScrollListener);
        StringBuffer group = new StringBuffer(MicEnergyMonitor.ATTENDEE_CATEGORY_VIEW);
        group.append(category);
        MicEnergyMonitor.getInstance().removeAudioEnergyListener(this, group.toString());
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        Log.i(TAG, "AttendeeCategoryView#onVisibilityChanged() visibility=" + visibility);
    }

    @Override
    public <T> void onItemClick(RecyclerViewAdapter<T> viewAdapter, int position, View itemView) {
        // 点击联系人的处理逻辑
        BaseUser local = userModel.getLocalUser();
        BaseUser operate = attendeeAdapter.getItem(position);
        if (!operate.isLocalUser() && !local.isManager() && !local.isMainSpeakerDone()) {
            return;
        }
        OperatorAttendeeDialog operator = new OperatorAttendeeDialog();
        Context context = getContext();
        if (!(context instanceof FragmentActivity)) {
            return;
        }
        if (operator.itemNum(operate, local) >= 1) {
            operator.show(((FragmentActivity) context).getSupportFragmentManager(), operate);
        }
    }

    @Override
    public void onUsersResult(int category, List<BaseUser> users) {
        if (users == null) {
            return;
        }
        attendeeAdapter.clear();
        attendeeAdapter.addAll(users);
        attendeeAdapter.notifyDataSetChanged();
        // 首次加载页面如果参会人不超过屏幕，则不会添加能量值跟踪(RecyclerView没有滑动)
        StringBuffer group = new StringBuffer(MicEnergyMonitor.ATTENDEE_CATEGORY_VIEW);
        group.append(this.category);
        MicEnergyMonitor.getInstance().removeAudioSourcesGroup(group.toString());
        sources.clear();
        int count = 0;
        BaseUser temp;
        for (int i = 0; i < attendeeAdapter.getItemCount(); i++) {
            if (count > 15) {
                break; // 最多添加10个(如果一屏超过15个则后面会显示不了)
            }
            temp = attendeeAdapter.getItem(i);
            if (temp == null || !temp.isSpeechDone()) {
                continue;
            }
            sources.add(temp);
            group = new StringBuffer(MicEnergyMonitor.ATTENDEE_CATEGORY_VIEW);
            group.append(this.category);
            MicEnergyMonitor.getInstance().addAudioSource(temp, group.toString());
            count++;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_camera_open(CameraEventOnWrap event_on) {

        if( event_on.flag == true )
        {
            BaseUser item = SdkUtil.getUserManager().getLocalUser();
            PermissionManager permissionManager = SdkUtil.getPermissionManager();
            boolean  hasPermission = permissionManager.checkUserPermission(item.getUserId(), false,
                    RolePermission.VIDEO_CAN_BE_BROADCASTED,
                    RolePermission.BROADCAST_OWN_VIDEO,
                    RolePermission.APPLY_BROADCAST_OWN_VIDEO);
            if (!hasPermission) {
                ToastUtils.showShort(R.string.meetingui_permission_denied);
            } else {
                if (item.getRoomUserInfo().vclmgr.getChannelCount() > 1) {
                    DeviceControlDialog dialog = new DeviceControlDialog();
                    if (!(getContext() instanceof FragmentActivity)) {
                        return;
                    }
                    dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(),
                            item, DeviceItem.CAMERA);
                    return;
                }
                videoModel.broadcastVideo(item);
            }
        }

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_audio_open(AudioEventOnWrap event_on) {

        if( event_on.flag == true )
        {
            BaseUser item = SdkUtil.getUserManager().getLocalUser();
            PermissionManager permissionManager = SdkUtil.getPermissionManager();
            boolean hasPermission = permissionManager.checkUserPermission(item.getUserId(), false,
                    RolePermission.AUDIO, RolePermission.AUDIO_CAN_BE_BROADCASTED,
                    RolePermission.BROADCAST_OWN_AUDIO, RolePermission.APPLY_BROADCAST_OWN_AUDIO);
            if (!hasPermission) {
                ToastUtils.showShort(R.string.meetingui_permission_denied);
            } else {
                audioModel.broadcastAudio(item);
            }
        }


        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onMicClick(int position, BaseUser item) {
        long userId = item.getUserId();
        PermissionManager permissionManager = SdkUtil.getPermissionManager();
        boolean hasPermission;
        if (item.isLocalUser()) {

            List<String> permissionList = PermissionUtils.requestMeetingPermission();
            if (permissionList != null && (permissionList.contains(Manifest.permission.RECORD_AUDIO))) {
                if( EventBus.getDefault().isRegistered(this) == false ) {
                    EventBus.getDefault().register(this);
                }
                ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO}, 61);
                return;
            }

            hasPermission = permissionManager.checkUserPermission(userId, false,
                    RolePermission.AUDIO, RolePermission.AUDIO_CAN_BE_BROADCASTED,
                    RolePermission.BROADCAST_OWN_AUDIO, RolePermission.APPLY_BROADCAST_OWN_AUDIO);
        } else {
            userId = SdkUtil.getUserManager().getLocalUser().getUserId();
            hasPermission = permissionManager.checkUserPermission(userId, true,
                    RolePermission.AUDIO, RolePermission.AUDIO_CAN_BE_BROADCASTED,
                    RolePermission.BROADCAST_OTHERS_AUDIO);
        }
        if (!hasPermission) {
            ToastUtils.showShort(R.string.meetingui_permission_denied);
        } else {
            audioModel.broadcastAudio(item);
        }
    }

    @Override
    public void onCameraClick(int position, BaseUser item) {
        long userId = item.getUserId();
        PermissionManager permissionManager = SdkUtil.getPermissionManager();
        boolean hasPermission;
        if (item.isLocalUser()) {


                List<String> permissionList = PermissionUtils.requestMeetingPermission();
                if (permissionList != null && (permissionList.contains(Manifest.permission.CAMERA))) {
                    if( EventBus.getDefault().isRegistered(this) == false ) {
                        EventBus.getDefault().register(this);
                    }
                    ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                            new String[]{Manifest.permission.CAMERA}, 60);
                    return;
                }

            hasPermission = permissionManager.checkUserPermission(userId, false,
                    RolePermission.VIDEO_CAN_BE_BROADCASTED,
                    RolePermission.BROADCAST_OWN_VIDEO,
                    RolePermission.APPLY_BROADCAST_OWN_VIDEO);




        } else {
            userId = userModel.getLocalUser().getUserId();
            hasPermission = permissionManager.checkUserPermission(userId, true,
                    RolePermission.VIDEO, RolePermission.VIDEO_CAN_BE_BROADCASTED,
                    RolePermission.BROADCAST_OTHERS_VIDEO);
        }
        if (!hasPermission) {
            ToastUtils.showShort(R.string.meetingui_permission_denied);
        } else {
            if (item.getRoomUserInfo().vclmgr.getChannelCount() > 1) {
                DeviceControlDialog dialog = new DeviceControlDialog();
                if (!(getContext() instanceof FragmentActivity)) {
                    return;
                }
                dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(),
                        attendeeAdapter.getItem(position), DeviceItem.CAMERA);
                return;
            }
            videoModel.broadcastVideo(item);
        }
    }

    @Override
    public void onMainSpeakerClick(int position, BaseUser item) {
        if (item.isLocalUser()) {
            return;
        }
        long userId = SdkUtil.getUserManager().getLocalUser().getUserId();
        boolean hasPermission = SdkUtil.getPermissionManager().checkUserPermission(userId, true,
                RolePermission.CONFIG_OTHERS_PRESENTER);
        if (hasPermission) {
            userModel.agreeApplyHost(item, true);
        } else {
            ToastUtils.showShort(R.string.meetingui_permission_denied);
        }
    }

    @Override
    public void onAudioEnergyChanged(List<BaseUser> users) {
        post(() -> {
            if (attendeeAdapter.getItemCount() < 1) {
                return;
            }
            for (BaseUser user : users) {
                if (!user.isSpeechDone() || !attendeeAdapter.getData().contains(user)) {
                    continue;
                }
                attendeeAdapter.updateUserStateOnly(user);
            }
        });
    }

    /**
     * 设置参会人列表类型
     *
     * @param category 参会人列表类型
     */
    public void setCategory(int category) {
        this.category = category;
        Log.i(TAG, "AttendeeCategoryView#setCategory()");
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_attendee_catogory, this);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        attendeeAdapter = new AttendeeAdapter(this);
        attendeeAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(attendeeAdapter);
        HorLineItemDecoration dec = new HorLineItemDecoration.Builder(context).showHeadLine(true).build();
        recyclerView.addItemDecoration(dec);
        recyclerView.addOnScrollListener(onScrollListener);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private final List<BaseUser> sources = new ArrayList<>();

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (RecyclerView.SCROLL_STATE_SETTLING != newState) {
                return;
            }
            setSoundSource();
        }

    };

    private void setSoundSource() {
        int first = layoutManager.findFirstVisibleItemPosition();
        if (first < 0 || first >= attendeeAdapter.getItemCount()) {
            Log.i(TAG, "setSoundSource 参数异常");
            return;
        }
        int end = layoutManager.findLastVisibleItemPosition();
        Log.i(TAG, String.format(Locale.CHINA, "观察能量值范围：first=%d end=%d", first, end));
        if (end >= attendeeAdapter.getItemCount()) {
            return;
        }
        StringBuffer group = new StringBuffer(MicEnergyMonitor.ATTENDEE_CATEGORY_VIEW);
        group.append(category);
        MicEnergyMonitor.getInstance().removeAudioSourcesGroup(group.toString());
        sources.clear();
        for (int i = first; i <= end; i++) {
            if (!attendeeAdapter.getItem(i).isSpeechDone()) {
                continue;
            }
            sources.add(attendeeAdapter.getItem(i));
            group = new StringBuffer(MicEnergyMonitor.ATTENDEE_CATEGORY_VIEW);
            group.append(category);
            MicEnergyMonitor.getInstance()
                    .addAudioSource(attendeeAdapter.getItem(i), group.toString());
        }
    }

    private void sortList() {
        if (attendeeAdapter == null || attendeeAdapter.getItemCount() > AttendeeComparator.MAX) {
            return;
        }
        try {
            AttendeeComparator comparator = new AttendeeComparator(attendeeAdapter.getItemCount());
            Collections.sort(attendeeAdapter.getData(), comparator);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            attendeeAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置任务code
     * @param channelCode code
     */
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
        Log.i(TAG, "AttendeeCategoryView#setChannelCode()");
    }

}











