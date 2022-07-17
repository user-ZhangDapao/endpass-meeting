package com.sdcz.endpass.adapter;

import static com.sdcz.endpass.util.AttendeeUtils.getMicStateLogo;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comix.meeting.entities.BaseUser;
import com.google.firebase.auth.UserInfo;
import com.inpor.nativeapi.adaptor.ChatMsgInfo;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.custommade.meetingover._manager._MeetingStateManager;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.AttendeeUtils;
import com.sdcz.endpass.util.ButtonDelayUtil;
import com.sdcz.endpass.util.GlideUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.widget.MarqueeTextView;

import org.json.JSONException;

import java.security.KeyStore;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Author: Administrator
 * CreateDate: 2021/7/6 9:22
 * Description: @任务视频界面适配器
 */
public class TaskUserListAdapter extends RecyclerView.Adapter{

    private List<ChannerUser> mData;
    private HashSet<Long> ids;
    private Context mContext;
    private ItemClickEvent mClick;

    public TaskUserListAdapter(Context mContext) {
        this.mContext = mContext;
    }

//    public TaskUserListAdapter(List<ChannerUser> mData, Context mContext) {
//        this.mData = mData;
//        this.mContext = mContext;
//    }

    public void setData(List<ChannerUser> mData) {
        Collections.sort(mData,new PersonComparator());
        this.mData = mData;
        notifyDataSetChanged();
    }

    class PersonComparator implements Comparator<ChannerUser> {
        @Override
        public int compare(ChannerUser p1, ChannerUser p2) {
            return p2.getIsOnline() - p1.getIsOnline();
        }
    }

    public void setMuteUserIds(List<Long> ids) {
        this.ids = new HashSet<>(ids);
        notifyDataSetChanged();
    }

    public void setVenueId(String id) {
        if (mData != null) {
            if (id.equals(0)) {
                for (int i = 0; i < mData.size(); i++) {
                    mData.get(i).setVenue(false);
                }
            } else {
                for (int i = 0; i < mData.size(); i++) {
                    mData.get(i).setVenue(false);
                    if (id.equals(String.valueOf(mData.get(i).getUserId()))) {
                        mData.get(i).setVenue(true);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    public void addMuteUserIds(Long id) {
        if (id == null) {
            return;
        }
        if (ids == null) {
            return;
        }
        ids.add(id);
        notifyDataSetChanged();
    }

    public void addAllMuteUserIds() {
        if (ids == null) {
            return;
        }
        for (ChannerUser channerUser : mData){
            ids.add(channerUser.getUserId());
        }

        notifyDataSetChanged();
    }

    public void removeMuteUserIds(Long id) {
        if (null == id) return;
        if (null == ids) return;

        if (ids.contains(id)){
            ids.remove(id);
        }else {
            return;
        }
        notifyDataSetChanged();
    }


    public void removeAllMuteUserIds() {
        ids.clear();
        notifyDataSetChanged();
    }


    public void setClickListener(ItemClickEvent mClick) {
        this.mClick = mClick;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_group_user, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        TaskUserListAdapter.ViewHolder viewHolder = (TaskUserListAdapter.ViewHolder) holder;
        if (payloads.isEmpty()){
            onBindViewHolder(viewHolder, position);
        }else {
            viewHolder.ivAudio.setImageResource(AttendeeUtils.getMicStateLogo(mData.get(position).getBaseUser()));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;

        ChannerUser user = mData.get(position);

        /**
         * 在离线状态
         */
        if (1 == mData.get(position).getIsOnline()) {
            viewHolder.ivIsOnline.setImageResource(R.drawable.icon_spot_green);
            viewHolder.ivVideo.setVisibility(View.VISIBLE);
            viewHolder.ivAudio.setVisibility(View.VISIBLE);
            viewHolder.ivCall.setVisibility(View.INVISIBLE);
            viewHolder.ivVenue.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivIsOnline.setImageResource(R.drawable.icon_spot_gray);
            viewHolder.ivVideo.setVisibility(View.INVISIBLE);
            viewHolder.ivAudio.setVisibility(View.INVISIBLE);
            viewHolder.ivCall.setVisibility(View.VISIBLE);
            viewHolder.ivVenue.setVisibility(View.INVISIBLE);
        }


        try {
            viewHolder.tvUserName.setText(SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserId).getJSONObject(String.valueOf(user.getUserId())).getString("nickName"));
        } catch (JSONException e) {
            viewHolder.tvUserName.setText(user.getNickName());
        }


        /**
         * 主会场
         */

        viewHolder.ivVenue.setImageResource(mData.get(position).isVenue() ? R.drawable.icon_venue_green2 : R.drawable.icon_venue_blue);


        /**
         * 麦克风
         */
        viewHolder.ivAudio.setImageResource(AttendeeUtils.getMicStateLogo(user.getBaseUser()));

        /**
         * 摄像头
         */
        viewHolder.ivVideo.setImageResource(AttendeeUtils.getCameraStateLogo(user.getBaseUser()));

        /**
         * 听筒
         */
        if (ids.contains(user.getUserId())) {
            viewHolder.ivListen.setImageResource(R.drawable.icon_off_listen);
        } else {
            viewHolder.ivListen.setImageResource(R.drawable.icon_open_listen);
        }

    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivIsOnline;
        ImageView ivHeadImg;
        ImageView ivAudio;
        ImageView ivVideo;
        ImageView ivCall;
        ImageView ivDelect;
        ImageView ivVenue;
        ImageView ivListen;
        MarqueeTextView tvUserName;

        public ViewHolder(View itemView) {
            super(itemView);

            ivIsOnline = itemView.findViewById(R.id.ivIsOnline);
            ivHeadImg = itemView.findViewById(R.id.iv_headImg);
            tvUserName = itemView.findViewById(R.id.group_user_item_tv_userid);
            ivAudio = itemView.findViewById(R.id.group_user_item_iv_audio);
            ivDelect = itemView.findViewById(R.id.ivDelect);
            ivVideo = itemView.findViewById(R.id.ivVideo);
            ivCall = itemView.findViewById(R.id.ivCall);

            ivVenue = itemView.findViewById(R.id.ivVenue);
            ivListen = itemView.findViewById(R.id.ivListen);

            if (mClick != null) {
                if (MobileMeetingActivity.isAdmin) {
                    ivCall.setOnClickListener(new View.OnClickListener() {  //给某人打电话
                        @Override
                        public void onClick(View v) {
                            if (ButtonDelayUtil.isFastClick()) {
                                if (mData.size() > getAdapterPosition()) {
                                    mClick.clickCall(mData.get(getAdapterPosition()).getPhonenumber());
                                }
                            }
                        }
                    });

                    ivAudio.setOnClickListener(new View.OnClickListener() { //关闭/打开某人麦克风
                        @Override
                        public void onClick(View v) {
                            if (ButtonDelayUtil.isFastClick()) {
                                if (mData.size() > getAdapterPosition()) {
                                    long id = (int) mData.get(getAdapterPosition()).getBaseUser().getUserId();
                                    if (AttendeeUtils.getMicState(mData.get(getAdapterPosition()).getBaseUser())) {
                                        ChatManager.getInstance().sendMessage(0,Constants.SharedPreKey.OFF_AUDIO + id);
                                    } else {
                                        ChatManager.getInstance().sendMessage(0,Constants.SharedPreKey.OPEN_AUDIO + id);
                                    }
                                }
                            }
                        }
                    });

                    ivVideo.setOnClickListener(new View.OnClickListener() { //关闭/打开某人摄像头
                        @Override
                        public void onClick(View v) {
                            if (ButtonDelayUtil.isFastClick()) {
                                if (mData.size() > getAdapterPosition()) {
                                    long id = (int) mData.get(getAdapterPosition()).getBaseUser().getUserId();
                                    if (AttendeeUtils.getCameraState(mData.get(getAdapterPosition()).getBaseUser())) {
                                        ChatManager.getInstance().sendMessage(0,Constants.SharedPreKey.OFF_VIDEO + id);
                                    } else {
                                        ChatManager.getInstance().sendMessage(0,Constants.SharedPreKey.OPEN_VIDEO + id);

                                    }
                                }
                            }
                        }
                    });

                    ivDelect.setOnClickListener(new View.OnClickListener() { //踢出某人
                        @Override
                        public void onClick(View v) {
                            if (ButtonDelayUtil.isFastClick()) {
                                if (mData.size() > getAdapterPosition()) {
                                    tvUserName.setMarqeeTrue();
                                    mClick.clickCallKickOut(mData.get(getAdapterPosition()).getUserId() + "");
                                    mData.remove(getAdapterPosition());
                                    //notifyDataSetChanged();
                                }
                            }
                        }
                    });

                    ivVenue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ButtonDelayUtil.isFastClick()) {
                                if (mData.size() > getAdapterPosition()) {
                                    mClick.clickVonue(mData.get(getAdapterPosition()).getUserId(), mData.get(getAdapterPosition()).isVenue());
                                }
                            }
                        }
                    });

                    ivListen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (ButtonDelayUtil.isFastClick()) {
                                int position = getAdapterPosition();
                                if (mData.size() > position) {
                                    long id = mData.get(position).getUserId();
                                    if (mData.get(position).getUserId() == SharedPrefsUtil.getUserId()) {
                                        return;
                                    }
                                    if (MobileMeetingActivity.isAdmin) {
                                        if (ids.contains(mData.get(position).getUserId())) {
                                            mClick.clickListen(mData.get(position).getUserId(),true);
                                        } else {
                                            mClick.clickListen(mData.get(position).getUserId(),false);
                                        }
                                    }
                                }
                            }
                        }
                    });

                }
            }
        }
    }

    public interface ItemClickEvent {
        void clickCall(String mobile);

        void clickCallKickOut(String userId);

        void clickVonue(long userId, boolean isVonue);

        void clickListen(long userId, boolean isListen);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void notifyItemChanged2() {
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i, R.id.group_user_item_iv_audio);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}