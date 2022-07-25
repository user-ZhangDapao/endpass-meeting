package com.sdcz.endpass.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTabHost;

import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.log.Logger;
import com.inpor.manager.beans.CompanyUserDto;
import com.inpor.manager.beans.DepartmentResultDto;
import com.inpor.manager.util.HandlerUtils;
import com.inpor.nativeapi.adaptor.OnlineUserInfo;
import com.inpor.sdk.PlatformConfig;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.online.InstantMeetingOperation;
import com.inpor.sdk.online.PaasOnlineManager;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.base.SdkBaseActivity;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.fragment.DispatchFragment;
import com.sdcz.endpass.fragment.MailListFragment;
import com.sdcz.endpass.fragment.MineFragment;
import com.sdcz.endpass.gps.FSKConfig;
import com.sdcz.endpass.gps.FSKDecoder;
import com.sdcz.endpass.gps.FSKEncoder;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginMeetingCallBack;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.presenter.MainPresenter;
import com.sdcz.endpass.recevier.NetWorkStateReceiver;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMainView;
import com.universal.clientcommon.beans.CompanyUserInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

public class MainActivityApp extends SdkBaseActivity {

    private FragmentTabHost fragmentTabHost;
    private int[] textsIDs = {R.string.tab_index, R.string.tab_dispath, R.string.tab_mine};
    private int[] imageButton = {R.drawable.tab_index_select
            , R.drawable.tab_dispatch_select, R.drawable.tab_mine_select};
//    private Class[] fragmentArray = {MailListFragment.class, DispatchFragment.class, MineFragment.class};
    private Class[] fragmentArray = {MailListFragment.class, DispatchFragment.class, MineFragment.class};
    private RelativeLayout llMain;
    private SoundPool mSoundPool;
    private NetWorkStateReceiver netWorkStateReceiver;


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
//                Log.e("在门口","Recoder thread recv");
                mRecorder.read(data, 0, mBufferSize / 2);
                mDecoder.appendSignal(data);
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        PaasOnlineManager.getInstance().setBusy(false);
        initView();
        initData();
        initListener();


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


    @Override
    protected void onResume() {
        super.onResume();
        PlatformConfig.getInstance().setLoginStatus(true);
    }



    public void initView() {
        fragmentTabHost = findViewById(android.R.id.tabhost);
        llMain = findViewById(R.id.ll_main);
        fragmentTabHost.setup(this,
                getSupportFragmentManager(),
                R.id.content);
        for (int i = 0; i < textsIDs.length; i++) {
            TabHost.TabSpec spec = fragmentTabHost.newTabSpec(getString(textsIDs[i])).setIndicator(getView(i));
            fragmentTabHost.addTab(spec, fragmentArray[i], null);
        }
        //      去掉分隔的竖线
        fragmentTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        mSoundPool.load(this, R.raw.tips, 1);
    }

    public void initData() {
//        PlayerManager.getManager().init(getContext());
//        PlayerManager.getManager().changeToSpeakerMode();
        getAllUser(this);
    }


    public void initListener() {
//        registerReceiver(headSetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        starFrameworklistener();
        SdkUtil.getContactManager().queryDeptInfo(1, 1000, new IRoomListResultInterface<Object>() {
            @Override
            public void failed(int code, String errorMsg) {
                Log.e("navi","queryCompanyDepartmentThenCompanyUsers failed-------->"+code);
            }

            @Override
            public void succeed(Object result) {
                Log.e("navi","queryCompanyDepartmentThenCompanyUsers succeed");
                if(result instanceof DepartmentResultDto){
                    Log.e("navi","queryCompanyDepartmentThenCompanyUsers DepartmentResultDto");
                    handleDepartmentLogic(((DepartmentResultDto)result));
                }else {
                    handleCompanyUserLogic(((CompanyUserDto)result));
                }
            }

            private void handleDepartmentLogic(DepartmentResultDto departmentResultDto) {
                if (departmentResultDto.getCode() == 20822) {
                    Log.e("navi", "RESULT_CODE_ERROR_NO_PERMISSION");
                } else {
                    if (departmentResultDto.getResult() != null) {
                        Log.e("navi", "succeed");
                        InstantMeetingOperation.getInstance()
                                .setDepartmentData(departmentResultDto.getResult());
                    }
                }
            }

            private void handleCompanyUserLogic(CompanyUserDto companyUserDto) {
                if (companyUserDto.getCode() == 20822) {
                    HandlerUtils.postToMain(() -> {
                    });
                } else {
                    if (companyUserDto.getResult() != null) {

                        Log.e("navi", "queryCompanyUsers succeed");
                        int currentPage = companyUserDto.getResult().getCurrentPage();
                        Logger.info("TAG", "get queryUserCallback is success, page is :" + currentPage);
                        Log.e("navi", "queryCompanyUsers succeed -------------22222");
                        InstantMeetingOperation.getInstance().addCompanyUserData(companyUserDto.getResult().getItems(),
                                    PlatformConfig.getInstance().getCurrentUserInfo().getUserId(), true);
                    }
                }
            }
        });

    }

    private View getView(int i) {
        //取得布局实例
        View view = View.inflate(MainActivityApp.this, R.layout.tab_content, null);
        //取得布局对象
        ImageView imageView = view.findViewById(R.id.image);
        ImageView redPoint = view.findViewById(R.id.redPoint);
        TextView textView = view.findViewById(R.id.text);
        RelativeLayout rlTip = view.findViewById(R.id.rlTip);
        //设置图标
        imageView.setImageResource(imageButton[i]);
        //设置标题
        textView.setText(getString(textsIDs[i]));
        return view;
    }

    public void setTab(int tab){
        fragmentTabHost.setCurrentTab(tab);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netWorkStateReceiver);
//        unregisterReceiver(headSetReceiver);
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void starFrameworklistener() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
        System.out.println("注册");
    }

//    private final BroadcastReceiver headSetReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String action = intent.getAction();
//
//            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
//                //headphone plugged
//                if(intent.getIntExtra("state", 0) == 1){
//                    //do something
////                    ToastUtils.show(getContext(), "插入");
//                    PlayerManager.getManager().changeToHeadsetMode();
//                    //headphone unplugged
//                }else{
//                    //do something
//                    PlayerManager.getManager().changeToSpeakerMode();
//                }
//            }
//        }
//    };



    public void getAllUser(Activity activity){

        RequestUtils.getAllUser(0,new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                SharedPrefsUtil.putString(Constants.SharedPreKey.AllUserName,getJsonStringByEntity(result));
            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });

        RequestUtils.getAllUser(1,new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                SharedPrefsUtil.putString(Constants.SharedPreKey.AllUserId,getJsonStringByEntity(result));
            }
            @Override
            public void onFailure(Throwable e, String errorMsg) {

            }
        });
    }

    public static String getJsonStringByEntity(Object o) {
        String strJson = "";
        Gson gson = new Gson();
        strJson = gson.toJson(o);
        return strJson;
    }

}