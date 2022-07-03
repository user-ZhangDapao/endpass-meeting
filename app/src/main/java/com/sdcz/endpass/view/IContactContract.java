package com.sdcz.endpass.view;

import android.app.Activity;

import com.inpor.manager.beans.DepartmentResultDto;
import com.universal.clientcommon.beans.CompanyUserInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class IContactContract {
    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_ERROR_NO_SERVER = 20820;
    public static final int RESULT_CODE_ERROR_NO_PERMISSION = 20822;
    public static final int RESULT_CODE_ERROR_EMPTY_DATA = 20901;


    public interface IContactPresent extends IBasePresenter {


        /**
         * 初始化数据
         */
        void initData();

        /**
         * 删除用户群组
         *
         * @param groudId  群组ID
         * @param position 当前所在群组数组的position
         */
        void deleteUserGroup(long groudId, int position);



        /**
         * 挂断
         */
        void onHangUp(ArrayList<CompanyUserInfo> onlineUserInfos);

        /**
         * 注册呼叫监听事件
         */
        void registerInstantListener();

        /**
         * 反注册呼叫监听事件
         */
        void unRegisterInstantListener();

        /**
         * 收藏用户
         */
        void collectUser(ArrayList<CompanyUserInfo> userInfos, boolean isCollection);

        /**
         * 获取用户名
         *
         * @return 当前用户名
         */
        String getUserName();

        void getNextSubDepartments(DepartmentResultDto.SubDepartments object);

        void search(String string);

        void requestUserOnlineData();

        void loginAgain();

        void forceLoginPaas();

        void releaseAll();
    }

    public interface IContactView extends IBaseView<IContactPresent> {

        void showMenuDialog(CompanyUserInfo companyUserInfo);

        void showInviteDialog(ArrayList<CompanyUserInfo> onlineUserInfos);

        void showContactDialog(DepartmentResultDto.SubDepartments sub, List<CompanyUserInfo> userInfos);

        void showEmptyView(int isFail);

        void refreshContactsData(ArrayList<DepartmentResultDto.SubDepartments> subs,
                                 List<CompanyUserInfo> infos);

        void showGroupDialog();

        void dismissInviteDialog();

        void showContactsView();

        WeakReference<Activity> getWeakReferenceActivity();

        boolean isVisible();

        void showForceDialog();
    }


    public interface IBasePresenter {
        /**
         * 初始启动方法，允许做一些初始化操作
         * */
        void start();

        void onStop();
    }

    public interface IBaseView<T> {
        void setPresenter(T t);
    }


}

