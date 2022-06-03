package com.sdcz.endpass.presenter;

import com.comix.meeting.entities.BaseUser;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.contract.AttendeeCategory;
import com.sdcz.endpass.contract.AttendeeContracts;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 参会人界面的P层
 */
public class AttendeePresenter implements AttendeeContracts.IPresenter {

    private AttendeeContracts.IView view;
    private AttendeeContracts.IModel model;

    /**
     * 绑定View
     * @param view AttendeeContracts.IView view
     */
    public void attachView(AttendeeContracts.IView view) {
        this.view = view;
    }

    /**
     * 绑定Model
     * @param model AttendeeContracts.IModel
     */
    public void attachModel(AttendeeContracts.IModel model) {
        this.model = model;
    }

    /**
     * 解除绑定
     */
    public void detachView() {
        this.view = null;
    }

    @Override
    public void queryUsers(int category) {
        // FIXME 请在线程中调用
        List<BaseUser> result = model.queryUsers(category, true);
        AttendeeContracts.IView view = this.view;
        if (view == null) {
            return;
        }
        view.onUsersResult(category, result);
    }

    @Override
    public void queryUsers(@AttendeeCategory int category, String key) {
        // FIXME 如果耗时则需要在
        List<BaseUser> all = model.queryUsers(category, false);
        List<BaseUser> keyUsers = model.queryUsers(all, key);
        model.sort(keyUsers);

        AttendeeContracts.IView view = this.view;
        if (view == null) {
            return;
        }
        view.onUsersResult(key, keyUsers);
    }

    @Override
    public void cancelKeywordSearch() {
    }

    @Override
    public void countUser() {
        List<BaseUser> result = model.queryUsers(AttendeeCategory.ALL, false);
        if (result == null || result.isEmpty()) {
            notifyCountChanged(0, 0, 0, 0);
            return;
        }
        int all = result.size();
        int speaking = speakingUsers(result);
        int requestSpeaking = waitSpeakingUsers(result);
        int offline = offlineUsers();
        notifyCountChanged(all, speaking, requestSpeaking, offline);
    }

    private void notifyCountChanged(int all, int speaking, int requestSpeaking, int offline) {
        AttendeeContracts.IView view = this.view;
        if (view == null) {
            return;
        }
        view.onCountUser(AttendeeCategory.ALL, all);
//        view.onCountUser(AttendeeCategory.SPEAKING, speaking);
//        view.onCountUser(AttendeeCategory.REQUEST_SPEAKING, requestSpeaking);
//        view.onCountUser(AttendeeCategory.OFFLINE, offline);
    }

    private int speakingUsers(List<BaseUser> all) {
        int count = 0;
        for (BaseUser user : all) {
            if (user.isSpeechDone()) {
                count++;
            }
        }
        return count;
    }

    private int waitSpeakingUsers(List<BaseUser> all) {
        int count = 0;
        for (BaseUser user : all) {
            if (user.isSpeechWait() || user.isVideoWait() || user.isMainSpeakerWait()) {
                count++;
            }
        }
        return count;
    }

    private int offlineUsers() {
        // FIXME 离线用户还没实现(貌似也不用实现)
        return 0;
    }

    private List<UserEntity> getChannelUser(String channelCode){
        RequestUtils.getChannelByCode(channelCode, new MyObserver<ChannelBean>() {
            @Override
            public void onSuccess(ChannelBean result) {

            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
        return new ArrayList<>();
    }

}

















