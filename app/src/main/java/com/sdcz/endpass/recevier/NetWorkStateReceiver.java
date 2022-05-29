package com.sdcz.endpass.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;



/**
 * Created by Carson_Ho on 16/10/31.
 */
public class NetWorkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("网络状态发生变化");
        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取ConnectivityManager对象对应的NetworkInfo对象
//            //获取WIFI连接的信息 wifiNetworkInfo.isConnected()
//            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (dataNetworkInfo.isConnected()) {
//                if ( FspManager.getInstance().init()){
//                    if (UserUtil.netWork == true){
                        Log.e("TAG", "网络状态发生变化: 移动数据链接" );
//                        FspManager.getInstance().login(UserUtil.userId,UserUtil.realName);
//                    }else {
//                        UserUtil.netWork = true;
//                    }
//                }
            } else {
//                FspManager.getInstance().loginOut();
                Log.e("TAG", "网络状态发生变化: 移动数据断开" );
            }
            //API大于23时使用下面的方式进行网络监听
        } else {

            System.out.println("API level 大于23");
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();
            //通过循环将网络信息逐个取出来
            for (int i=0; i < networks.length; i++){
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
            }
            //此时wifi断开
            if (TextUtils.isEmpty(sb.toString())){
//                FspManager.getInstance().loginOut();
                Log.e("TAG", "网络状态发生变化: wifi断开" );
            } else {
//                if ( FspManager.getInstance().init()){
//                    if (UserUtil.netWork == true){
//                        FspManager.getInstance().login(UserUtil.userId,UserUtil.realName);
                        Log.e("TAG", "网络状态发生变化: wifi连接" );
//                    }else {
//                        UserUtil.netWork = true;
//                    }
//                }
            }
        }
    }
}

