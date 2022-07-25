package com.sdcz.endpass.ui;

import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.RoomTypeAdapter;
import com.sdcz.endpass.dialog.InputPasswordDialog;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.gps.EasyPermissions;
import com.sdcz.endpass.gps.FSKConfig;
import com.sdcz.endpass.gps.FSKDecoder;
import com.sdcz.endpass.gps.FSKEncoder;
import com.sdcz.endpass.gps.PosService;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.presenter.RoomListViewModel;
import com.google.gson.Gson;
import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.base.sdk.roomlist.RoomListManager;
import com.inpor.sdk.PlatformConfig;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;
import com.inpor.sdk.repository.BaseResponse;
import com.inpor.sdk.repository.bean.InstantMeetingInfo;
import com.sdcz.endpass.util.PermissionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class RoomListActivity extends AppCompatActivity {

    public static final String EXTRA_PWD = "password";
    private static final String TAG = "RoomList-Act";
    private ImageView ivBack;
    private ImageView ivSearch;
    private TextView tvTitle;
    private TextView tvCreateMeeting;
    private TextView tvJoinMeeting;
    private RadioGroup rgMeeting;
    private RoomTypeAdapter roomTypeAdapter;
    private ViewPager2 vpRoomType;
    private LoadingDialog loadingDialog;
    private InputPasswordDialog inputPasswordDialog;
    private int errorPwdCount = 0;
    private InstantMeetingInfo mRoomInfo;
    private TextView tvRecord;



    /*----- add by zlcats 20220522 begin */

    private final int duration = 1; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 10086;// 9886; // hz
    private final byte generatedSnd[] = new byte[2 * numSamples];


    private String recvGPSStr = "";
    private  String aFinalStr ="";

    private String myEncoderData = null;

    protected FSKConfig mConfig;
    protected FSKEncoder mEncoder;
    protected FSKDecoder mDecoder;
    protected AudioTrack mAudioTrack;
    protected AudioTrack zGiveLocTrack;
    protected AudioRecord mRecorder;
    protected int mBufferSize = 0;

    @SuppressLint("MissingPermission")
    protected Runnable zGvLocThread = new Runnable() {
        @Override
        public void run() {
            Log.i("zDebug212", "into zGv thread2");
            zGiveLocTrack.write(generatedSnd, 0, generatedSnd.length);
            Log.i("zDebug212", "Write OK2");
        }
    };
    @SuppressLint("MissingPermission")
    protected Runnable mRecordFeed = new Runnable() {
        @Override
        public void run() {
            while (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                short[] data = new short[mBufferSize / 2]; //the buffer size is in bytes
                // gets the audio output from microphone to short array samples
                Log.e("在门口","Recoder thread recv");
                mRecorder.read(data, 0, mBufferSize / 2);
                mDecoder.appendSignal(data);
            }
        }
    };


    @Override
    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);
        initView();
        initListener();
        roomTypeAdapter = new RoomTypeAdapter(getSupportFragmentManager(), getLifecycle());
        vpRoomType.setAdapter(roomTypeAdapter);
        loadingDialog = new LoadingDialog(this);

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
        //2022-05-14 add byzlcat
        /// INIT FSK DECODER
        mDecoder = new FSKDecoder(mConfig, new FSKDecoder.FSKDecoderCallback() {
            //          1         2         3
            //01234567890123456789012345678901234567890123456789012345678901234*/
            //$117.747887E39.112629N55.6m#

            public boolean isInteger(String str) {
                Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                return pattern.matcher(str).matches();
            }

            public String trancIncome(String gpsInfo) {
                String aStr = "", retStr = "";
                String vGps = gpsInfo;

                if ((vGps.contains("$")) && (vGps.contains("E")) && (vGps.contains("N")))
                {
                    retStr = "FullStr: " + vGps + "\n";
                    int vPosiE, vPosiN;
                    vPosiE = vGps.indexOf("E");
                    vPosiN = vGps.indexOf("N");
                    if (vPosiE >4 && vPosiN >20) {
                        retStr = retStr + " E: " + vGps.substring(1, vPosiE) + "\n";
                        retStr = retStr + " N: " + vGps.substring(vPosiE + 1, vPosiN) + "\n";
                    }
                } else {
                    retStr = "recvGpsInfo : " + gpsInfo;
                }
                aStr = retStr + "\n";
                ToastUtils.showShort(aStr);
                return aStr;
            }

            //2022-05-14 add byzlcat end
            @Override
            public void decoded(byte[] newData) {
                final String fskRecvTxt = new String(newData);
                runOnUiThread(new Runnable() {
                    public void run() {
                        recvGPSStr = recvGPSStr + fskRecvTxt;
                        //TextView view = ((TextView) findViewById(R.id.result));
                        if (recvGPSStr.length() >= 21) {
                            aFinalStr = trancIncome(recvGPSStr.trim());
                            Log.e("recv:",aFinalStr);
                            recvGPSStr = "";
                        } else {
                            ;//recvGPSStr =recvGPSStr + fskRecvTxt;
                        }
                    }
                });
            }
        });

        mBufferSize = AudioRecord.getMinBufferSize(FSKConfig.SAMPLE_RATE_44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mBufferSize *= 10;

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, FSKConfig.SAMPLE_RATE_44100,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);

        if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            mRecorder.startRecording();
            //start a thread to read the audio data
            Thread thread = new Thread(mRecordFeed);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
        else
        {
            Log.e("mRecoderStatus byz :",String.valueOf(mRecorder.getState()));
        }

    }
    private boolean validateMicAvailability(){
        Boolean available = true;
        @SuppressLint("MissingPermission")
        AudioRecord recorder =
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
    /*----- add by zlcats 20220522 over */
// add by zlcats 2022-05-22
    void genTone(){
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }
        int idx = 0;
        for (final double dVal : sample) {
            final short val = (short) ((dVal * 32767));
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }
    // add by zlcats 2022-05-22
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

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        rgMeeting = findViewById(R.id.radioGroup);
        ivSearch = findViewById(R.id.imgSearch);
        tvCreateMeeting = findViewById(R.id.tv_create_meeting);
        tvJoinMeeting = findViewById(R.id.tv_join_meeting);
        tvRecord = findViewById(R.id.tv_record);
        vpRoomType = findViewById(R.id.vp_room_type);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PlatformConfig.getInstance().setLoginStatus(true);
    }

    private void initListener() {
        ivBack.setOnClickListener(this::onClick);
        ivSearch.setOnClickListener(this::onClick);
        tvCreateMeeting.setOnClickListener(this::onClick);
        tvJoinMeeting.setOnClickListener(this::onClick);
        tvRecord.setOnClickListener(this::onClick);
        rgMeeting.setOnCheckedChangeListener(this::onCheckedChange);
        vpRoomType.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case RoomListViewModel.TYPE_CLOUD_ROOM:
                        rgMeeting.check(R.id.rbMeetingRoom);
                        ivSearch.setVisibility(View.VISIBLE);
                        break;
                    case RoomListViewModel.TYPE_SCHEDULE_ROOM:
                        rgMeeting.check(R.id.rbMeetingSchedule);
                        ivSearch.setVisibility(View.VISIBLE);
                        break;
                    case RoomListViewModel.TYPE_INSTANT_ROOM:
                        rgMeeting.check(R.id.rbMeetingInstant);
                        ivSearch.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }
    private static final long DOUBLE_CLICK_INTERVAL_TIME = 1 * 1000;
    private int lastClickId;
    private long lastClickTime;

    private void onClick(View view) {
        int id = view.getId();
        long currentClickTime = System.currentTimeMillis();
        if (lastClickId == id && currentClickTime - lastClickTime < DOUBLE_CLICK_INTERVAL_TIME) {
            return;
        }
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.imgSearch) {
            Intent intent = new Intent(this, RoomSearchActivity.class);
            intent.putExtra(RoomSearchActivity.EXTRA_TYPE,vpRoomType.getCurrentItem());
            startActivity(intent);
        } else if (id == R.id.tv_create_meeting) {
            loadingDialog.show();
            String nickName = PlatformConfig.getInstance().getUserName();
            RoomListManager manager = SdkUtil.getRoomListManager();
            String meetingName = String.format(getString(R.string.create_instant_meeting_format), nickName);
            manager.createInstantMeeting(meetingName, Collections.emptyList(), 2,
                    30, "", "", new IRoomListResultInterface<BaseResponse<InstantMeetingInfo>>() {
                        @Override
                        public void failed(int code, String errorMsg) {
                            Log.i(TAG, "failed: code is " + code);
                            Log.i(TAG, "failed: errorMsg is " + errorMsg);
                            ToastUtils.showShort(R.string.instant_meeting_create_fail);
                            loadingDialog.dismiss();
                        }

                        @Override
                        public void succeed(BaseResponse<InstantMeetingInfo> result) {
                            Log.i(TAG, "succeed: result is " + new Gson().toJson(result));
                            if (result.getResCode() != 1) {
                                ToastUtils.showShort(result.getResMessage());
                                loadingDialog.dismiss();
                                return;
                            }
                            mRoomInfo = result.getResult();
                            /*
                            List<String> permissionList = PermissionUtils.requestMeetingPermission();
                            if (permissionList != null && !permissionList.isEmpty()) {
                                PermissionUtils.requestPermission(RoomListActivity.this,permissionList);
                                return;
                            }*/
                            joinMeeting();
                        }
                    });
        } else if (id == R.id.tv_join_meeting) {
            Intent intent = new Intent(this, JoinMeetingActivity.class);
            startActivity(intent);
        }else if (id == R.id.tv_record) {
           // ToastUtils.showShort("点击了录音");
            getLocInfo();

        }
        lastClickId = id;
        lastClickTime = System.currentTimeMillis();
    }

    private void joinMeeting() {
        if (mRoomInfo == null) {
            return;
        }
        String userName = PlatformConfig.getInstance().getUserName();
        JoinMeetingManager.getInstance().loginRoomId(String.valueOf(mRoomInfo.getInviteCode()), userName, "",
                false, new JoinMeetingCallback() {

                    @Override
                    public void onStart(Procedure procedure) {
                        loadingDialog.updateText(R.string.logging);
                    }

                    @Override
                    public void onState(int state, String msg) {
                        loadingDialog.updateText(LoginStateUtil.convertStateToString(state));
                    }

                    @Override
                    public void onBlockFailed(ProcessStep step, int code, String msg) {
                        ToastUtils.showShort(LoginErrorUtil.getErrorSting(code));
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailed() {
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onInputPassword(boolean isFrontVerify, InputPassword inputPassword) {
                        showInputPasswordDialog(isFrontVerify, inputPassword);
                    }

                    @Override
                    public void onSuccess() {
                        loadingDialog.dismiss();

                        Intent intent = new Intent(RoomListActivity.this, MobileMeetingActivity.class);
                        intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN,false);
                        startActivity(intent);
                    }
                });
    }

    private void showInputPasswordDialog(boolean isFrontVerify, InputPassword inputPassword) {
        Disposable disposable = Flowable.just(inputPassword)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(input -> {
                    if (this.inputPasswordDialog == null) {
                        inputPasswordDialog = InputPasswordDialog.showInputPwdDialog(this);
                        inputPasswordDialog.setButtonCallback((dialog, which) -> {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                loadingDialog.dismiss();
                            }
                        });
                    }
                    inputPasswordDialog.update(isFrontVerify, inputPassword);
                    if (errorPwdCount > 0) {
                        ToastUtils.showShort(R.string.check_password);
                    }
                    this.inputPasswordDialog.show();
                    errorPwdCount++;
                });
    }

    private void onCheckedChange(RadioGroup radioGroup, int i) {
        if (i == R.id.rbMeetingRoom) {
            vpRoomType.setCurrentItem(RoomListViewModel.TYPE_CLOUD_ROOM);
        } else if (i == R.id.rbMeetingSchedule) {
            vpRoomType.setCurrentItem(RoomListViewModel.TYPE_SCHEDULE_ROOM);
        } else if (i == R.id.rbMeetingInstant) {
            vpRoomType.setCurrentItem(RoomListViewModel.TYPE_INSTANT_ROOM);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            JoinMeetingManager.getInstance().initLogger(this);
            joinMeeting();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlatformConfig.getInstance().setLoginStatus(false);


    }


}