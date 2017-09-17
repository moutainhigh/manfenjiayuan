package com.jabra.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EmptyUtil {
    public static boolean isEmpty(String paramString) {
        return (paramString == null) || (paramString.equals(""));
    }

    public static boolean isEmpty(Collection<?> paramCollection) {
        return (paramCollection == null) || (paramCollection.isEmpty());
    }

    public static boolean isEmpty(List<?> paramList) {
        return (paramList == null) || (paramList.isEmpty());
    }

    public static boolean isEmpty(Map<?, ?> paramMap) {
        return (paramMap == null) || (paramMap.isEmpty());
    }

    public static boolean isEmpty(byte[] paramArrayOfByte) {
        return (paramArrayOfByte == null) || (paramArrayOfByte.length == 0);
    }

    public static boolean isEmpty(char[] paramArrayOfChar) {
        return (paramArrayOfChar == null) || (paramArrayOfChar.length == 0);
    }

    public static boolean isEmpty(double[] paramArrayOfDouble) {
        return (paramArrayOfDouble == null) || (paramArrayOfDouble.length == 0);
    }

    public static boolean isEmpty(float[] paramArrayOfFloat) {
        return (paramArrayOfFloat == null) || (paramArrayOfFloat.length == 0);
    }

    public static boolean isEmpty(int[] paramArrayOfInt) {
        return (paramArrayOfInt == null) || (paramArrayOfInt.length == 0);
    }

    public static boolean isEmpty(long[] paramArrayOfLong) {
        return (paramArrayOfLong == null) || (paramArrayOfLong.length == 0);
    }

    public static boolean isEmpty(Object[] paramArrayOfObject) {
        return (paramArrayOfObject == null) || (paramArrayOfObject.length == 0);
    }

    public static boolean isEmpty(short[] paramArrayOfShort) {
        return (paramArrayOfShort == null) || (paramArrayOfShort.length == 0);
    }

    public static boolean isEmpty(boolean[] paramArrayOfBoolean) {
        return (paramArrayOfBoolean == null) || (paramArrayOfBoolean.length == 0);
    }

    public static boolean notEmpty(String paramString) {
        return !isEmpty(paramString);
    }

    public static boolean notEmpty(Collection<?> paramCollection) {
        return !isEmpty(paramCollection);
    }

    public static boolean notEmpty(List<?> paramList) {
        return !isEmpty(paramList);
    }

    public static boolean notEmpty(Map<?, ?> paramMap) {
        return !isEmpty(paramMap);
    }

    public static boolean notEmpty(byte[] paramArrayOfByte) {
        return !isEmpty(paramArrayOfByte);
    }

    public static boolean notEmpty(char[] paramArrayOfChar) {
        return !isEmpty(paramArrayOfChar);
    }

    public static boolean notEmpty(double[] paramArrayOfDouble) {
        return !isEmpty(paramArrayOfDouble);
    }

    public static boolean notEmpty(float[] paramArrayOfFloat) {
        return !isEmpty(paramArrayOfFloat);
    }

    public static boolean notEmpty(int[] paramArrayOfInt) {
        return !isEmpty(paramArrayOfInt);
    }

    public static boolean notEmpty(long[] paramArrayOfLong) {
        return !isEmpty(paramArrayOfLong);
    }

    public static boolean notEmpty(Object[] paramArrayOfObject) {
        return !isEmpty(paramArrayOfObject);
    }

    public static boolean notEmpty(short[] paramArrayOfShort) {
        return !isEmpty(paramArrayOfShort);
    }

    public static boolean notEmpty(boolean[] paramArrayOfBoolean) {
        return !isEmpty(paramArrayOfBoolean);
    }
}
