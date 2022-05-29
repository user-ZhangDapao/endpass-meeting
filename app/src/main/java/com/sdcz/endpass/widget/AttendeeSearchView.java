package com.sdcz.endpass.widget;

import android.Manifest;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.MeetingModelListener;
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
import com.sdcz.endpass.util.SoftKeyboardHelper;
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
 * @Date: 2020/12/18
 * @Author: hugo
 * @Description: 参会人搜索页面
 */
public class AttendeeSearchView extends FrameLayout implements View.OnClickListener, OnItemClickListener,
        AttendeeContracts.IView, AttendeeAdapter.ItemListener, MicEnergyMonitor.AudioEnergyListener {

    private static final String TAG = "AttendeeSearchView";
    private MeetingManager meetingManager;

    public interface InteractionListener {
        void onCancelSearch();
    }

    private LinearLayout linearSearch;
    private EditText edtSearch;
    private ImageView imgSearch2;
    private ImageView imgClear;
    private TextView tvCancel;
    private RecyclerView recyclerView;

    private LinearLayout linearSearchTips;
    private ImageView imgSearchTips;
    private TextView tvSearchTips;

    private LinearLayoutManager layoutManager;
    private AttendeeAdapter attendeeAdapter;
    private InteractionListener listener;

    private AttendeePresenter presenter;
    private UserManager userModel;
    private AudioManager audioModel;
    private VideoManager videoModel;
    @AttendeeCategory
    int category;

    public void setListener(InteractionListener listener) {
        this.listener = listener;
    }

    private final MeetingModelListener meetingModelListener = new MeetingModelListener() {

        @Override
        public void onUserEnter(List<Long> userIds) {
            if (userIds == null || userIds.isEmpty()) {
                return;
            }
            onUserEnterAll(userIds);
        }

        @Override
        public void onUserLeave(BaseUser user) {
            if (user == null || !attendeeAdapter.contains(user)) {
                return;
            }
            attendeeAdapter.remove(user);
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
                MicEnergyMonitor.getInstance().removeAudioSource(temp, MicEnergyMonitor.ATTENDEE_VIEW);
            }
        }

        @Override
        public void onMainSpeakerChanged(BaseUser user) {
            if (user == null || !attendeeAdapter.contains(user)) {
                return;
            }
            attendeeAdapter.updateUserState(user);
        }

        private void onUserEnterAll(List<Long> userIds) {
            BaseUser temp;
            int count = 0;
            String key = edtSearch.getText().toString().toLowerCase();
            if (TextUtils.isEmpty(key)) {
                return;
            }
            for (Long userId : userIds) {
                temp = userModel.getUserInfo(userId);
                if (temp == null) {
                    continue;
                }
                String nickName = temp.getNickName().toLowerCase();
                if (nickName.contains(key)) {
                    attendeeAdapter.add(temp);
                    count++;
                }
            }
            if (count > 0) {
                sortList();
            }
        }
    };

    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.AUDIO_STATE
                    | UserModelListenerImpl.VIDEO_STATE
                    | UserModelListenerImpl.USER_OTHER_STATE
                    | UserModelListenerImpl.USER_RIGHT,
            UserModelListenerImpl.ThreadMode.MAIN) {

        @Override
        public void onUserChanged(int type, BaseUser user) {
            if (user == null) {
                return;
            }
            if (UserModelListenerImpl.VIDEO_STATE == type
                    || UserModelListenerImpl.AUDIO_STATE == type
                    || UserModelListenerImpl.USER_RIGHT == type
                    || UserModelListenerImpl.USER_OTHER_STATE == type) {
                attendeeAdapter.updateUserStateOnly(user);
            } else {
                attendeeAdapter.updateUserState(user);
            }
            if (UserModelListenerImpl.AUDIO_STATE == type) {
                addScrollSoundEnergySource();
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
    };

    public AttendeeSearchView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public AttendeeSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AttendeeSearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            presenter.detachView();
            userModel.removeEventListener(userModelListener);
            meetingManager.removeEventListener(meetingModelListener);
            MicEnergyMonitor.getInstance().removeAudioSourcesGroup(MicEnergyMonitor.ATTENDEE_VIEW);
        }else {
            AttendeeContracts.IModel model = new AttendeeModel();
            presenter = new AttendeePresenter();
            presenter.attachModel(model);
            presenter.attachView(this);
            meetingManager = SdkUtil.getMeetingManager();
            userModel =  SdkUtil.getUserManager();
            audioModel = SdkUtil.getAudioManager();
            videoModel = SdkUtil.getVideoManager();
            userModel.addEventListener(userModelListener);
            meetingManager.addEventListener(meetingModelListener);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.imgSearch2) {
            doSearch(edtSearch.getText().toString());
        } else if (id == R.id.tvCancel) {
            edtSearch.setText("");
            SoftKeyboardHelper helper = new SoftKeyboardHelper(getContext());
            helper.hideSoftKeyboard(edtSearch);
            if (listener != null) {
                listener.onCancelSearch();
            }
        } else if (id == R.id.imgClear) {
            edtSearch.setText("");
        }
    }

    @Override
    public <T> void onItemClick(RecyclerViewAdapter<T> viewAdapter, int position, View itemView) {
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
    public void onUsersResult(String key, List<BaseUser> users) {
        attendeeAdapter.clear();
        attendeeAdapter.addAll(users);
        attendeeAdapter.notifyDataSetChanged();
        addSoundEnergySource();
        if (users.isEmpty()) {
            linearSearchTips.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            linearSearchTips.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_camera_open(CameraEventOnWrap event_on) {

        if( event_on.flag == true )
        {
            BaseUser item = SdkUtil.getUserManager().getLocalUser();
            PermissionManager permissionManager = SdkUtil.getPermissionManager();
            boolean hasPermission = permissionManager.checkUserPermission(item.getUserId(), false,
                    RolePermission.VIDEO_CAN_BE_BROADCASTED,
                    RolePermission.BROADCAST_OWN_VIDEO,
                    RolePermission.APPLY_BROADCAST_OWN_VIDEO);
            if (!hasPermission) {
                ToastUtils.showShort(R.string.meetingui_permission_denied);
            } else {
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
            if (item.getRoomUserInfo().vclmgr.getChannelCount() > 1) {
                DeviceControlDialog dialog = new DeviceControlDialog();
                if (!(getContext() instanceof FragmentActivity)) {
                    return;
                }
                dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(),
                        attendeeAdapter.getItem(position), DeviceItem.CAMERA);
                return;
            }
        }
        if (!hasPermission) {
            ToastUtils.showShort(R.string.meetingui_permission_denied);
        } else {
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
     * 点击搜索，显示搜索View
     */
    public void showSearchAttendeeView(@AttendeeCategory int category) {
        this.category = category;
        edtSearch.requestFocus();
        SoftKeyboardHelper helper = new SoftKeyboardHelper(getContext());
        helper.showSoftKeyboard(edtSearch);
    }

    /**
     * 关闭搜索View
     */
    public void hideSearchAttendeeView() {
        removeSoundEnergySource();
    }

    /**
     * 移除正在被监听声音能量值的用户
     */
    private void removeSoundEnergySource() {
        MicEnergyMonitor.getInstance().removeAudioSourcesGroup(MicEnergyMonitor.ATTENDEE_VIEW);
    }


    /**
     * @Description: 每次查询后加载 都是首次加载
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/19 17:20
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/19 17:20
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    private void addSoundEnergySource() {
        // 首次加载页面如果参会人不超过屏幕，则不会添加能量值跟踪(RecyclerView没有滑动)
        MicEnergyMonitor.getInstance().removeAudioSourcesGroup(MicEnergyMonitor.ATTENDEE_VIEW);
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
            MicEnergyMonitor.getInstance().addAudioSource(temp, MicEnergyMonitor.ATTENDEE_VIEW);
            count++;
        }
    }

    /**
     * 滚动或者跟新添加列表可见用户到声音能量值监听(从新删除所有 添加当前界面的)
     */
    private void addScrollSoundEnergySource() {
        int first = layoutManager.findFirstVisibleItemPosition();
        if (first < 0 || first >= attendeeAdapter.getItemCount()) {
            Log.i(TAG, "setSoundSource 参数异常" + first);
            return;
        }
        int end = layoutManager.findLastVisibleItemPosition();
        Log.i(TAG, String.format(Locale.CHINA, "观察能量值范围：first=%d end=%d", first, end));
        if (end >= attendeeAdapter.getItemCount()) {
            Log.i(TAG, "return");
            return;
        }
        MicEnergyMonitor.getInstance().removeAudioSourcesGroup(MicEnergyMonitor.ATTENDEE_VIEW);
        sources.clear();
        for (int i = first; i <= end; i++) {
            if (!attendeeAdapter.getItem(i).isSpeechDone()) {
                continue;
            }
            sources.add(attendeeAdapter.getItem(i));
            MicEnergyMonitor.getInstance().addAudioSource(attendeeAdapter.getItem(i), MicEnergyMonitor.ATTENDEE_VIEW);
        }
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_attendee_search, this);
        linearSearch = findViewById(R.id.linearSearch);
        edtSearch = findViewById(R.id.edtSearch);
        imgSearch2 = findViewById(R.id.imgSearch2);
        imgClear = findViewById(R.id.imgClear);
        tvCancel = findViewById(R.id.tvCancel);
        imgSearch2.setOnClickListener(this);
        imgClear.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        edtSearch.addTextChangedListener(searchTextWatcher);
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

        linearSearchTips = findViewById(R.id.linearSearchTips);
        imgSearchTips = findViewById(R.id.imgSearchTips);
        tvSearchTips = findViewById(R.id.tvSearchTips);
    }

    private final List<BaseUser> sources = new ArrayList<>();

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (RecyclerView.SCROLL_STATE_SETTLING != newState) {
                return;
            }
            addScrollSoundEnergySource();
        }
    };

    private final TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence sequence, int start, int before, int count) {
            String key = sequence.toString();
            if (key.isEmpty()) {
                attendeeAdapter.clear();
                attendeeAdapter.notifyDataSetChanged();
                return;
            }
            doSearch(key);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void doSearch(String keyword) {
        if (presenter == null) {
            return;
        }
        presenter.queryUsers(category, keyword);
    }

    private void sortList() {
        if (attendeeAdapter == null || attendeeAdapter.getItemCount() > AttendeeComparator.MAX) {
            return;
        }
        AttendeeComparator comparator = new AttendeeComparator(attendeeAdapter.getItemCount());
        Collections.sort(attendeeAdapter.getData(), comparator);
        attendeeAdapter.notifyDataSetChanged();
    }
}
