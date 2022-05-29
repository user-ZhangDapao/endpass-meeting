package com.sdcz.endpass.gps;


import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: Administrator
 * CreateDate: 2021/11/5 10:43
 * Description: @ 北斗定位
 * 当连接USB时打开此service
 */
public class PosService extends Service {

    private final int duration = 1; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 10086; // hz
    private final byte generatedSnd[] = new byte[2 * numSamples];

    static Context mContext = null;
    protected FSKConfig mConfig;
    protected FSKDecoder mDecoder;
    protected AudioTrack zGiveLocTrack;


    protected AudioRecord mRecorder; //录音
    protected int mBufferSize = 0;

    private String result = "";
    protected Runnable zGvLocThread = new Runnable() {

        @Override
        public void run() {
            // TextView v2 = ((TextView) findViewById(R.id.result));
            //  v2.setText("Getting location .......");
            Log.i("zDebug", "into zGv thread");
            zGiveLocTrack.write(generatedSnd, 0, generatedSnd.length);
            Log.i("zDebug", "Write OK");
            //playSound();
        }

    };

//    protected Runnable mRecordFeed = new Runnable() {
//
//        @Override
//        public void run() {
//            Log.e("111111111111",AudioRecord.RECORDSTATE_RECORDING+"=="+mRecorder.getRecordingState());
//            while (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
//                short[] data = new short[mBufferSize / 2]; //the buffer size is in bytes
//                // gets the audio output from microphone to short array samples
//                mRecorder.read(data, 0, mBufferSize / 2);
//                mDecoder.appendSignal(data);
//            }
//        }
//    };

    private final Timer timer = new Timer();
    private TimerTask task;
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            // TODO Auto-generated method stub
//            // 要做的事情
//            genTone();
//            zGiveLocTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                    mConfig.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT, 4096,
//                    AudioTrack.MODE_STREAM);
//            zGiveLocTrack.play();
//            new Thread(zGvLocThread).start();
//            super.handleMessage(msg);
//        }
//    };


    private void showDialog(){

        AlertDialog.Builder builder=new AlertDialog.Builder(getApplicationContext());
        builder.setIcon(R.drawable.ic_launcher_background);
      //  builder.setTitle("指令");
        builder.setMessage(result);
//        builder.setPositiveButton("点击发送",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                    }
//                });
        AlertDialog dialog=builder.create();
        dialog.show();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private int i = 0;
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        initGps();

        Log.e( "==============",  "调用了oncreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mRecorder != null) {
            Log.e( "==============",  "调用了onStartCommand");
            //initGps();
        }
        Log.e( "byz",  "getLocInfo...");
        getLocInfo();
        return super.onStartCommand(intent, flags, startId);
    }
    //    // add by zlcats 2022-05-22
    void getLocInfo()
    {
        genTone();
        zGiveLocTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mConfig.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 4096,
                AudioTrack.MODE_STREAM);
        zGiveLocTrack.play();
        new Thread(zGvLocThread).start();
    }
    @SuppressLint("MissingPermission")
    private void initGps() {

        try {
            mConfig = new FSKConfig(
                    FSKConfig.SAMPLE_RATE_44100,
                    FSKConfig.PCM_16BIT,
                    FSKConfig.CHANNELS_MONO,
                    FSKConfig.SOFT_MODEM_MODE_4,
                    FSKConfig.THRESHOLD_20P);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
//
//        /// INIT FSK DECODER
//        mDecoder = new FSKDecoder(mConfig, new FSKDecoder.FSKDecoderCallback() {
//
//            @Override
//            public void decoded(byte[] newData) {
//                final String text = new String(newData);
//
//                runOnUiThread(new Runnable() {
//                    public void run() {
//
//                        result = result + text;
//                        Log.e( "==============",  result);
//                        i++;
//                        Log.e( "888888888",  "执行了" + i);
//                        if (i == 3) {
////                            mRecorder.stop();
////                            mRecorder.release();
//                            result = "";
//                            i = 0;
//                        }
//                    }
//                });
//            }
//        });
//
//
//        mBufferSize = AudioRecord.getMinBufferSize(FSKConfig.SAMPLE_RATE_44100,
//                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//        mBufferSize *= 10;
//        //again, make sure the recorder settings match the decoder settings
//
//        if (mRecorder == null) {
//            mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, FSKConfig.SAMPLE_RATE_44100,
//                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);
//        }
//
//        if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
//            mRecorder.startRecording();
//            Log.e("22222222",mRecorder.getRecordingState()+ "");
//            //start a thread to read the audio data
//            Thread thread = new Thread(mRecordFeed);
//            thread.setPriority(Thread.MAX_PRIORITY);
//            thread.start();
//        }
//
//        genTone();
//        zGiveLocTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                mConfig.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, 4096,
//                AudioTrack.MODE_STREAM);
//        zGiveLocTrack.play();
//        new Thread(zGvLocThread).start();

//        if (verifyPermissions()) {
//            Log.e("7777777",mRecorder.getRecordingState()+ "");
//        }

//        task = new TimerTask() {
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                Message message = new Message();
//                message.what = 1;
//                handler.sendMessage(message);
//            }
//        };
//
//        timer.schedule(task, 5000, 10000);

    }


    private boolean validateMicAvailability(){
        Boolean available = true;
        @SuppressLint("MissingPermission") AudioRecord recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT, 44100);
        try{
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED ){
                available = false;
            }

            recorder.startRecording();
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
                recorder.stop();
                available = false;
            }
            recorder.stop();
        } finally{
            recorder.release();
            recorder = null;
        }
        return available;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            zGiveLocTrack.stop();
            zGiveLocTrack.release();
        }
        ToastUtils.showShort("关闭gps");
    //    timer.cancel();
    }

    public static boolean verifyPermissions() {
        boolean hasPermission = true;
        if (!EasyPermissions.hasPermissions(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)) {
            EasyPermissions.requestPermissions(mContext,
                    EasyPermissions.RC_CAMERA_PERM,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO);
            hasPermission = false;
        }
        return hasPermission;
    }

    void genTone() {
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }


}

