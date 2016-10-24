package com.mfh.litecashier.ui.prepare;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.business.presenter.ScOrderPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IScOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 首页－－采购
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrepareStep1Fragment extends BaseFragment implements IScOrderView {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.label_quantity)
    MultiLayerLabel labelQuantity;
    @Bind(R.id.label_amount)
    MultiLayerLabel labelAmount;
    @Bind(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;

    private String tradeNo;
    private ScOrderPresenter mScOrderPresenter;


    public static PrepareStep1Fragment newInstance(Bundle args) {
        PrepareStep1Fragment fragment = new PrepareStep1Fragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pick_step1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
        mScOrderPresenter = new ScOrderPresenter(this);
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        ZLogger.df(String.format("打开支付页面，%s", StringUtils.decodeBundle(args)));

        if (args != null) {
            tradeNo = args.getString(PrepareActivity.EXTRA_KEY_TRADENO);
        }

        toolbar.setTitle("拣货");
//        setSupportActionBar(toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_close) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_normal);

        initInlvBarcode();
        inlvBarcode.clear();
        inlvBarcode.requestFocusEnd();
        inlvBarcode.setEnabled(true);

        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_normal, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initInlvBarcode() {
        inlvBarcode.setEnterKeySubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
        inlvBarcode.setDigits(2);
        inlvBarcode.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d("setOnKeyListener(PayByCashFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submitOrder();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    /**
     * 查询订单
     */
    private void submitOrder() {
        String barcode = inlvBarcode.getInputString();
        inlvBarcode.clear();
        inlvBarcode.requestFocusEnd();
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        // TODO: 17/10/2016 查询订单
//        PmcStockApiImpl.getGoodsOrderListByHuman(barcode,
//                MfhLoginService.get().getCurrentGuId(),responseRC);
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        mScOrderPresenter.getByBarcode(barcode, ScOrder.MFHORDER_STATUS_BUYING, true);
    }

    private NetCallBack.QueryRsCallBack responseRC = new NetCallBack.QueryRsCallBack<>(
            new NetProcessor.QueryRsProcessor<ScOrder>(new PageInfo(1, 10)) {
                @Override
                public void processQueryResult(RspQueryResult<ScOrder> rs) {
                    //此处在主线程中执行。
                    ScOrder scOrder = null;
                    if (rs != null && rs.getReturnNum() > 0) {
                        scOrder = rs.getRowEntity(0);
                    }

                    refreshScOrder(scOrder);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    DialogUtil.showHint(errMsg);
                    hideProgressDialog();
                    ZLogger.d("加载待拣货订单失败:" + errMsg);
//                    goodsAdapter.setEntityList(null);
                }
            }, ScOrder.class, MfhApplication.getAppContext());


    private void refresh(){
        List<CashierShopcartEntity> entities = null;

        if (!StringUtils.isEmpty(tradeNo)){
            entities = CashierShopcartService.getInstance()
                    .queryAllBy(String.format("posTradeNo = '%s'", tradeNo));
        }

        Double bcount = 0D, amount = 0D;
        if (entities != null && entities.size() > 0){
            for (CashierShopcartEntity entity : entities){
                bcount += entity.getBcount();
                amount += entity.getFinalAmount();
            }
        }

        labelQuantity.setTopText(MUtils.formatDouble(bcount, ""));
        labelAmount.setTopText(MUtils.formatDouble(amount, ""));
    }
    /**
     * 刷新订单
     * */
    private void refreshScOrder(ScOrder scOrder) {
        if (scOrder == null){
            hideProgressDialog();
            DialogUtil.showHint("未找到订单");
            return;
        }

        List<ScOrderItem> items = scOrder.getItems();
        if (!StringUtils.isEmpty(tradeNo) && items != null && items.size() > 0){
            for (ScOrderItem item : items){
                String sqlWhere = String.format("posTradeNo = '%s' and proSkuId = '%d'",
                        tradeNo, item.getSkuId());
                CashierShopcartEntity entity = CashierShopcartService
                        .getInstance().getEntityBy(sqlWhere);
                if (entity != null){
                    item.setQuantityCheck(entity.getBcount());
                }
                else{
                    item.setQuantityCheck(item.getBcount());
                }
            }
        }
        hideProgressDialog();

        Bundle args = new Bundle();
        args.putSerializable(PrepareEvent.KEY_SCORDER, scOrder);
        EventBus.getDefault().post(new PrepareEvent(PrepareEvent.ACTION_PREPARE, args));
    }

    @Override
    public void onIScOrderViewProcess() {

    }

    @Override
    public void onIScOrderViewError(String errorMsg) {
        refreshScOrder(null);
    }

    @Override
    public void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList) {

    }

    @Override
    public void onIScOrderViewSuccess(ScOrder data) {
        refreshScOrder(data);
    }

}
