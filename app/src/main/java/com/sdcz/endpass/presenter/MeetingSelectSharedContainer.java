package com.sdcz.endpass.presenter;

import android.content.Context;
import android.media.projection.MediaProjectionManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseContainer;
import com.sdcz.endpass.constant.Constant;
import com.sdcz.endpass.widget.MeetingSelectSharedView;

/**
 * @Description     底部菜单点击共享时，选择共享弹窗的P层
 */
public class MeetingSelectSharedContainer extends BaseContainer<MeetingSelectSharedView>
        implements MeetingSelectSharedView.SelectSharedViewListener {


    public MeetingSelectSharedContainer(Context context) {
        super(context);
        initParams(context);
    }

    private void initParams(Context context) {
        view = new MeetingSelectSharedView(context);
        view.addSelectSharedVerticalViewListener(this);
    }

    /**
     * 点击共享屏幕Item
     */
    @Override
    public void onClickScreenSharedItemListener() {
        view.dismissPopupWindow();
        if (SdkUtil.getShareManager().isScreenSharing()) {
            return;
        }
        MediaProjectionManager pm = (MediaProjectionManager) context.
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        ActivityUtils.getTopActivity().startActivityForResult(
                pm.createScreenCaptureIntent(), Constant.SCREEN_SHARE_REQUEST_CODE);
    }

}
