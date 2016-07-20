package com.mfh.litecashier.utils;

import com.mfh.framework.core.logger.ZLogger;

import java.util.List;

/**
 * Created by bingshanguxue on 15/9/9.
 */
public class DataCacheHelper {

    private Double netWeight;//净重
    private List<String> comDevicesPath;

    private static DataCacheHelper instance;

    public static DataCacheHelper getInstance() {
        if (instance == null) {
            synchronized (DataCacheHelper.class) {
                if (instance == null) {
                    instance = new DataCacheHelper();
                }
            }
        }
        return instance;
    }

    private void init() {
        netWeight = 0D;
    }


    public void reset(){
        netWeight = 0D;
    }

    public synchronized Double getNetWeight() {
        if (netWeight == null) {
            netWeight = 0D;
        }
        return netWeight;
    }

    public synchronized void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }

    public synchronized void setNetWeight(byte[] data) {
        try {
            if (data == null || data.length < 5) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (byte b : data) {
                char c = (char) b;
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }

            String dest = sb.toString();
            int val = Integer.valueOf(dest);
            netWeight = 0.001 * val;
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public List<String> getComDevicesPath() {
        return comDevicesPath;
    }

    public synchronized void setComDevicesPath(List<String> comDevicesPath) {
        this.comDevicesPath = comDevicesPath;
    }
}
