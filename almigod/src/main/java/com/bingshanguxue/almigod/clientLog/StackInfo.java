package com.bingshanguxue.almigod.clientLog;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 11/04/2017.
 */

public class StackInfo implements Serializable{
    private String terminalId;
    private String feedback;

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
