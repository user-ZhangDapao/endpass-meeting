package com.sdcz.endpass;

public class Constants {
    /* clientId 缓存key */
    public static final String SP_KEY_CLIENT_ID = "sp_key_client_id";
    /* clientSecret 缓存key */
    public static final String SP_KEY_CLIENT_SECRET = "sp_key_client_secret";
    /* 会议室号登录 昵称 缓存key */
    public static final String SP_KEY_NICK_NAME = "sp_key_nick_name";
    /* 会议室号登录/用户名密码登录 会议室号或者邀请码 缓存key */
    public static final String SP_KEY_ROOM_ID = "sp_key_room_id";
    /* 会议室号登录 会议室密码 缓存key */
    public static final String SP_KEY_ROOM_PWD = "sp_key_room_pwd";
    /* 用户名密码登录 用户名 缓存key */
    public static final String SP_KEY_ACCOUNT_NAME = "sp_key_account_name";
    /* 用户名密码登录 密码 缓存key */
    public static final String SP_KEY_ACCOUNT_PWD = "sp_key_account_pwd";
    /* 服务器地址 缓存key */
    public static final String SP_KEY_SERVER_ADDRESS = "sp_key_server_address";
    /* 服务器端口号  缓存Key */
    public static final String SP_KEY_SERVER_PORT = "sp_key_server_port";

    //========================================>

    public static class HttpKey{
        //设置默认超时时间
        public static final int DEFAULT_TIME = 10;

        public static final String BASE_URL = "http://49.232.143.181:8280";

        public static final int RESPONSE_0 = 0;
        public static final int RESPONSE_200 = 200;
    }

    public static class SharedPreKey{
        public static final int REQUEST_CODE_1 = 1;
        public static final int REQUEST_CODE_2 = 2;
        public static final int REQUEST_CODE_201 = 201;
        public static final String INTENT_TYPE = "intent_type";

        public static final String DEPTID = "deptId";
        public static final String GROUPNAME = "groupName";
        public static final String DEPTNAME = "deptName";

        public static final int CREATERECORD_VOIDE = 1;
        public static final int CREATERECORD_VOICE = 0;


        public static final String Token = "token";
        public static final String UserInfo = "UserInfo";
        public static final String AllUserName = "AllUserName";
        public static final String AllUserId = "AllUserId";
        public static final String isAdmin = "isAdmin";

        public static final String SELECT_DEPT_NAME = "selectDeptName";
        public static final String SELECT_USER_LIST = "";
        public static final String SELECT_DEPT_ID = "selectDeptId";
        public static final String CHANNEL_CODE = "channelCode";
        public static final String CHANNEL_NAME = "channelName";

        public static final String POS_LON = "lon";
        public static final String POS_LAT = "lat";


        public static final String OPEN_AUDIO = "OPEN_AUDIO*";
        public static final String OPEN_AUDIO_ALL = "OPEN_AUDIO*ALL";
        public static final String OFF_AUDIO = "OFF_AUDIO*";
        public static final String OFF_AUDIO_ALL = "OFF_AUDIO*ALL";
        public static final String OPEN_VIDEO = "OPEN_VIDEO*";
        public static final String OFF_VIDEO = "OFF_VIDEO*";
        public static final String SWITCH_VIDEO = "SWITCH_VIDEO*";
        public static final String MAIN_VENUE = "MAIN_VENUE*";
        public static final String ON_LISTEN = "ON_LISTEN*";
        public static final String ON_LISTEN_ALL = "ON_LISTEN*ALL";
        public static final String OFF_LISTEN = "OFF_LISTEN*";
        public static final String OFF_LISTEN_ALL = "OFF_LISTEN*ALL";
        public static final String PLEASE_LEAVE = "PLEASE_LEAVE*";
        public static final String APPLY_LEAVE = "APPLY_LEAVE*";
        public static final String PLEASE_LEAVE_ALL = "PLEASE_LEAVE*ALL";
        public static final String ADD_CHANNEL_USER = "ADD_CHANNEL_USER";
        public static final String YES_APPLY_LEAVE = "YES_APPLY_LEAVE*";
        public static final String NO_APPLY_LEAVE = "NO_APPLY_LEAVE*";

    }

}


