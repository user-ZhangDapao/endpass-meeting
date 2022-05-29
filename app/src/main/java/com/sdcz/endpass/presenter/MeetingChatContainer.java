package com.sdcz.endpass.presenter;

import android.content.Context;
import android.util.Log;

import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseContainer;
import com.sdcz.endpass.widget.MeetingChatView;
import com.inpor.base.sdk.user.UserManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 会中 聊天界面的P层
 */
public class MeetingChatContainer extends BaseContainer<MeetingChatView> {
    private static final String TAG = "MeetingChatContainer";
    List<BaseUser> allUsers;
    List<BaseUser> searchUserResultList;
    private final UserManager userModel;


    /**
     * 构造函数
     */
    public MeetingChatContainer(Context context) {
        super(context);
        userModel = SdkUtil.getUserManager();
        userModel.addEventListener(userModelListener);
        allUsers = userModel.getAllUsers();
        searchUserResultList = new ArrayList<>();
        view = new MeetingChatView(context);
        updateChatInputState();
    }

    /**
     * 用户状态回调
     */
    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.CHAT_STATE
                    | UserModelListenerImpl.CHAT_MSG_CHECK_STATE, UserModelListenerImpl.ThreadMode.MAIN) {
        @Override
        public void onUserChanged(int type, BaseUser user) {
            if (type == UserModelListenerImpl.CHAT_STATE) {
                long targetUserId = user.getUserId();
                BaseUser localUser = userModel.getLocalUser();
                long userId = localUser.getUserId();
                if (userId == targetUserId) {
                    updateChatInputState();
                }
                Log.i(TAG, "onUserChanged:" + type + "  " + (userId == targetUserId));
            }
        }
    };

    private void updateChatInputState() {
        view.setInputEditTextState();
    }


    @Override
    public void recycle() {
        if (view != null) {
            view.recycle();
            view = null;
        }
        userModel.removeEventListener(userModelListener);
    }
}
