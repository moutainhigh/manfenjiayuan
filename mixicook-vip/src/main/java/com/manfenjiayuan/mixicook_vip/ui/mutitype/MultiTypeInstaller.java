package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import com.mfh.framework.api.anon.sc.storeRack.StoreRackCard;

import me.drakeet.multitype.MultiTypePool;

/**
 * Created by bingshanguxue on 09/10/2016.
 */

public class MultiTypeInstaller {
    public static void register() {
        MultiTypePool.register(Card1.class, new Card1ViewProvider());
        MultiTypePool.register(StoreRackCard.class, new Card2ViewProvider());
        MultiTypePool.register(Card6.class, new Card6ViewProvider());
        MultiTypePool.register(Card9.class, new Card9ViewProvider());
        MultiTypePool.register(Card10.class, new Card10ViewProvider());
    }
}
