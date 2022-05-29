package com.sdcz.endpass.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.MeetingModelListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.DeviceAdapter;
import com.sdcz.endpass.adapter.HorLineItemDecoration;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.bean.DeviceItem;
import com.sdcz.endpass.callback.OnItemClickListener;
import com.sdcz.endpass.util.AttendeeUtils;
import com.inpor.base.sdk.audio.AudioManager;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.base.sdk.video.VideoManager;
import com.inpor.nativeapi.adaptor.VideoChannel;
import com.inpor.nativeapi.adaptor.VideoChannelManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: (参会人列表 - 参会人Item ， 多个音视频设备)音视频设备列表
 */
public class DeviceControlDialog extends DialogFragment implements
        OnItemClickListener, DeviceAdapter.ItemListener, View.OnClickListener {

    private int deviceType;
    private BaseUser attendee;

    private TextView tvUserName;
    private TextView tvCancel;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private DeviceAdapter adapter;
    private int orientation = -1;

    private UserManager userModel;
    private AudioManager audioModel;
    private VideoManager videoModel;
    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.AUDIO_STATE
                    | UserModelListenerImpl.VIDEO_STATE
                    | UserModelListenerImpl.MAIN_SPEAKER
                    | UserModelListenerImpl.USER_RIGHT, UserModelListenerImpl.ThreadMode.MAIN) {
        @Override
        public void onUserChanged(int type, BaseUser user) {
            if (attendee.getUserId() != user.getUserId()) {
                return;
            }
            attendee = user;
            switch (type) {
                case UserModelListenerImpl.AUDIO_STATE:
                case UserModelListenerImpl.VIDEO_STATE:
                    adapter.clear();
                    adapter.addAll(initDevice(attendee, deviceType));
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onBatchUserChanged(int type, BaseUser[] batchUsers) {
            for (BaseUser user : batchUsers) {
                if (user.getUserId() != attendee.getUserId()) {
                    continue;
                }
                onUserChanged(type, user);
                break;
            }
        }

    };

    private final MeetingModelListener meetingModelListener = new MeetingModelListener() {
        @Override
        public void onUserEnter(List<Long> userId) {
            // 暂不处理
        }

        @Override
        public void onUserLeave(BaseUser user) {
            // 暂不处理，直接退出
            if (user.getUserId() == attendee.getUserId()) {
                dismiss();
            }
        }

        @Override
        public void onUserKicked(long userId) {
            if (userId == attendee.getUserId()) {
                dismiss();
            }
        }

        @Override
        public void onMainSpeakerChanged(BaseUser user) {
            // nothing to do
        }
    };
    private MeetingManager meetingManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog == null) {
            super.onActivityCreated(savedInstanceState);
            return;
        }
        Window window = getDialog().getWindow();
        if (window == null) {
            super.onActivityCreated(savedInstanceState);
            return;
        }
        super.onActivityCreated(savedInstanceState);
        window.setBackgroundDrawable(new ColorDrawable(0x00000000));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        boolean portrait = ScreenUtils.isPortrait();
        window.setGravity(portrait ? Gravity.BOTTOM : Gravity.BOTTOM | Gravity.RIGHT);
        window.setWindowAnimations(R.style.bottom_dialog);
        dialog.setCanceledOnTouchOutside(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        userModel.removeEventListener(userModelListener);
        meetingManager.removeEventListener(meetingModelListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (attendee == null) {
            dismiss(); // BaseUser不是可序列化的，导致无法保存状态，如果遇到恢复状态的，直接关闭算了
        }
        return createContentView(inflater);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (orientation != newConfig.orientation) {
            orientation = newConfig.orientation;
            View view = createContentView(LayoutInflater.from(getContext()));
            getDialog().setContentView(view);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvCancel) {
            dismiss();
        }
    }

    @Override
    public <T> void onItemClick(RecyclerViewAdapter<T> viewAdapter, int position, View itemView) {

    }

    @Override
    public void onDeviceItemClick(int position, DeviceItem item) {
        if (DeviceItem.CAMERA == deviceType) {
            videoModel.broadcastVideo(attendee, item.getVideoChannel());
        } else if (DeviceItem.MICROPHONE == deviceType) {
            audioModel.broadcastAudio(attendee);
        }
    }

    /**
     * show
     */
    public void show(@NonNull FragmentManager manager, BaseUser attendee, int deviceType) {
        this.attendee = attendee;
        this.deviceType = deviceType;
        initModel();
        super.show(manager, "dialog");
    }

    private View createContentView(LayoutInflater inflater) {
        View content = inflater.inflate(R.layout.dialog_attendee_device, null);
        tvUserName = content.findViewById(R.id.tvUserName);
        tvCancel = content.findViewById(R.id.tvCancel);
        tvCancel.setOnClickListener(this);
        recyclerView = content.findViewById(R.id.recyclerView);
        initRecyclerView();
        tvUserName.setText(AttendeeUtils.getNickName(getContext(), attendee));
        return content;
    }

    private void initRecyclerView() {
        if (attendee == null) {
            return;
        }
        adapter = new DeviceAdapter(this);
        adapter.addAll(initDevice(attendee, deviceType));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        HorLineItemDecoration dec = new HorLineItemDecoration.Builder(getContext())
                .showHeadLine(true)
                .showBottomLine(true)
                .height(SizeUtils.dp2px(1))
                .lineColor(getContext().getResources().getColor(R.color.color_55383C4B))
                .build();
        recyclerView.addItemDecoration(dec);
        adapter.notifyDataSetChanged();
    }

    private void initModel() {
        meetingManager = SdkUtil.getMeetingManager();
        userModel = SdkUtil.getUserManager();
        audioModel = SdkUtil.getAudioManager();
        videoModel = SdkUtil.getVideoManager();
        userModel.addEventListener(userModelListener);
        meetingManager.addEventListener(meetingModelListener);
    }

    private List<DeviceItem> initDevice(BaseUser attendee, int deviceType) {
        ArrayList<DeviceItem> deviceItems = new ArrayList<>();
        if (DeviceItem.MICROPHONE == deviceType) {
            // 好像没有多个麦克风
            return deviceItems;
        }
        VideoChannelManager manager = attendee.getRoomUserInfo().vclmgr;
        if (manager == null) {
            return deviceItems;
        }
        ArrayList<VideoChannel> channels = manager.getChannelList();
        if (channels == null || channels.isEmpty()) {
            return deviceItems;
        }
        for (VideoChannel channel : channels) {
            deviceItems.add(new DeviceItem(channel));
        }
        return deviceItems;
    }
}
