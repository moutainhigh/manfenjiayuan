package com.tencent.mm.sdk.ext;

import android.os.Build;

public class CApiLevel
{
  public static final int API03_CUPCAKE_15 = 3;
  public static final int API04_DONUT_16 = 4;
  public static final int API05_ECLAIR_20 = 5;
  public static final int API06_ECLAIR_201 = 6;
  public static final int API07_ECLAIR_21 = 7;
  public static final int API08_FROYO_22 = 8;
  public static final int API09_GINGERBREAD_23 = 9;
  public static final int API10_GINGERBREAD_MR1_233 = 10;
  public static final int API11_HONEYCOMB_30 = 11;
  public static final int API12_HONEYCOMB_MR1_31X = 12;
  public static final int API13_HONEYCOMB_MR2_32 = 13;
  public static final int API14_ICE_CREAM_SANDWICH_40 = 14;
  public static final int API15_ICE_CREAM_SANDWICH_404 = 15;
  public static final int API16_JELLY_BEAN_41 = 16;
  public static final int API17_JELLY_BEAN_42 = 17;
  public static final int API18_JELLY_BEAN_43 = 18;
  public static final int API19_KITKAT_44 = 19;
  
  public static boolean versionBelow(int paramInt)
  {
    return Build.VERSION.SDK_INT < paramInt;
  }
  
  public static boolean versionHigher(int paramInt)
  {
    return Build.VERSION.SDK_INT > paramInt;
  }
  
  public static boolean versionNotBelow(int paramInt)
  {
    return Build.VERSION.SDK_INT >= paramInt;
  }
}
