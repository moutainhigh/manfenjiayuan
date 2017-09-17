package com.jabra.bean;

import com.tencent.mm.sdk.ext.MMOpenApiCaller;
import com.tencent.mm.sdk.ext.MMOpenApiCaller.MsgItem;

public class Message {
    public static final int CONTENT_TYPE_OTHER = 3;
    public static final int CONTENT_TYPE_TEXT = 1;
    public static final int CONTENT_TYPE_VOICE = 2;
    public static final int MSG_TYPE_RECEIVE = 1;
    public static final int MSG_TYPE_SEND = 2;
    public static final int STATUS_READED = 1;
    public static final int STATUS_UNREAD = 2;
    public static final int TTS_SYNTHESIZED = 2;
    public static final int TTS_SYNTHESIZING = 1;
    public static final int TTS_UNSYNTHESIZE = 0;
    private MMOpenApiCaller.MsgItem item;
    private int ttsState;

    public Message(MMOpenApiCaller.MsgItem paramMsgItem) {
        this.item = paramMsgItem;
    }

    public boolean equals(Object paramObject) {
        if ((paramObject == null) || (!(paramObject instanceof Message))) {
            return false;
        }
        Message localMessage = (Message) paramObject;
        if (((localMessage.item == null) || (localMessage.item.msgId == null) || (this.item == null) || (this.item.msgId == null))) {
            return false;
        }
        return localMessage.item.msgId.equals(this.item.msgId);
    }

    public String getContent() {
        return this.item.content;
    }

    public int getContentType() {
        return this.item.contentType;
    }

    public long getCreateTime() {
        return this.item.createTime;
    }

    public String getFromUserId() {
        return this.item.fromUserId;
    }

    public String getFromUserNickName() {
        return this.item.fromUserNickName;
    }

    public String getMsgId() {
        return this.item.msgId;
    }

    public int getMsgType() {
        return this.item.msgType;
    }

    public int getStatus() {
        return this.item.status;
    }

    public int getTtsState() {
        return this.ttsState;
    }

    public void setContent(String paramString) {
        this.item.content = paramString;
    }

    public void setContentType(int paramInt) {
        this.item.contentType = paramInt;
    }

    public void setCreateTime(long paramLong) {
        this.item.createTime = paramLong;
    }

    public void setFromUserId(String paramString) {
        this.item.fromUserId = paramString;
    }

    public void setFromUserNickName(String paramString) {
        this.item.fromUserNickName = paramString;
    }

    public void setMsgId(String paramString) {
        this.item.msgId = paramString;
    }

    public void setMsgType(int paramInt) {
        this.item.msgType = paramInt;
    }

    public void setStatus(int paramInt) {
        this.item.status = paramInt;
    }

    public void setTtsState(int paramInt) {
        this.ttsState = paramInt;
    }

    public String toString() {
        return "Message [msgId=" + this.item.msgId + ", content=" + this.item.content + ", ttsState=" + this.ttsState + "]";
    }
}
