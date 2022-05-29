package com.sdcz.endpass.model;

import com.sdcz.endpass.bean.BaseShareBean;
import com.sdcz.endpass.bean.VncShareBean;
import com.inpor.nativeapi.adaptor.RoomWndState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 共享页面管理
 */
public class ShareBeanManager {

    private static CopyOnWriteArrayList<BaseShareBean> beans = new CopyOnWriteArrayList<>();

    /**
     * 获取值
     */
    public static synchronized List<BaseShareBean> getBeans() {
        return new ArrayList<>(beans);
    }

    private static synchronized void setBeans(CopyOnWriteArrayList<BaseShareBean> beans) {
        ShareBeanManager.beans = beans;
    }

    /**
     * 通过id获取bean
     */
    public static synchronized BaseShareBean getById(long id) {
        for (BaseShareBean baseShareBean : beans) {
            if (baseShareBean.getId() == id) {
                return baseShareBean;
            }
        }
        return null;
    }

    /**
     * 根据类型获取数据列表
     */
    public static synchronized List<BaseShareBean> getByType(RoomWndState.DataType type) {
        List<BaseShareBean> tempList = new ArrayList<>();
        for (BaseShareBean baseShareBean : beans) {
            if (baseShareBean.getType() == type) {
                tempList.add(baseShareBean);
            }
        }
        return tempList;
    }

    /**
     * 获取数据DataBlock
     */
    public static synchronized RoomWndState.DataBlock[] getDataBlock() {
        RoomWndState.DataBlock[] shareItemPoses = new RoomWndState.DataBlock[beans.size()];
        for (int pos = 0; pos < beans.size(); pos++) {
            BaseShareBean shareBean = beans.get(pos);
            if (shareBean != null) {
                shareItemPoses[pos] = new RoomWndState.DataBlock();
                shareItemPoses[pos].dataID = shareBean.getId();
                shareItemPoses[pos].pos = (byte) pos;
                shareItemPoses[pos].dataType = shareBean.getType().getValue();
            }
        }
        return shareItemPoses;
    }

    /**
     * 获取活动的类型
     */
    public static synchronized RoomWndState.DataType getActiveType() {
        for (BaseShareBean baseShareBean : beans) {
            if (baseShareBean.isShow()) {
                return baseShareBean.getType();
            }
        }
        return RoomWndState.DataType.DATA_TYPE_NONE;
    }

    /**
     * 获取活动数据的下标
     */
    public static synchronized int getActiveIndex() {
        for (int i = 0; i < beans.size(); i++) {
            if (beans.get(i).isShow()) {
                return i;
            }
        }

        if (beans.size() > 0) {
            return beans.size() - 1;
        }

        return 0;
    }

    /**
     * 设置正在显示的ID
     */
    public static synchronized boolean setShowById(long id) {
        if (getById(id) == null) {
            return false;
        }
        for (BaseShareBean baseShareBean : beans) {
            baseShareBean.setShow(baseShareBean.id == id);
        }
        return true;
    }

    /**
     * 设置正在显示的ID
     */
    public static synchronized long getShowId() {
        for (BaseShareBean baseShareBean : beans) {
            if (baseShareBean.isShow()) {
                return baseShareBean.getId();
            }
        }
        return 0;
    }

    /**
     * 根据类型设置显示的数据
     */
    public static synchronized boolean setShowByType(RoomWndState.DataType type) {
        List<BaseShareBean> tempBaseSharebeans = getByType(type);
        if (tempBaseSharebeans == null || tempBaseSharebeans.size() == 0) {
            return false;
        }
        boolean result = false;
        for (BaseShareBean baseShareBean : beans) {
            if (baseShareBean.type == type) {
                baseShareBean.setShow(true);
                result = true;
            } else {
                baseShareBean.setShow(false);
            }
        }
        return result;
    }

    /**
     * 新增数据
     */
    public static synchronized void add(BaseShareBean bean) {
        if (bean == null) {
            return;
        }

        if (getById(bean.id) != null && bean.getType() == RoomWndState.DataType.DATA_TYPE_WB) {
            return;
        }

        beans.add(bean);
    }


    /**
     * 移除数据
     */
    public static synchronized void remove(long id) {

        int removedShownBeanIndex = -1;

        for (BaseShareBean shareBean : beans) {
            if (shareBean.getId() == id) {

                if (shareBean.isShow()) {

                    removedShownBeanIndex = beans.indexOf(shareBean);

                    if (beans.size() > 1 && removedShownBeanIndex >= 0) {

                        if (removedShownBeanIndex == 0) { // 删除的bean是首位，则返回现在首位的bean
                            beans.get(removedShownBeanIndex).setShow(true);
                        } else if (beans.size() >= removedShownBeanIndex) { // 要保证返回的值在beans个数范围内
                            beans.get(removedShownBeanIndex - 1).setShow(true); // 删除的bean
                            // 不是首位，返回前一个。
                        }
                    }
                }
                beans.remove(shareBean);
                return;
            }
        }
    }

    /**
     * 删除 接收屏幕共享 bean
     */
    public static synchronized void removeVncRecvBean() {
        for (BaseShareBean baseShareBean : beans) {
            if (baseShareBean.getType() == RoomWndState.DataType.DATA_TYPE_APPSHARE) {
                VncShareBean vncShareBean = (VncShareBean) baseShareBean;
                if (!vncShareBean.isSendVnc) {
                    remove(vncShareBean.getId());
                }
            }
        }
    }

    /**
     * 获取 接收屏幕共享 bean
     */
    public static synchronized VncShareBean getVncRecvBean() {
        for (BaseShareBean baseShareBean : beans) {
            if (baseShareBean.getType() == RoomWndState.DataType.DATA_TYPE_APPSHARE) {
                VncShareBean vncShareBean = (VncShareBean) baseShareBean;
                if (!vncShareBean.isSendVnc) {
                    return vncShareBean;
                }
            }
        }
        return null;
    }

    /**
     * 设置接收屏幕共享
     */
    public static synchronized void setVncRecvShow() {
        if (getVncRecvBean() == null) {
            return;
        }

        for (BaseShareBean baseShareBean : beans) {
            if (baseShareBean.getType() == RoomWndState.DataType.DATA_TYPE_APPSHARE) {
                VncShareBean vncShareBean = (VncShareBean) baseShareBean;
                vncShareBean.setShow(!vncShareBean.isSendVnc);
            } else {
                baseShareBean.setShow(false);
            }
        }
    }

    /**
     * 根据类型移除数据
     */
    public static synchronized void removeByType(RoomWndState.DataType type) {
        List<BaseShareBean> beanIdList = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            if (beans.get(i).getType() == type) {
                beanIdList.add(beans.get(i));
            }
        }

        int removedShownBeanIndex = -1;

        for (BaseShareBean shareBean : beanIdList) {
            if (shareBean.isShow()) {
                removedShownBeanIndex = beans.indexOf(shareBean);

                if (beans.size() > 1 && removedShownBeanIndex >= 0 && type != RoomWndState
                        .DataType.DATA_TYPE_WB) {

                    if (removedShownBeanIndex == 0) { // 删除的bean是首位，则返回现在首位的bean
                        beans.get(removedShownBeanIndex).setShow(true);
                    } else if (beans.size() >= removedShownBeanIndex) { // 要保证返回的值在beans个数范围内
                        beans.get(removedShownBeanIndex - 1).setShow(true); // 删除的bean 不是首位，返回前一个。
                    }
                }
            }
            beans.remove(shareBean);
        }
    }

    public static synchronized int findSubByObject(BaseShareBean baseShareBean) {
        return beans.indexOf(baseShareBean);
    }

    public static synchronized BaseShareBean getBeanByIndex(int index) {
        return beans.get(index);
    }

    /**
     * 清除所有的数据
     */
    public static synchronized void clear() {
        beans.clear();
    }

    /**
     * 是否为空
     */
    public static synchronized boolean isEmpty() {
        return beans.isEmpty();
    }
}
