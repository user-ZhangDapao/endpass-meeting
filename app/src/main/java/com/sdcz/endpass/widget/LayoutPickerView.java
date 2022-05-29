package com.sdcz.endpass.widget;

import static com.inpor.nativeapi.adaptor.RoomWndState.SplitStyle.SPLIT_STYLE_6;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.comix.meeting.MeetingModule;
import com.comix.meeting.entities.LayoutType;
import com.comix.meeting.entities.MeetingInfo;
import com.comix.meeting.listeners.LayoutModelListener;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.GridItemDecoration;
import com.sdcz.endpass.adapter.LayoutPickerAdapter;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.bean.LayoutItem;
import com.sdcz.endpass.callback.OnItemClickListener;
import com.google.gson.Gson;
import com.inpor.nativeapi.adaptor.RoomWndState;

import java.util.List;

/**
 * @Date: 2020/12/10
 * @Author: hugo
 * @Description: 选择布局的View
 */
public class LayoutPickerView extends BasePopupWindowContentView implements OnItemClickListener,
        View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        LayoutModelListener {

    private static final String TAG = "LayoutPickerView";
    private ImageView imgBack;
    private ImageView imgCloseLayout;
    private CheckBox cbFollowLayout;
    private RecyclerView recyclerView;
    private LayoutPickerAdapter adapter;
    private FrameLayout parentLayout;
    private MeetingModule proxy;


    public LayoutPickerView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public LayoutPickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        SdkUtil.getMeetingManager().addLayoutListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        SdkUtil.getMeetingManager().removeLayoutListener(this);
    }

    @Override
    public void onLayoutChanged(MeetingInfo meetingInfo) {
        setLayoutByMeetingInfo(meetingInfo);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }


    @Override
    public void onLandscapeListener() {
        Log.i(TAG, "横屏");
        initLandLayoutParams();
        refreshView(2);

    }


    @Override
    public void onPortraitListener() {
        Log.i(TAG, "竖屏");
        refreshView(3);
        initPortraitLayoutParams();

    }

    @Override
    public <T> void onItemClick(RecyclerViewAdapter<T> viewAdapter, int position, View itemView) {
        this.adapter.setSelectItem(position);
        adapter.notifyDataSetChanged();
        LayoutItem layoutItem = this.adapter.getItem(position);
        SdkUtil.getMeetingManager().setMeetingLayoutType(layoutItem.getLayoutType(), layoutItem.getSplitStyle());
        dismissPopupWindow();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imgCloseLayout || view.getId() == R.id.close_background_view) {
            dismissPopupWindow();
        } else if (view.getId() == R.id.imgBack) {
            popupWindowBack();
        }
    }

    public void refreshView(int spanCount) {
        initRecyclerView(getContext(), spanCount);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_layout_picker, this);
        parentLayout = findViewById(R.id.fl_parent_layout);
        imgBack = findViewById(R.id.imgBack);
        imgCloseLayout = findViewById(R.id.imgCloseLayout);
        cbFollowLayout = findViewById(R.id.cbFollowLayout);
        cbFollowLayout.setOnCheckedChangeListener(this);
        recyclerView = findViewById(R.id.selectLayoutRecyclerView);
        View closeBackgroundView = findViewById(R.id.close_background_view);
        closeBackgroundView.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        imgCloseLayout.setOnClickListener(this);
        proxy = SdkUtil.getMeetingManager().getMeetingModule();

        GridItemDecoration.Builder builder = new GridItemDecoration.Builder(context);
        builder.setColorResource(R.color.color_transparent)
                .setHorizontalSpan(R.dimen.dp17)
                .setVerticalSpan(R.dimen.margin_8dp)
                .setShowLastLine(true);
        recyclerView.addItemDecoration(builder.build());
        boolean portrait = ScreenUtils.isPortrait();
        post(() -> initRecyclerView(context, portrait ? 3 : 2));
        if (portrait) {
            initPortraitLayoutParams();
        } else {
            initLandLayoutParams();
        }

        // 目前不做，默认跟随布局
        cbFollowLayout.setVisibility(View.GONE);
    }

    private void initLandLayoutParams() {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.leftToLeft = R.id.guideline_vertical_left;
        parentLayout.setBackgroundResource(R.drawable.shape_select_shared_right);
        parentLayout.setLayoutParams(layoutParams);
    }

    private void initPortraitLayoutParams() {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.leftToLeft = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.topToTop = R.id.guideline_horizontal_top;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        parentLayout.setBackgroundResource(R.drawable.shape_select_shared);
        parentLayout.setLayoutParams(layoutParams);

    }

    private void initRecyclerView(Context context, int spanCount) {
        GridLayoutManager linearLayoutManager = new GridLayoutManager(context, spanCount);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new LayoutPickerAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        initLayoutItem();
    }

    private void setLayoutByMeetingInfo(MeetingInfo meetingInfo) {
        if (meetingInfo == null || adapter == null) {
            return;
        }
        Log.i(TAG, "setLayoutByMeetingInfo: meetingInfo is " + new Gson().toJson(meetingInfo));
        int position = RecyclerView.NO_POSITION;
        int sixScreenPosition = RecyclerView.NO_POSITION;
        List<LayoutItem> data = adapter.getData();
        for (int i = 0; i < data.size(); i++) {
            LayoutItem item = data.get(i);
            if (item.getLayoutType() != meetingInfo.layoutType) {
                continue;
            }
            if (item.getLayoutType() != LayoutType.VIDEO_LAYOUT) {
                position = i;
                break;
            }
            if (item.getSplitStyle() == RoomWndState.SplitStyle.SPLIT_STYLE_6) {
                sixScreenPosition = i;
            }
            if (item.getSplitStyle() == meetingInfo.splitStyle) {
                position = i;
                break;
            }
        }
        if (position == RecyclerView.NO_POSITION) {
            position = sixScreenPosition;
        }
        adapter.setSelectItem(position);
        adapter.notifyDataSetChanged();
    }

    private void initLayoutItem() {
        boolean portrait = ScreenUtils.isPortrait();
        // 数据布局
        LayoutItem dataLayout = new LayoutItem(LayoutType.CULTIVATE_LAYOUT,
                RoomWndState.SplitStyle.SPLIT_STYLE_P_IN_P,
                portrait ? R.mipmap.layout_1 : R.mipmap.layout_h1, R.string.meetingui_layout_data);
        adapter.add(dataLayout);
        // 数据+平铺(平铺)
        LayoutItem dataAndTile = new LayoutItem(LayoutType.STANDARD_LAYOUT,
                RoomWndState.SplitStyle.SPLIT_STYLE_4,
                portrait ? R.mipmap.layout_2 : R.mipmap.layout_h2, R.string.meetingui_layout_data_tile);
        adapter.add(dataAndTile);
        ////////// 分屏布局 ///////////////
        // 一分屏
        LayoutItem videoSplit1 = new LayoutItem(LayoutType.VIDEO_LAYOUT,
                RoomWndState.SplitStyle.SPLIT_STYLE_1,
                portrait ? R.mipmap.layout_7 : R.mipmap.layout_h7, R.string.meetingui_layout_video_one);
        adapter.add(videoSplit1);
        // 二分屏
        LayoutItem videoSplit2 = new LayoutItem(LayoutType.VIDEO_LAYOUT,
                RoomWndState.SplitStyle.SPLIT_STYLE_2,
                portrait ? R.mipmap.layout_8 : R.mipmap.layout_h8, R.string.meetingui_layout_video_two);
        adapter.add(videoSplit2);
        // 画中画
        LayoutItem videoPip = new LayoutItem(LayoutType.VIDEO_LAYOUT,
                RoomWndState.SplitStyle.SPLIT_STYLE_P_IN_P,
                portrait ? R.mipmap.layout_9 : R.mipmap.layout_h9, R.string.meetingui_layout_video_pip);
        adapter.add(videoPip);
        // 四分屏
        LayoutItem videoSplit4 = new LayoutItem(LayoutType.VIDEO_LAYOUT,
                RoomWndState.SplitStyle.SPLIT_STYLE_4,
                portrait ? R.mipmap.layout_10 : R.mipmap.layout_h10, R.string.meetingui_layout_video_four);
        adapter.add(videoSplit4);
        // 六分屏
        LayoutItem videoSplit6 = new LayoutItem(LayoutType.VIDEO_LAYOUT,
                SPLIT_STYLE_6,
                portrait ? R.mipmap.layout_11 : R.mipmap.layout_h11, R.string.meetingui_layout_video_six);
        adapter.add(videoSplit6);
        setLayoutByMeetingInfo(proxy.getMeetingInfo());
    }

    /**
     * 横竖屏切换回调
     *
     * @param newConfig 当前Configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onLandscapeListener();
        } else {
            onPortraitListener();
        }
    }
}
