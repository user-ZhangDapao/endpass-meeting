package com.sdcz.endpass.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.ToastUtils;
import com.jaeger.library.StatusBarUtil;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.UserInfoPresenter;
import com.sdcz.endpass.util.ContactEnterUtils;
import com.sdcz.endpass.util.GlideUtils;
import com.sdcz.endpass.util.PhoneFormatCheckUtil;
import com.sdcz.endpass.util.PopWindowUtil;
import com.sdcz.endpass.view.IUserInfoView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class UserInfoActivity extends BaseActivity<UserInfoPresenter> implements IUserInfoView, View.OnClickListener {

    private LinearLayout llRoot;
    private ImageView ivBack;
    private TextView tvTitle;
    private ImageView ivHead;
    private EditText etNikeName;
    private TextView tvName;
    private TextView tvTake;
    private TextView tvPicture;
    private TextView tvClose;
    private TextView tvDeptName;
    private EditText etPhone;
    private Button btnFinish;
    private View popuView,rootview;
    private String phoneNum;
    private String realName = "";

    //相册请求码
    private static final int ALBUM_REQUEST_CODE = 1;
    //相机请求码
    private static final int CAMERA_REQUEST_CODE = 2;
    //剪裁请求码
    private static final int CROP_REQUEST_CODE = 3;
    //调用照相机返回图片文件
    private File tempFile;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_user_info;
    }

    @Override
    protected UserInfoPresenter createPresenter() {
        return new UserInfoPresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        popuView = LayoutInflater.from(getContext()).inflate(R.layout.popup_selectimage_layout,null);
        rootview = LayoutInflater.from(getContext()).inflate(R.layout.fragment_mine, null);
        llRoot = findViewById(R.id.llRoot);
        ivBack = findViewById(R.id.ivBack);
        tvTake = popuView.findViewById(R.id.tv_take);
        tvPicture = popuView.findViewById(R.id.tv_picture);
        tvClose = popuView.findViewById(R.id.tvClose);
        tvTitle = findViewById(R.id.tvTitle);
        ivHead = findViewById(R.id.iv_head);
        etNikeName = findViewById(R.id.etNikeName);
        tvName = findViewById(R.id.tvName);
        tvDeptName = findViewById(R.id.tvDeptName);
        etPhone = findViewById(R.id.etPhone);
        btnFinish = findViewById(R.id.btnFinish);
        ivBack = findViewById(R.id.ivBack);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setText("个人信息");
        return llRoot;
    }

    @Override
    public void initData() {
        super.initData();
        showLoading();
        mPresenter.getUserInfo(this);
    }

    @Override
    public void initListener() {
        super.initListener();
        ivHead.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        tvTake.setOnClickListener(this);
        tvPicture.setOnClickListener(this);
        tvClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_head:
                PopWindowUtil.getInstance().makePopupWindow(getContext(),popuView).showLocationWithAnimation(getContext(),rootview, Gravity.BOTTOM,R.style.anim_bottonbar);
                break;
            case R.id.ivBack:
                finish();
                break;
            case R.id.btnFinish:
                String num = etPhone.getText().toString();
                String realName1 = etNikeName.getText().toString();

                if (num.equals(phoneNum) && realName1.equals(realName)){
                    finish();
                }else {
                    if (!(PhoneFormatCheckUtil.isMobile(num)) || !(PhoneFormatCheckUtil.isMobile(num))){
                        ToastUtils.showLong("联系方式 格式错误~");
                        return;
                    }
                    mPresenter.updateUser(this,  realName1,num,"","","" );
                }
                break;
            case R.id.tv_take:
                UserInfoActivityPermissionsDispatcher.getPicFromCameraWithPermissionCheck(UserInfoActivity.this);
                PopWindowUtil.getInstance().dissMiss();
                break;
            case R.id.tv_picture:
                getPicFromAlbm();
                PopWindowUtil.getInstance().dissMiss();
                break;
            case R.id.tvClose:
                PopWindowUtil.getInstance().dissMiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void showData(UserEntity data) {
        hideLoading();
        if (data != null){
            GlideUtils.showCircleImage(this, ivHead, data.getAvatar(), R.drawable.icon_head);
            tvName.setText(data.getUserName());
            etNikeName.setText(data.getNickName());
            realName = data.getNickName();
            tvDeptName.setText(data.getDept().getDeptName());
            phoneNum = data.getPhonenumber();
            if (!"".equals(phoneNum)){
                etPhone.setText(phoneNum);
            }else {
                etPhone.setHint("暂无联系电话");
            }
        }
    }

    @Override
    public void reviseSuccess(Object o) {
        ToastUtils.showLong("修改成功");
        setResult(Constants.HttpKey.RESPONSE_200);
        ContactEnterUtils.getInstance(this).sendRefash();
        finish();
    }

    @Override
    public void updataName(Object data) {
        ToastUtils.showLong("修改成功");
        setResult(Constants.HttpKey.RESPONSE_200);
        ContactEnterUtils.getInstance(this).sendRefash();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:   //调用相机后返回
                if (resultCode == RESULT_OK) {
                    //用相机返回的照片去调用剪裁也需要对Uri进行处理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri contentUri = FileProvider.getUriForFile(UserInfoActivity.this, getPackageName() + ".fileprovider", tempFile);
                        cropPhoto(contentUri);
                    } else {
                        cropPhoto(Uri.fromFile(tempFile));
                    }
                }
                break;
            case ALBUM_REQUEST_CODE:    //调用相册后返回
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    cropPhoto(uri);
                }
                break;
            case CROP_REQUEST_CODE:     //调用剪裁后返回
                if (intent == null) {
                    return;
                }
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    //在这里获得了剪裁后的Bitmap对象，可以用于上传
                    Bitmap image = bundle.getParcelable("data");
                    //设置到ImageView上
                    ivHead.setImageBitmap(image);
//                    //也可以进行一些保存、压缩等操作后上传
                    String path = saveImage("crop", image);
                    if (null != path){
                        mPresenter.updateImg(this, path);
                    }
//                    //  ----   转码base64   ----
//                    ImageToBase64(path);
                }
                break;
        }
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColorNoTranslucent(this, Color.WHITE);
    }

    /**
     * 从相册选取图片
     */
    private void getPicFromAlbm() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, ALBUM_REQUEST_CODE);
    }

    /**
     * 从相机获取图片
     */
    @NeedsPermission({Manifest.permission.CAMERA})
    public void getPicFromCamera() {
        //用于保存调用相机拍照后所生成的文件
        tempFile = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".jpg");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断 版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            String authority = getApplicationInfo().packageName + ".fileprovider";
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), authority, tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {    //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        }
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /**
     * 裁剪图片
     */
    private void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, CROP_REQUEST_CODE);
    }


    public String saveImage(String name, Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory().getPath()+"/LS");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String fileName = name + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}