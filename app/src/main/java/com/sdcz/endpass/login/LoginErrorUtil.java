package com.sdcz.endpass.login;

import androidx.annotation.StringRes;

import com.sdcz.endpass.R;

/**
 * 登录、入会时错误码提示
 */
public class LoginErrorUtil {

    private static final int CONFIG_CENTER_TIME_OUT= 666;
    private static final int UNKNOWN_ERROR= 4097;
    private static final int EXCEPTION= 4098;
    private static final int SERVICE_ERROR= 4099;
    private static final int INVALID_SESSION= 4100;
    private static final int USER_NOT_FOUND= 4101;
    private static final int ACCESS_DENIED= 4103;
    private static final int PARAM_ERROR= 4104;
    private static final int SYSTEM_MAINTENANCE= 4105;
    private static final int TOO_ACCESS= 4106;
    private static final int UNAUTHORIZED= 6145;
    private static final int UNSUPPORTED= 6146;
    private static final int PASSWORD_ERROR=8449;
    private static final int ROOM_NOT_FOUND=8450;
    private static final int ROOM_FULL=8451;
    private static final int APP_NOT_MATCH=8452;
    private static final int SERVICE_NOT_MATCH=8453;
    private static final int SERVICE_NOT_FOUND=8454;
    private static final int SERVICE_FULL=8455;
    private static final int USER_EXISTED=8456;
    private static final int NEED_ROOM_PASSWORD=8457;
    private static final int ROOM_LOCKED=8458;
    private static final int ROOM_EXPIRED=8459;
    private static final int ROOM_CLOSED=8460;
    private static final int ADMIN_REFUSE=8461;
    private static final int BLACK_LIST_REFUSE=8462;
    private static final int SERVICE_FAILED=8463;
    private static final int SERVICE_NOT_START=8464;
    private static final int SERVICE_STOP=8465;
    private static final int NEED_PAYMENT=8466;
    private static final int PRODUCT_STOP=8467;
    private static final int ROOM_NOT_EXITED=8469;
    private static final int ONLY_USER_PASSWORD=8470;
    private static final int INVITE_CODE_ERROR=8473;
    private static final int INVITE_CODE_EXPIRED=8474;
    private static final int NOT_SUPPORT_APP=8475;
    private static final int MEETING_NOT_START=8476;
    private static final int PRODUCT_FULL=8477;
    private static final int PRODUCE_OCCUPY=8483;
    private static final int ACCOUNT_PWD_ERROR=400;
    private static final int CLIENT_ERROR=401;
    private static final int CLIENT_INFO_ERROR=4102;
    private static final int LOGIN_PASS_FAIL=30;
    private static final int MEETING_NOT_FOUND=20401;

    @StringRes
    public static int getErrorSting(int errorCode) {
        switch (errorCode) {
            case EXCEPTION:
                return R.string.EXCEPTION;
            case CONFIG_CENTER_TIME_OUT:
                return R.string.CONFIG_CENTER_TIME_OUT;
            case SERVICE_ERROR:
                return R.string.SERVICE_ERROR;
            case INVALID_SESSION:
                return R.string.INVALID_SESSION;
            case USER_NOT_FOUND:
                return R.string.USER_NOT_FOUND;
            case ACCESS_DENIED:
                return R.string.ACCESS_DENIED;
            case PARAM_ERROR:
                return R.string.PARAM_ERROR;
            case SYSTEM_MAINTENANCE:
                return R.string.SYSTEM_MAINTENANCE;
            case TOO_ACCESS:
                return R.string.TOO_ACCESS;
            case UNAUTHORIZED:
                return R.string.UNAUTHORIZED;
            case UNSUPPORTED:
                return R.string.UNSUPPORTED;
            case PASSWORD_ERROR:
                return R.string.PASSWORD_ERROR;
            case ROOM_NOT_FOUND:
                return R.string.ROOM_NOT_FOUND;
            case ROOM_FULL:
                return R.string.ROOM_FULL;
            case APP_NOT_MATCH:
                return R.string.APP_NOT_MATCH;
            case SERVICE_NOT_MATCH:
                return R.string.SERVICE_NOT_MATCH;
            case SERVICE_NOT_FOUND:
                return R.string.SERVICE_NOT_FOUND;
            case SERVICE_FULL:
                return R.string.SERVICE_FULL;
            case USER_EXISTED:
                return R.string.USER_EXISTED;
            case NEED_ROOM_PASSWORD:
                return R.string.NEED_ROOM_PASSWORD;
            case ROOM_LOCKED:
                return R.string.ROOM_LOCKED;
            case ROOM_EXPIRED:
                return R.string.ROOM_EXPIRED;
            case ROOM_CLOSED:
                return R.string.ROOM_CLOSED;
            case ADMIN_REFUSE:
                return R.string.ADMIN_REFUSE;
            case BLACK_LIST_REFUSE:
                return R.string.BLACK_LIST_REFUSE;
            case SERVICE_FAILED:
                return R.string.SERVICE_FAILED;
            case SERVICE_NOT_START:
                return R.string.SERVICE_NOT_START;
            case SERVICE_STOP:
                return R.string.SERVICE_STOP;
            case NEED_PAYMENT:
                return R.string.NEED_PAYMENT;
            case PRODUCT_STOP:
                return R.string.PRODUCT_STOP;
            case ROOM_NOT_EXITED:
                return R.string.ROOM_NOT_EXITED;
            case ONLY_USER_PASSWORD:
                return R.string.ONLY_USER_PASSWORD;
            case INVITE_CODE_ERROR:
                return R.string.INVITE_CODE_ERROR;
            case INVITE_CODE_EXPIRED:
                return R.string.INVITE_CODE_EXPIRED;
            case NOT_SUPPORT_APP:
                return R.string.NOT_SUPPORT_APP;
            case MEETING_NOT_START:
                return R.string.MEETING_NOT_START;
            case PRODUCT_FULL:
                return R.string.PRODUCT_FULL;
            case PRODUCE_OCCUPY:
                return R.string.PRODUCE_OCCUPY;
            case ACCOUNT_PWD_ERROR:
                return R.string.ACCOUNT_PWD_ERROR;
            case CLIENT_ERROR:
            case CLIENT_INFO_ERROR:
                return R.string.CLIENT_INFO_ERROR;
            case LOGIN_PASS_FAIL:
                return R.string.LOGIN_PASS_FAIL;
            case MEETING_NOT_FOUND:
                return R.string.MEETING_NOT_FOUND;
            default:
                return R.string.UNKNOWN_ERROR;
        }
    }
}
