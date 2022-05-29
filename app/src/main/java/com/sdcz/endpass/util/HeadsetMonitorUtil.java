package com.sdcz.endpass.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

/**
 * @Description: 监控输入设备状态 调整输入设备
 */
public class HeadsetMonitorUtil {
    private int isheadset = 2;
    private final Context context;

    public HeadsetMonitorUtil(Context context) {
        this.context = context;
    }

    /**
     * 耳机接入监听
     */
    private final BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (BluetoothProfile.STATE_DISCONNECTED
                        == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    isheadset--; //Bluetooth headset is now disconnected
                    checkoutInputType(false);
                } else {
                    isheadset++;
                    checkoutInputType(true);
                }
            } else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {
                        isheadset--;
                    } else if (intent.getIntExtra("state", 0) == 1) {
                        isheadset++;
                    }
                }
            }
        }

    };

    /**
     * @Description: 获取蓝牙耳机状态
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/30 17:28
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/30 17:28
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public boolean isBlueToothHeadsetConnected() {
        boolean retval = false;
        try {
            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            if (ba != null) {
                retval = ba.getProfileConnectionState(BluetoothProfile.HEADSET)
                        != BluetoothProfile.STATE_DISCONNECTED;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return retval;
    }

    /**
     * @Description: 切换输入设备
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/30 17:31
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/30 17:31
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void checkoutInputType(boolean retval) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (retval) {
            manager.setMode(AudioManager.MODE_NORMAL);
            manager.setBluetoothScoOn(true);
            manager.setSpeakerphoneOn(false);
            manager.startBluetoothSco();
        } else {
            if (manager.isBluetoothScoOn()) {
                manager.setBluetoothScoOn(false);
                manager.setSpeakerphoneOn(true);
                manager.stopBluetoothSco();
            }
        }
    }

    /**
     * @Description: 注册耳机状态广播监听
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/30 17:28
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/30 17:28
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void registerHeadsetPlugReceiver() {
        checkoutInputType(isBlueToothHeadsetConnected());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(headsetPlugReceiver, intentFilter);

        // for bluetooth headset connection receiver
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiver(headsetPlugReceiver, bluetoothFilter);
    }

    /**
     * @Description:解绑
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/30 17:28
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/30 17:28
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void unregisterHeadsetPlugReceiver() {
        context.unregisterReceiver(headsetPlugReceiver);
    }

    /**
     * @Description: 是否存在耳机插入
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/30 13:32
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/30 13:32
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public boolean isWiredHeadsetType() {
        AudioManager audoManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //耳机ok
        //耳机不ok
        return audoManager.isWiredHeadsetOn();
    }

    /**
     * @Description: 获取耳机状态
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/30 17:27
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/30 17:27
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public int getBluetoothHeadsetType() {
        AudioManager audoManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audoManager.isWiredHeadsetOn()) {
            return 1;
        } else {
            //耳机不ok
        }

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        //蓝牙适配器是否存在，即是否发生了错误
        if (ba == null) {
            return -1;
        } else if (ba.isEnabled()) {
            int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP);              //可操控蓝牙设备，如带播放暂停功能的蓝牙耳机
            int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET);        //蓝牙头戴式耳机，支持语音输入输出
            int health = ba.getProfileConnectionState(BluetoothProfile.HEALTH);          //蓝牙穿戴式设备
            //查看是否蓝牙是否连接到三种设备的一种，以此来判断是否处于连接状态还是打开并没有连接的状态
            int flag = -1;
            if (a2dp == BluetoothProfile.STATE_CONNECTED) {
                flag = a2dp;
                if (flag != -1) {
                    return 2;
                }
            } else if (headset == BluetoothProfile.STATE_CONNECTED) {
                flag = headset;
                if (flag != -1) {
                    return 3;
                }
            } else if (health == BluetoothProfile.STATE_CONNECTED) {
                flag = health;       //connected
                if (flag != -1) {
                    return 4;
                }
            }
        }
        return -2;
    }
}
