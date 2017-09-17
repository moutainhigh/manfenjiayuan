package com.jabra.listener;


import com.jabra.bean.Message;

public interface PlayMsgListener {
    void onCompleted();

    void onPlayEnd(Message message);

    void onPlayStart(Message message);
}
