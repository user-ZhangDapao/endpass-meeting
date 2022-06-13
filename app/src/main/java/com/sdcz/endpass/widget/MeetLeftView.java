package com.sdcz.endpass.widget;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.comix.meeting.entities.BaseUser;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MeetLeftView extends BasePopupWindowContentView
        implements View.OnClickListener {

    private Context context;
    private String channelCode;

    TextView tvOffAudio ;
    TextView tvOpenAudio;
    TextView tvSetting;
    TextView tvHorizontal;
    TextView tvCloseHear;
    TextView tvOpenHear;

    public MeetLeftView(@NonNull Context context, String channelCode) {
        super(context);
        this.context = context;
        this.channelCode = channelCode;
        init(context);
        initListener();
        initData();
    }


    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_video_setting, this);
        tvOffAudio =findViewById(R.id.tvOffAudio);
        tvOpenAudio =findViewById(R.id.tvOpenAudio);
        tvSetting = findViewById(R.id.tvSetting);
        tvHorizontal =findViewById(R.id.tvHorizontal);
        tvCloseHear =findViewById(R.id.tvCloseHear);
        tvOpenHear = findViewById(R.id.tvOpenHear);
    }

    private void initData() {
    }

    private void initListener() {
         tvOffAudio.setOnClickListener(this);
         tvOpenAudio.setOnClickListener(this);
         tvSetting.setOnClickListener(this);
         tvHorizontal.setOnClickListener(this);
         tvCloseHear.setOnClickListener(this);
         tvOpenHear.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.tvOffAudio:
                ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.OFF_AUDIO_ALL);
                dismissPopupWindow();
                break;
            case R.id.tvOpenAudio:
                ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.OPEN_AUDIO_ALL);
                dismissPopupWindow();
                break;
            case R.id.tvCloseHear:
                queryChannelUser(channelCode);
                break;
            case R.id.tvOpenHear:
                cancelAllMute(channelCode);
                break;
            default:
                break;
        }
    }


    public void queryChannelUser(String channelCode){
        RequestUtils.queryChannelUser(channelCode, new MyObserver<List<ChannerUser>>(context) {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(List<ChannerUser> result) {
                HashSet<Long> userIds = new HashSet<>();
                for (BaseUser user : SdkUtil.getUserManager().getAllUsers()){
                    userIds.add(user.getUserId());
                }
                for (ChannerUser channerUser : result){
                    userIds.add(channerUser.getUserId());
                }
                List<Long> users = new ArrayList<>();
                users.addAll(userIds);
                setAllMute(channelCode, users);

            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
    }




    public void setAllMute(String channelCode, List<Long> userIds){

        RequestUtils.setAllMute(channelCode, userIds, new MyObserver<Object>(context) {
            @Override
            public void onSuccess(Object result) {
                ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.OFF_LISTEN_ALL);
                dismissPopupWindow();
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                dismissPopupWindow();
            }
        });
    }

    public void cancelAllMute(String channelCode){

        RequestUtils.cancelAllMute(channelCode, new MyObserver<Object>(context) {
            @Override
            public void onSuccess(Object result) {
                ChatManager.getInstance().sendMessage(0, Constants.SharedPreKey.ON_LISTEN_ALL);
                dismissPopupWindow();
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                dismissPopupWindow();
            }
        });
    }
}
