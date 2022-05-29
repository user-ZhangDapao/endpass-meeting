package com.sdcz.endpass.view;


import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.ChannelBean;

/**
 * Author: Administrator
 * CreateDate: 2021/7/8 13:27
 * Description: @
 */
public interface ICreateTaskView extends IBaseView {

    void showData(ChannelBean data);

    void addUserResult(Object data,String groupId,String[] userIds);

    void onDelete(Object data);
}
