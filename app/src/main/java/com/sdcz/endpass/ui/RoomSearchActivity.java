package com.sdcz.endpass.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.RoomListAdapter;
import com.sdcz.endpass.bean.BaseRoomData;
import com.sdcz.endpass.bean.CloudRoom;
import com.sdcz.endpass.bean.Result;
import com.sdcz.endpass.bean.ScheduleMeeting;
import com.sdcz.endpass.dialog.InputPasswordDialog;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.presenter.RoomListViewModel;
import com.sdcz.endpass.widget.SearchView;
import com.inpor.sdk.PlatformConfig;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class RoomSearchActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "type";
    private static final String TAG = "RoomSearch-act";
    private int type = RoomListViewModel.TYPE_CLOUD_ROOM;
    private SearchView searchView;
    private RecyclerView recycleView;
    private TextView tvEmpty;
    private RoomListAdapter adapter;
    private RoomListViewModel viewModel;
    private LoadingDialog loadingDialog;
    private InputPasswordDialog inputPasswordDialog;
    private int errorPwdCount;
    private BaseRoomData mRoomData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_search);
        if (getIntent() != null) {
            type = getIntent().getIntExtra(EXTRA_TYPE, RoomListViewModel.TYPE_CLOUD_ROOM);
        }
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PlatformConfig.getInstance().setLoginStatus(true);
    }

    private void initView() {
        searchView = findViewById(R.id.search);
        recycleView = findViewById(R.id.recycle);
        tvEmpty = findViewById(R.id.tv_no_meeting);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(v -> finish());
    }

    private void initData() {
        viewModel = new ViewModelProvider(this).get(RoomListViewModel.class);
        searchView.setOnClickSearch(this::onClickSearch);
        searchView.setOnClearSearch(this::onClearSearch);
        adapter = new RoomListAdapter();
        recycleView.setAdapter(adapter);
        adapter.setOnItemClickListener(this::onItemClick);
        switch (type) {
            case RoomListViewModel.TYPE_CLOUD_ROOM:
                viewModel.getCloudRoomResult().observe(this, this::onCloudRoomChange);
                break;
            case RoomListViewModel.TYPE_SCHEDULE_ROOM:
                viewModel.getScheduleRoomResult().observe(this, this::onScheduleRoomChange);
                break;
        }

    }

    private void onClearSearch() {
        adapter.setNewData(Collections.emptyList());
    }

    private void onClickSearch(String s) {
        if (TextUtils.isEmpty(s)) {
            adapter.setNewData(Collections.emptyList());
        } else {
            viewModel.queryRoomListByKeyword(type, 1, Integer.MAX_VALUE, s);
        }
    }

    private void onScheduleRoomChange(Result<List<ScheduleMeeting>> listResult) {
        int code = listResult.getCode();
        if (code == Result.SUCCESS_CODE) {
            tvEmpty.setVisibility(View.GONE);
            List<ScheduleMeeting> data = listResult.getData();
            adapter.setNewData(new ArrayList<>(data));
        } else if (code == Result.SUCCESS_EMPTY) {
            adapter.replaceData(Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            ToastUtils.showShort(listResult.getErrorMsg());
        }
    }

    private void onCloudRoomChange(Result<List<CloudRoom>> listResult) {
        int code = listResult.getCode();
        if (code == Result.SUCCESS_CODE) {
            tvEmpty.setVisibility(View.GONE);
            List<CloudRoom> data = listResult.getData();
            adapter.setNewData(new ArrayList<>(data));
        } else if (code == Result.SUCCESS_EMPTY) {
            adapter.replaceData(Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            ToastUtils.showShort(listResult.getErrorMsg());
        }
    }

    private void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        BaseRoomData roomData = (BaseRoomData) baseQuickAdapter.getData().get(i);
        if (roomData == null) {
            return;
        }
        mRoomData = roomData;
        /*
        List<String> permissionList = PermissionUtils.requestMeetingPermission();
        if (permissionList != null && !permissionList.isEmpty()) {
            PermissionUtils.requestPermission(this, permissionList);
            return;
        }*/
        joinMeetingByRoomData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            JoinMeetingManager.getInstance().initLogger(this);
            joinMeetingByRoomData();
        }
    }

    private void joinMeetingByRoomData() {
        if (mRoomData == null || TextUtils.isEmpty(mRoomData.getDisplayInviteCode())) {
            return;
        }
        String userName = PlatformConfig.getInstance().getUserName();
        JoinMeetingManager.getInstance().loginRoomId(mRoomData.getDisplayInviteCode(), userName,
                "", false, new JoinMeetingCallback() {

                    @Override
                    public void onStart(Procedure procedure) {
                        if (loadingDialog == null) {
                            loadingDialog = new LoadingDialog(RoomSearchActivity.this, R.string.logging);
                        }
                        loadingDialog.show();
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
                        Intent intent = new Intent(RoomSearchActivity.this, MobileMeetingActivity.class);
                        intent.putExtra(MobileMeetingActivity.EXTRA_ANONYMOUS_LOGIN,false);
                        startActivity(intent);
                    }
                });
    }

    private void showInputPasswordDialog(boolean isFrontVerify, InputPassword inputPassword) {
        Disposable disposable = Flowable.just(inputPassword)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(input -> {
                    if (inputPasswordDialog == null) {
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
                    inputPasswordDialog.show();
                    errorPwdCount++;
                });
    }
}
