package com.manfenjiayuan.pda_supermarket.ui.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.DataSyncManager;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.QueryBarcodeFragment;
import com.manfenjiayuan.pda_supermarket.ui.activity.PrimaryActivity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 库存商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsSalesFragment extends BaseFragment {

    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_barcodee)
    TextLabelView labelBarcode;
    @Bind(R.id.label_buyprice)
    TextLabelView labelBuyprice;
    @Bind(R.id.label_sellMonthNum)
    TextLabelView labelSellMonthNum;
    @Bind(R.id.label_grossProfit)
    TextLabelView labelGrossProfit;

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

            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelBuyprice.setTvSubTitle("");
            labelGrossProfit.setTvSubTitle("");
            labelSellMonthNum.setTvSubTitle("");


//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelProductName.setTvSubTitle(curGoods.getSkuName());
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
//            labelLowerLimit.setEtContent(MUtils.formatDouble(curGoods.getLowerLimit(), ""));
//            labelLowerLimit.setEnabled(true);


            //计算毛利率:(costPrice-buyPrice) / costPrice
            String grossProfit = MUtils.retrieveFormatedGrossMargin(curGoods.getCostPrice(),
                    (curGoods.getCostPrice() - curGoods.getBuyPrice()));
            labelGrossProfit.setTvSubTitle(grossProfit);
            labelSellMonthNum.setTvSubTitle(MUtils.formatDouble(curGoods.getSellMonthNum(), ""));
            labelBuyprice.setTvSubTitle(MUtils.formatDouble(curGoods.getBuyPrice(), ""));
        }
    }


}
