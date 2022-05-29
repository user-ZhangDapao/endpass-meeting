package com.sdcz.endpass.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.MeetingSettingsKey;
import com.sdcz.endpass.model.AppCache;
import com.sdcz.endpass.model.MeetingSettings;
import com.sdcz.endpass.model.MeetingSettingsModel;

/**
 * @Description: 设置页面
 */
public class SettingsFragment extends Fragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    // 美颜
    private View rlBeautyLevel;
    private CheckBox cbBeautyLevel;
    // 视频镜像
    private View rlHorFlip;
    private CheckBox cbHorFlip;
    // 音频数据
    private View rlAudioData;
    private CheckBox cbAudioData;

    private final AppCache appCache = AppCache.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.meeting_settings_fragment, null);
        initView(view);
        return view;
    }

    @Override
    public void onClick(View view) {
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        MeetingSettingsModel settings = MeetingSettingsModel.getInstance();
        if (id == R.id.cbBeautyLevel) {
            settings.updateSettings(MeetingSettingsKey.KEY_BEAUTY_LEVEL, isChecked ? 2 : 0);
        } else if (id == R.id.cbHorFlip) {
            settings.updateSettings(MeetingSettingsKey.KEY_HOR_FLIP, isChecked);
        } else if (id == R.id.cb_audio_data) {
            settings.updateSettings(MeetingSettingsKey.KEY_AUDIO_DATA, isChecked);
        }
    }

    private void initSettings() {
        cbBeautyLevel.setChecked(appCache.getBeautyLevel() > 0);
        cbHorFlip.setChecked(appCache.isHorFlip());
        cbAudioData.setChecked(new MeetingSettings(getContext()).isWriteAudioData());
    }

    private void initView(View view) {
        rlBeautyLevel = view.findViewById(R.id.rlBeautyLevel);
        cbBeautyLevel = view.findViewById(R.id.cbBeautyLevel);
        rlHorFlip = view.findViewById(R.id.rlHorFlip);
        cbHorFlip = view.findViewById(R.id.cbHorFlip);
        rlAudioData = view.findViewById(R.id.rl_audio_data);
        cbAudioData = view.findViewById(R.id.cb_audio_data);
        initSettings();
        cbBeautyLevel.setOnCheckedChangeListener(this);
        cbHorFlip.setOnCheckedChangeListener(this);
        cbAudioData.setOnCheckedChangeListener(this);
    }

}














