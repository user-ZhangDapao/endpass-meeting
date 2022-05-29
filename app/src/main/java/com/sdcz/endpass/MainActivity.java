package com.sdcz.endpass;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sdcz.endpass.databinding.ActivityMainBinding;
import com.sdcz.endpass.ui.activity.LoginActivityApp;
import com.sdcz.endpass.ui.activity.MainActivityApp;
import com.sdcz.endpass.util.SharedPrefsUtil;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private Animation rotateAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setOnClickListener(this::onClick);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mBinding.tvVersion.setText("V" + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        startAnim();
    }

    private void startAnim() {
        if (rotateAnim == null) {
            rotateAnim = AnimationUtils.loadAnimation(this, R.anim.main_rotate_anim);
            LinearInterpolator interpolator = new LinearInterpolator();
            rotateAnim.setInterpolator(interpolator);
        }
        if (rotateAnim != null) {
            mBinding.imSdkBg.setAnimation(rotateAnim);
        }
    }

    private void onClick(View view) {
        String token = SharedPrefsUtil.getString(Constants.SharedPreKey.Token, "");
        if (token.isEmpty()){
            startActivity(new Intent(this, LoginActivityApp.class));
        }else {
            startActivity(new Intent(this, MainActivityApp.class));
        }
        finish();
    }
}