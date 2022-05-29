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
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.Opcode;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.MeetingModelListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.HorLineItemDecoration;
import com.sdcz.endpass.adapter.OperatorAdapter;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.bean.OperatorItem;
import com.sdcz.endpass.callback.OnItemClickListener;
import com.sdcz.endpass.util.AttendeeUtils;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.nativeapi.adaptor.RolePermission;

import java.util.List;

/**
 * @Description: 管理员有权操作参会人的音视频，权限等
 */
public class OperatorAttendeeDialog extends DialogFragment implements OnItemClickListener, View.OnClickListener {

    private BaseUser attendee;
    private TextView tvUserName;
    private TextView tvCancel;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OperatorAdapter adapter;

    private UserManager userModel;
    private MeetingManager meetingModel;
    private boolean userLeaved;
    private int orientation = -1;

    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.AUDIO_STATE
                    | UserModelListenerImpl.VIDEO_STATE
                    | UserModelListenerImpl.MAIN_SPEAKER
                    | UserModelListenerImpl.USER_RIGHT, UserModelListenerImpl.ThreadMode.MAIN) {
        @Override
        public void onUserChanged(int type, BaseUser user) {
            // 1.不考虑本地用户的主持权限变更，本地用户没有权限操作，在后面操作时会提示；
            // 本地用户管理员状态变更，变更时应该显示或者隐藏“请出会议室”操作项
            if (UserModelListenerImpl.USER_RIGHT == type && user.isLocalUser()) {
                // 用户管理员权限变更，如果是从管理员状态变更则需要重新布局
                initOperationItem();
                return;
            }
            if (attendee.getUserId() != user.getUserId()) {
                return;
            }
            attendee = user;
            switch (type) {
                case UserModelListenerImpl.AUDIO_STATE:
                    //onAudioStateChanged(user);
                    break;
                case UserModelListenerImpl.VIDEO_STATE:
                    //onVideoStateChanged(user);
                    break;
                case UserModelListenerImpl.MAIN_SPEAKER:
                    onMainSpeakerStateChanged(user);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onBatchUserChanged(int type, BaseUser[] batchUsers) {
            for (BaseUser user : batchUsers) {
                onUserChanged(type, user);
            }
        }
    };

    private final MeetingModelListener meetingModelListener = new MeetingModelListener() {
        @Override
        public void onUserEnter(List<Long> userId) {
            if (!userLeaved) {
                return;
            }
            // 用户离开了又进来了
            userLeaved = false;
            for (Long id : userId) {
                if (attendee.getUserId() == id) {
                    attendee = userModel.getUserInfo(id);
                    break;
                }
            }
        }

        @Override
        public void onUserLeave(BaseUser user) {
            if (attendee != null && user != null && user.getUserId() == attendee.getUserId()) {
                userLeaved = true;
            }
        }

        @Override
        public void onUserKicked(long userId) {
            if (attendee != null && attendee.getUserId() == userId) {
                userLeaved = true;
            }
        }

        @Override
        public void onMainSpeakerChanged(BaseUser user) {
            if (user.getUserId() != attendee.getUserId()) {
                return;
            }
            attendee = user;
            onMainSpeakerStateChanged(user);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        boolean portrait = ScreenUtils.isPortrait();
        getDialog().getWindow().setGravity(portrait ? Gravity.BOTTOM : Gravity.BOTTOM | Gravity.RIGHT);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setWindowAnimations(R.style.bottom_dialog);
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
        meetingModel.removeEventListener(meetingModelListener);
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

    /**
     * show
     */
    public void show(@NonNull FragmentManager manager, BaseUser attendee) {
        this.attendee = attendee;
        initModel();
        super.show(manager, "dialog");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvCancel) {
            dismiss();
        }
    }

    @Override
    public <T> void onItemClick(RecyclerViewAdapter<T> viewAdapter, int position, View itemView) {
        BaseUser local = userModel.getLocalUser();
        if (!attendee.isLocalUser() && !local.isManager() && !local.isMainSpeakerDone()) {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
            return;
        }
        if (userLeaved) {
            ToastUtils.showShort(R.string.meetingui_user_leaved);
            dismiss();
            return;
        }
        adapter.getItem(position).getAction().run();
        dismiss();
    }

    private void initModel() {
        userModel = SdkUtil.getUserManager();
        meetingModel = SdkUtil.getMeetingManager();
        userModel.addEventListener(userModelListener);
        meetingModel.addEventListener(meetingModelListener);
    }

    /**
     * @Description:
     * @Author: xingwt
     * @return:
     * @Parame: attendee 当前item 用户
     * @Parame: local 本都用户
     * @CreateDate: 2021/4/12 11:49
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/4/12 11:49
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public int itemNum(BaseUser attendee, BaseUser local) {
        int num = 0;
        if ((attendee.isLocalUser() &&
                !SdkUtil.getPermissionManager().checkUserPermission(attendee.getUserId(), true,
                        RolePermission.DISABLE_MODIFY_SELF_USER_INFO))
                || local.isManager()) {
            num++;
        }
        if (!attendee.isLocalUser()) {
            num++;
        }
        if (!attendee.isLocalUser() && local.isManager()) {
            num++;
        }
        return num;
    }

    private void initOperationItem() {
        if (attendee == null) {
            dismiss();
            return;
        }
        int num = 0;
        adapter = new OperatorAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        BaseUser local = userModel.getLocalUser();
        if ((attendee.isLocalUser() &&
                !SdkUtil.getPermissionManager().checkUserPermission(attendee.getUserId(),true,
                        RolePermission.DISABLE_MODIFY_SELF_USER_INFO))
                || local.isManager()) {
            adapter.add(createNickname());
            num++;
        }
        if (!attendee.isLocalUser()) {
            adapter.add(createHostItem());
            num++;
        }
        if (!attendee.isLocalUser() && local.isManager()) {
            adapter.add(createKickItem());
            num++;
        }
        if (num < 1) {
            dismiss();
        } else {
            updateRecyclerviewHeight();
            layoutManager = new LinearLayoutManager(getContext());
            HorLineItemDecoration dec = new HorLineItemDecoration.Builder(getContext())
                    .showHeadLine(true)
                    .showBottomLine(true)
                    .height(SizeUtils.dp2px(1))
                    .lineColor(getContext().getResources().getColor(R.color.color_55383C4B))
                    .build();
            recyclerView.addItemDecoration(dec);
            recyclerView.setLayoutManager(layoutManager);
            adapter.notifyDataSetChanged();
        }
    }

    private OperatorItem createNickname() {
        return new OperatorItem(OperatorItem.NICKNAME, AttendeeUtils.getCameraStateLogo(attendee),
                R.string.meetingui_attendee_rename, () -> {
            SingleInputDialog.Builder builder = new SingleInputDialog.Builder();
            builder.title(getString(R.string.meetingui_attendee_rename))
                    .defaultHint(attendee.getNickName())
                    .interactionListener(new SingleInputDialog.InteractionListener() {
                        @Override
                        public void onLeftBtnClick(DialogFragment dialog) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onRightBtnClick(DialogFragment dialog, String input) {
                            int result = userModel.modifyUserNickname(attendee.getUserId(), input);
                            if (result == Opcode.SUCCESS) {
                                dialog.dismiss();
                            } else {
                                ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
                            }
                        }
                    })
                    .build()
                    .show(((FragmentActivity) getContext()).getSupportFragmentManager(), "input_dialog");
        });
    }

    private OperatorItem createHostItem() {
        return new OperatorItem(OperatorItem.HOST, AttendeeUtils.getHostStateLogo(attendee),
                AttendeeUtils.getHostStateDesc(attendee), () -> {
            // 1.先判断对方有没有权限成为主持人，没有则提示
            if (attendee.isLocalUser()) {
                if (attendee.isMainSpeakerNone()) {
                    userModel.applyToBeHost(true);
                } else if (attendee.isMainSpeakerWait()) {
                    userModel.applyToBeHost(false);
                } else {
                    userModel.abandonTheHost();
                }
                return;
            }
            int code;
            if (attendee.isMainSpeakerNone()) {
                code = userModel.grantTheHostForRemoteUser(attendee);
            } else if (attendee.isMainSpeakerWait()) {
                code = userModel.agreeApplyHost(attendee, true);
            } else {
                code = userModel.depriveTheHostForRemoteUser(attendee);
            }
            checkCode(code);
        });
    }

    private OperatorItem createKickItem() {
        return new OperatorItem(OperatorItem.KECK,
                R.mipmap.more_icon_admin,
                R.string.meetingui_kick_user, () -> {
            int code = meetingModel.kickUser(attendee.getUserId());
            checkCode(code);
        });
    }

    private void onMainSpeakerStateChanged(BaseUser user) {
        OperatorItem item = adapter.getOperatorItem(OperatorItem.HOST);
        // item.setLogo(AttendeeUtils.getHostStateLogo(user));
        item.setDesc(AttendeeUtils.getHostStateDesc(user));
        adapter.notifyItemChanged(adapter.getPosition(item));
    }

    private void checkCode(int code) {
        if (Opcode.NO_PERMISSION == code || Opcode.UNAVAILABLE == code) {
            ToastUtils.showShort(R.string.meetingui_permission_denied);
        }
    }

    private View createContentView(LayoutInflater inflater) {
        View content = inflater.inflate(R.layout.dialog_attendee_operator, null);
        tvUserName = content.findViewById(R.id.tvUserName);
        tvCancel = content.findViewById(R.id.tvCancel);
        tvCancel.setOnClickListener(this);
        recyclerView = content.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        initOperationItem();
        String nickName;
        if (attendee.isLocalUser()) {
            nickName = "(" + getString(R.string.meetingui_me) + ")" + attendee.getNickName();
        } else {
            nickName = attendee.getNickName();
        }
        tvUserName.setText(nickName);
        return content;
    }

    private void updateRecyclerviewHeight() {
        int count = adapter.getItemCount();
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (count == 1) {
            params.height = getContext().getResources().getDimensionPixelSize(R.dimen.dp49);
        } else if (count == 2) {
            params.height = getContext().getResources().getDimensionPixelSize(R.dimen.dp98);
        } else {
            params.height = getContext().getResources().getDimensionPixelSize(R.dimen.dp147);
        }
        recyclerView.setLayoutParams(params);
    }
}














