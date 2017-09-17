package com.jabra.listener;


import com.jabra.data.ContactsSortList;

public interface MsgListener {
    void onReceive(ContactsSortList contactsSortList);
}
