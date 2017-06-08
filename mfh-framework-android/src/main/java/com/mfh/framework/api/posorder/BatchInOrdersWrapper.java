package com.mfh.framework.api.posorder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bingshanguxue on 24/04/2017.
 */

public class BatchInOrdersWrapper implements Serializable {
    private String JSESSIONID;
    private List<BatchInOrder> jsonStr;

    public String getJSESSIONID() {
        return JSESSIONID;
    }

    public void setJSESSIONID(String JSESSIONID) {
        this.JSESSIONID = JSESSIONID;
    }

    public List<BatchInOrder> getJsonStr() {
        return jsonStr;
    }

    public void setJsonStr(List<BatchInOrder> jsonStr) {
        this.jsonStr = jsonStr;
    }
}
