package com.sdcz.endpass.ui.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
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

import androidx.fragment.app.FragmentTabHost;

import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.kit.workflow.Procedure;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.fragment.DispatchFragment;
import com.sdcz.endpass.fragment.MailListFragment;
import com.sdcz.endpass.fragment.MineFragment;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginMeetingCallBack;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.presenter.MainPresenter;
import com.sdcz.endpass.recevier.NetWorkStateReceiver;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMainView;

public class MainActivityApp extends BaseActivity<MainPresenter> implements IMainView {

    private FragmentTabHost fragmentTabHost;
    private int[] textsIDs = {R.string.tab_index, R.string.tab_dispath, R.string.tab_mine};
    private int[] imageButton = {R.drawable.tab_index_select
            , R.drawable.tab_dispatch_select, R.drawable.tab_mine_select};
//    private Class[] fragmentArray = {MailListFragment.class, DispatchFragment.class, MineFragment.class};
    private Class[] fragmentArray = {MailListFragment.class, DispatchFragment.class, MineFragment.class};
    private RelativeLayout llMain;
    private SoundPool mSoundPool;
    private NetWorkStateReceiver netWorkStateReceiver;


    @Override
    protected MainPresenter createPresenter() {
        return new MainPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_main_app;
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        fragmentTabHost = findViewById(android.R.id.tabhost);
        llMain = findViewById(R.id.ll_main);
        fragmentTabHost.setup(this, getSupportFragmentManager(), R.id.content);
        for (int i = 0; i < textsIDs.length; i++) {
            TabHost.TabSpec spec = fragmentTabHost.newTabSpec(getString(textsIDs[i])).setIndicator(getView(i));
            fragmentTabHost.addTab(spec, fragmentArray[i], null);
        }
        //      去掉分隔的竖线
        fragmentTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        mSoundPool.load(this, R.raw.tips, 1);
        return llMain;
    }

    @Override
    public void initData() {
        super.initData();

//        PlayerManager.getManager().init(getContext());
//        PlayerManager.getManager().changeToSpeakerMode();
        mPresenter.getAllUser(this);
        checkLogin();
    }

    private void checkLogin() {
        String userName = SharedPrefsUtil.getUserInfo().getUserName();
        JoinMeetingManager.getInstance().loginAccount("", "mdt" + userName, "mdt0"+userName, new LoginMeetingCallBack() {

            @Override
            public void onConflict(boolean isMeeting) {
            }

            @Override
            public void onStart(Procedure procedure) {
            }

            @Override
            public void onState(int state) {
            }

            @Override
            public void onBlockFailed(ProcessStep step, int code, String msg) {
                SharedPrefsUtil.clean(MainActivityApp.this);
                startActivity(new Intent(MainActivityApp.this, LoginActivityApp.class));
            }

            @Override
            public void onFailed() {
                SharedPrefsUtil.clean(MainActivityApp.this);
                startActivity(new Intent(MainActivityApp.this, LoginActivityApp.class));
            }

            @Override
            public boolean onLoginSuccess() {
                return true;
            }
        });

    }

    @Override
    public void initListener() {
        super.initListener();
//        registerReceiver(headSetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        starFrameworklistener();

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
    protected void setStatusBar() {

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

    @Override
    public void showData(Object o) {
        SharedPrefsUtil.putString(Constants.SharedPreKey.AllUserName,getJsonStringByEntity(o));
        Log.e("TAG", "showData: " + getJsonStringByEntity(o) );
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

    public static String getJsonStringByEntity(Object o) {
    String strJson = "";
    Gson gson = new Gson();
    strJson = gson.toJson(o);
    return strJson;
    }

}