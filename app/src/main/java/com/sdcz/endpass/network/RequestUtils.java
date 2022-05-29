package com.sdcz.endpass.network;


import android.os.Environment;

import com.alibaba.fastjson.JSONObject;
import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.DemoApp;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.UserEntity;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


/**
 * 提交参数方式
 */
public class RequestUtils {


    /**
     * Post
     * 1.用户登录
     *
     * @param observer
     */
    public static void postLogin(String username, String password, MyObserver<String> observer) {
        JSONObject postInfo = new JSONObject();
        postInfo.put("username", username);
        postInfo.put("password", password);
        RetrofitUtils.getApiUrl()
                .postLogin(postInfo).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }


    /**
     * Post
     * 2.联系人列表
     *
     * @param observer
     */
    public static void getContactList(String deptId, MyObserver<MailListBean> observer) {
        RetrofitUtils.getApiUrl()
                .getContactList(Integer.parseInt(deptId)).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 3.查询用户收藏状态
     *
     * @param observer
     */
    public static void postCollectStatus(String collectUserId, MyObserver<Integer> observer) {
        RetrofitUtils.getApiUrl()
                .postCollectStatus(collectUserId).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 4.取消收藏
     *
     * @param observer
     */
    public static void collectUser(String collectUserId, MyObserver<Object> observer) {
        RetrofitUtils.getApiUrl()
                .collectUser(collectUserId).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 5.获取收藏列表
     *
     * @param
     */
    public static void getCollectList(MyObserver<List<UserEntity>> observer) {
        RetrofitUtils.getApiUrl()
                .getCollectList().compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 6.获取调度任务列表
     *
     * @param
     */
    public static void getChannelList(MyObserver<List<ChannelBean>> observer) {
        RetrofitUtils.getApiUrl()
                .getChannelList().compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 7.获取全部人员信息
     *
     * @param
     */
    public static void getAllUser(MyObserver<Object> observer) {
        RetrofitUtils.getApiUrl()
                .getAllUser().compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

//    /**
//     * Post
//     * 8.跟据任务编码获取 信息（包含用户列表）
//     *
//     * @param
//     */
//    public static void getChannelByCode(String channelCode, MyObserver<ChannelInfoBean> observer) {
//        RetrofitUtils.getApiUrl()
//                .getChannelByCode(channelCode).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
//                .subscribe(observer);
//    }
//
    /**
     * Post
     * 9.解散任务
     *
     * @param
     */
    public static void deletChannel(String groupId, MyObserver<Object> observer) {
        RetrofitUtils.getApiUrl()
                .deletChannel(groupId).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 10.添加任务
     *
     * @param
     */
    public static void addChannel(String channelName, String deptId, String details, MyObserver<ChannelBean> observer) {
        JSONObject postInfo = new JSONObject();
        postInfo.put("channelName",channelName);
        postInfo.put("deptId",deptId);
        postInfo.put("details",details);
        RetrofitUtils.getApiUrl()
                .addChannel(postInfo).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 11.添加任务成员
     *
     * @param
     */
    public static void addChannelUser(String channelCode, String[] userIds, MyObserver<Object> observer) {
        JSONObject postInfo = new JSONObject();
        postInfo.put("channelCode",channelCode);
        postInfo.put("userIds",userIds);
        RetrofitUtils.getApiUrl()
                .addChannelUser(postInfo).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 12.查询任务人员
     *
     * @param
     */
    public static void queryChannelUser(String channelCode, MyObserver<List<ChannerUser>> observer) {
        RetrofitUtils.getApiUrl()
                .queryChannelUser(channelCode).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 13.删除任务人员
     *
     * @param
     */
    public static void deleteChannelUser(String channelCode, String userId, MyObserver<Object> observer) {
        JSONObject postInfo = new JSONObject();
        postInfo.put("channelCode",channelCode);
        postInfo.put("userId",userId);
        RetrofitUtils.getApiUrl()
                .deleteChannelUser(postInfo).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 14.获取用户信息
     *
     * @param
     */
    public static void getUser(String userId, MyObserver<UserEntity> observer) {
        RetrofitUtils.getApiUrl()
                .getUser(userId).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 15.修改用户信息
     *
     * @param observer
     */
    public static void updateUser(String nickName, String phonenumber,String userCode, String lon, String lat, MyObserver<Object> observer) {
        JSONObject postInfo = new JSONObject();
        postInfo.put("nickName",nickName);
        postInfo.put("phonenumber",phonenumber);
        postInfo.put("userCode",userCode);
        postInfo.put("lon",lon);
        postInfo.put("lat",lat);
        RetrofitUtils.getApiUrl()
                .updateUser(postInfo).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 16.修改用户头像
     *
     * @param observer
     */
    public static void updateHeadImg(JSONObject info, MyObserver<Object> observer) {
        RetrofitUtils.getApiUrl()
                .updateHeadImg(info).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }
  /**
     * Post
     * 16.修改用户头像
     *
     * @param observer
     */
    public static void updateImg(String fileUrl, MyObserver<Object> observer) {
        File file = new File(fileUrl);
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part =  MultipartBody.Part.createFormData("avatarfile", file.getName(), body);


        RetrofitUtils.getApiUrl()
                .uploadImage(part).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 17.修改密码
     *
     * @param observer
     */
    public static void changePassWord(String password, String newPassword, MyObserver<Object> observer) {
        RetrofitUtils.getApiUrl()
                .changePassWord(password, newPassword).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

    /**
     * Post
     * 18.创建临时会话
     *
     * @param observer
     */
    public static void creatRecord(String recordType, MyObserver<String> observer) {
        RetrofitUtils.getApiUrl()
                .creatRecord(recordType).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }
//
    /**
     * Post
     * 19.模糊搜索联系人
     *
     * @param observer
     */
    public static void getUserByNameLike(String name, MyObserver<List<UserEntity>> observer) {
        RetrofitUtils.getApiUrl()
                .getUserByNameLike(name).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }
//
//    /**
//     * Post
//     * 20.临时会话记录
//     *
//     * @param observer
//     */
//    public static void updateRecord(String userId, String code, String agreeStatus, MyObserver<Object> observer) {
//        RetrofitUtils.getApiUrl()
//                .updateRecord(userId, code, agreeStatus).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
//                .subscribe(observer);
//    }
//
//    /**
//     * Post
//     * 21.离开临时会话记录
//     *
//     * @param observer
//     */
//    public static void updateRecordLeaveTime(String code, MyObserver<Object> observer) {
//        RetrofitUtils.getApiUrl()
//                .updateRecordLeaveTime(code).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
//                .subscribe(observer);
//    }
//
//    /**
//     * Post
//     * 22.离开临时会话记录
//     *
//     * @param observer
//     */
//    public static void setVenue(String channelCode, String userId, MyObserver<Object> observer) {
//        RetrofitUtils.getApiUrl()
//                .setVenue(channelCode, userId).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
//                .subscribe(observer);
//    }
//
//    /**
//     * Post
//     * 23.设置静音
//     *
//     * @param observer
//     */
//    public static void setMute(String channelCode, String userId, MyObserver<Object> observer) {
//        RetrofitUtils.getApiUrl()
//                .setMute(channelCode, userId).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
//                .subscribe(observer);
//    }
//
//    /**
//     * Post
//     * 24.取消静音
//     *
//     * @param observer
//     */
//    public static void cancelMute(String channelCode, String userId, MyObserver<Object> observer) {
//        RetrofitUtils.getApiUrl()
//                .cancelMute(channelCode, userId).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
//                .subscribe(observer);
//    }
//
//    /**
//     * Post
//     * 25.修改昵称
//     *
//     * @param observer
//     */
//    public static void updateUserRealname(String userId, String realname, MyObserver<Object> observer) {
//        RetrofitUtils.getApiUrl()
//                .updateUserRealname(userId, realname).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
//                .subscribe(observer);
//    }

    /**
     * Post
     * 26.修改部门昵称
     *
     * @param observer
     */
    public static void updateChannel(String channelCode, String channelName, MyObserver<Object> observer) {
        RetrofitUtils.getApiUrl()
                .updateChannel(channelCode, channelName).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
                .subscribe(observer);
    }

//    /**
//     * Post
//     * 27.上传位置信息
//     *
//     * @param observer
//     */
//    public static void uploadLocation(String lon, String lat, MyObserver<Object> observer) {
//        RetrofitUtils.getApiUrl()
//                .uploadLocation(SharedPrefsUtil.getValue(DemoApp.getContext(), KeyStore.USERID, ""), lon, lat).compose(RxHelper.observableIO2Main(DemoApp.getContext()))
//                .subscribe(observer);
//    }
}

