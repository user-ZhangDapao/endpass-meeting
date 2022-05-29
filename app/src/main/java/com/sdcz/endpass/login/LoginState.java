package com.sdcz.endpass.login;

public interface LoginState {
    /**
     * 异常状态
     */
    int ERROR = -1;
    /**
     * 获取版本号
     */
    int GETTING_VERSION = 0x01;
    /**
     * 连接服务器
     */
    int CONNECTING_SERVER = GETTING_VERSION + 1;
    /**
     * 登录会议室
     */
    int LOGIN_ROOM = CONNECTING_SERVER + 1;
    /**
     * 会议室被锁定
     */
    int ROOM_LOCKED_WAITING = LOGIN_ROOM + 1;
    /**
     * 初始化会议室组件
     */
    int INITIALIZING_COMPONENT = ROOM_LOCKED_WAITING + 1;
    /**
     * 初始化音频设备
     */
    int INITIALIZING_AUDIO_DEVICE = INITIALIZING_COMPONENT + 1;
    /**
     * 初始化视频设备
     */
    int INITIALIZING_VIDEO_DEVICE = INITIALIZING_AUDIO_DEVICE + 1;
    /**
     * 初始化房间
     */
    int INITIALIZING_ROOM = INITIALIZING_VIDEO_DEVICE + 1;
    /**
     * 登录分组会议室
     */
    int LOGIN_GROUP_ROOM = INITIALIZING_ROOM + 1;
    /**
     * 登录主会议会议室
     */
    int LOGIN_MAIN_ROOM = LOGIN_GROUP_ROOM + 1;
    /**
     * 用户校验成功
     */
    int USER_VERIFY_SUCCESS = LOGIN_MAIN_ROOM + 1;
    /**
     * 房间校验成功
     */
    int ROOM_VERIFY_SUCCESS = USER_VERIFY_SUCCESS + 1;
    /**
     * 配置中心
     */
    int CONFIG_CENTER = ROOM_VERIFY_SUCCESS + 1;
    /**
     * 获取接口信息
     */
    int GET_ADDRESS = CONFIG_CENTER + 1;
    /**
     * 获取本地配置
     */
    int GET_LOCAL_CONFIG = GET_ADDRESS + 1;
    /**
     * 获取房间信息
     */
    int GET_ROOM_INFO = GET_LOCAL_CONFIG + 1;
    /**
     * 获取网络配置
     */
    int GET_NET_CONFIG = GET_ROOM_INFO + 1;
    /**
     * 更新用户信息
     */
    int UPDATE_USER_INFO = GET_NET_CONFIG + 1;
    /**
     * 用户登录
     */
    int LOGIN = UPDATE_USER_INFO + 1;
    /**
     * 获取会议室信息
     */
    int GET_ROOM = LOGIN + 1;
    /**
     * 获取本地用户信息
     */
    int GET_LOCAL_USER_INFO = GET_ROOM + 1;
    /**
     * 获取PAAS认证信息
     */
    int GET_PAAS_OAUTH = GET_LOCAL_USER_INFO + 1;
    /**
     * 登录PAAS
     */
    int PAAS_LOGIN = GET_PAAS_OAUTH + 1;
    /**
     * 私有云登录
     */
    int PRIVATE_LOGIN = PAAS_LOGIN + 1;
    /**
     * 进入会议室
     */
    int ENTER_MEETING_ROOM = PRIVATE_LOGIN + 1;
}
