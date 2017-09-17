package com.jabra.utils;

public class EasySql {
    public static final String CMD_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    public static final String CMD_DROP_TABLE = "DROP TABLE IF EXISTS";
    public static final String CONSTRAINT_CHECK = "CHECK";
    public static final String CONSTRAINT_DEFAULT = "DEFAULT";
    public static final String CONSTRAINT_FOREIGN_KEY = "FOREIGN KEY";
    public static final String CONSTRAINT_NOT_NULL = "NOT NULL";
    public static final String CONSTRAINT_PRIMARY_KEY = "PRIMARY KEY";
    public static final String CONSTRAINT_UNIQUE = "UNIQUE";
    public static final String TYPE_BLOG = "BLOG";
    public static final String TYPE_INTERGER = "INTERGER";
    public static final String TYPE_NULL = "NULL";
    public static final String TYPE_REAL = "REAL";
    public static final String TYPE_TEXT = "TEXT";

    public static String buildCreateTableSql(String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3) {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("CREATE TABLE IF NOT EXISTS " + paramString + " (");
        for (int i = 0; ; i++) {
            if (i >= paramArrayOfString1.length) {
                localStringBuilder.replace(-1 + localStringBuilder.length(), localStringBuilder.length(), ");");
                return localStringBuilder.toString();
            }
            String str1 = paramArrayOfString1[i];
            String str2 = paramArrayOfString2[i];
            String str3 = "";
            if ((paramArrayOfString3 != null) && (i < paramArrayOfString3.length)) {
                str3 = paramArrayOfString3[i];
            }
            localStringBuilder.append(str1 + " " + str2 + " " + str3 + ",");
        }
    }

    public static String buildDropTableSql(String paramString) {
        return "DROP TABLE IF EXISTS " + paramString + ";";
    }
}
