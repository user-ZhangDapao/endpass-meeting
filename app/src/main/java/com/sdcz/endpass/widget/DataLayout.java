package com.sdcz.endpass.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.VncShareBean;
import com.comix.meeting.entities.WhiteBoard;
import com.comix.meeting.listeners.ShareModelListener;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.IDataContainer;
import com.sdcz.endpass.model.UiEntrance;
import com.sdcz.endpass.share.VncReceptionContainer;
import com.sdcz.endpass.share.VncSendContainer;
import com.sdcz.endpass.share.WhiteBoardContainer;
import com.inpor.base.sdk.whiteboard.WBShareManager;
import com.inpor.nativeapi.adaptor.RoomWndState;

/**
 * @author yinhui
 * @date create at 2019/2/25
 * @description
 */
public class DataLayout extends FrameLayout {

    private static final String TAG = "DataLayout";
    private final FrameLayout containerParent;
    private TextView emptyView;
    private WBShareManager shareModel;
    /**
     * 类似状态机,根据不同共享类型持有对应Container
     */
    private IDataContainer dataContainer;
    private final ArrayMap<Class<? extends IDataContainer>, IDataContainer> containerMap
            = new ArrayMap<>(8);
    private final SparseIntArray countMap = new SparseIntArray(8);

    private boolean isEmptyBackground;

    /**
     * 构造
     *
     * @param context 上下文
     */
    public DataLayout(Context context) {
        super(context);
        containerParent = this;
        init(context);
    }

    private void init(Context context) {
        emptyView = new TextView(context);
        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        emptyView.setLayoutParams(layoutParams);
        emptyView.setTextColor(Color.GRAY);
        emptyView.setTextSize(14f);
        emptyView.setGravity(Gravity.CENTER);
        Drawable backgroundDrawable = ContextCompat.getDrawable(context,
                R.mipmap.meetingui_share_background);
        emptyView.setCompoundDrawablesWithIntrinsicBounds(null, backgroundDrawable, null, null);
        emptyView.setText(R.string.meetingui_no_data_share);
        int backgroundColor = Color.parseColor("#e9ebee");
        containerParent.setBackgroundColor(backgroundColor);
        changeLayoutBackground(true);
    }

    /**
     *
     */
    public void subscribe() {
        if (shareModel == null) {
            shareModel = SdkUtil.getWbShareManager();
        }
        shareModel.addWhiteBoardListener(shareModelListener);
    }

    /**
     *
     */
    public void unSubscribe() {
        if (shareModel != null) {
            shareModel.removeWhiteBoardListener(shareModelListener);
        }
        containerMap.clear();
        countMap.clear();
    }

    void onHiddenChanged(boolean hidden) {
        if (dataContainer == null) {
            return;
        }
        dataContainer.onHiddenChanged(hidden);
    }

    private void changeDataContainer(BaseShareBean shareBean) {
        IDataContainer temp = getDataContainer(shareBean);
        if (temp == null) {
            return;
        }
        if (dataContainer != null) {
            if (dataContainer.getClass() != temp.getClass()) {
                containerParent.removeAllViews();
                dataContainer = null;
            }
        }
        dataContainer = temp;
        dataContainer.setShareBean(shareBean);
        if (containerParent.getChildCount() == 0) {
            View dataView = dataContainer.getDataView();
            View stateView = dataContainer.getStateView();
            if (dataView != null) {
                containerParent.addView(dataView);
            }
            if (stateView != null) {
                containerParent.addView(stateView);
            }
        }
        dataContainer.updateDataView(shareBean);
        dataContainer.updateStateView(shareBean);
    }

    private IDataContainer getDataContainer(BaseShareBean shareBean) {
        Class<? extends IDataContainer> clz = getContainerClass(shareBean);
        if (clz == null) {
            return null;
        }
        return containerMap.get(clz);
    }

    private void initDataContainer(BaseShareBean shareBean) {
        Class<? extends IDataContainer> clz = getContainerClass(shareBean);
        if (clz == null) {
            return;
        }
        IDataContainer container = containerMap.get(clz);
        if (container != null) {
            countPlus(clz);
            return;
        }
        try {
            container = clz.getConstructor().newInstance();
            container.init(getContext());
            containerMap.put(clz, container);
            countPlus(clz);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void releaseDataContainer(BaseShareBean shareBean) {
        Class<? extends IDataContainer> clz = getContainerClass(shareBean);
        if (clz == null) {
            return;
        }
        IDataContainer container = containerMap.get(clz);
        int count = countLess(clz);
        if (container != null && count == 0) {
            container.release();
            containerMap.remove(clz);
        }
    }

    private Class<? extends IDataContainer> getContainerClass(BaseShareBean shareBean) {
        Class<? extends IDataContainer> clz = null;
        RoomWndState.DataType dataActive = shareBean.getType();
        if (dataActive == RoomWndState.DataType.DATA_TYPE_WB) {
            clz = WhiteBoardContainer.class;
        } else if (dataActive == RoomWndState.DataType.DATA_TYPE_APPSHARE) {
            VncShareBean vncShareBean = (VncShareBean) shareBean;
            if (vncShareBean.isReceive()) {
                clz = VncReceptionContainer.class;
            } else {
                clz = VncSendContainer.class;
            }
        }
        return clz;
    }

    private int countPlus(Class<? extends IDataContainer> clz) {
        int count = countMap.get(clz.hashCode(), 0);
        int afterCount = count + 1;
        countMap.put(clz.hashCode(), afterCount);
        return afterCount;
    }

    private int countLess(Class<? extends IDataContainer> clz) {
        int count = countMap.get(clz.hashCode(), 0);
        if (count <= 0) {
            return 0;
        }
        int afterCount = count - 1;
        countMap.put(clz.hashCode(), afterCount);
        return afterCount;
    }

    private void changeLayoutBackground(boolean empty) {
        if (containerParent == null) {
            return;
        }
        if (isEmptyBackground == empty) {
            return;
        }
        this.isEmptyBackground = empty;
        if (empty) {
            containerParent.addView(emptyView);
            //containerParent.setBackground(backgroundDrawable);
        } else {
            containerParent.removeView(emptyView);
            //containerParent.setBackgroundColor(backgroundColor);
        }
    }

    /**
     * 共享监听
     */
    private final ShareModelListener shareModelListener = new ShareModelListener() {

        @Override
        public void onShareTabChanged(int type, BaseShareBean shareBean) {
            Log.i(TAG, "onShareTabChanged: type is " + type);
            Log.i(TAG, "onShareTabChanged: shareBean is " + shareBean);
            if (type == SHARE_ADD) {
                // to do something
                initDataContainer(shareBean);
            } else if (type == SHARE_REMOVE) {
                // 检查是否还有同类型共享
                releaseDataContainer(shareBean);
            } else if (type == SHARE_ACTIVE) {
                changeLayoutBackground(false);
                changeDataContainer(shareBean);
            }
        }

        @Override
        public void onNoSharing() {
            Log.i(TAG, "onNoSharing: ");
            dataContainer = null;
            containerParent.removeAllViews();
            changeLayoutBackground(true);
        }

        @Override
        public void onWhiteBoardChanged(WhiteBoard whiteBoard) {
            Log.i(TAG, "onWhiteBoardChanged: whiteBoard is " + whiteBoard.getId());
            IDataContainer container = getDataContainer(whiteBoard);
            if (container != null) {
                container.updateDataView(whiteBoard);
            }
        }

        @Override
        public void onWhiteBoardStateChanged(WhiteBoard whiteBoard, int state) {
            Log.i(TAG, "onWhiteBoardStateChanged: whiteBoard is " + whiteBoard.getId());
            Log.i(TAG, "onWhiteBoardStateChanged: state is " + state);
            IDataContainer container = getDataContainer(whiteBoard);
            if (container != null) {
                container.updateStateView(whiteBoard);
            }
        }

        @Override
        public void onMediaShareReceivingStatusChanged(BaseShareBean shareBean, int state) {
            Log.i(TAG, "onMediaShareReceivingStatusChanged: state is " + state);
            IDataContainer container = getDataContainer(shareBean);
            if (container != null) {
                container.updateStateView(shareBean);
            }
        }

        @Override
        public void onVncReceivingStateChanged(BaseShareBean shareBean, int state) {
            Log.i(TAG, "onVncReceivingStateChanged: ");
            IDataContainer container = getDataContainer(shareBean);
            if (container != null) {
                container.updateStateView(shareBean);
            }
        }

        @Override
        public void onVncSendStateChanged(BaseShareBean shareBean, int state) {
            Log.i(TAG, "onVncSendStateChanged: state is " + state);
        }

        @Override
        public void onVoteChanged(int type, BaseShareBean shareBean) {
            Log.i(TAG, "onVoteChanged: ");
            IDataContainer container = getDataContainer(shareBean);
            if (container != null) {
                container.updateStateView(shareBean);
            }
        }
    };


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            UiEntrance.getInstance().notify2MenuHelper();
        }
        return super.dispatchTouchEvent(ev);
    }


}
