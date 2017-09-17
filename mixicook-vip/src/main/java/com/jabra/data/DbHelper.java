package com.jabra.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.jabra.bean.Contacts;
import com.jabra.bean.Message;
import com.jabra.utils.EasyDbHelper;
import com.jabra.utils.EasySql;
import com.jabra.utils.SPFile;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.tencent.mm.sdk.ext.MMOpenApiCaller;

import java.util.Iterator;

public class DbHelper
        extends EasyDbHelper {
    private SPFile mSPFile;

    public DbHelper(Context paramContext) {
        super(paramContext, "wx.db", 4);
        this.mSPFile = new SPFile(paramContext, "FILE_CONFIG");
    }

    private void createTable(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase.execSQL(EasySql.buildCreateTableSql("tb_contacts", new String[]{"f_id", "f_user_id", "f_name", "f_sort"}, new String[]{"TEXT", "TEXT", "TEXT", "INTERGER"}, new String[]{"PRIMARY KEY"}));
        paramSQLiteDatabase.execSQL(EasySql.buildCreateTableSql("tb_message", new String[]{"f_id", "f_to_user_id", "f_from_user_id", "f_from_user_name", "f_msg_type", "f_content_type", "f_content", "f_status", "f_create_time"}, new String[]{"TEXT", "TEXT", "TEXT", "TEXT", "INTERGER", "INTERGER", "TEXT", "INTERGER", "INTERGER"}, new String[]{"PRIMARY KEY"}));
    }

    private void deleteContactsList(SQLiteDatabase paramSQLiteDatabase, String[] paramArrayOfString) {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("f_id");
        localStringBuilder.append(" in (");
        int i = paramArrayOfString.length;
        for (int j = 0; ; j++) {
            if (j >= i) {
                localStringBuilder.replace(-1 + localStringBuilder.length(), localStringBuilder.length(), ")");
                paramSQLiteDatabase.delete("tb_contacts", localStringBuilder.toString(), null);
                return;
            }
            String str = paramArrayOfString[j];
            localStringBuilder.append("'");
            localStringBuilder.append(str);
            localStringBuilder.append("',");
            paramSQLiteDatabase.delete("tb_message", "f_to_user_id= '" + str + "'", null);
        }
    }

    private void deleteMessageList(SQLiteDatabase paramSQLiteDatabase, String[] paramArrayOfString) {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("f_id");
        localStringBuilder.append(" in (");
        int i = paramArrayOfString.length;
        for (int j = 0; ; j++) {
            if (j >= i) {
                localStringBuilder.replace(-1 + localStringBuilder.length(), localStringBuilder.length(), ")");
                paramSQLiteDatabase.delete("tb_message", localStringBuilder.toString(), null);
                return;
            }
            String str = paramArrayOfString[j];
            localStringBuilder.append("'");
            localStringBuilder.append(str);
            localStringBuilder.append("',");
        }
    }

    private void dropTable(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase.execSQL(EasySql.buildDropTableSql("tb_contacts"));
        paramSQLiteDatabase.execSQL(EasySql.buildDropTableSql("tb_message"));
    }

    private ContactsSortList getAllContacts(SQLiteDatabase paramSQLiteDatabase) {
        String str1 = this.mSPFile.getString("KEY_OPEN_ID", "");
        ZLogger.e("user_id:" + str1);
        Cursor localCursor1 = paramSQLiteDatabase.query("tb_contacts", null, "f_user_id='" + str1 + "'", null, null, null, "f_sort");
        ContactsSortList localContactsSortList = new ContactsSortList();
        if (!localCursor1.moveToNext()) {
            localCursor1.close();
            return localContactsSortList;
        }
        Contacts localContacts = new Contacts();
        String str2 = localCursor1.getString(localCursor1.getColumnIndex("f_id"));
        String str3 = localCursor1.getString(localCursor1.getColumnIndex("f_name"));
        localContacts.setUserId(str2);
        localContacts.setNickName(str3);
        Cursor localCursor2 = paramSQLiteDatabase.query("tb_message", null, "f_to_user_id='" + str2 + "'", null, null, null, null);
        MsgSortList localMsgSortList = new MsgSortList();
        for (; ; ) {
            if (!localCursor2.moveToNext()) {
                localCursor2.close();
                localContacts.setMsgSortList(localMsgSortList);
                localContactsSortList.add(localContacts);
                break;
            }
            MMOpenApiCaller.MsgItem localMsgItem = new MMOpenApiCaller.MsgItem();
            localMsgItem.msgId = localCursor2.getString(localCursor2.getColumnIndex("f_id"));
            localMsgItem.fromUserId = localCursor2.getString(localCursor2.getColumnIndex("f_from_user_id"));
            localMsgItem.fromUserNickName = localCursor2.getString(localCursor2.getColumnIndex("f_from_user_name"));
            localMsgItem.content = localCursor2.getString(localCursor2.getColumnIndex("f_content"));
            localMsgItem.contentType = localCursor2.getInt(localCursor2.getColumnIndex("f_content_type"));
            localMsgItem.msgType = localCursor2.getInt(localCursor2.getColumnIndex("f_msg_type"));
            localMsgItem.createTime = localCursor2.getLong(localCursor2.getColumnIndex("f_create_time"));
            localMsgItem.status = localCursor2.getInt(localCursor2.getColumnIndex("f_status"));
            localMsgSortList.add(new Message(localMsgItem));
        }

        return localContactsSortList;
    }

    private void updateContactsList(SQLiteDatabase paramSQLiteDatabase, ContactsSortList paramContactsSortList) {
        String str = this.mSPFile.getString("KEY_OPEN_ID", "");
        Iterator localIterator1 = paramContactsSortList.iterator();
        for (; ; ) {
            if (!localIterator1.hasNext()) {
                return;
            }
            Contacts localContacts = (Contacts) localIterator1.next();
            ContentValues localContentValues1 = new ContentValues();
            localContentValues1.put("f_id", localContacts.getUserId());
            localContentValues1.put("f_name", localContacts.getNickName());
            localContentValues1.put("f_user_id", str);
            paramSQLiteDatabase.replace("tb_contacts", null, localContentValues1);
            Iterator localIterator2 = localContacts.getMsgSortList().iterator();
            while (localIterator2.hasNext()) {
                Message localMessage = (Message) localIterator2.next();
                ContentValues localContentValues2 = new ContentValues();
                localContentValues2.put("f_id", localMessage.getMsgId());
                localContentValues2.put("f_to_user_id", localContacts.getUserId());
                localContentValues2.put("f_from_user_id", localMessage.getFromUserId());
                localContentValues2.put("f_from_user_name", localMessage.getFromUserNickName());
                localContentValues2.put("f_msg_type", Integer.valueOf(localMessage.getMsgType()));
                localContentValues2.put("f_content_type", Integer.valueOf(localMessage.getContentType()));
                localContentValues2.put("f_content", localMessage.getContent());
                localContentValues2.put("f_status", Integer.valueOf(localMessage.getStatus()));
                localContentValues2.put("f_create_time", Long.valueOf(localMessage.getCreateTime()));
                paramSQLiteDatabase.replace("tb_message", null, localContentValues2);
            }
        }
    }

    private void updateContactsSort(SQLiteDatabase paramSQLiteDatabase, ContactsSortList paramContactsSortList) {
        String str = this.mSPFile.getString("KEY_OPEN_ID", "");
        Iterator localIterator = paramContactsSortList.iterator();
        for (; ; ) {
            if (!localIterator.hasNext()) {
                return;
            }
            Contacts localContacts = (Contacts) localIterator.next();
            ContentValues localContentValues = new ContentValues();
            ZLogger.e("sortIndex:" + localContacts.getSortIndex());
            localContentValues.put("f_id", localContacts.getUserId());
            localContentValues.put("f_name", localContacts.getNickName());
            localContentValues.put("f_sort", Integer.valueOf(localContacts.getSortIndex()));
            localContentValues.put("f_user_id", str);
            paramSQLiteDatabase.replace("tb_contacts", null, localContentValues);
        }
    }

    public void deleteContactsList(final String[] paramArrayOfString) {
        agentWrite(new EasyDbHelper.Writer() {
            public void doWrite(SQLiteDatabase paramAnonymousSQLiteDatabase) {
                DbHelper.this.deleteContactsList(paramAnonymousSQLiteDatabase, paramArrayOfString);
            }
        });
    }

    public void deleteMessageList(final String[] paramArrayOfString) {
        agentWrite(new EasyDbHelper.Writer() {
            public void doWrite(SQLiteDatabase paramAnonymousSQLiteDatabase) {
                DbHelper.this.deleteMessageList(paramAnonymousSQLiteDatabase, paramArrayOfString);
            }
        });
    }

    public ContactsSortList getAllContacts() {
//        (ContactsSortList) agentRead(new EasyDbHelper.Reader() {
//            public ContactsSortList doRead(SQLiteDatabase paramAnonymousSQLiteDatabase) {
//                return DbHelper.this.getAllContacts(paramAnonymousSQLiteDatabase);
//            }
//        });
        return null;
    }

    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
        createTable(paramSQLiteDatabase);
    }

    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
        if (paramInt1 >= paramInt2) {
            return;
        }
        dropTable(paramSQLiteDatabase);
        createTable(paramSQLiteDatabase);
    }

    public void updateContactsList(final ContactsSortList paramContactsSortList) {
        agentWrite(new EasyDbHelper.Writer() {
            public void doWrite(SQLiteDatabase paramAnonymousSQLiteDatabase) {
                DbHelper.this.updateContactsList(paramAnonymousSQLiteDatabase, paramContactsSortList);
            }
        });
    }

    public void updateContactsSort(final ContactsSortList paramContactsSortList) {
        agentWrite(new EasyDbHelper.Writer() {
            public void doWrite(SQLiteDatabase paramAnonymousSQLiteDatabase) {
                DbHelper.this.updateContactsSort(paramAnonymousSQLiteDatabase, paramContactsSortList);
            }
        });
    }
}
