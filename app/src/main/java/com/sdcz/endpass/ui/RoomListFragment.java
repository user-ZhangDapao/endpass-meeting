package com.sdcz.endpass.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.RoomListAdapter;
import com.sdcz.endpass.bean.BaseRoomData;
import com.sdcz.endpass.bean.CloudRoom;
import com.sdcz.endpass.bean.InstantMeeting;
import com.sdcz.endpass.bean.Result;
import com.sdcz.endpass.bean.ScheduleMeeting;
import com.sdcz.endpass.dialog.InputPasswordDialog;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.login.LoginErrorUtil;
import com.sdcz.endpass.login.LoginStateUtil;
import com.sdcz.endpass.presenter.RoomListViewModel;
import com.inpor.sdk.PlatformConfig;
import com.inpor.sdk.annotation.ProcessStep;
import com.inpor.sdk.callback.JoinMeetingCallback;
import com.inpor.sdk.kit.workflow.Procedure;
import com.inpor.sdk.open.pojo.InputPassword;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class RoomListFragment extends Fragment {

    /* 取值范围 0固定 1预约 2即时 */
    public static final String EXTRA_TYPE = "type";
    private int type = 0;
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private RoomListAdapter adapter;
    private TextView tvEmpty;
    private RoomListViewModel viewModel;
    private int currentPage = 1;
    private InputPasswordDialog inputPasswordDialog;
    private LoadingDialog loadingDialog;
    private int errorPwdCount = 0;
    private BaseRoomData mRoomData;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt(EXTRA_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_list, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initView(View view) {
        refreshLayout = view.findViewById(R.id.refresh);
        recyclerView = view.findViewById(R.id.recycle);
        tvEmpty = view.findViewById(R.id.tv_no_meeting);
    }

    private void initData() {
        viewModel = new ViewModelProvider(requireActivity()).get(RoomListViewModel.class);
        adapter = new RoomListAdapter();
        adapter.setOnItemClickListener(this::onItemClick);
        recyclerView.setAdapter(adapter);
        refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        refreshLayout.setOnRefreshListener(this::onRefresh);
        refreshLayout.setOnLoadMoreListener(this::onLoadMore);
        refreshLayout.setEnableLoadMore(type == RoomListViewModel.TYPE_SCHEDULE_ROOM);
        refreshLayout.autoRefresh();
        switch (type) {
            case RoomListViewModel.TYPE_CLOUD_ROOM:
                viewModel.getCloudRoomResult().observe(getViewLifecycleOwner(), this::onCloudRoomChange);
                break;
            case RoomListViewModel.TYPE_SCHEDULE_ROOM:
                viewModel.getScheduleRoomResult().observe(getViewLifecycleOwner(), this::onScheduleRoomChange);
                break;
            case RoomListViewModel.TYPE_INSTANT_ROOM:
                viewModel.getInstantRoomResult().observe(getViewLifecycleOwner(), this::onInstantRoomChange);
                break;
        }
    }

    private void onCloudRoomChange(Result<List<CloudRoom>> listResult) {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
        int code = listResult.getCode();
        if (code == Result.SUCCESS_CODE) {
            tvEmpty.setVisibility(View.GONE);
            adapter.replaceData(listResult.getData());
        } else if (code == Result.SUCCESS_EMPTY) {
            adapter.replaceData(Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            adapter.replaceData(Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
            ToastUtils.showShort(listResult.getErrorMsg());
        }
    }

    /* 支持分页 */
    private void onScheduleRoomChange(Result<List<ScheduleMeeting>> listResult) {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
        int code = listResult.getCode();
        if (code == Result.SUCCESS_CODE) {
            tvEmpty.setVisibility(View.GONE);
            if (currentPage == 1) {
                adapter.replaceData(listResult.getData());
            } else {
                adapter.addData(listResult.getData());
            }
        } else if (code == Result.SUCCESS_EMPTY) {
            if (currentPage == 1) {
                adapter.replaceData(Collections.emptyList());
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                refreshLayout.setEnableLoadMore(false);
            }
        } else {
            if (currentPage == 1) {
                adapter.replaceData(Collections.emptyList());
                tvEmpty.setVisibility(View.VISIBLE);
            }
            ToastUtils.showShort(listResult.getErrorMsg());
        }
    }

    private void onInstantRoomChange(Result<List<InstantMeeting>> listResult) {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
        int code = listResult.getCode();
        if (code == Result.SUCCESS_CODE) {
            tvEmpty.setVisibility(View.GONE);
            adapter.replaceData(listResult.getData());
        } else if (code == Result.SUCCESS_EMPTY) {
            adapter.replaceData(Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            adapter.replaceData(Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
            ToastUtils.showShort(listResult.getErrorMsg());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            JoinMeetingManager.getInstance().initLogger(requireContext());
            joinMeetingByRoomData();
        }
    }

    private void onItemClick(@NonNull BaseQuickAdapter baseQuickAdapter, View view, int i) {
        BaseRoomData roomData = (BaseRoomData) baseQuickAdapter.getData().get(i);
        if (roomData == null) {
            return;
        }
        mRoomData = roomData;
        /*
        List<String> permissionList = PermissionUtils.requestMeetingPermission();
        if (permissionList != null && !permissionList.isEmpty()) {
            PermissionUtils.requestPermission(this,permissionList);
            return;
        }*/
        joinMeetingByRoomData();
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
                            loadingDialog = new LoadingDialog(requireContext(), R.string.logging);
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
                        Intent intent = new Intent(requireActivity(), MobileMeetingActivity.class);
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
                        inputPasswordDialog = InputPasswordDialog.showInputPwdDialog(requireContext());
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

    private void onRefresh(RefreshLayout refreshLayout) {
        currentPage = 1;
        viewModel.queryRoomList(type, currentPage);
    }

    private void onLoadMore(RefreshLayout refreshLayout) {
        currentPage++;
        viewModel.queryRoomList(type, currentPage);
    }

}
