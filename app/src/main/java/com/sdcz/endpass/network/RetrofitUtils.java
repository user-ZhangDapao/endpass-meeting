package com.sdcz.endpass.network;



import com.sdcz.endpass.Constants;

import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Retrofit封装
 */
public class RetrofitUtils {
    private static final String TAG = "RetrofitUtils";
    private static ApiUrl mApiUrl;

    private RetrofitUtils(){}

    /**
     * 单例模式
     * post
     */
    public static ApiUrl getApiUrl() {
        if (mApiUrl == null) {
            synchronized (RetrofitUtils.class) {
                if (mApiUrl == null) {
                    mApiUrl = new RetrofitUtils().getRetrofit();
                }
            }
        }
        return mApiUrl;
    }

    public ApiUrl getRetrofit() {
        // 初始化Retrofit
        ApiUrl apiUrl = initRetrofit(initOkHttp()).create(ApiUrl.class);
        return apiUrl;
    }

    /**
     * 初始化Retrofit
     */
    @NonNull
    private Retrofit initRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                    .client(client)
                    .baseUrl(Constants.HttpKey.BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * 初始化okhttp
     */
    @NonNull
    private OkHttpClient initOkHttp() {
        return new OkHttpClient()
                .newBuilder()
                    .readTimeout(Constants.HttpKey.DEFAULT_TIME, TimeUnit.SECONDS)//设置读取超时时间
                    .connectTimeout(Constants.HttpKey.DEFAULT_TIME, TimeUnit.SECONDS)//设置请求超时时间
                    .writeTimeout(Constants.HttpKey.DEFAULT_TIME, TimeUnit.SECONDS)//设置写入超时时间
                    .addInterceptor(new TokenInterceptor())
                    .addInterceptor(new LogInterceptor())//添加打印拦截器
                    .retryOnConnectionFailure(true)//设置出现错误进行重新连接。
                    .build();
    }
}

