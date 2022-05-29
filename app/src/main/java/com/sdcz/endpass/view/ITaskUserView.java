package com.sdcz.endpass.view;

import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.UserEntity;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/7/9 14:33
 * Description: @
 */
public interface ITaskUserView extends IBaseView {
    void showData(List<ChannerUser> data);

    void onDelete(Object data , String userId);
    void addUserResult(Object data);
    void updateChannelResult(Object data);
}
