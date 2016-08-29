package com.mfh.framework.network;

/**
 * Created by bingshanguxue on 7/11/16.
 */
public class HttpAgent {
    private static HttpAgent instance = null;

    public static HttpAgent getInstance() {
        if (instance == null) {
            synchronized (HttpAgent.class) {
                if (instance == null) {
                    instance = new HttpAgent();
                }
            }
        }
        return instance;
    }

    public void postDefault(){

    }

}
