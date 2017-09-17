package com.jabra.data;

import android.content.Context;
import android.util.Log;

import com.jabra.bean.Contacts;
import com.jabra.bean.Message;
import com.jabra.utils.EmptyUtil;
import com.jabra.utils.SPFile;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ContactsContainer {
    private static final int MAX_CONTACTS_COUNT = 8;
    private static final long MAX_RETAIN_TIME = 86400000L;
    private static final String TAG = "ContactsContainer";
    private ContactsSortList contactsSortList = new ContactsSortList();
    private Context context;
    private Contacts currentContacts;
    private DbHelper dbHelper;
    private SPFile mSPFile;

    public ContactsContainer(Context paramContext) {
        this.context = paramContext;
        this.dbHelper = new DbHelper(paramContext);
        this.mSPFile = new SPFile(paramContext, "FILE_CONFIG");
        ZLogger.d("ContactsContainer init");
        getAllContacts();
    }

    private void sort() {
        long l = System.currentTimeMillis();
        ArrayList localArrayList = new ArrayList();
        Iterator localIterator1 = this.contactsSortList.iterator();
        String[] arrayOfString = new String[0];
        int j = 0;
        if (!localIterator1.hasNext()) {
            if (localArrayList.size() > 0) {
                arrayOfString = new String[localArrayList.size()];
                j = 0;
//        label54:
                if (j < localArrayList.size()) {
//                    break label250;
                }
                this.dbHelper.deleteMessageList(arrayOfString);
            }

            this.contactsSortList.sort();
        }

        for (int i = 0; ; i++) {
            if (i >= this.contactsSortList.size()) {
                this.dbHelper.updateContactsSort(this.contactsSortList);
//                return;
                Contacts localContacts = (Contacts) localIterator1.next();
                LinkedList localLinkedList = new LinkedList();
                Iterator localIterator2 = localContacts.getMsgSortList().iterator();
                for (; ; ) {
                    if (!localIterator2.hasNext()) {
                        localContacts.getMsgSortList().removeAll(localLinkedList);
                        break;
                    }
                    Message localMessage = (Message) localIterator2.next();
                    if (l - 86400000L > localMessage.getCreateTime()) {
                        ZLogger.e("delete msg:" + localMessage.getContent() + "   time:" + localMessage.getCreateTime());
                        localLinkedList.add(localMessage);
                        localArrayList.add(localMessage.getMsgId());
                    }
                }
//                label250:
                arrayOfString[j] = ((String) localArrayList.get(j));
                j++;
                break;
            }
            this.contactsSortList.get(i).setSortIndex(i);
            ZLogger.e("sortIndex:" + this.contactsSortList.get(i).getSortIndex()
                    + " nickname:" + this.contactsSortList.get(i).getNickName());
        }
    }

    public void abandonCurrentContactsMsgList() {
        ZLogger.d("abandonCurrentContactsMsgList   currentContacts:" + this.currentContacts.getUserId());
        if ((this.currentContacts == null) || (this.currentContacts.getMsgSortList().isEmpty())) {
            return;
        }
        int i = this.currentContacts.getMsgSortList().size();
        String[] arrayOfString = new String[i];
        for (int j = 0; ; j++) {
            if (j >= i) {
                this.currentContacts.getMsgSortList().clear();
                setMsgsReaded(arrayOfString);
                return;
            }
            arrayOfString[j] = ((Message) this.currentContacts.getMsgSortList().get(j)).getMsgId();
        }
    }

    public void getAllContacts() {
        ContactsSortList localContactsSortList = this.dbHelper.getAllContacts();
        if (localContactsSortList != null) {
            this.contactsSortList = localContactsSortList;
            ZLogger.d("contactsSortList.size:" + this.contactsSortList.size());
            this.currentContacts = this.contactsSortList.peekLast();
        } else {
            ZLogger.w("no contacts exist");
        }
    }

    public Contacts getCurrentContacts() {
        return this.currentContacts;
    }

    public boolean isEmpty() {
        Iterator localIterator1 = this.contactsSortList.iterator();
        Contacts localContacts;
        do {
            if (!localIterator1.hasNext()) {
                return true;
            }
            localContacts = (Contacts) localIterator1.next();
        } while (!EmptyUtil.notEmpty(localContacts.getMsgSortList()));
        ZLogger.d("������������������������" + localContacts.getNickName() + "   msg.size:" + localContacts.getMsgSortList().size());
        Iterator localIterator2 = localContacts.getMsgSortList().iterator();
        for (; ; ) {
            if (!localIterator2.hasNext()) {
                return false;
            }
            Message localMessage = (Message) localIterator2.next();
            ZLogger.e("msg:" + localMessage.getContent());
        }
    }

    public Contacts next() {
        if (this.contactsSortList.size() < 1) {
            return null;
        }
        sort();
        int i = -1 + this.contactsSortList.indexOf(this.currentContacts);
        if (i < 0) {
            i = -1 + this.contactsSortList.size();
        }
        ZLogger.d("index:" + i + "  contactsSortList.size:" + this.contactsSortList.size());
        this.currentContacts = ((Contacts) this.contactsSortList.get(i));
        return this.currentContacts;
    }

    public Contacts recently() {
        sort();
        this.currentContacts = this.contactsSortList.peekLast();
        ZLogger.e("recently :" + this.currentContacts.getNickName());
        return this.currentContacts;
    }

    public void setMsgsReaded(String[] paramArrayOfString) {
        ZLogger.d("setMsgsReaded");
        this.dbHelper.deleteMessageList(paramArrayOfString);
    }

    public void update(ContactsSortList paramContactsSortList) {
        ZLogger.d("update msg  newContactsSortList.size:" + paramContactsSortList.size());
        this.contactsSortList.putAll(paramContactsSortList);
        this.dbHelper.updateContactsList(paramContactsSortList);
        sort();
        updateContactsList();
    }

    public void updateContactsList() {
        ZLogger.e("updateContactsList");
        String[] arrayOfString = new String[0];
        int i = 0;
        if (this.contactsSortList.size() > 8) {
            arrayOfString = new String[-8 + this.contactsSortList.size()];
            i = this.contactsSortList.size();
        }
        for (int j = 0; ; j++) {
            if (j >= i - 8) {
                this.dbHelper.deleteContactsList(arrayOfString);
                ZLogger.e("updateContactsList   contactsSortList.size(): " + this.contactsSortList.size());
                return;
            }
            ZLogger.e("updateContactsList   ������ 8������" + j);
            arrayOfString[j] = ((Contacts) this.contactsSortList.removeFirst()).getUserId();
        }
    }
}
