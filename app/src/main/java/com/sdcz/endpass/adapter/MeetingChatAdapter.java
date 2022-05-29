package com.sdcz.endpass.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.util.SpanStringUtils;
import com.inpor.nativeapi.adaptor.ChatMsgInfo;
import com.inpor.nativeapi.interfaces.UserManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 聊天窗口的Adapter
 */
public class MeetingChatAdapter extends RecyclerView.Adapter<MeetingChatAdapter.ChatViewHolder> {


    private final ArrayList<ChatMsgInfo> chatMessageArray = new ArrayList<>();
    private static final int LEFT_ITEM = 1;
    private static final int RIGHT_ITEM = 2;
    private final SpannableStringBuilder spannableStringBuilder;
    private final Context context;
    private final long localUserId;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetingChatAdapter(Context context) {
        this.context = context;
        spannableStringBuilder = new SpannableStringBuilder();
        localUserId = SdkUtil.getUserManager().getLocalUser().getUserId();
    }

    /**
     * 设置聊天数据
     *
     * @param chatMessageCache 聊天数据
     */
    public void setChatMsgInfoDataArray(List<ChatMsgInfo> chatMessageCache) {
        this.chatMessageArray.addAll(chatMessageCache);
        notifyDataSetChanged();
    }

    public ArrayList<ChatMsgInfo> getChatMessageArray() {
        return chatMessageArray;
    }

    /**
     * 追加信息Item
     *
     * @param chatMsgInfo 聊天信息对象
     */
    public void appendChatMsgInfo(ChatMsgInfo chatMsgInfo) {
        chatMessageArray.add(chatMsgInfo);
        notifyItemInserted(chatMessageArray.size());
    }


    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ChatViewHolder chatViewHolder;
        if (viewType == LEFT_ITEM) {
            View leftItemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.meeting_chat_msg_left_item, parent, false);
            chatViewHolder = new ChatViewHolder(leftItemView);
        } else {
            View rightItemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.meeting_chat_msg_reight_item, parent, false);
            chatViewHolder = new ChatViewHolder(rightItemView);
        }
        return chatViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMsgInfo chatMsgInfo = chatMessageArray.get(position);
        byte[] msg = chatMsgInfo.msg;
        SpannableStringBuilder nicknameStyle = createNicknameStyle(chatMsgInfo);
        holder.nicknameTextView.setText(nicknameStyle);
        holder.msgContentView.setText(SpanStringUtils.parseEmotionString(context, new String(msg)));
        long dstUserId = chatMsgInfo.dstUserId;
        if (chatMsgInfo.srcUserId == localUserId) {
            holder.msgContentView.setBackgroundResource(R.drawable.select_chat_msg_bg_1);
        } else {
            if (dstUserId <= 0) {
                holder.msgContentView.setBackgroundResource(R.drawable.select_chat_msg_bg_2);
            } else {
                holder.msgContentView.setBackgroundResource(R.drawable.select_chat_msg_bg_3);
            }
        }


    }

    @Override
    public int getItemViewType(int position) {
        ChatMsgInfo chatMsgInfo = chatMessageArray.get(position);
        long sourceUserId = chatMsgInfo.srcUserId;
        if (sourceUserId == localUserId) {
            return RIGHT_ITEM;
        }
        return LEFT_ITEM;
    }

    @Override
    public int getItemCount() {
        return chatMessageArray.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameTextView;
        TextView msgContentView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.tv_name_time);
            msgContentView = itemView.findViewById(R.id.tv_chat_msg);
        }
    }


    /**
     * 构建昵称颜色样式
     *
     * @param chatMsgInfo 聊天信息对象
     * @return SpannableStringBuilder
     */
    private SpannableStringBuilder createNicknameStyle(ChatMsgInfo chatMsgInfo) {
        spannableStringBuilder.clear();
        String sourceDisplayName = getUserDisplayName(chatMsgInfo.srcUserId, chatMsgInfo.srcRealUser.displayName, 12);
        String desDisplayName = getUserDisplayName(chatMsgInfo.dstUserId, chatMsgInfo.dstRealUser.displayName, 12);
        long sourceUserId = chatMsgInfo.srcUserId;

        if (sourceUserId == localUserId) {
            spannableStringBuilder.append(chatMsgInfo.time);
            spannableStringBuilder.append(" ");
            spannableStringBuilder.append(context.getText(R.string.meetingui__chat_rescve_yu));
        } else {
            spannableStringBuilder.append(sourceDisplayName);
            spannableStringBuilder.append(context.getText(R.string.meetingui__chat_sendto));

        }


        if (TextUtils.isEmpty(desDisplayName)) {
            spannableStringBuilder.append(context.getText(R.string.meetingui_chat_send_all));
            spannableStringBuilder.append(context.getText(R.string.meetingui_chat_sendto_said));
            spannableStringBuilder.append(" ");
            if (sourceUserId != localUserId) {
                spannableStringBuilder.append(chatMsgInfo.time);
            }
        } else {

            if (chatMsgInfo.dstUserId == localUserId) {
                desDisplayName = context.getResources().getString(R.string.meetingui__chat_sendto_you);
            }
            SpannableString spannableString = new SpannableString(desDisplayName);
            spannableString.setSpan(
                    new ForegroundColorSpan(Color.parseColor("#3290F6")), 0, spannableString.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append(" ");
            spannableStringBuilder.append(spannableString);
            spannableStringBuilder.append(" ");
            spannableStringBuilder.append(context.getText(R.string.meetingui_chat_sendto_said));
            spannableStringBuilder.append(" ");
            if (sourceUserId != localUserId) {
                spannableStringBuilder.append(chatMsgInfo.time);
            }
        }

        return spannableStringBuilder;
    }


    /**
     * @param userId    用户id
     * @param spareName 备用名,这个显示名字不是首选，只有在数据库内无法读取对应用户信息时才会使用
     * @param length    保留字符串长度，中文默认少保留一位
     */
    private String getUserDisplayName(long userId, String spareName, int length) {
        String disPlayName = UserManager.getInstance().getUser(userId).strNickName;
        if (TextUtils.isEmpty(disPlayName)) {
            disPlayName = UserManager.getInstance().getUser(userId).strUserName;
        }
        if (TextUtils.isEmpty(disPlayName)) {
            disPlayName = spareName;
        }
        if (disPlayName == null) {
            return "";
        }
        if (disPlayName.length() >= length) {
            disPlayName = disPlayName.substring(0, length) + "... ";
        }
        return disPlayName;
    }
}
