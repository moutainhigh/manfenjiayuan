package com.mfh.framework.core.utils;

/**
 * Created by bingshanguxue on 16/3/15.
 */
public class ObjectsCompact {

    /**
     * Null-safe equivalent of {@code a.equals(b)}.
     */
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}
