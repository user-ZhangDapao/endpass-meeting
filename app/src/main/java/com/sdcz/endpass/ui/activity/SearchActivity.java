package com.sdcz.endpass.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.MailUserAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.SearchPresenter;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.util.StatusBarUtils;
import com.sdcz.endpass.view.ISearchView;
import com.sdcz.endpass.widget.PopupWindowToCall;
import com.sdcz.endpass.widget.PopupWindowToUserData;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 搜索联系人
 */
//@RuntimePermissions
public class SearchActivity extends BaseActivity<SearchPresenter> implements ISearchView, View.OnClickListener {

    private RecyclerView rlRoot;
    private TextView tvTitle;
    private ImageView ivBack;
    private ImageView ivClose;
    private EditText etSearch;
    private UserEntity info;
    private List<UserEntity> userInfoList;
    private MailUserAdapter adapter;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_search;
    }

    @Override
    protected SearchPresenter createPresenter() {
        return new SearchPresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        rlRoot = findViewById(R.id.rlRoot);
        tvTitle = findViewById(R.id.tvTitle);
        ivBack = findViewById(R.id.ivBack);
        ivClose = findViewById(R.id.ivClose);
        etSearch = findViewById(R.id.et_search);
        tvTitle.setText("搜索联系人");
        ivBack.setVisibility(View.VISIBLE);
        return rlRoot;
    }

    @Override
    public void initData() {
        super.initData();
        rlRoot.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void initListener() {
        super.initListener();
        ivBack.setOnClickListener(this);
        ivClose.setOnClickListener(this);
//        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    String search = etSearch.getText().toString().trim();
//                    mPresenter.getUserByNameLike(SearchActivity.this, search);
//                    return true;
//                }
//                return false;
//            }
//        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    ivClose.setVisibility(View.VISIBLE);
                    mPresenter.getUserByNameLike(SearchActivity.this,s.toString());
                } else {
                    ivClose.setVisibility(View.INVISIBLE);
                    mPresenter.getUserByNameLike(SearchActivity.this,"");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void setStatusBar() {
        StatusBarUtils.setTranslucentBar(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivBack:
                finish();
                break;
            case R.id.ivClose:
                etSearch.setText("");
                break;
        }
    }

    @Override
    public void showData(List<UserEntity> data) {
        if (null != data){
            userInfoList = data;
            adapter = new MailUserAdapter(R.layout.item_maillist_user, data, new MailUserAdapter.onItemClick() {
                @Override
                public void onClick(UserEntity item) {
                    info = item;
                    mPresenter.postCollectStatus(SearchActivity.this, item.getUserId()+"");
                }
            });
            rlRoot.setAdapter(adapter);
//            FspManager.getInstance().refreshAllUserStatus();

            if (data.size()>0){
                rlRoot.setVisibility(View.VISIBLE);
            }else {
                rlRoot.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void showStatus(Integer data) {
        if (data != null) {
            PopupWindowToUserData popuWin = new PopupWindowToUserData(this, data, info, "",
                    new PopupWindowToUserData.OnPopWindowClickListener() {
                        @Override
                        public void onCreatRecord(String userId, String collectUserId, String recordType) {
                            //创建临时会话
                            mPresenter.creatRecord(SearchActivity.this, collectUserId, recordType);
                        }

                        @Override
                        public void onCollectUser(String userId, String collectUserId) {
                            //取消收藏
                            mPresenter.collectUser(SearchActivity.this, collectUserId);
                        }

                        @Override
                        public void onCallPhone(String phoneNum) {
                            new PopupWindowToCall(SearchActivity.this, new PopupWindowToCall.OnPopWindowClickListener() {
                                @Override
                                public void onPopWindowClickListener(View view) {
//                                    SearchActivityPermissionsDispatcher.CallWithPermissionCheck(SearchActivity.this, phoneNum);
                                }
                            }, info.getPhonenumber()).show();
                        }
                    });
            popuWin.show();
        }
    }

    /**
     * 创建临时任务
     *
     * @param data
     */

    @Override
    public void creatRecordSuccess(String data, String collectUserId, String recordType) {
//        //TODO:data需要修改
//        SharedPrefsUtil.putValue(this, KeyStore.RECORDCODE,data);
//        String[] array = {collectUserId};
//        joinGroupVoiceUser(array,data,recordType);
    }

    /**
     * 修改是否收藏用户
     *
     * @param data
     */
    @Override
    public void cancelLikeSuccess(Object data) {
        ToastUtils.showLong("操作成功");
    }
//
//    @Override
//    protected void onRefreshAllUserStatusFinished(FspUserInfo[] infos) {
//        super.onRefreshAllUserStatusFinished(infos);
////        for (FspUserInfo info : infos) {
////            Log.e(TAG, "onEventRefreshUserStatusFinished -- onLineName --> " + info.getUserId());
////        }
//        //在线人员名单
//        List<FspUserInfo> m_data = new ArrayList();
//        m_data.addAll(Arrays.asList(infos));
//        if (userInfoList != null) {
//            for (int j = 0; j < userInfoList.size(); j++) {
//                String userId = userInfoList.get(j).getUserId() + "";
////                userInfoList.get(j).setIsOnline(0);
//                for (int i = 0; i < m_data.size(); i++) {
//                    String hstUser = m_data.get(i).getUserId();
//                    if (hstUser.equals(userId)) {
//                        userInfoList.get(j).setIsOnline(1);
//                    }
//                }
//            }
//            adapter.notifyDataSetChanged();
//        }
//    }
//
//    @NeedsPermission({Manifest.permission.CALL_PHONE})
//    public void Call(String phoneNum) {
//        Intent intent = new Intent(Intent.ACTION_CALL);
//        Uri data = Uri.parse("tel:" + phoneNum);
//        intent.setData(data);
//        startActivity(intent);
//    }

}