package com.sdcz.endpass.network;

import com.sdcz.endpass.Constants;
import com.sdcz.endpass.util.SharedPrefsUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor{

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = SharedPrefsUtil.getString(Constants.SharedPreKey.Token); //SpUtils是SharedPreferences的工具类，自行实现
        if (null == token || token.isEmpty()) {
            Request originalRequest = chain.request();
            return chain.proceed(originalRequest);
        }else {
            Request originalRequest = chain.request();
            Request updateRequest = originalRequest.newBuilder().header("Authorization", token).build();
            return chain.proceed(updateRequest);
        }
    }
}
