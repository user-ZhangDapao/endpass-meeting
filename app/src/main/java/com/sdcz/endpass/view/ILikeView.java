package com.sdcz.endpass.view;

import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.UserEntity;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/7/5 9:36
 * Description: @
 */
public interface ILikeView extends IBaseView {

    void showData(List<UserEntity> data);

    /**
     * 请求用户收藏状态
     * @param data
     */
    void showCollectStatus(Integer data);

    /**
     * 创建临时任务
     * @param data
     */
    void creatRecordSuccess(String data, String collectUserId, String recordType);

    /**
     * 修改是否收藏用户成功
     * @param data
     */
    void collectUserSuccess(Object data);
}
