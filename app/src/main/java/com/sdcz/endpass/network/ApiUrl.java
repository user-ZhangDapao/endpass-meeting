package com.sdcz.endpass.network;



import com.alibaba.fastjson.JSONObject;
import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.bean.BaseResponse;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.UserEntity;


import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Post
 */
public interface ApiUrl {

    @POST("/login")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Observable<BaseResponse<String>> postLogin(@Body JSONObject info);

    @GET("api/getDeptUser")
    Observable<BaseResponse<MailListBean>> getContactList(@Query("deptId") long deptId);

    @GET("api/getCollectStatus")
    Observable<BaseResponse<Integer>> postCollectStatus(@Query("collectUserId") String collectUserId);

    @GET("api/collectUser")
    Observable<BaseResponse<Object>> collectUser(@Query("collectUserId") String collectUserId);

    @GET("api/getCollectList")
    Observable<BaseResponse<List<UserEntity>>> getCollectList();

    @GET("api/getChannelList")
    Observable<BaseResponse<List<ChannelBean>>> getChannelList();

    @GET("api/getAllUser")
    Observable<BaseResponse<Object>> getAllUser();

//    @POST("api/getChannelByCode")
//    @FormUrlEncoded
//    Observable<BaseResponse<ChannelInfoBean>> getChannelByCode(@Field("channelCode") String channelCode);
//
    @GET("api/dismissChannel")
    Observable<BaseResponse<Object>> deletChannel(@Query("channelCode") String channelCode);

    @POST("api/addChannel")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Observable<BaseResponse<ChannelBean>> addChannel(@Body JSONObject body);

    @POST("api/addChannelUser")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Observable<BaseResponse<Object>> addChannelUser(@Body JSONObject body);

    @GET("api/queryChannelUser")
    Observable<BaseResponse<List<ChannerUser>>> queryChannelUser(@Query("channelCode") String channelCode);


    @POST("api/deleteChannelUser")
    Observable<BaseResponse<Object>> deleteChannelUser(@Body JSONObject body);

    @GET("api/getUserInfo")
    Observable<BaseResponse<UserEntity>> getUser(@Query("userId") String userId);

    @POST("api/updateUser")
    Observable<BaseResponse<Object>> updateUser(@Body JSONObject body);

    @POST("system/user/profile/avatar")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Observable<BaseResponse<Object>> updateHeadImg(@Body JSONObject body);

    @Multipart
    @POST("system/user/profile/avatar")
    Observable<BaseResponse<Object>> uploadImage(@Part MultipartBody.Part parts);

    @PUT("/system/user/profile/updatePwd")
    @Multipart
    Observable<BaseResponse<Object>> changePassWord(@Part("oldPassword") String newPassword, @Part("newPassword") String oldPassword);
//
    @GET("api/creatRecord")
    Observable<BaseResponse<String>> creatRecord(@Query("recordType") String recordType);
//
    @GET("api/getUserByNameLike")
    Observable<BaseResponse<List<UserEntity>>> getUserByNameLike(@Query("name") String name);
//
//    @POST("api/updateRecord")
//    @FormUrlEncoded
//    Observable<BaseResponse<Object>> updateRecord(@Field("userId") String userId, @Field("code") String code, @Field("agreeStatus") String agreeStatus);
//
//    @POST("api/updateRecordLeaveTime")
//    @FormUrlEncoded
//    Observable<BaseResponse<Object>> updateRecordLeaveTime(@Field("code") String code);
//
//    @POST("api/setVenue")
//    @FormUrlEncoded
//    Observable<BaseResponse<Object>> setVenue(@Field("channelCode") String channelCode, @Field("userId") String userId);
//
//    @POST("api/setMute")
//    @FormUrlEncoded
//    Observable<BaseResponse<Object>> setMute(@Field("channelCode") String channelCode, @Field("userId") String userId);
//
//    @POST("api/cancelMute")
//    @FormUrlEncoded
//    Observable<BaseResponse<Object>> cancelMute(@Field("channelCode") String channelCode, @Field("userId") String userId);
//
//    @POST("api/updateUserRealname")
//    @FormUrlEncoded
//    Observable<BaseResponse<Object>> updateUserRealname(@Field("userId") String userId, @Field("realname") String realname);

    @GET("api/updateChannel")
    Observable<BaseResponse<Object>> updateChannel(@Query("channelCode") String channelCode, @Query("channelName") String channelName);

//    @POST("api/uploadLocation")
//    @FormUrlEncoded
//    Observable<BaseResponse<Object>> uploadLocation(@Field("userId") String userId, @Field("lon") String lon, @Field("lat") String lat);

}
