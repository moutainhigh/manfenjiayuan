package com.mfh.framework.core.sms;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bingshanguxue on 07/11/2016.
 */

public class Message implements Serializable{
    private String sender;
    private String content;
    private String author;
    private String count;
    private String checkAvatarFlag;
    private String receiveDate;
    private boolean isMessage;
    private String captchas;
    private String smsId;
    private String companyName;
    private String resultContent;
    private int readStatus;
    private int fromSmsDB;
    private Date date;
    private long threadId;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getCheckAvatarFlag() {
        return checkAvatarFlag;
    }

    public void setCheckAvatarFlag(String checkAvatarFlag) {
        this.checkAvatarFlag = checkAvatarFlag;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public boolean isMessage() {
        return isMessage;
    }

    public void setMessage(boolean message) {
        isMessage = message;
    }

    public String getCaptchas() {
        return captchas;
    }

    public void setCaptchas(String captchas) {
        this.captchas = captchas;
    }

    public String getSmsId() {
        return smsId;
    }

    public void setSmsId(String smsId) {
        this.smsId = smsId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getResultContent() {
        return resultContent;
    }

    public void setResultContent(String resultContent) {
        this.resultContent = resultContent;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public int getFromSmsDB() {
        return fromSmsDB;
    }

    public void setFromSmsDB(int fromSmsDB) {
        this.fromSmsDB = fromSmsDB;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }
}
