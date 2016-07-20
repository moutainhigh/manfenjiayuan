package com.mfh.framework.login;

import java.util.Arrays;
import java.util.List;

/**
 * 模块
 * Created by bingshanguxue on 4/29/16.
 */
public class MfhModule {
    public static final String SUPM_MANAGER     = "SUPM.MANAGER";   //门店
    public static final String CHAIN_MANAGER    = "CHAIN.MANAGER";  //批发商

    public static boolean checkModule(String moduleName, List<String> collections){
        if (collections == null){
            return false;
        }

        return collections.contains(moduleName);
    }

    public static boolean checkModule(String moduleName, String[] collections){
        if (collections == null){
            return false;
        }

        List<String> modulesList = Arrays.asList(collections);

        return modulesList.contains(moduleName);
    }
}
