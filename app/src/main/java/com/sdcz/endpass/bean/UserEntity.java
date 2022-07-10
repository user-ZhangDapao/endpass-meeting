package com.sdcz.endpass.bean;

import java.util.List;

public class UserEntity {


    private int IsOnline;


    public int getIsOnline() {
        return IsOnline;
    }

    public void setIsOnline(int isOnline) {
        IsOnline = isOnline;
    }


    private Object searchValue;
    private String createBy;
    private String createTime;
    private Object updateBy;
    private Object updateTime;
    private String remark;
    private ParamsBean params;
    private int userId;
    private int deptId;
    private String userName;
    private String nickName;
    private Object userCode;
    private String email;
    private String phonenumber;
    private double lon;
    private double lat;
    private String sex;
    private String avatar;
    private Object salt;
    private String status;
    private String delFlag;
    private String loginIp;
    private String loginDate;
    private DeptBean dept;
    private List<RolesBean> roles;
    private Object roleIds;
    private Object postIds;
    private Object roleId;
    private String roleName;
    private String channelName;
    private String channelCode;
    private long roomId;
    private Object location;
    private boolean admin;
    private Long mdtUserId;


    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public long getMdtUserId() {
        return mdtUserId;
    }

    public void setMdtUserId(long mdtUserId) {
        this.mdtUserId = mdtUserId;
    }

    public Object getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(Object searchValue) {
        this.searchValue = searchValue;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Object getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Object updateBy) {
        this.updateBy = updateBy;
    }

    public Object getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Object updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public ParamsBean getParams() {
        return params;
    }

    public void setParams(ParamsBean params) {
        this.params = params;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Object getUserCode() {
        return userCode;
    }

    public void setUserCode(Object userCode) {
        this.userCode = userCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Object getSalt() {
        return salt;
    }

    public void setSalt(Object salt) {
        this.salt = salt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public String getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(String loginDate) {
        this.loginDate = loginDate;
    }

    public DeptBean getDept() {
        return dept;
    }

    public void setDept(DeptBean dept) {
        this.dept = dept;
    }

    public List<RolesBean> getRoles() {
        return roles;
    }

    public void setRoles(List<RolesBean> roles) {
        this.roles = roles;
    }

    public Object getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Object roleIds) {
        this.roleIds = roleIds;
    }

    public Object getPostIds() {
        return postIds;
    }

    public void setPostIds(Object postIds) {
        this.postIds = postIds;
    }

    public Object getRoleId() {
        return roleId;
    }

    public void setRoleId(Object roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Object getLocation() {
        return location;
    }

    public void setLocation(Object location) {
        this.location = location;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public static class ParamsBean {
    }

    public static class DeptBean {
        private Object searchValue;
        private Object createBy;
        private Object createTime;
        private Object updateBy;
        private Object updateTime;
        private Object remark;
        private ParamsBean params;
        private int deptId;
        private int parentId;
        private String ancestors;
        private String deptName;
        private String orderNum;
        private String leader;
        private Object phone;
        private Object email;
        private String status;
        private Object delFlag;
        private Object lon;
        private Object lat;
        private Object parentName;
        private Object userCount;
        private List<?> children;

        public Object getSearchValue() {
            return searchValue;
        }

        public void setSearchValue(Object searchValue) {
            this.searchValue = searchValue;
        }

        public Object getCreateBy() {
            return createBy;
        }

        public void setCreateBy(Object createBy) {
            this.createBy = createBy;
        }

        public Object getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Object createTime) {
            this.createTime = createTime;
        }

        public Object getUpdateBy() {
            return updateBy;
        }

        public void setUpdateBy(Object updateBy) {
            this.updateBy = updateBy;
        }

        public Object getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Object updateTime) {
            this.updateTime = updateTime;
        }

        public Object getRemark() {
            return remark;
        }

        public void setRemark(Object remark) {
            this.remark = remark;
        }

        public ParamsBean getParams() {
            return params;
        }

        public void setParams(ParamsBean params) {
            this.params = params;
        }

        public int getDeptId() {
            return deptId;
        }

        public void setDeptId(int deptId) {
            this.deptId = deptId;
        }

        public int getParentId() {
            return parentId;
        }

        public void setParentId(int parentId) {
            this.parentId = parentId;
        }

        public String getAncestors() {
            return ancestors;
        }

        public void setAncestors(String ancestors) {
            this.ancestors = ancestors;
        }

        public String getDeptName() {
            return deptName;
        }

        public void setDeptName(String deptName) {
            this.deptName = deptName;
        }

        public String getOrderNum() {
            return orderNum;
        }

        public void setOrderNum(String orderNum) {
            this.orderNum = orderNum;
        }

        public String getLeader() {
            return leader;
        }

        public void setLeader(String leader) {
            this.leader = leader;
        }

        public Object getPhone() {
            return phone;
        }

        public void setPhone(Object phone) {
            this.phone = phone;
        }

        public Object getEmail() {
            return email;
        }

        public void setEmail(Object email) {
            this.email = email;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Object getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(Object delFlag) {
            this.delFlag = delFlag;
        }

        public Object getLon() {
            return lon;
        }

        public void setLon(Object lon) {
            this.lon = lon;
        }

        public Object getLat() {
            return lat;
        }

        public void setLat(Object lat) {
            this.lat = lat;
        }

        public Object getParentName() {
            return parentName;
        }

        public void setParentName(Object parentName) {
            this.parentName = parentName;
        }

        public Object getUserCount() {
            return userCount;
        }

        public void setUserCount(Object userCount) {
            this.userCount = userCount;
        }

        public List<?> getChildren() {
            return children;
        }

        public void setChildren(List<?> children) {
            this.children = children;
        }

        public static class ParamsBean {
        }
    }

    public static class RolesBean {
        private Object searchValue;
        private Object createBy;
        private Object createTime;
        private Object updateBy;
        private Object updateTime;
        private Object remark;
        private ParamsBean params;
        private int roleId;
        private String roleName;
        private String roleKey;
        private String roleSort;
        private String dataScope;
        private boolean menuCheckStrictly;
        private boolean deptCheckStrictly;
        private String status;
        private Object delFlag;
        private boolean flag;
        private Object menuIds;
        private Object deptIds;
        private boolean admin;

        public Object getSearchValue() {
            return searchValue;
        }

        public void setSearchValue(Object searchValue) {
            this.searchValue = searchValue;
        }

        public Object getCreateBy() {
            return createBy;
        }

        public void setCreateBy(Object createBy) {
            this.createBy = createBy;
        }

        public Object getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Object createTime) {
            this.createTime = createTime;
        }

        public Object getUpdateBy() {
            return updateBy;
        }

        public void setUpdateBy(Object updateBy) {
            this.updateBy = updateBy;
        }

        public Object getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Object updateTime) {
            this.updateTime = updateTime;
        }

        public Object getRemark() {
            return remark;
        }

        public void setRemark(Object remark) {
            this.remark = remark;
        }

        public ParamsBean getParams() {
            return params;
        }

        public void setParams(ParamsBean params) {
            this.params = params;
        }

        public int getRoleId() {
            return roleId;
        }

        public void setRoleId(int roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public String getRoleKey() {
            return roleKey;
        }

        public void setRoleKey(String roleKey) {
            this.roleKey = roleKey;
        }

        public String getRoleSort() {
            return roleSort;
        }

        public void setRoleSort(String roleSort) {
            this.roleSort = roleSort;
        }

        public String getDataScope() {
            return dataScope;
        }

        public void setDataScope(String dataScope) {
            this.dataScope = dataScope;
        }

        public boolean isMenuCheckStrictly() {
            return menuCheckStrictly;
        }

        public void setMenuCheckStrictly(boolean menuCheckStrictly) {
            this.menuCheckStrictly = menuCheckStrictly;
        }

        public boolean isDeptCheckStrictly() {
            return deptCheckStrictly;
        }

        public void setDeptCheckStrictly(boolean deptCheckStrictly) {
            this.deptCheckStrictly = deptCheckStrictly;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Object getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(Object delFlag) {
            this.delFlag = delFlag;
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public Object getMenuIds() {
            return menuIds;
        }

        public void setMenuIds(Object menuIds) {
            this.menuIds = menuIds;
        }

        public Object getDeptIds() {
            return deptIds;
        }

        public void setDeptIds(Object deptIds) {
            this.deptIds = deptIds;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public static class ParamsBean {
        }
    }
}
