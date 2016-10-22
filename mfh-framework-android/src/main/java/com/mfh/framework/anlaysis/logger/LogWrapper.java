package com.mfh.framework.anlaysis.logger;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.StringUtils;

/**
 * Created by bingshanguxue on 12/10/2016.
 */

public class LogWrapper {
    public static final String TAG_DEFAULT = "bingshanguxue";

    private static final String SUFFIX = ".java";
    private static final String PARAM = "Param";
    private static final String NULL = "null";


    public static String[] wrapper(String tagStr, Object... objects) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = 5;
        String className = stackTrace[index].getClassName();//stackTrace[index].getFileName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + SUFFIX;
        }
        if (className.contains("$")) {
            className = className.split("\\$")[0] + SUFFIX;
        }

        String methodName = stackTrace[index].getMethodName();
        int lineNumber = stackTrace[index].getLineNumber();
        if (lineNumber < 0) {
            lineNumber = 0;
        }

        String methodNameShort = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        String tag = (tagStr == null ? className : tagStr);
        if (StringUtils.isEmpty(tag)) {
            tag = TAG_DEFAULT;
        }

        String msg = (objects == null) ? "" : getObjectsString(objects);
        String processName = MfhApplication.getProcessName(MfhApplication.getAppContext(),
                android.os.Process.myPid());

        StringBuilder sb = new StringBuilder();
        sb.append("[ (").append(className)
                .append(":").append(lineNumber)
                .append(")#").append(methodNameShort)
                .append(")#").append(processName)
                .append(" ]");
        String headString = sb.toString();

        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {
        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object == null) {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(NULL).append("\n");
                } else {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return object == null ? NULL : object.toString();
        }
    }
}
