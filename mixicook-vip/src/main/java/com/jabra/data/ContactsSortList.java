package com.jabra.data;

import com.jabra.bean.Contacts;
import com.jabra.bean.Message;
import com.jabra.utils.EmptyUtil;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class ContactsSortList
        extends LinkedList<Contacts> {
    private static final String TAG = "ContactsSortList";
    private static final long serialVersionUID = -7658294394871420753L;
    private Comparator<Contacts> comparator = new Comparator<Contacts>() {
        public int compare(Contacts paramAnonymousContacts1, Contacts paramAnonymousContacts2) {
            long l1 = 0L;
            long l2 = 0L;
            try {
                long l4 = ((Message) paramAnonymousContacts1.getMsgSortList().getLast()).getCreateTime();
                l1 = l4;
            } catch (Exception localException1) {
            }
            try {
                long l3 = ((Message) paramAnonymousContacts2.getMsgSortList().getLast()).getCreateTime();
                l2 = l3;
            } catch (Exception localException2) {
            }

            if ((l1 == 0L) && (l2 == 0L)) {
                return 0;
            }
            if (l1 == 0L) {
                return -1;
            }
            if (l2 == 0L) {
                return 1;
            }
            return (int) (l1 - l2);
        }
    };

    public boolean put(Contacts paramContacts) {
        if (EmptyUtil.isEmpty(paramContacts.getNickName())) {
            ZLogger.d("put  contacts nickname is null");
            return false;
        }
        ZLogger.d("put   ��������������������������� object.getMsgSortList().size:" + paramContacts.getMsgSortList().size());
        Iterator localIterator = iterator();
        Contacts localContacts;
        do {
            if (!localIterator.hasNext()) {
                ZLogger.d("put   ���������������������������  object.getUserId():" + paramContacts.getUserId());
                return super.add(paramContacts);
            }
            localContacts = (Contacts) localIterator.next();
            ZLogger.d("put   ��������������������������� contacts.getUserId:" + localContacts.getUserId());
        } while (!localContacts.equals(paramContacts));
        ZLogger.d("put   ������������������������������ ������������������������������MessgaeList���");
        return localContacts.getMsgSortList().putAll(paramContacts.getMsgSortList());
    }

    public boolean putAll(Collection<? extends Contacts> paramCollection) {
        boolean bool = true;
        Iterator localIterator = paramCollection.iterator();
        for (; ; ) {
            if (!localIterator.hasNext()) {
                ZLogger.d("ContactsSortList", "putAll  result:" + bool);
                return bool;
            }
            bool &= put((Contacts) localIterator.next());
        }
    }

    public void sort() {
        Iterator localIterator = iterator();
        for (; ; ) {
            if (!localIterator.hasNext()) {
                Collections.sort(this, this.comparator);
                return;
            }
            Contacts localContacts = (Contacts) localIterator.next();
            if (localContacts.getMsgSortList() != null) {
                localContacts.getMsgSortList().sort();
            }
        }
    }
}
