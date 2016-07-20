package com.mfh.owner.ui.shake;

import android.content.SharedPreferences;


/**
 * Created by Administrator on 2015/7/2.
 */
public class ShakeHelper {

    private static final String PREF_NAME = "mfh_shakeConf";
    private static final String PREF_KEY_IMAGE_FILE_NAME = "PREF_KEY_IMAGE_FILE_NAME";
    private static final String PREF_KEY_AD_TITLE = "PREF_KEY_AD_TITLE";//广告语
    private static final String PREF_KEY_AD_AUTHOR = "PREF_KEY_AD_AUTHOR";//广告来源
    private static final String PREF_KEY_AD_LINK = "PREF_KEY_AD_LINK";//广告地址

    private static SharedPreferences sp = SharedPreferencesManager.getPreferences(PREF_NAME);

    private static ShakeHelper instance;
    public static ShakeHelper getInstance(){
        if(instance == null){
            sp = SharedPreferencesManager.getPreferences(PREF_NAME);
            return new ShakeHelper();
        }
        return instance;
    }

    public ShakeHelper(){

    }

    public String getImageFileName(){
        return sp.getString(PREF_KEY_IMAGE_FILE_NAME, "");
    }
    public String getAdTitle(){
        return sp.getString(PREF_KEY_AD_TITLE, "");
    }
    public String getAdAuthor(){
        return sp.getString(PREF_KEY_AD_AUTHOR, "");
    }
    public String getAdLink(){
        return sp.getString(PREF_KEY_AD_LINK, "");
    }

    public void saveShakeConf(String fileName, String adTitle, String adAuthor, String adLink){
        SharedPreferences sp = SharedPreferencesManager.getPreferences(PREF_NAME);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_KEY_IMAGE_FILE_NAME, fileName);
        editor.putString(PREF_KEY_AD_TITLE, adTitle);
        editor.putString(PREF_KEY_AD_AUTHOR, adAuthor);
        editor.putString(PREF_KEY_AD_LINK, adLink);
        editor.commit();
    }


}
