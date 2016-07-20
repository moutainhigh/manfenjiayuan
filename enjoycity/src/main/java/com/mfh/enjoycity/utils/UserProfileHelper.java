package com.mfh.enjoycity.utils;

import android.content.SharedPreferences;

import com.mfh.enjoycity.bean.UserProfile;


/**
 * UserProfile帮助类
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/17.
 */
public class UserProfileHelper {

    private static final String PREF_NAME = "mfh_userProfile";
    private static final String PREF_KEY_AMOUNT = "PREF_KEY_AMOUNT";
    private static final String PREF_KEY_SCORE = "PREF_KEY_SCORE";
    private static final String PREF_KEY_FAVORITE_NUM = "PREF_KEY_FAVORITE_NUM";
    private static final String PREF_KEY_WAIT_PAY_NUM = "PREF_KEY_WAIT_PAY_NUM";
    private static final String PREF_KEY_WAIT_RECEIVE_NUM = "PREF_KEY_WAIT_RECEIVE_NUM";
    private static final String PREF_KEY_WAIT_PRAISE_NUM= "PREF_KEY_WAIT_PRAISE_NUM";
    private static final String PREF_KEY_SHOPPING_CART_NUM = "PREF_KEY_SHOPPING_CART_NUM";

    /**
     * 保存用户信息
     * */
    public static void saveUserProfile(UserProfile userProfile){
        if(userProfile == null){
            return;
        }

        SharedPreferences sp = SharedPreferencesManager.getPreferences(PREF_NAME);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_KEY_AMOUNT, userProfile.getAmount());
        editor.putString(PREF_KEY_SCORE, userProfile.getScore());
        editor.putString(PREF_KEY_FAVORITE_NUM, userProfile.getFavoriteNum());
        editor.putString(PREF_KEY_WAIT_PAY_NUM, userProfile.getWaitPayNum());
        editor.putString(PREF_KEY_WAIT_RECEIVE_NUM, userProfile.getWaitReceiveNum());
        editor.putString(PREF_KEY_WAIT_PRAISE_NUM, userProfile.getWaitPraiseNum());
        editor.putString(PREF_KEY_SHOPPING_CART_NUM, userProfile.getShoppingCartNum());

//                            userProfile.getCardCouponsNum()));
//                        userProfile.getDefaultStock()));
//                            userProfile.getDefaultSubids()));
        editor.commit();
    }

    /**
     * 清空个人信息
     * */
    public static void cleanUserProfile(){
        SharedPreferences sp = SharedPreferencesManager.getPreferences(PREF_NAME);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 获取用户信息
     * */
    public static UserProfile getUserProfile(){
        UserProfile userProfile = new UserProfile();

        SharedPreferences sp = SharedPreferencesManager.getPreferences(PREF_NAME);
        userProfile.setAmount(sp.getString(PREF_KEY_AMOUNT, "0.0"));
        userProfile.setScore(sp.getString(PREF_KEY_SCORE, "0"));
        userProfile.setFavoriteNum(sp.getString(PREF_KEY_FAVORITE_NUM, "0"));
        userProfile.setWaitPayNum(sp.getString(PREF_KEY_WAIT_PAY_NUM, "0"));
        userProfile.setWaitReceiveNum(sp.getString(PREF_KEY_WAIT_RECEIVE_NUM, "0"));
        userProfile.setWaitPraiseNum(sp.getString(PREF_KEY_WAIT_PRAISE_NUM, "0"));
        userProfile.setShoppingCartNum(sp.getString(PREF_KEY_SHOPPING_CART_NUM, "0"));

        return userProfile;
    }


}
