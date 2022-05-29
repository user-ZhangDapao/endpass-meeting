package com.sdcz.endpass.gps;

import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.sdcz.endpass.R;

import java.io.IOException;

public class GPSActivity extends AppCompatActivity {

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

    protected Runnable mRecordFeed = new Runnable() {

        @Override
        public void run() {
            Log.e("111111111111",AudioRecord.RECORDSTATE_RECORDING+"=="+mRecorder.getRecordingState());
            while (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                short[] data = new short[mBufferSize / 2]; //the buffer size is in bytes
                // gets the audio output from microphone to short array samples
                mRecorder.read(data, 0, mBufferSize / 2);
                mDecoder.appendSignal(data);
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        /// INIT FSK DECODER
        mDecoder = new FSKDecoder(mConfig, new FSKDecoder.FSKDecoderCallback() {

            @Override
            public void decoded(byte[] newData) {
                final String text = new String(newData);
                runOnUiThread(new Runnable() {
                    public void run() {

                        showDialog(text);

                        Log.e("=======", "---" + text );

                        if (text.length() > 350) {

                        }
                    }
                });
            }
        });

        mBufferSize = AudioRecord.getMinBufferSize(FSKConfig.SAMPLE_RATE_44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mBufferSize *= 10;
        //again, make sure the recorder settings match the decoder settings

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, FSKConfig.SAMPLE_RATE_44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);

        Log.e("22222222",mRecorder.getRecordingState()+ "");
        if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            mRecorder.startRecording();
            //start a thread to read the audio data
            Thread thread = new Thread(mRecordFeed);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
        if (verifyPermissions()) {
            Log.i("000000000000", "onCreate() has permissions.");
            //    AfterPermissionGranted();
        }
        genTone();
        zGiveLocTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mConfig.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 4096,
                AudioTrack.MODE_STREAM);
        zGiveLocTrack.play();
        new Thread(zGvLocThread).start();
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

    private void showDialog(String result){

        AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
        builder.setIcon(R.drawable.ic_launcher_background);
        builder.setTitle("指令");
        builder.setMessage(result);
        builder.setPositiveButton("点击发送",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog=builder.create();
        dialog.show();

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
}
