package com.mfh.framework.core.utils;

import java.util.Date;
import java.util.List;

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

    public static int compare(Date a, Date b) {
        if (a == null){
            return -1;
        }

        if (b == null){
            return 1;
        }

        return a.compareTo(b);
    }

    public static String splitLong(List<Long> entities, String split){
        StringBuilder sb = new StringBuilder();

        if (entities != null && entities.size() > 0){
            for (Long entity : entities){
                if (sb.length() > 0){
                    sb.append(split);
                }
                sb.append(entity);
            }
        }
        return sb.toString();
    }

}
