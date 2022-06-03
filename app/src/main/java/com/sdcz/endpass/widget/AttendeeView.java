package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Constraints;
import androidx.lifecycle.MutableLiveData;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ScreenUtils;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.listeners.MeetingModelListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.sdcz.endpass.LiveDataBus;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.AttendeeCategoryAdapter;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.contract.AttendeeCategory;
import com.sdcz.endpass.contract.AttendeeContracts;
import com.sdcz.endpass.model.AttendeeModel;
import com.sdcz.endpass.model.MicEnergyMonitor;
import com.sdcz.endpass.presenter.AttendeePresenter;
import com.google.android.material.tabs.TabLayout;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.user.UserManager;

import java.util.List;


/**
 * @Date: 2020/12/11
 * @Author: hugo
 * @Description:
 */

public class AttendeeView extends BasePopupWindowContentView implements
        View.OnClickListener, AttendeeContracts.IView,
        AttendeeSearchView.InteractionListener {
    private static final String TAG = "AttendeeView";
    private static final String USR_COUNT_FORMAT = "(%d)";

    ////////// 参会人 ///////////
    private FrameLayout parentLayout;
    private LinearLayout linearAttendee;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private LinearLayout linearManager;
    private LinearLayout linearHost;
    private LinearLayout linearAudio;
    private ImageView imgHost;
    private ImageView imgAudio;
    private TextView tvHost;
    private TextView tvAudio;
    ////////// 搜索 //////////
    private AttendeeSearchView attendeeSearchView;

    private AttendeeCategoryAdapter adapter;

    private AttendeePresenter presenter;

    private MeetingManager meetingModel;
    private UserManager userModel;

    private TabLayout.Tab tabAllUser;
    private TabLayout.Tab tabSpeakingUser;
    private TabLayout.Tab tabRequestSpeakingUser;
    private TabLayout.Tab tabOfflineUser;

    private final MeetingModelListener meetingModelListener = new MeetingModelListener() {

        @Override
        public void onUserEnter(List<Long> userIds) {
            if (userIds == null || userIds.isEmpty()) {
                return;
            }
            // 有用户进入会议室重新计算各个类别的总数
            Log.i(TAG, "count user on user enter");
            presenter.countUser();
        }

        @Override
        public void onUserLeave(BaseUser user) {
            if (user == null) {
                return;
            }
            // 用户离开会议室重新计算各个类别的总数
            Log.i(TAG, "count user on user leave");
            presenter.countUser();
        }

        public void onSetRoomMute(byte mute) {
            setAllAudioOffState(mute);
        }

        @Override
        public void onMainSpeakerChanged(BaseUser user) {
            if (user.isLocalUser()) {
                updateHost();
                updateMute(user);
            }
            presenter.countUser();
        }
    };

    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.MAIN_SPEAKER,
            UserModelListenerImpl.ThreadMode.MAIN) {

        @Override
        public void onUserChanged(int type, BaseUser user) {
            if (user.isLocalUser() && (UserModelListenerImpl.MAIN_SPEAKER == type
                    || UserModelListenerImpl.USER_RIGHT == type)) {
                updateHost();
                updateMute(user);
            }
            Log.i(TAG, "count user on user changed");
            presenter.countUser();
        }

        @Override
        public void onBatchUserChanged(int type, BaseUser[] batchUsers) {
            if (batchUsers == null || batchUsers.length == 0) {
                return;
            }
            Log.i(TAG, "count user on batch user state changed");
            presenter.countUser();
            for (BaseUser user : batchUsers) {
                if (user.isLocalUser()) {
                    updateHost();
                    updateMute(user);
                    break;
                }
            }
        }

    };

    private final TabLayout.OnTabSelectedListener tabListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            for (int index = 0; index < tabLayout.getTabCount(); index++) {
                if (tabLayout.getTabAt(index).equals(tab)) {
                    viewPager.setCurrentItem(index);
                    break;
                }
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };


    public AttendeeView(@NonNull Context context) {
        super(context);
        initView(context);
        MutableLiveData<Configuration> liveData = LiveDataBus.getInstance().
                getLiveData(LiveDataBus.KEY_MEETING_ACTIVITY_CONFIG);
        liveData.observeForever(this::configurationChanged);
    }

    private void configurationChanged(Configuration newConfig) {
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onLandscapeListener();
        } else {
            onPortraitListener();
        }
        viewPager.setCurrentItem(0);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "AttendeeView#onAttachedToWindow()");
        // 初始化Presenter，UserModel，MeetingModel
        AttendeeContracts.IModel model = new AttendeeModel();
        presenter = new AttendeePresenter();
        presenter.attachModel(model);
        presenter.attachView(this);
        presenter.countUser();
        meetingModel = SdkUtil.getMeetingManager();
        meetingModel.addEventListener(meetingModelListener);
        userModel = SdkUtil.getUserManager();
        userModel.addEventListener(userModelListener);
        updateHost();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "AttendeeView#onDetachedFromWindow()");
        // 释放Presenter，UserModel，MeetingModel
        presenter.detachView();
        meetingModel.removeEventListener(meetingModelListener);
        Log.i(TAG, "移除:Observer");
    }


    @Override
    public void onLandscapeListener() {
        tabLayout.setInlineLabel(false);
        initLandLayoutParams();
    }


    @Override
    public void onPortraitListener() {
        tabLayout.setInlineLabel(true);
        initPortraitLayoutParams();
    }

    @Override
    public void onCountUser(@AttendeeCategory int category, int count) {
        String countStr = String.valueOf(count); // String.format(Locale.getDefault(), USR_COUNT_FORMAT, count);
        switch (category) {
            case AttendeeCategory.ALL:
                // tabAllUser.setText(countStr);
                ((TextView) tabAllUser.getCustomView().findViewById(R.id.text1)).setText(countStr);
                break;
            case AttendeeCategory.SPEAKING:
                // tabSpeakingUser.setText(countStr);
                ((TextView) tabSpeakingUser.getCustomView().findViewById(R.id.text1)).setText(countStr);
                break;
            case AttendeeCategory.REQUEST_SPEAKING:
                // tabRequestSpeakingUser.setText(countStr);
                ((TextView) tabRequestSpeakingUser.getCustomView().findViewById(R.id.text1)).setText(countStr);
                break;
            case AttendeeCategory.OFFLINE:
                // tabOfflineUser.setText(countStr);
                ((TextView) tabOfflineUser.getCustomView().findViewById(R.id.text1)).setText(countStr);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.imgQuit || id == R.id.close_background_view) {
            dismissPopupWindow();
        } else if (id == R.id.imgBack) {
            popupWindowBack();
        } else if (id == R.id.imgSearch) {
            linearAttendee.setVisibility(View.GONE);
            attendeeSearchView.setVisibility(View.VISIBLE);
            attendeeSearchView.showSearchAttendeeView(adapter.getItem(viewPager.getCurrentItem()));
            MicEnergyMonitor.getInstance().addAudioEnergyListener(attendeeSearchView, MicEnergyMonitor.ATTENDEE_VIEW);
        } else if (id == R.id.linearHost) {
            onBtnHostClick();
        } else if (id == R.id.tvCancel) {
            linearAttendee.setVisibility(View.VISIBLE);
            attendeeSearchView.setVisibility(View.GONE);
            attendeeSearchView.hideSearchAttendeeView();
        } else if (id == R.id.linearAudio) {
            //只有管理员或主持人才能
            meetingModel.setAllMute(true);
        }
    }

    @Override
    public void onCancelSearch() {
        linearAttendee.setVisibility(View.VISIBLE);
        attendeeSearchView.setVisibility(View.GONE);
        MicEnergyMonitor.getInstance().removeAudioEnergyListener(attendeeSearchView, MicEnergyMonitor.ATTENDEE_VIEW);
    }

    private void onBtnHostClick() {
        BaseUser user = userModel.getLocalUser();
        if (user.isMainSpeakerNone()) {
            userModel.applyToBeHost(true);
        } else if (user.isMainSpeakerWait()) {
            userModel.applyToBeHost(false);
        } else if (user.isMainSpeakerDone()) {
            userModel.applyToBeHost(false);
        }
    }

    private void initView(Context context) {
        Log.i(TAG, "添加:Observer");
        LayoutInflater.from(context).inflate(R.layout.view_attendee, this);
        parentLayout = findViewById(R.id.fl_parent_layout);
        parentLayout.setOnClickListener(this);
        View closeBackgroundView = findViewById(R.id.close_background_view);
        closeBackgroundView.setOnClickListener(this);
        ImageView back = findViewById(R.id.imgBack);
        back.setOnClickListener(this);
        // 申请/放弃主持人；全场静音
        linearManager = findViewById(R.id.linearManager);
        linearHost = findViewById(R.id.linearHost);
        linearAudio = findViewById(R.id.linearAudio);
        imgHost = findViewById(R.id.imgHost);
        imgAudio = findViewById(R.id.imgAudio);
        tvHost = findViewById(R.id.tvHost);
        tvAudio = findViewById(R.id.tvAudio);
        linearHost.setOnClickListener(this);
        linearAudio.setOnClickListener(this);

        linearAttendee = findViewById(R.id.linearAttendee);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tabLayout.addOnTabSelectedListener(tabListener);
        initTabs2();
        initViewPager();
        findViewById(R.id.imgQuit).setOnClickListener(this);
        findViewById(R.id.imgSearch).setOnClickListener(this);
        attendeeSearchView = findViewById(R.id.attendeeSearchView);
        attendeeSearchView.setListener(this);

        if (ScreenUtils.isPortrait()) {
            initPortraitLayoutParams();
        } else {
            initLandLayoutParams();
        }
    }


    private void initLandLayoutParams() {

        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.leftToLeft = R.id.guideline_vertical_left;
        parentLayout.setBackgroundResource(R.drawable.shape_select_shared_right);
        parentLayout.setLayoutParams(layoutParams);
        linearManager.setBackgroundResource(R.drawable.shape_select_shared_right);
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


    private void initViewPager() {
        adapter = new AttendeeCategoryAdapter();
        adapter.add(AttendeeCategory.ALL);
//        adapter.add(AttendeeCategory.SPEAKING);
//        adapter.add(AttendeeCategory.REQUEST_SPEAKING);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                if (userModel == null) {
                    return;
                }
                BaseUser local = userModel.getLocalUser();
                updateMute(local);
            }
        });
    }

    private void initTabs2() {
        tabAllUser = tabLayout.newTab().setCustomView(customTabView(R.drawable.selector_attendee_all));
//        tabSpeakingUser = tabLayout.newTab().setCustomView(customTabView(R.drawable.selector_mic_speaking));
//        tabRequestSpeakingUser = tabLayout.newTab().setCustomView(customTabView(R.drawable.selector_attendee_handup));
//        tabOfflineUser = tabLayout.newTab().setCustomView(customTabView(R.drawable.selector_attendee_offline));
        tabLayout.addTab(tabAllUser);
//        tabLayout.addTab(tabSpeakingUser);
//        tabLayout.addTab(tabRequestSpeakingUser);
        boolean isPort = ScreenUtils.isPortrait();
        tabLayout.setInlineLabel(isPort);
    }

    private View customTabView(@DrawableRes int id) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.tab_item_attendee, null);
        ImageView icon = view.findViewById(R.id.icon);
        icon.setImageResource(id);
        TextView tv = view.findViewById(R.id.text1);
        tv.setText("(0)");
        return view;
    }

    /**
     * 更新申请主持人/放弃主持人按钮的文本和图标
     */
    private void updateHost() {
        BaseUser user = userModel.getLocalUser();
        int tvRes;
        int imgRes;
        int tvColor;
        if (user.isMainSpeakerDone()) {
            tvRes = R.string.meetingui_give_up_applying_main_speaker;
            imgRes = R.mipmap.ul_speaker_on;
            tvColor = R.color.color_F0834D;
        } else if (user.isMainSpeakerWait()) {
            tvRes = R.string.meetingui_applying_main_speaker;
            imgRes = R.mipmap.ul_speaker_applying;
            tvColor = R.color.color_white;
        } else {
            tvRes = R.string.meetingui_apply_main_speaker;
            imgRes = R.mipmap.ul_speaker_off;
            tvColor = R.color.color_white;
        }
        tvHost.setTextColor(getContext().getResources().getColor(tvColor));
        tvHost.setText(tvRes);
        imgHost.setImageResource(imgRes);
    }

    /**
     * 更新全场静音按钮(主讲和管理员权限时显示)
     */
    private void updateMute(BaseUser user) {
        if (!user.isLocalUser()) {
            return;
        }
        if (user.isMainSpeakerDone() || user.isManager()) {
            linearAudio.setVisibility(View.VISIBLE);
            linearAudio.setEnabled(true);
        } else {
            linearAudio.setVisibility(View.INVISIBLE);
            linearAudio.setEnabled(false);
        }
    }

    public void setAllAudioOffState(int muteStatus) {
        tvAudio.setText(muteStatus == 1 ? R.string.meetingui_unmute : R.string.meetingui_mute_all);
    }

    @Override
    public void onUsersResult(int category, List<BaseUser> users) {

    }

    @Override
    public void onUsersResult(String key, List<BaseUser> users) {

    }
}














