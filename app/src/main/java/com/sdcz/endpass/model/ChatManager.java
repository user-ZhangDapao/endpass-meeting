package com.sdcz.endpass.model;

import com.comix.meeting.listeners.ChatModelListener;
import com.sdcz.endpass.SdkUtil;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.nativeapi.adaptor.ChatMsgInfo;

import java.util.List;

/**
 * @Description     聊天管理类
 */
public class ChatManager implements ChatModelListener {
    private static volatile ChatManager chatManager;
    private com.inpor.base.sdk.chat.ChatManager chatModel;
    private ChatMessageListener chatMessageListener;
    private long desUserId = 0L;
    private long sourceUserId = 0L;
    private UnReadMsgUpdateListener unReadMsgUpdateListener;

    /**
     * 获取 ChatManager 实例
     *
     * @return ChatManager
     */
    public static ChatManager getInstance() {
        if (chatManager == null) {
            synchronized (ChatManager.class) {
                if (chatManager == null) {
                    chatManager = new ChatManager();
                }
            }
        }
        return chatManager;
    }

    private ChatManager() {
        init();
    }

    /**
     * 初始参数
     */
    public void init() {
        chatModel = SdkUtil.getChatManager();
        chatModel.addEventListener(this);
        UserManager userModel = SdkUtil.getUserManager();
        sourceUserId = userModel.getLocalUser().getUserId();
    }

    @Override
    public void onChatMessage(ChatMsgInfo message) {
        if (message == null) {
            return;
        }
        if (chatManager.chatMessageListener != null) {
            chatManager.chatMessageListener.onChatMessage(message);
            chatManager.chatModel.getUnReadMsg().clear();
        }

        if (chatManager.unReadMsgUpdateListener != null) {
            List<ChatMsgInfo> unReadMsg = chatManager.chatModel.getUnReadMsg();
            int size = 0;
            if (unReadMsg != null) {
                size = unReadMsg.size();
            }
            chatManager.unReadMsgUpdateListener.onUnReadMsgUpdateListener(size);
        }

    }

    @Override
    public void onChatPermissionChanged() {

    }

    public void updateUnReadMsgNum() {
        if (chatManager.chatMessageListener != null) {
            chatManager.unReadMsgUpdateListener.onUnReadMsgUpdateListener(getUnReadMsgNumber());
        }
    }

    public int sendMessage(long dstUserId, String msg) {
        checkNullValue();
        return chatManager.chatModel.sendChatMessage(dstUserId, msg);
    }

    public List<ChatMsgInfo> getAllMsg() {
        checkNullValue();
        return chatManager.chatModel.getCacheChatMsg();
    }

    public long getDesUserId() {
        return chatManager.desUserId;
    }

    public void setDesUserId(long desUserId) {
        chatManager.desUserId = desUserId;
    }

    public long getSourceUserId() {
        return chatManager.sourceUserId;
    }

    public void setSourceUserId(long sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public void setSourceAndDesUserId(long sourceUserId, long desUserId) {
        chatManager.sourceUserId = sourceUserId;
        chatManager.desUserId = desUserId;
    }


    public int getUnReadMsgNumber() {
        return chatManager.chatModel.getUnReadMsgCount();
    }

    /**
     * 判空
     */
    private void checkNullValue() {
        if (chatManager.chatModel == null) {
            throw new NullPointerException("chatModel is null,Please call # init() # initialize");
        }
    }

    /**
     * 资源回收
     */
    public void recycle() {
        if (chatManager.chatModel == null) {
            return;
        }
        chatManager.chatMessageListener = null;
        chatManager.chatModel.removeEventListener(this);
        this.chatMessageListener = null;
        chatManager.chatModel = null;
        chatManager = null;
    }


    public interface ChatMessageListener {
        /**
         * 接收到信息后回调给视图展示
         *
         * @param message 聊天数据
         */
        void onChatMessage(ChatMsgInfo message);
    }


    /**
     * 设置聊天信息事件监听
     *
     * @param chatMessageListener 监听接口
     */
    public void setChatMessageListener(ChatMessageListener chatMessageListener) {
        this.chatMessageListener = chatMessageListener;
    }

    /**
     * 是否拥有公聊权限
     *
     * @return true 有，反之则无
     */
    public boolean hasPubChatPermission() {
        return chatModel.hasPubChatPermission();
    }

    /**
     * 未读信息增加实时监听接口
     */
    public interface UnReadMsgUpdateListener {
        void onUnReadMsgUpdateListener(int number);
    }


    public void setUnReadMsgUpdateListener(UnReadMsgUpdateListener unReadMsgUpdateListener) {
        this.unReadMsgUpdateListener = unReadMsgUpdateListener;
    }
}
