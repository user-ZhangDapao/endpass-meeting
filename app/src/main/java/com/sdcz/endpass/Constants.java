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
        public static final String CREATERECORD_VOIDE = "1";
        public static final String CREATERECORD_VOICE = "0";


        public static final String Token = "token";
        public static final String UserInfo = "UserInfo";
        public static final String AllUser = "AllUser";

        public static final String SELECT_DEPT_NAME = "selectDeptName";
        public static final String SELECT_USER_LIST = "";
        public static final String SELECT_DEPT_ID = "selectDeptId";
        public static final String CHANNEL_CODE = "channelCode";
        public static final String CHANNEL_NAME = "channelName";

        public static final String POS_LON = "lon";
        public static final String POS_LAT = "lat";

    }

}


