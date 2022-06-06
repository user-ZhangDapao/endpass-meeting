package com.sdcz.endpass.adapter;

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
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.ui.MobileMeetingActivity;
import com.sdcz.endpass.util.AttendeeUtils;
import com.sdcz.endpass.util.ButtonDelayUtil;
import com.sdcz.endpass.util.GlideUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.widget.MarqueeTextView;

import org.json.JSONException;

import java.security.KeyStore;
import java.util.HashSet;
import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/7/6 9:22
 * Description: @任务视频界面适配器
 */
public class TaskUserListAdapter extends RecyclerView.Adapter {

    private List<ChannerUser> mData;
//    private HashSet<String> ids;
    private Context mContext;
    private ItemClickEvent mClick;

    public TaskUserListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public TaskUserListAdapter(List<ChannerUser> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    public void setData(List<ChannerUser> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    public void setMuteUserIds(HashSet<String> ids) {
//        this.ids = ids;
        notifyDataSetChanged();
    }

//    public HashSet<String> getMuteUserIds() {
//        return ids;
//    }

    public void addMuteUserIds(String id) {
        if (id == null) {
            return;
        }
//        if (ids == null) {
//            return;
//        }
//        ids.add(id);
        notifyDataSetChanged();
    }

//    public void addAllMuteUserIds(String id) {
//        if (ids == null) {
//            return;
//        }
//        ids.addAll(FspManager.getInstance().getGroupUsers());
//        if (ids.contains(id)){
//            ids.remove(id);
//        }
//        notifyDataSetChanged();
//    }
//
//    public void removeMuteUserIds(String id) {
//        ids.remove(id);
//        notifyDataSetChanged();
//    }

//    public void removeAllMuteUserIds(String id) {
//        if (ids.contains(id)){
//            ids.clear();
//            ids.add(id);
//        }else {
//            ids.clear();
//        }
//        notifyDataSetChanged();
//    }

    public void setClickListener(ItemClickEvent mClick) {
        this.mClick = mClick;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        View view = null;
//
//        if (style) {
           View view = LayoutInflater.from(mContext).inflate(R.layout.item_group_user, parent, false);
//        } else {
//            view = LayoutInflater.from(mContext).inflate(R.layout.item_group_user_false, parent, false);
//        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;

        ChannerUser user = mData.get(position);

//        if (userId.equals(SharedPrefsUtil.getValue(mContext, KeyStore.USERID, null))){
//            viewHolder.ivDelect.setVisibility(View.INVISIBLE);
//        }


//        viewHolder.pbVioce.setProgress(null == user.getBaseUser() ? 0 : user.getBaseUser().getVideoManager().ge);

        /**
         * 在离线状态
         */
        if (null == mData.get(position).getBaseUser()) {
            viewHolder.ivIsOnline.setImageResource(R.drawable.icon_spot_gray);
            viewHolder.ivVideo.setVisibility(View.INVISIBLE);
            viewHolder.ivAudio.setVisibility(View.INVISIBLE);
            viewHolder.ivCall.setVisibility(View.VISIBLE);
            viewHolder.ivVenue.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.ivIsOnline.setImageResource(R.drawable.icon_spot_green);
            viewHolder.ivVideo.setVisibility(View.VISIBLE);
            viewHolder.ivAudio.setVisibility(View.VISIBLE);
            viewHolder.ivCall.setVisibility(View.INVISIBLE);
            viewHolder.ivVenue.setVisibility(View.VISIBLE);
        }


        viewHolder.tvUserName.setText(user.getNickName());


        /**
         * 主会场
         */

//        if (mData.get(position).getIsVenue()) {
//            viewHolder.ivVenue.setImageResource(style ? R.drawable.icon_venue_blue : R.drawable.icon_venue_green2);
//        } else {
//            viewHolder.ivVenue.setImageResource(style ? R.drawable.icon_venue_gray : R.drawable.icon_venue_gray2);
//        }
//
//        /**
//         * 名字 和 头像
//         */
//        try {
//            viewHolder.tvUserName.setText(SharedPrefsUtil.getJSONValue(mContext).getJSONObject(userId).get("realname").toString());
//            if (SharedPrefsUtil.getJSONValue(mContext).getJSONObject(userId).has("headImg")) {
//                GlideUtils.showCircleImage(mContext, viewHolder.ivHeadImg, SharedPrefsUtil.getJSONValue(mContext).getJSONObject(userId).get("headImg").toString(), R.drawable.icon_head);
//            } else {
//                viewHolder.ivHeadImg.setImageResource(R.drawable.icon_head);
//            }
//        } catch (JSONException e) {
////            //e.printStackTrace();
//            viewHolder.ivHeadImg.setImageResource(R.drawable.icon_head);
//        }

        //---------------------------------

        /**
         * 麦克风
         */
        viewHolder.ivAudio.setImageResource(AttendeeUtils.getMicStateLogo(user.getBaseUser()));

        /**
         * 摄像头
         */
        viewHolder.ivVideo.setImageResource(AttendeeUtils.getCameraStateLogo(user.getBaseUser()));
//        if (FspManager.getInstance().HaveUserVideo(userId)) {
//            viewHolder.ivVideo.setImageResource(style ? R.drawable.group_user_video_open : R.drawable.group_user_video_open2);
//            viewHolder.ivVenue.setVisibility(View.VISIBLE);
//        } else {
//            viewHolder.ivVideo.setImageResource(style ? R.drawable.group_user_video_close : R.drawable.group_user_video_close2);
//            viewHolder.ivVenue.setVisibility(View.INVISIBLE);
//        }
//        if (userId.equals(SharedPrefsUtil.getValue(mContext, KeyStore.USERID, null))) {
//            if (FspManager.getInstance().isVideoPublishing()) {
//                viewHolder.ivVideo.setImageResource(style ? R.drawable.group_user_video_open : R.drawable.group_user_video_open2);
//                viewHolder.ivVenue.setVisibility(View.VISIBLE);
//            } else {
//                viewHolder.ivVideo.setImageResource(style ? R.drawable.group_user_video_close : R.drawable.group_user_video_close2);
//                viewHolder.ivVenue.setVisibility(View.INVISIBLE);
//            }
//        }

//        /**
//         * 主会场
//         */
//
//        if (!VideoActivity.getUserRole()) {
//            viewHolder.ivDelect.setVisibility(View.INVISIBLE);
//            viewHolder.ivCall.setVisibility(View.INVISIBLE);
//            viewHolder.ivVenue.setVisibility(View.INVISIBLE);
//        }

//        /**
//         * 听筒
//         */
//        if (ids.contains(userId)) {
//            viewHolder.ivListen.setImageResource(style ? R.drawable.icon_off_listen : R.drawable.icon_off_listen2);
//        } else {
//            viewHolder.ivListen.setImageResource(style ? R.drawable.icon_open_listen : R.drawable.icon_open_listen2);
//        }

    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivIsOnline;
        ProgressBar pbVioce;
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
                                        int a = ChatManager.getInstance().sendMessage(0,Constants.SharedPreKey.OFF_VIDEO + id);
                                        Log.d("=====asdas=====","send msg=====" + a);
                                    } else {
                                        int a = ChatManager.getInstance().sendMessage(0,Constants.SharedPreKey.OPEN_VIDEO + id);
                                        Log.d("=====asdas=====","send msg=====" + a);
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
                                }
                            }
                        }
                    });

//                    ivVenue.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (ButtonDelayUtil.isFastClick()) {
//                                if (mData.size() > getAdapterPosition()) {
//                                    mClick.clickVonue(mData.get(getAdapterPosition()).getUserId(), mData.get(getAdapterPosition()).getIsVenue());
//                                }
//                            }
//                        }
//                    });

                    ivListen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

//                            if (ButtonDelayUtil.isFastClick()) {
//                                if (mData.size() > getAdapterPosition()) {
//                                    int id = mData.get(getAdapterPosition()).getUserId();
//                                    if (mData.get(getAdapterPosition()).getUserId() == SharedPrefsUtil.getUserId()) {
//                                        return;
//                                    }
//                                    if (MobileMeetingActivity.isAdmin) {
//                                        if (mData.get(getAdapterPosition()).getVoice() == 0) {
//                                            FspManager.getInstance().sendGroupMsg(Constants.OPEN_LISEN + "-" + id);
//                                            removeMuteUserIds(id);
//                                        } else {
//                                            FspManager.getInstance().sendGroupMsg(Constants.OFF_LISEN + "-" + id);
//                                            addMuteUserIds(id);
//                                        }
//                                    }
//                                }
//                            }
                        }
                    });

                }
            }
        }
    }

    public interface ItemClickEvent {
        void clickCall(String mobile);

        void clickCallKickOut(String userId);

        void clickVonue(String userId, boolean isVonue);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

//    public void setVonueState(String id) {
//        if (mData != null) {
//            if (id.equals("0")) {
//                for (int i = 0; i < mData.size(); i++) {
//                    mData.get(i).setIsVenue(false);
//                }
//            } else {
//                for (int i = 0; i < mData.size(); i++) {
//                    mData.get(i).setIsVenue(false);
//                    if (id.equals(mData.get(i).getUserId())) {
//                        mData.get(i).setIsVenue(true);
//                    }
//                }
//            }
//            notifyDataSetChanged();
//        }
//    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}