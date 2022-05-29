package com.sdcz.endpass.view;

import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.UserEntity;

/**
 * Author: Administrator
 * CreateDate: 2021/6/30 10:24
 * Description: @
 */
public interface IUserInfoView extends IBaseView {
    void showData(UserEntity data);
    void reviseSuccess(Object data);
    void updataName(Object data);
}
