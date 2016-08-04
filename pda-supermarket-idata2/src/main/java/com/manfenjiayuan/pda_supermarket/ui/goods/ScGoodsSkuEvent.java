package com.manfenjiayuan.pda_supermarket.ui.goods;

import android.os.Bundle;

public class ScGoodsSkuEvent {
        public static final int EVENT_ID_SKU_UPDATE = 0X01;//商品刷新

        private int eventId;
        private Bundle args;//参数

        public ScGoodsSkuEvent(int eventId) {
            this.eventId = eventId;
        }

        public ScGoodsSkuEvent(int eventId, Bundle args) {
            this.eventId = eventId;
            this.args = args;
        }

        public int getEventId() {
            return eventId;
        }

        public Bundle getArgs() {
            return args;
        }
    }