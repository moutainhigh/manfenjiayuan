package com.manfenjiayuan.pda_supermarket.ui.store.cashier;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.LastOrderInfo;
import com.manfenjiayuan.pda_supermarket.cashier.CashierAgent;
import com.manfenjiayuan.pda_supermarket.cashier.CashierOrderInfo;
import com.manfenjiayuan.pda_supermarket.database.entity.CashierShopcartEntity;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderEntity;
import com.manfenjiayuan.pda_supermarket.database.entity.PosProductEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.CashierShopcartService;
import com.manfenjiayuan.pda_supermarket.service.UploadSyncManager;
import com.manfenjiayuan.pda_supermarket.ui.store.CashierPayActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 收银
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CashierFragment extends PDAScanFragment implements ICashierView {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

    @Bind(R.id.label_quantity)
    MultiLayerLabel labelQuantity;
    @Bind(R.id.label_amount)
    MultiLayerLabel labelAmount;

    @Bind(R.id.product_list)
    RecyclerView productRecyclerView;
    private ItemTouchHelper itemTouchHelper;
    private CashierSwipAdapter productAdapter;
    @Bind(R.id.fab_submit)
    FloatingActionButton btnSubmit;
    @Bind(R.id.fab_scan)
    FloatingActionButton btnSweep;

    /**
     * POS唯一订单号，由POS机本地生成的12位字符串
     */
    private String curPosTradeNo;
    /**
     * 订单折扣，默认值为1，新扫描商品默认使用该折扣
     */
    private Double orderDiscount = 100D;
    private CashierPresenter cashierPresenter;


//    private DoubleInputDialog quantityCheckDialog = null;

    public static CashierFragment newInstance(Bundle args) {
        CashierFragment fragment = new CashierFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected boolean isResponseBackPressed() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier;
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;

        queryGoods(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        cashierPresenter = new CashierPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
        }
        mToolbar.setTitle("收银");
        if (animType == ANIM_TYPE_NEW_FLOW) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_settings) {
//                    getActivity().onBackPressed();
//                }
//                return true;
//            }
//        });
//        // Inflate a menu to be displayed in the toolbar
//        mToolbar.inflateMenu(R.menu.menu_main);
        mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                queryGoods(text);
            }

            @Override
            public void onAction1Click(String text) {
                queryGoods(text);
            }
        });

        if (SharedPrefesManagerFactory.isCameraSweepEnabled()) {
            btnSweep.setVisibility(View.VISIBLE);
        } else {
            btnSweep.setVisibility(View.GONE);
        }
        initCashierRecyclerView();

        reload();
    }

    @Override
    public void onResume() {
        super.onResume();

        mScanBar.reset();
    }

    @Override
    public boolean onBackPressed() {
        hangUpOrder();

        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();

        return isResponseBackPressed();
    }

    @OnClick(R.id.fab_scan)
    @Override
    protected void zxingSweep() {
        super.zxingSweep();
    }

    /**
     * 获取当前订单交易编号
     */
    public void obtaincurPosTradeNo(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            curPosTradeNo = MUtils.getOrderBarCode();
        } else {
            curPosTradeNo = barcode;
        }
    }

    /**
     * 挂单
     * */
    public void hangUpOrder() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "保存数据...", false);
        mScanBar.reset();

        //Step 1:
        if (productAdapter.getItemCount() > 0) {
            ZLogger.d(String.format("挂单：%s", curPosTradeNo));
            CashierAgent.settle(curPosTradeNo, PosOrderEntity.ORDER_STATUS_HANGUP,
                    productAdapter.getEntityList());
        }

//        //重新生成订单
        if (!StringUtils.isEmpty(curPosTradeNo)) {
            CashierShopcartService.getInstance()
                    .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
        }
        obtaincurPosTradeNo(null);
        productAdapter.setEntityList(null);
        hideConfirmDialog();
    }

    /**
     * 重新加载数据
     */
    private void reload() {
        try {
            if (mScanBar != null) {
                mScanBar.reset();
            }

            //加载订单
            if (!StringUtils.isEmpty(curPosTradeNo)) {
                //删除旧订单数据
                CashierShopcartService.getInstance()
                        .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
            }

            PosOrderEntity hangupEntity = CashierAgent.fetchOrderEntity(BizType.POS,
                    PosOrderEntity.ORDER_STATUS_HANGUP);
            if (hangupEntity != null){
                obtaincurPosTradeNo(hangupEntity.getBarCode());
                CashierShopcartService.getInstance().readOrderItems(curPosTradeNo,
                        CashierAgent.resume(curPosTradeNo));
            }
            else{
                ZLogger.d("没有挂单数据");
                obtaincurPosTradeNo(null);
            }

            if (productAdapter != null) {
                productAdapter.setEntityList(CashierShopcartService.getInstance()
                        .queryAllBy(String.format("posTradeNo = '%s'", curPosTradeNo)));
            }

            ZLogger.d("重新加载数据，准备同步数据...");
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }


    /**
     * 签收采购订单
     */
    @OnClick(R.id.fab_submit)
    public void settle() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        btnSubmit.setEnabled(false);

        //判断是否登录
        if (!MfhLoginService.get().haveLogined()) {
            DialogUtil.showHint("请先登录");
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        //判断是否登录
        if (productAdapter.getItemCount() <= 0) {
            DialogUtil.showHint("商品明细不能为空");
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        if (productAdapter.haveEmptyPrice()) {
            showConfirmDialog("有商品未设置价格或价格为零，是否确认结算？",
                    "结算", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            doPosSettleStuff();
                        }
                    }, "点错了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            btnSubmit.setEnabled(true);
                            hideProgressDialog();
                        }
                    });
        } else {
            doPosSettleStuff();
        }
    }


    /**
     * <ol>
     * 结算
     * <li>判断当前收银台购物车的商品是否为空，若不为空，则继续第2步，否则结束；</li>
     * <li>生成订单,［并拆单］；</li>
     * <li>更新订单明细（需要删除历史记录）；</li>
     * <li>结束</li>
     * </ol>
     */
    private void doPosSettleStuff() {
        Observable.create(new Observable.OnSubscribe<CashierOrderInfo>() {
            @Override
            public void call(Subscriber<? super CashierOrderInfo> subscriber) {
                CashierOrderInfo cashierOrderInfo = CashierAgent.settle(curPosTradeNo,
                        PosOrderEntity.ORDER_STATUS_STAY_PAY, productAdapter.getEntityList());

                subscriber.onNext(cashierOrderInfo);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CashierOrderInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CashierOrderInfo cashierOrderInfo) {
                        if (cashierOrderInfo != null) {
                            hideProgressDialog();
                            Intent intent = new Intent(getActivity(), CashierPayActivity.class);
                            Bundle extras = new Bundle();
                            extras.putSerializable(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
                            intent.putExtras(extras);
                            startActivityForResult(intent, ARCode.ARC_CASHIER);
                        } else {
                            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "订单创建失败", true);
                            btnSubmit.setEnabled(true);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_CASHIER: {
                if (resultCode == Activity.RESULT_OK) {
                    ZLogger.df("订单支付成功");
                    if (data != null) {
                        CashierOrderInfo cashierOrderInfo = (CashierOrderInfo) data
                                .getSerializableExtra(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO);

                        saveSettleResult(curPosTradeNo, cashierOrderInfo);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    ZLogger.df("取消收银订单支付");
                    boolean isClearOrder = false;
                    if (data != null) {
                        isClearOrder = data.getBooleanExtra(CashierPayActivity.EXTRA_KEY_IS_CLEAR_ORDER, false);
                    }
                    if (isClearOrder) {
                        ZLogger.df("清空收银购物车，重新开始新订单");
//加载订单
                        if (StringUtils.isEmpty(curPosTradeNo)) {
                            CashierShopcartService.getInstance()
                                    .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
                        }
                        obtaincurPosTradeNo(null);
                        productAdapter.setEntityList(null);
                    }
                    btnSubmit.setEnabled(true);

                } else {
                    ZLogger.df("取消收银订单支付2");
                    btnSubmit.setEnabled(true);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 异步处理订单支付结果
     * <p/>
     * 保存订单信息，打印小票，显示上一单信息，同步订单，统计订单金额，语音播报
     */
    private void saveSettleResult(final String posTradeNo, final CashierOrderInfo cashierOrderInfo) {
        //清空当前收银列表，开始新的订单
        obtaincurPosTradeNo(null);
        productAdapter.setEntityList(null);

        Observable.create(new Observable.OnSubscribe<LastOrderInfo>() {
            @Override
            public void call(Subscriber<? super LastOrderInfo> subscriber) {

                //重新生成订单，清空购物车
                if (!StringUtils.isEmpty(posTradeNo)) {
                    CashierShopcartService.getInstance()
                            .deleteBy(String.format("posTradeNo = '%s'", posTradeNo));
                }

                ZLogger.df(String.format("%s支付，流水编号：%s\n%s",
                        WayType.name(cashierOrderInfo.getBizType()), cashierOrderInfo.getPosTradeNo(),
                        JSONObject.toJSONString(cashierOrderInfo)));

                PosOrderEntity orderEntity = CashierAgent.fetchOrderEntity(BizType.POS,
                        cashierOrderInfo.getPosTradeNo());
                //同步订单信息
                UploadSyncManager.getInstance().stepUploadPosOrder(orderEntity);

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LastOrderInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(LastOrderInfo lastOrderInfo) {

                        btnSubmit.setEnabled(true);
                    }

                });
    }

    /**
     * 初始化商品列表
     */
    private void initCashierRecyclerView() {
        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        productRecyclerView.setHasFixedSize(true);
        //设置Item增加、移除动画
//        productRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //分割线
        productRecyclerView.addItemDecoration(new LineItemDecoration(getActivity(),
                LineItemDecoration.VERTICAL_LIST));

        productAdapter = new CashierSwipAdapter(getActivity(), null);
        productAdapter.setOnAdapterListener(new CashierSwipAdapter.OnAdapterListener() {

            @Override
            public void onPriceClicked(int position) {
                changeGoodsPrice(position);
            }

            @Override
            public void onQuantityClicked(int position) {
                changeGoodsQuantity(position);
            }

            @Override
            public void onDataSetChanged(boolean needScroll) {
                labelQuantity.setTopText(String.format("%.2f", productAdapter.getBcount()));
                labelAmount.setTopText(String.format("%.2f", productAdapter.getFinalAmount()));//成交价

                if (productAdapter.getItemCount() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
                if (needScroll) {
                    //后来者居上
                    productRecyclerView.scrollToPosition(0);
                }
            }
        });

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(productAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        //关联到RecyclerView
        itemTouchHelper.attachToRecyclerView(productRecyclerView);

        // specify an adapter
        productRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 修改商品数量
     */
    private void changeGoodsQuantity(final int position) {
        final CashierShopcartEntity entity = productAdapter.getEntity(position);
        if (entity == null) {
            return;
        }

        if (changePriceDialog == null) {
            changePriceDialog = new NumberInputDialog(getActivity());
            changePriceDialog.setCancelable(true);
            changePriceDialog.setCanceledOnTouchOutside(true);
        }
        changePriceDialog.initializeDecimalNumber(EditInputType.PRICE, "修改数量",
                MUtils.formatDouble(entity.getBcount(), "数量"),
                2,  "元",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {

                    }

                    @Override
                    public void onNext(Double value) {
                        entity.setBcount(value);
                        entity.setAmount(entity.getBcount() * entity.getCostPrice());
                        entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
                        CashierShopcartService.getInstance().saveOrUpdate(entity);

                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged(position, false);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }

                });
        if (!changePriceDialog.isShowing()) {
            changePriceDialog.show();
        }
    }

    private NumberInputDialog changePriceDialog = null;
    /**
     * 修改商品价格
     */
    private void changeGoodsPrice(final int position) {
        final CashierShopcartEntity entity = productAdapter.getEntity(position);
        if (entity == null) {
            return;
        }
//
        if (changePriceDialog == null) {
            changePriceDialog = new NumberInputDialog(getActivity());
            changePriceDialog.setCancelable(true);
            changePriceDialog.setCanceledOnTouchOutside(true);
        }
        changePriceDialog.initializeDecimalNumber(EditInputType.PRICE, "修改价格",
                MUtils.formatDouble(entity.getFinalPrice(), "价格"),
                2, "元",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {

                    }

                    @Override
                    public void onNext(Double value) {
                        entity.setFinalPrice(value);
                        entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
                        CashierShopcartService.getInstance().saveOrUpdate(entity);

                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged(position, false);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }

                });
        if (!changePriceDialog.isShowing()) {
            changePriceDialog.show();
        }
    }


    /**
     * 查询商品信息
     */
    public void queryGoods(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.requestFocus();
            isAcceptBarcodeEnabled = true;
            return;
        }

        mScanBar.reset();

        if (!NetworkUtils.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            return;
        }

        cashierPresenter.findGoods(barcode);
    }


    @Override
    public void onFindGoods(final PosProductEntity goods, int packFlag) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            isAcceptBarcodeEnabled = true;

            return;
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            DialogUtil.showHint("暂时不支持记重商品收银");
//            if (quantityCheckDialog == null) {
//                quantityCheckDialog = new DoubleInputDialog(this);
//                quantityCheckDialog.setCancelable(true);
//                quantityCheckDialog.setCanceledOnTouchOutside(true);
//            }
//            quantityCheckDialog.initialzie("重量", 3, weightVal, goods.getUnit(),
//                    new DoubleInputDialog.OnResponseCallback() {
//                        @Override
//                        public void onQuantityChanged(Double quantity) {
//                            addGoods2Cashier(curPosTradeNo, goods, quantity);
//                        }
//                    });
//            quantityCheckDialog.show();
        } else {
            if (packFlag == 1) {
                addGoods2Cashier(curPosTradeNo, goods, goods.getPackageNum());
            } else {
                addGoods2Cashier(curPosTradeNo, goods, 1D);
            }
        }
    }

    @Override
    public void onFindFreshGoods(PosProductEntity goods, Double weight) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            isAcceptBarcodeEnabled = true;

            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            isAcceptBarcodeEnabled = true;

            return;
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            //计重商品直接读取条码中的重量信息
            addGoods2Cashier(curPosTradeNo, goods, weight);
        } else {
            // TODO: 8/24/16 如果是扫描秤打印的条码，计件商品又可能不是1
            //计件商品默认商品数量加1
            addGoods2Cashier(curPosTradeNo, goods, 1D);
        }
    }

    @Override
    public void onFindGoodsEmpty(String barcode) {
//        Intent intent = new Intent(this, SimpleDialogActivity.class);
//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE,
//                SimpleDialogActivity.FRAGMENT_TYPE_CREATE_PURCHASE_GOODS);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE,
//                SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
//        extras.putString(ScSkuGoodsStoreInFragment.EXTRY_KEY_BARCODE, barcode);
//        intent.putExtras(extras);
//        startActivity(intent);
//        ActivityRoute.redirect2StoreIn(this, barcode);
        DialogUtil.showHint("未找到商品");
        isAcceptBarcodeEnabled = true;

    }

    /**
     * 添加商品到收银台
     */
    private void addGoods2Cashier(final String orderBarCode, final PosProductEntity goods,
                                  final Double bCount) {
        if (goods == null) {
            isAcceptBarcodeEnabled = true;

            return;
        }

        Double costPrice = goods.getCostPrice();
        if (costPrice == null) {
            DialogUtil.showHint("商品零售价为空，补填后才可以收银");
//            ZLogger.df("商品零售价为空，补填后才可以收银");
//            commitGoodsCostprice1(orderBarCode, goods, bCount);
        } else {
            saveGoods2Cashier(orderBarCode, goods, bCount);
        }
    }

    /**
     * 保存商品到收银台
     */
    private void saveGoods2Cashier(final String orderBarCode, final PosProductEntity goods,
                                   final Double bCount) {
        Observable.create(new Observable.OnSubscribe<List<CashierShopcartEntity>>() {
            @Override
            public void call(Subscriber<? super List<CashierShopcartEntity>> subscriber) {
                //添加商品
                CashierShopcartService.getInstance().append(orderBarCode, orderDiscount, goods, bCount);

                //刷新订单列表
                List<CashierShopcartEntity> shopcartEntities = CashierShopcartService.getInstance()
                        .queryAllByDesc(String.format("posTradeNo = '%s'", orderBarCode));

                subscriber.onNext(shopcartEntities);
                subscriber.onCompleted();

            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CashierShopcartEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<CashierShopcartEntity> cashierShopcartEntities) {
                        productAdapter.setEntityList(cashierShopcartEntities);
                        isAcceptBarcodeEnabled = true;

                    }
                });
    }

}
