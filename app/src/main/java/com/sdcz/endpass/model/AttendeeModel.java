package com.sdcz.endpass.model;

import android.text.TextUtils;

import com.comix.meeting.entities.BaseUser;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.contract.AttendeeCategory;
import com.sdcz.endpass.contract.AttendeeContracts;
import com.sdcz.endpass.util.AttendeeComparator;
import com.inpor.base.sdk.user.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  参会人数据  M层
 */
public class AttendeeModel implements AttendeeContracts.IModel {

    private final UserManager userModel;

    /**
     * 构造函数
     */
    public AttendeeModel() {
        this.userModel = SdkUtil.getUserManager();
    }

    @Override
    public List<BaseUser> queryUsers(@AttendeeCategory int category, boolean sort) {
        List<BaseUser> all = userModel.getAllUsers();
        switch (category) {
            case AttendeeCategory.SPEAKING:
                return speakingUsers(all, sort);
            case AttendeeCategory.REQUEST_SPEAKING:
                return waitSpeakingUsers(all, sort);
            case AttendeeCategory.OFFLINE:
                return offlineUsers(sort);
            default:
                return allUsers(all, sort);
        }
    }

    @Override
    public List<BaseUser> queryUsers(List<BaseUser> all, String keyword) {
        ArrayList<BaseUser> keywordUsers = new ArrayList<>();
        if (TextUtils.isEmpty(keyword)) {
            return all;
        }
        keyword = keyword.toLowerCase();
        String nickName;
        for (BaseUser user : all) {
            nickName = user.getNickName().toLowerCase();
            if (nickName.contains(keyword)) {
                keywordUsers.add(user);
            }
        }
        return keywordUsers;
    }

    @Override
    public void sort(List<BaseUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        AttendeeComparator comparator = new AttendeeComparator(users.size());
        Collections.sort(users, comparator);
    }
    
    private List<BaseUser> allUsers(List<BaseUser> users, boolean sort) {
        if (sort) {
            sort(users);
        }
        return users;
    }

    private List<BaseUser> speakingUsers(List<BaseUser> users, boolean sort) {
        List<BaseUser> result = new ArrayList<>();
        for (BaseUser user : users) {
            if (user.isSpeechDone()) {
                result.add(user);
            }
        }
        if (sort) {
            sort(result);
        }
        return result;
    }

    private List<BaseUser> waitSpeakingUsers(List<BaseUser> all, boolean sort) {
        List<BaseUser> result = new ArrayList<>();
        for (BaseUser user : all) {
            if (user.isSpeechWait() || user.isVideoWait() || user.isMainSpeakerWait()) {
                result.add(user);
            }
        }
        if (sort) {
            sort(result);
        }
        return result;
    }

    private List<BaseUser> offlineUsers(boolean sort) {
        // FIXME 离线用户还没实现
        return new ArrayList<>();
    }

}










