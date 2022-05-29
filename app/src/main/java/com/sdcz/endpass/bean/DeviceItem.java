package com.sdcz.endpass.bean;

import com.inpor.nativeapi.adaptor.AudioChannel;
import com.inpor.nativeapi.adaptor.VideoChannel;

/**
 * @Description: 参会人的设备列表(麦克风，Camera)
 */
public class DeviceItem {
    public static final int MICROPHONE = 1;
    public static final int CAMERA = 2;

    private final int deviceType;
    private AudioChannel audioChannel;
    private VideoChannel videoChannel;

    public DeviceItem(AudioChannel audioChannel) {
        this.deviceType = MICROPHONE;
        this.audioChannel = audioChannel;
    }

    public DeviceItem(VideoChannel videoChannel) {
        this.deviceType = CAMERA;
        this.videoChannel = videoChannel;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public AudioChannel getAudioChannel() {
        return audioChannel;
    }

    public VideoChannel getVideoChannel() {
        return videoChannel;
    }
}
