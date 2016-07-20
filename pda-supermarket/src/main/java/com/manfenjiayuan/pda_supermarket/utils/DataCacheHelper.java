package com.manfenjiayuan.pda_supermarket.utils;


import com.mfh.framework.login.entity.Office;

/**
 * Created by bingshanguxue on 15/9/9.
 */
public class DataCacheHelper {

    private Office currentOffice;//当前网点

    private static DataCacheHelper instance;
    public static DataCacheHelper getInstance(){
        if (instance == null){
            instance = new DataCacheHelper();
            instance.init();
        }
        return instance;
    }

    private void init(){
    }

    public Office getCurrentOffice() {
        return currentOffice;
    }

    public void setCurrentOffice(Office currentOffice) {
        this.currentOffice = currentOffice;
    }

}
