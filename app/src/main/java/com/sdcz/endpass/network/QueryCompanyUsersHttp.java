package com.sdcz.endpass.network;

import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.log.Logger;
import com.inpor.manager.beans.CompanyUserDto;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.view.IContactContract;

/**
 * @author xwt
 * @creatdate $
 * @updatedate $
 */
public class QueryCompanyUsersHttp {
    protected static final String TAG = "QueryCompanyUsersHttp";
    protected static final int REQUST_COUNT = 2;

    private int currnetPage;
    private int pageSize;
    private int requstNum = 0;
    private QueryCompanyUsersHttpCallck queryCompanyUsersHttpCallck;

    /**
     * @author: xingwt
     * @creatDate: 2021/12/7 18:46
     * @description: 初始化
     */
    public QueryCompanyUsersHttp(int currnetPage, int pageSize,
                                 QueryCompanyUsersHttpCallck queryCompanyUsersHttpCallck) {
        this.currnetPage = currnetPage;
        this.pageSize = pageSize;
        this.queryCompanyUsersHttpCallck = queryCompanyUsersHttpCallck;
        requst(0);
    }

    /**
     * @author: xingwt
     * @creatDate: 2021/12/7 18:46
     * @description: 请求
     */
    public void requst(int requstNum) {
        this.requstNum = requstNum;
        // 查下一页的 1000 个数据
        SdkUtil.getContactManager().queryCompanyUsers(currnetPage + 1,
                pageSize, new IRoomListResultInterface<CompanyUserDto>() {
                     public void failed(int code, String errorMsg) {

                         if (queryCompanyUsersHttpCallck != null) {
                             queryCompanyUsersHttpCallck.onError();
                         }
                    }

                    public   void succeed(CompanyUserDto result) {
                        if (result.getCode() == IContactContract.RESULT_CODE_ERROR_NO_PERMISSION){
                            if (queryCompanyUsersHttpCallck != null) {
                                queryCompanyUsersHttpCallck.onNoPermission();
                            }
                        }else {
                            if (result.getResult() != null) {
                                Logger.info(TAG, "onSuccess :" + currnetPage + " requstNum" + requstNum);
                                if (queryCompanyUsersHttpCallck != null) {
                                    queryCompanyUsersHttpCallck.onSuccess(result);
                                }
                            }
                        }

                    }

                });
    }


    /**
     * @author: xingwt
     * @creatDate: 2021/12/7 17:33
     * @description: 请求回调
     */
    public interface QueryCompanyUsersHttpCallck {
        public void onFail();

        public void onError();

        public void onNoPermission();

        public void onSuccess(CompanyUserDto companyUserDto);
    }




}
