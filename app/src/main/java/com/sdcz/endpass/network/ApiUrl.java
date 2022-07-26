package com.sdcz.endpass.network;



import com.alibaba.fastjson.JSONObject;
import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.bean.BaseResponse;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannelTypeBean;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.bean.PosBean;
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

    /// type 0 name为key , type 1 name为key
    @GET("api/getAllUser")
    Observable<BaseResponse<Object>> getAllUser(@Query("type") int type);

    @GET("api/getChannelByCode")
    Observable<BaseResponse<ChannelBean>> getChannelByCode(@Query("channelCode") String channelCode);

    @GET("api/checkChannelAdmin")
    Observable<BaseResponse<Boolean>> checkChannelAdmin(@Query("channelCode") String channelCode);
//
    @GET("api/dismissChannel")
    Observable<BaseResponse<Object>> deletChannel(@Query("channelCode") String channelCode);

    @POST("api/addChannel")
    Observable<BaseResponse<ChannelBean>> addChannel(@Body JSONObject body);

    @POST("api/addChannelUser")
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
    Observable<BaseResponse<String>> creatRecord(@Query("recordType") int recordType, @Query("recordCode") Long recordCode , @Query("sendUserId") Long sendUserId );
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
    @POST("api/setVenue")
    Observable<BaseResponse<Object>> setVenue(@Body JSONObject body);

    @POST("api/setMute")
    Observable<BaseResponse<Object>> setMute(@Body JSONObject body);

    @POST("api/cancelMute")
    Observable<BaseResponse<Object>> cancelMute(@Body JSONObject body);

    @POST("api/setALLMute")
    Observable<BaseResponse<Object>> setAllMute(@Body JSONObject body);

    @POST("api/cancelALLMute")
    Observable<BaseResponse<Object>> cancelAllMute(@Body JSONObject body);

//    @POST("api/updateUserRealname")
//    @FormUrlEncoded
//    Observable<BaseResponse<Object>> updateUserRealname(@Field("userId") String userId, @Field("realname") String realname);

    @GET("api/updateChannel")
    Observable<BaseResponse<Object>> updateChannel(@Query("channelCode") String channelCode, @Query("channelName") String channelName);

    @GET("api/getUserLocationRecord")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Observable<BaseResponse<List<PosBean>>> getUserLocationRecord();

    @POST("api/uploadLocation")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Observable<BaseResponse<Object>> uploadLocation(@Body JSONObject body);


    @GET("api/checkRoomType")
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    Observable<BaseResponse<ChannelTypeBean>> getChannelTypeByCode(@Query("inviteCode") Long inviteCode);


}
