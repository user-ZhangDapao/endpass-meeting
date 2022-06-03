package com.sdcz.endpass.presenter;


import android.app.Activity;

import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.view.IMobileMeetingView;

public class MobileMeetingPresenter extends BasePresenter<IMobileMeetingView> {
    public MobileMeetingPresenter(IMobileMeetingView view) {
        super(view);
    }



    public void getChannelByCode(Activity activity, String channelCode){
        RequestUtils.getChannelByCode(channelCode, new MyObserver<ChannelBean>(activity) {
            @Override
            public void onSuccess(ChannelBean result) {
                iView.showData(result);
            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
    }
}
