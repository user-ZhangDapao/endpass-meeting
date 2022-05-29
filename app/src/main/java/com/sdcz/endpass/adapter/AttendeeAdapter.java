package com.sdcz.endpass.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.Utils;
import com.comix.meeting.entities.BaseUser;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.base.RecyclerViewHolder;
import com.sdcz.endpass.util.AttendeeUtils;
import com.sdcz.endpass.util.Terminal;

/**
 * @Description: 参会人列表Adapter
 */
public class AttendeeAdapter extends RecyclerViewAdapter<BaseUser> {

    public interface ItemListener {
        /**
         * 点击参会人的麦克风图标
         *
         * @param position position
         * @param item     参会人
         */
        void onMicClick(int position, BaseUser item);

        /**
         * 点击参会人的摄像头图标
         *
         * @param position position
         * @param item     参会人
         */
        void onCameraClick(int position, BaseUser item);

        /**
         * 点击授予主讲按钮
         *
         * @param position position
         * @param item     参会人
         */
        void onMainSpeakerClick(int position, BaseUser item);
    }

    private final ItemListener itemListener;

    public AttendeeAdapter(ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerViewHolder<BaseUser> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        view = inflater.inflate(R.layout.recycler_item_attendee, parent, false);
        AttendeeViewHolder holder = new AttendeeViewHolder(view, itemListener);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                int pos = holder.getLayoutPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                onItemClickListener.onItemClick(AttendeeAdapter.this, pos, holder.itemView);
            }
        });
        return holder;
    }

    /**
     * 通过ID获取用户，如果不存在则返回null
     */
    public BaseUser getUser(long userId) {
        for (BaseUser user : data) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    /**
     * 更新用户的信息(移除，插入，直接替换掉这个用户)
     *
     * @param user BaseUser
     */
    public synchronized void updateUserState(BaseUser user) {
        BaseUser temp = null;
        int tempIndex = 0;
        for (int index = 0; index < data.size(); index++) {
            temp = data.get(index);
            if (temp.getUserId() == user.getUserId()) {
                tempIndex = index;
                break;
            }
        }
        if (temp == null) {
            return;
        }
        data.remove(temp);
        if (data.isEmpty()) {
            data.add(user);
            //notifyItemChanged(0);
        } else if (!user.isLocalUser() && user.isMainSpeakerDone()) {
            data.add(1, user); // 主讲在本地用户之后
            //notifyItemChanged(1);
        } else {
            data.add(tempIndex, user);
            //notifyItemChanged(tempIndex);
        }
        notifyItemRangeChanged(0, data.size());
    }

    /**
     * 只是更新用户的音视频状态，扬声器状态信息，不会整个更新列表
     */
    public void updateUserStateOnly(BaseUser user) {
        BaseUser temp;
        int tempIndex = -1;
        for (int index = 0; index < data.size(); index++) {
            temp = data.get(index);
            if (temp.getUserId() == user.getUserId()) {
                tempIndex = index;
                break;
            }
        }
        if (tempIndex == -1) {
            return;
        }
        data.get(tempIndex).updateRoomInfo(user.getRoomUserInfo());
        if (data.isEmpty()) {
            notifyItemChanged(0);
        } else {
            notifyItemChanged(tempIndex);
        }
    }

    /**
     * 更新列表可见Item
     *
     * @param layoutManager LinearLayoutManager
     */
    public void updateVisibleUser(LinearLayoutManager layoutManager) {
        if (data.isEmpty()) {
            return;
        }
        int first = layoutManager.findFirstVisibleItemPosition();
        if (first < 0) {
            return;
        }
        int last = layoutManager.findLastVisibleItemPosition();
        if (last >= data.size()) {
            last = data.size() - 1;
        }
        notifyItemRangeChanged(first, last - first + 1);
    }

    public static class AttendeeViewHolder extends RecyclerViewHolder<BaseUser> implements View.OnClickListener {
        private final LinearLayout linearDetails;
        private final TextView tvUserName;
        private final ImageView imgDevice;
        private final TextView tvDetails;  // 显示：我，主持人，管理员
        private final ImageView imgMute;   // 显示是否有扬声器设备？
        private final ImageView imgHost;   // 显示正在申请主持人
        private final ImageView imgMic;    // 麦克风状态：正在发言，正在申请发言，没有发言，别禁止发言...
        private final ImageView imgCamera; // 视频状态：
        private final ItemListener itemListener;

        /**
         * 构造函数
         */
        public AttendeeViewHolder(View itemView, ItemListener itemListener) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            imgDevice = itemView.findViewById(R.id.imgDevice);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            imgMute = itemView.findViewById(R.id.imgMute);
            imgHost = itemView.findViewById(R.id.imgHost);
            imgMic = itemView.findViewById(R.id.imgMic);
            imgCamera = itemView.findViewById(R.id.imgCamera);
            linearDetails = itemView.findViewById(R.id.linearDetails);
            imgMic.setOnClickListener(this);
            imgCamera.setOnClickListener(this);
            imgHost.setOnClickListener(this);
            this.itemListener = itemListener;
        }

        @Override
        protected void onBindViewHolder(int position, BaseUser item) {
            tvUserName.setText(AttendeeUtils.getNickName(Utils.getApp(), item));
            imgDevice.setImageResource(Terminal.getLogo(item.getTerminalType()));
            String details = "";
            details = AttendeeUtils.getRoleDesc(Utils.getApp(), item);
            if (TextUtils.isEmpty(details)) {
                linearDetails.setVisibility(View.GONE);
            } else {
                linearDetails.setVisibility(View.VISIBLE);
                tvDetails.setText(details);
            }

            if (item.isMainSpeakerWait()) {
                imgHost.setVisibility(View.VISIBLE);
            } else {
                imgHost.setVisibility(View.GONE);
            }
            tvDetails.setTextColor(getColor(R.color.color_F0834D));
            bindAudioOutMute(item);
            bindMic(item);
            bindCamera(item);
        }

        @Override
        public void onClick(View view) {
            int pos = getLayoutPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            if (view.getId() == R.id.imgMic) {
                if (itemListener != null) {
                    itemListener.onMicClick(pos, item);
                }
            } else if (view.getId() == R.id.imgCamera) {
                if (itemListener != null) {
                    itemListener.onCameraClick(pos, item);
                }
            } else if (view.getId() == R.id.imgHost) {
                if (itemListener != null) {
                    itemListener.onMainSpeakerClick(position, item);
                }
            }
        }

        private int getColor(@ColorRes int id) {
            return Utils.getApp().getResources().getColor(id);
        }

        private void bindMic(BaseUser item) {
            // FIXME 禁用麦克风时，JNI层返回最后一个值，而不是0，导致无法显示禁用麦克风
            if (item.isAudioDeviceConnected()) {
                imgMic.setVisibility(View.VISIBLE);
                imgMic.setImageResource(AttendeeUtils.getMicStateLogo(item));
            } else {
                imgMic.setVisibility(View.GONE);
            }
        }

        private void bindCamera(BaseUser item) {
            boolean hasVideoDevice = item.isVideoDeviceConnected();
            if (!hasVideoDevice) {
                imgCamera.setVisibility(View.GONE);
            } else {
                imgCamera.setVisibility(View.VISIBLE);
                imgCamera.setImageResource(AttendeeUtils.getCameraStateLogo(item));
            }
        }

        private void bindAudioOutMute(BaseUser item) {
            if (item.isAudioOutMute()) {
                imgMute.setVisibility(View.VISIBLE);
            } else {
                imgMute.setVisibility(View.GONE);
            }
        }
    }
}
