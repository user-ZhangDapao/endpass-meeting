package com.sdcz.endpass.bean;

/**
 * @description  媒体分享bean
 */
public class MediaShareBean extends BaseShareBean{

    public long userId;
    public byte videoId;
    protected byte audioId;

    public long renderId;

    protected boolean isOpeningAudio = false;
    public boolean isOpeningVideo = false;

    public long getUserId() {
        return userId;
    }

    public byte getVideoId() {
        return videoId;
    }

    public byte getAudioId() {
        return audioId;
    }

    public boolean isOpeningAudio() {
        return isOpeningAudio;
    }

    public boolean isOpeningVideo() {
        return isOpeningVideo;
    }
}
