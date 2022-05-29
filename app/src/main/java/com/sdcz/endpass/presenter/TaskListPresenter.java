package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ITaskListView;

import java.security.KeyStore;
import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:13
 * Description: @
 */
public class TaskListPresenter extends BasePresenter<ITaskListView> {
    public TaskListPresenter(ITaskListView view) {
        super(view);
    }


    public void getChannelList(Activity activity){
        RequestUtils.getChannelList(new MyObserver<List<ChannelBean>>(activity) {
            @Override
            public void onSuccess(List<ChannelBean> result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }
}
