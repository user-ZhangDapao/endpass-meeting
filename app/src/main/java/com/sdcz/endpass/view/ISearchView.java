package com.sdcz.endpass.view;

import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.UserEntity;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/30 14:50
 * Description: @
 */
public interface ISearchView extends IBaseView {
    void showData(List<UserEntity> data);

    void showStatus(Integer data);

    void cancelLikeSuccess(Object data);

    void creatRecordSuccess(String data, String collectUserId, String recordType);
}
