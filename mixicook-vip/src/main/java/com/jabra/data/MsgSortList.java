package com.jabra.data;


import com.jabra.bean.Message;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class MsgSortList
        extends LinkedList<Message> {
    private static final long serialVersionUID = -5216177308726340907L;
    private Comparator<Message> comparator = new Comparator<Message>() {
        public int compare(Message paramAnonymousMessage1, Message paramAnonymousMessage2) {
            return (int) (paramAnonymousMessage1.getCreateTime() - paramAnonymousMessage2.getCreateTime());
        }
    };

    public boolean put(Message paramMessage) {
        if (contains(paramMessage)) {
            return false;
        }
        return super.add(paramMessage);
    }

    public boolean putAll(Collection<? extends Message> paramCollection) {
        boolean bool = true;
        Iterator localIterator = paramCollection.iterator();
        for (; ; ) {
            if (!localIterator.hasNext()) {
                return bool;
            }
            bool &= add((Message) localIterator.next());
        }
    }

    public void sort() {
        Collections.sort(this, this.comparator);
    }
}
