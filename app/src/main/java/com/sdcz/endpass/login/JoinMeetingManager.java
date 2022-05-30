package com.sdcz.endpass.login;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sdcz.endpass.SdkUtil;
import com.inpor.base.sdk.SdkManager;
import com.inpor.base.sdk.login.LoginManager;
import com.inpor.sdk.annotation.GrantType;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.callback.LoginStateCallback;
import com.inpor.sdk.callback.SetServerCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.JoinMeetingParam;

import java.util.HashMap;

public class JoinMeetingManager {

    private static final String TAG = "JoinMeetingManager";
    private static JoinMeetingManager sInstance;
    private final SdkManager sdkManager;
    private boolean isInit = false;
    private HashMap<ProcessStep, Integer> stepCodeMap = new HashMap<>();

    private JoinMeetingManager() {
        sdkManager = SdkUtil.getSdkManager();
    }

    public static JoinMeetingManager getInstance() {
        if (sInstance == null) {
            synchronized (JoinMeetingManager.class) {
                if (sInstance == null) {
                    sInstance = new JoinMeetingManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化接口，无须手动调用
     *
     * @param application  Application对象
     * @param clientId
     * @param clientSecret
     */
    public void initSdk(Application application, String clientId, String clientSecret) {
        try {
            if (isInit) {
                return;
            }
            sdkManager.initSdk(application, clientId, clientSecret);
            isInit = true;
        } catch (Exception var5) {
            isInit = false;
        }
    }

    /**
     * 设置clientId和clientSecret
     *
     * @param clientId
     * @param clientSecret
     */
    public void setClientIdInfo(String clientId, String clientSecret) {
        assert isInit : "sdk未初始化,需要先初始化sdk!";
        sdkManager.setClientIdInfo(clientId, clientSecret);
    }

    /**
     * 设置服务器信息
     *
     * @param host     IP地址
     * @param port     端口号
     * @param callback 设置服务器信息的回调
     */
    public void setServer(String host, int port, SetServerCallback callback) {
        assert isInit : "sdk未初始化,需要先初始化sdk!";
        sdkManager.setServer(host, port, callback);
    }

    /**
     * 实名进入会议室
     *
     * @param roomId   房间ID 如果为null，则不会走入会流程，
     * @param userName 用户名
     * @param password 密码
     * @param callback 用户登录结果的回调
     */
    public void loginAccount(String roomId, String userName, String password, LoginMeetingCallBack callback) {
        assert isInit : "sdk未初始化,需要先初始化sdk!";
        LoginManager loginManager = SdkUtil.getLoginManager();
        if (!TextUtils.isEmpty(roomId)) {
            loginManager.setRoomId(roomId);
        }
        DefaultJoinMeetingCallBack joinMeetingCallBack = new DefaultJoinMeetingCallBack(callback);
        loginManager.loginAccount(GrantType.PASSWORD, userName, password, new LoginStateCallback() {
            @Override
            public void onStart(Procedure procedure) {
                Log.i(TAG, "Login onStart: ");
                callback.onStart(procedure);
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "Login onSuccess: ");
                if (!callback.onLoginSuccess()) {
                    return;
                }
                if (!TextUtils.isEmpty(roomId)) {
                    loginRoomId(roomId, userName, password, false, joinMeetingCallBack);
                }
            }

            @Override
            public void onConflict(boolean b) {
                Log.i(TAG, "Login onConflict: b is " + b);
                callback.onConflict(b);
            }

            @Override
            public void onState(ProcessStep processStep) {
                Log.i(TAG, "Login onState: processStep is " + processStep);
                callback.onState(convertStep(processStep));
            }

            @Override
            public void onBlockFailed(ProcessStep processStep, int i, String s) {
                Log.i(TAG, "Login onBlockFailed: processStep is " + processStep + " --"
                        + " -- i is " + i + " -- s is " + s);
                callback.onBlockFailed(processStep, i, s);
            }

            @Override
            public void onFailed() {
                Log.i(TAG, "Login onFailed: ");
                callback.onFailed();
            }
        });
    }

    private int convertStep(ProcessStep processStep) {
        if (stepCodeMap.containsKey(processStep)) {
            return stepCodeMap.get(processStep);
        }
        int loginState = mappingStepToLoginState(processStep);
        stepCodeMap.put(processStep, loginState);
        return loginState;
    }

    private int mappingStepToLoginState(ProcessStep processStep) {
        int code;
        switch (processStep) {
            case CONFIG_CENTER:
                code = LoginState.CONFIG_CENTER;
                break;
            case GET_ADDRESS:
                code = LoginState.GET_ADDRESS;
                break;
            case GET_LOCAL_CONFIG:
                code = LoginState.GET_LOCAL_CONFIG;
                break;
            case GET_ROOM_INFO:
                code = LoginState.GET_ROOM_INFO;
                break;
            case GET_NET_CONFIG:
                code = LoginState.GET_NET_CONFIG;
                break;
            case UPDATE_USER_INFO:
                code = LoginState.UPDATE_USER_INFO;
                break;
            case LOGIN:
                code = LoginState.LOGIN;
                break;
            case GET_ROOM:
                code = LoginState.GET_ROOM;
                break;
            case GET_LOCAL_USER_INFO:
                code = LoginState.GET_LOCAL_USER_INFO;
                break;
            case GET_PAAS_OAUTH:
                code = LoginState.GET_PAAS_OAUTH;
                break;
            case PAAS_LOGIN:
                code = LoginState.PAAS_LOGIN;
                break;
            case PRIVATE_LOGIN:
                code = LoginState.PRIVATE_LOGIN;
                break;
            case ENTER_MEETING_ROOM:
                code = LoginState.ENTER_MEETING_ROOM;
                break;
            default:
                code = LoginState.ERROR;
                break;
        }
        return code;
    }

    private void checkRoomId(String roomId) {
        try {
            Long.parseLong(roomId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("roomId must be number!");
        }
    }

    /**
     * 匿名进入会议室
     *
     * @param roomId   房间ID
     * @param nickName 昵称
     * @param password 密码   (可选参数，可以为null)
     * @param callback 加入会议室结果的回调
     */
    public void loginRoomId(String roomId, String nickName, String password, boolean isAnonymous, JoinMeetingCallback callback) {
        assert isInit : "sdk未初始化,需要先初始化sdk";
        checkRoomId(roomId);
        LoginManager loginManager = SdkUtil.getLoginManager();
        JoinMeetingParam param = new JoinMeetingParam(Long.parseLong(roomId), nickName, password, null, isAnonymous);
        loginManager.loginRoomId(param, callback);
    }

    /**
     * 初始化log(务必在获取权限之后调用一次)
     */
    public void initLogger(Context context) {
        sdkManager.initLogger(context);
    }
}
