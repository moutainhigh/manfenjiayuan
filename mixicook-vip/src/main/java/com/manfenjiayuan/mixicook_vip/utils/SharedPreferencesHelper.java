package com.manfenjiayuan.mixicook_vip.utils;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesUltimate;


/**
 * <ol>
 * Preference Key (简称PK，命名格式也是以PK开头)
 * <li>命名格式: PK_[数据类型]_[功能模块]_[方法名]_[辅助参数]</li>
 * <li>数据类型(DATATYPE)
 * <table>
 * <th>PK_[DATATYPE]</th>
 * <tr>
 * <td>PK_S</td>
 * <td>PK_B</td>
 * <td>PK_I</td>
 * <td>PK_L</td>
 * </tr>
 * <tr>
 * <td>String</td>
 * <td>boolean/Boolean</td>
 * <td>int/Integer</td>
 * <td>long/Long</td>
 * </tr>
 * </table>
 * </li>
 * <li>功能模块(DOMAIN)</li>
 * <p/>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 2015/6/17.<br>
 */
public class SharedPreferencesHelper {
    public static String PREF_NAME_APP = "pref_mixicook_vip";

    /**
     * 前缀＋公司编号＋部门编号
     * 使用静态数据会导致数据不能同步
     */
    public static String register() {
        return String.format("%s_%d_%d", PREF_NAME_APP, MfhLoginService.get().getSpid(),
                MfhLoginService.get().getCurOfficeId());
    }

    public static String getText(String key) {
        return SharedPrefesUltimate.getString(PREF_NAME_APP, key, "");
    }

    public static String getText(String key, String defVal) {
        return SharedPrefesUltimate.getString(PREF_NAME_APP, key, defVal);
    }


    public static int getInt(String key, int defVal) {
        return SharedPrefesUltimate.getInt(PREF_NAME_APP, key, defVal);
    }

    public static Long getLong(String key, Long defVal) {
        return SharedPrefesUltimate.getLong(PREF_NAME_APP, key, defVal);
    }

    public static boolean getBoolean(String key, boolean defVal) {
        return SharedPrefesUltimate.getBoolean(PREF_NAME_APP, key, defVal);
    }

    public static void set(String key, String value) {
        SharedPrefesUltimate.set(PREF_NAME_APP, key, value);
    }

    public static void set(String key, int value) {
        SharedPrefesUltimate.set(PREF_NAME_APP, key, value);
    }

    public static void set(String key, Long value) {
        SharedPrefesUltimate.set(PREF_NAME_APP, key, value);
    }

    public static void set(String key, boolean value) {
        SharedPrefesUltimate.set(PREF_NAME_APP, key, value);
    }


}
