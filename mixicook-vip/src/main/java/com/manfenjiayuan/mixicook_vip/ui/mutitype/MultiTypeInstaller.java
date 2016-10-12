package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import com.mfh.framework.api.anon.storeRack.StoreRackCard;

import me.drakeet.multitype.MultiTypePool;

/**
 * Created by bingshanguxue on 09/10/2016.
 */

public class MultiTypeInstaller {
    public static void start() {
        MultiTypePool.register(Card1.class, new Card1ViewProvider());
        MultiTypePool.register(StoreRackCard.class, new Card2ViewProvider());
        MultiTypePool.register(Card9.class, new Card9ViewProvider());
//        MultiTypePool.register(ImageItem.class, new ImageItemViewProvider());
//        MultiTypePool.register(RichItem.class, new RichItemViewProvider());
//        MultiTypePool.register(Category.class, new CategoryItemViewProvider());
//        MultiTypePool.register(PostRowItem.class, new PostRowItemViewProvider());
//        MultiTypePool.register(PostList.class, new HorizontalItemViewProvider());
//        MultiTypePool.register(Square.class, new SquareViewProvider());
    }
}
