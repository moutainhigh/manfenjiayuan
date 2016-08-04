package com.manfenjiayuan.pda_supermarket.ui.fragment.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 商品－－销量
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsSalesFragment extends BaseFragment {

    @Bind(R.id.label_sellNumber)
    TextLabelView labelSellNumber;
    @Bind(R.id.label_avgSellNum)
    TextLabelView labelAvgSellNum;
    @Bind(R.id.label_sellDayNum)
    TextLabelView labelSellDayNum;
    @Bind(R.id.label_sellMonthNum)
    TextLabelView labelSellMonthNum;

    private ScGoodsSku curGoods = null;

    public static GoodsSalesFragment newInstance(Bundle args) {
        GoodsSalesFragment fragment = new GoodsSalesFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods_sales;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 验证
     */
    public void onEventMainThread(ScGoodsSkuEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ScGoodsSkuEvent(%d)", eventId));
        switch (eventId) {
            case ScGoodsSkuEvent.EVENT_ID_SKU_UPDATE: {
                ScGoodsSku sku = (ScGoodsSku) args.getSerializable("scGoodsSku");
                refresh(sku);
            }
            break;

        }
    }


    /**
     * 刷新信息
     */
    private void refresh(ScGoodsSku invSkuGoods) {
        curGoods = invSkuGoods;
        if (curGoods == null) {
            labelSellNumber.setTvSubTitle("");
            labelAvgSellNum.setTvSubTitle("");
            labelSellDayNum.setTvSubTitle("");
            labelSellMonthNum.setTvSubTitle("");
        } else {
            labelSellNumber.setTvSubTitle(MUtils.formatDouble(curGoods.getSellNumber(), ""));
            labelAvgSellNum.setTvSubTitle(MUtils.formatDouble(curGoods.getAvgSellNum(), ""));
            labelSellDayNum.setTvSubTitle(MUtils.formatDouble(curGoods.getSellDayNum(), ""));
            labelSellMonthNum.setTvSubTitle(MUtils.formatDouble(curGoods.getSellMonthNum(), ""));
        }
    }


}
