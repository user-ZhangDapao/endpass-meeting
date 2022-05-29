package com.sdcz.endpass.view;


import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.MailListBean;

/**
 * Author: Administrator
 * CreateDate: 2021/6/28 14:22
 * Description: @
 */
public interface IMailListView extends IBaseView {

    void showUserInfo();

    void showData(MailListBean data);

    void showStatus(Integer data);

    void cancelLikeSuccess(Object data);

    void creatRecordSuccess(String data, String collectUserId, String recordType);

}
