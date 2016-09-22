package com.manfenjiayuan.loveshopping.eventbus;

import com.mfh.framework.api.account.Subdis;

/**
 * 事务
 * Created by kun on 15/9/23.
 */
public class CommunityEvent {
    public static final int EVENT_ID_UPDATED = 0X01;//更新

    private int eventId;
    private Subdis mSubdis;

    public CommunityEvent(int eventId, Subdis mSubdis) {
        this.eventId = eventId;
        this.mSubdis = mSubdis;
    }

    public int getEventId() {
        return eventId;
    }

    public Subdis getSubdis() {
        return mSubdis;
    }
}
