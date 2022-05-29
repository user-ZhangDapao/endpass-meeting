package com.sdcz.endpass.login;

import com.blankj.utilcode.util.Utils;
import com.sdcz.endpass.R;

public class LoginStateUtil {
    public static String convertStateToString(Integer state) {
        int stateId;
        switch (state) {
            case LoginState.ERROR:
                stateId = R.string.UNKNOWN_ERROR;
                break;
            case LoginState.GETTING_VERSION:
                stateId = R.string.GETTING_VERSION;
                break;
            case LoginState.CONNECTING_SERVER:
            case LoginState.GET_ADDRESS:
                stateId = R.string.CONNECTING_SERVER;
                break;
            case LoginState.LOGIN_ROOM:
                stateId = R.string.LOGIN_ROOM;
                break;
            case LoginState.ROOM_LOCKED_WAITING:
                stateId = R.string.ROOM_LOCKED_WAITING;
                break;
            case LoginState.INITIALIZING_COMPONENT:
                stateId = R.string.INITIALIZING_COMPONENT;
                break;
            case LoginState.INITIALIZING_AUDIO_DEVICE:
                stateId = R.string.INITIALIZING_AUDIO_DEVICE;
                break;
            case LoginState.INITIALIZING_VIDEO_DEVICE:
                stateId = R.string.INITIALIZING_VIDEO_DEVICE;
                break;
            case LoginState.INITIALIZING_ROOM:
                stateId = R.string.INITIALIZING_ROOM;
                break;
            case LoginState.LOGIN_GROUP_ROOM:
                stateId = R.string.LOGIN_GROUP_ROOM;
                break;
            case LoginState.LOGIN_MAIN_ROOM:
            case LoginState.USER_VERIFY_SUCCESS:
            case LoginState.ROOM_VERIFY_SUCCESS:
                stateId = R.string.LOGIN_MAIN_ROOM;
                break;
            case LoginState.CONFIG_CENTER:
                stateId = R.string.CONFIG_CENTER;
                break;
            case LoginState.GET_LOCAL_CONFIG:
                stateId = R.string.GET_LOCAL_CONFIG;
                break;
            case LoginState.GET_ROOM_INFO:
            case LoginState.GET_ROOM:
                stateId = R.string.GET_ROOM_INFO;
                break;
            case LoginState.GET_NET_CONFIG:
                stateId = R.string.GET_NET_CONFIG;
                break;
            case LoginState.UPDATE_USER_INFO:
            case LoginState.GET_LOCAL_USER_INFO:
                stateId = R.string.GET_NET_CONFIG;
                break;
            case LoginState.LOGIN:
            case LoginState.PRIVATE_LOGIN:
                stateId = R.string.LOGIN;
                break;
            case LoginState.GET_PAAS_OAUTH:
            case LoginState.PAAS_LOGIN:
                stateId = R.string.PAAS_LOGIN;
                break;
            case LoginState.ENTER_MEETING_ROOM:
                stateId = R.string.ENTER_MEETING_ROOM;
                break;
            default:
                stateId = R.string.LOGIN_MAIN_ROOM;
        }
        return Utils.getApp().getResources().getString(stateId);
    }
}
