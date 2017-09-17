package com.jabra.bean;


import com.jabra.data.MsgSortList;

public class Contacts {
    private MsgSortList msgSortList = new MsgSortList();
    private String nickName;
    private int sortIndex;
    private String userId;

    public boolean equals(Object paramObject) {
        if ((paramObject == null) || (!(paramObject instanceof Contacts))) {
            return false;
        }
        Contacts localContacts = (Contacts) paramObject;
        if (localContacts.userId == null) {
            return false;
        }
        return localContacts.userId.equals(this.userId);
    }

    public MsgSortList getMsgSortList() {
        return this.msgSortList;
    }

    public String getNickName() {
        return this.nickName;
    }

    public int getSortIndex() {
        return this.sortIndex;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setMsgSortList(MsgSortList paramMsgSortList) {
        this.msgSortList = paramMsgSortList;
    }

    public void setNickName(String paramString) {
        this.nickName = paramString;
    }

    public void setSortIndex(int paramInt) {
        this.sortIndex = paramInt;
    }

    public void setUserId(String paramString) {
        this.userId = paramString;
    }

    public String toString() {
        return "Contacts [userId=" + this.userId + ", nickName=" + this.nickName + ", sortIndex = " + this.sortIndex + ", msgSortSet=" + this.msgSortList + "]";
    }
}
