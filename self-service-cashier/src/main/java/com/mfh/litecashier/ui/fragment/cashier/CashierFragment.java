package com.mfh.litecashier.ui.fragment.cashier;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfoWrapper;
import com.mfh.litecashier.bean.wrapper.HangupOrder;
import com.mfh.litecashier.com.PrintManager;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.event.CashierAffairEvent;
import com.mfh.litecashier.presenter.CashierPresenter;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.service.OrderSyncManager2;
import com.mfh.litecashier.ui.activity.CashierPayActivity;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.CashierSwipAdapter;
import com.mfh.litecashier.ui.dialog.ChangeQuantityDialog;
import com.mfh.litecashier.ui.dialog.HangupOrderDialog;
import com.mfh.litecashier.ui.fragment.inventory.StockScSkuGoodsFragment;
import com.mfh.litecashier.ui.view.ICashierView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.AnalysisHelper;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.DataCacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 收银页面＋服务台
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CashierFragment extends BaseFragment
        implements ICashierView {

    @Bind(R.id.tv_quantity)
    TextView tvQuantity;
    @Bind(R.id.tv_amount)
    TextView tvAmount;
    @Bind(R.id.tv_last_amount)
    TextView tvLastAmount;
    @Bind(R.id.tv_last_quantity)
    TextView tvLastQuantity;
    @Bind(R.id.tv_last_discount)
    TextView tvLastDiscount;
    @Bind(R.id.tv_last_charge)
    TextView tvLastCharge;
    @Bind(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;
    @Bind(R.id.button_settle)
    Button btnSettle;
    @Bind(R.id.product_list)
    RecyclerView productRecyclerView;
    @Bind(R.id.float_hangup)
    TextView fabHangup;

    private ItemTouchHelper itemTouchHelper;
    private CashierSwipAdapter productAdapter;
    private ChangeQuantityDialog changePriceDialog = null;
    private ChangeQuantityDialog changeQuantityDialog = null;

    private ChangeQuantityDialog quantityCheckDialog = null;
    private HangupOrderDialog hangupOrderDialog = null;


    /**
     * POS唯一订单号，由POS机本地生成的12位字符串
     */
    private String curPosTradeNo;

    private CashierPresenter cashierPresenter;

    public static CashierFragment newInstance(Bundle args) {
        CashierFragment fragment = new CashierFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        cashierPresenter = new CashierPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            initBarCodeInput();
            initCashierRecyclerView();
            initCashierOrder();

            //刷新挂单
            refreshFloatHangup();
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        autorequestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(CashierAffairEvent event) {
        ZLogger.d(String.format("CashierAffairEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == CashierAffairEvent.EVENT_ID_RESET_CASHIER) {
            initCashierOrder();
        }else if (event.getAffairId() == CashierAffairEvent.EVENT_ID_OPEN_MONEYBOX) {
            openMoneyBox();
        } else if (event.getAffairId() == CashierAffairEvent.EVENT_ID_HANGUPORDER) {
            hangUpOrder();
        }
    }

    /**
     * 结算(需要登录)
     */
    @OnClick(R.id.button_settle)
    public void settle() {
        //判断是否登录
        if (!MfhLoginService.get().haveLogined()) {
            DialogUtil.showHint("请先登录");
            return;
        }

        //判断当天是否日结
        if (AnalysisHelper.validateHaveDateEnd(new Date())) {
            DialogUtil.showHint("该网点今天已经日结，请先挂单。");
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
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        CashierOrderInfo cashierOrderInfo = CashierAgent.settle(curPosTradeNo,
                PosOrderEntity.ORDER_STATUS_STAY_PAY, productAdapter.getEntityList());
        if (cashierOrderInfo == null) {
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "订单创建失败", true);
            return;
        }
        ZLogger.df(String.format("[点击结算]--生成结算信息：%s",
                JSON.toJSONString(cashierOrderInfo)));

        //显示客显
        updatePadDisplay(CashierOrderInfoWrapper.CMD_PAY_ORDER, cashierOrderInfo);

        hideProgressDialog();

        Intent intent = new Intent(getActivity(), CashierPayActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_MFPAY);
    }

    /**
     * 更新客显信息
     */
    private void updatePadDisplay(int cmdType, CashierOrderInfo cashierOrderInfo) {
        if (!SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PREF_KEY_PAD_CUSTOMERDISPLAY_ENABLED, false)) {
            ZLogger.d("PAD客显功能未打开");
            return;
        }
        CashierOrderInfoWrapper cashierOrderInfoWrapper = new CashierOrderInfoWrapper();
        cashierOrderInfoWrapper.setCmdType(cmdType);
        cashierOrderInfoWrapper.setCashierOrderInfo(cashierOrderInfo);
        NetProcessor.ComnProcessor processor = new NetProcessor.ComnProcessor<EmbMsg>() {
            @Override
            protected void processOperResult(EmbMsg result) {
//                doAfterSendSuccess(result);
                ZLogger.d("发送订单信息到客显成功");
            }
        };
        EmbMsgService msgService = ServiceFactory.getService(EmbMsgService.class, getContext());
        msgService.sendText(MfhLoginService.get().getCurrentGuId(),
                MfhLoginService.get().getCurrentGuId(),
                IMBizType.CUSTOMER_DISPLAY_PAYORDER, JSON.toJSONString(cashierOrderInfoWrapper), processor);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_MFPAY: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    CashierOrderInfo cashierOrderInfo = (CashierOrderInfo) data
                            .getSerializableExtra(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO);
                    processSettleResult(cashierOrderInfo);
                }
            }
            break;
            case Constants.ARC_CREATE_PURCHASE_GOODS: {
                if (resultCode == Activity.RESULT_OK) {
                    //TODO,新增商品成功，同步商品
                    DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理订单支付结果
     */
    private void processSettleResult(CashierOrderInfo cashierOrderInfo) {
        //重新生成订单
        if (!StringUtils.isEmpty(curPosTradeNo)){
            CashierShopcartService.getInstance()
                    .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
        }
        obtaincurPosTradeNo(null);
        productAdapter.setEntityList(null);

        if (cashierOrderInfo == null) {
            return;
        }
        ZLogger.d(JSONObject.toJSONString(cashierOrderInfo));

        // TODO: 7/5/16 下个版本放到支付页面去 
        updatePadDisplay(CashierOrderInfoWrapper.CMD_FINISH_ORDER, cashierOrderInfo);

        List<PosOrderEntity> orderEntities = CashierFactory
                .fetchActiveOrderEntities(BizType.POS, cashierOrderInfo.getPosTradeNo());
        if (orderEntities != null && orderEntities.size() > 0) {
            Double finalAmount = 0D, bCount = 0D, discountAmount = 0D, changeAmount = 0D;
            for (PosOrderEntity orderEntity : orderEntities) {
                OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());

                finalAmount += orderEntity.getFinalAmount();
                bCount += orderEntity.getBcount();
                discountAmount += payWrapper.getRuleDiscount();
                changeAmount += payWrapper.getChange();
            }
            refreshLastOrder(finalAmount, bCount, discountAmount, changeAmount);

            //显示找零
//        SerialManager.show(4, Math.abs(cashierOrderInfo.getHandleAmount()));
            SerialManager.vfdShow(String.format("Change:%.2f\r\nThank You!", changeAmount));
        }
        //同步订单信息
        OrderSyncManager2.get().stepUploadPosOrder(orderEntities);

        //打印订单
        PrintManager.printPosOrder(orderEntities, true);
    }

    @OnClick(R.id.float_hangup)
    public void showOrder() {
        if (hangupOrderDialog == null) {
            hangupOrderDialog = new HangupOrderDialog(getActivity());
            hangupOrderDialog.setCancelable(true);
            hangupOrderDialog.setCanceledOnTouchOutside(true);
        }
        hangupOrderDialog.init(new HangupOrderDialog.OnResponseCallback() {

            @Override
            public void onResumeOrder(String orderBarCode) {
                resumeOrder(orderBarCode);
            }
        });
        if (!hangupOrderDialog.isShowing()) {
            hangupOrderDialog.show();
        }
    }

    /**
     * 获取焦点
     */
    public void autorequestFocus() {
        if (inlvBarcode != null) {
            inlvBarcode.clear();
            inlvBarcode.requestFocus();
        } else {
            if (rootView != null) {
                inlvBarcode = (InputNumberLabelView) rootView.findViewById(R.id.inlv_barcode);
                inlvBarcode.clear();
                inlvBarcode.requestFocus();
            }
        }
    }

    /**
     * 初始化商品列表
     */
    private void initCashierRecyclerView() {
        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        productRecyclerView.setHasFixedSize(true);
        //设置Item增加、移除动画
//        productRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //分割线
        productRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));

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
                tvQuantity.setText(String.format("%.2f", productAdapter.getBcount()));
                tvAmount.setText(String.format("%.2f", productAdapter.getFinalAmount()));//成交价

                if (productAdapter.getItemCount() > 0) {
                    btnSettle.setEnabled(true);
                } else {
                    //清除屏幕上的字符
                    SerialManager.clear();
                    //TODO,清除客显屏幕

                    btnSettle.setEnabled(false);
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
     * 开钱箱
     */
    public void openMoneyBox() {
        //打开钱箱
        SerialManager.openMoneyBox();
    }


    /**
     * 初始化条码输入
     */
    private void initBarCodeInput() {
        inlvBarcode.setEnterKeySubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
        inlvBarcode.requestFocus();
        inlvBarcode.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String barcode = inlvBarcode.getInputString();
                        searchGoodsByBarcode(barcode);
                    }

                    return true;
                }
                //Press “*”
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //判断是否已经有数字，如果已经有则直接加数字，否则弹窗
                        String inputText = inlvBarcode.getInputString();
                        if (StringUtils.isEmpty(inputText)) {
                            changeGoodsQuantity(0);
//                            if (productAdapter != null) {
//                                productAdapter.changeQuantity();
//                            }
                        } else {
                            inlvBarcode.clear();
                            try {
                                if (productAdapter != null) {
                                    productAdapter.changeQuantity(Double.valueOf(inputText));
                                }
                            } catch (Exception e) {
                                ZLogger.e(e.toString());
                            }
                        }
                    }
                    return true;
                }
                //Press “＋”
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_ADD) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (btnSettle.isEnabled()) {
                            settle();
                        }
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
     * 修改商品价格
     */
    private void changeGoodsPrice(final int position) {
        final CashierShopcartEntity entity = productAdapter.getEntity(position);
        if (entity == null) {
            return;
        }

        if (changePriceDialog == null) {
            changePriceDialog = new ChangeQuantityDialog(getActivity());
            changePriceDialog.setCancelable(true);
            changePriceDialog.setCanceledOnTouchOutside(true);
        }
        changePriceDialog.init("成交价", 2, entity.getFinalPrice(),
                new ChangeQuantityDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        entity.setFinalPrice(quantity);
                        entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
                        CashierShopcartService.getInstance().saveOrUpdate(entity);

                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged(position, false);
                        }
                    }
                });
        changePriceDialog.show();
    }

    /**
     * 修改商品数量
     */
    private void changeGoodsQuantity(final int position) {
        final CashierShopcartEntity entity = productAdapter.getEntity(position);
        if (entity == null) {
            return;
        }

        if (changeQuantityDialog == null) {
            changeQuantityDialog = new ChangeQuantityDialog(getActivity());
            changeQuantityDialog.setCancelable(true);
            changeQuantityDialog.setCanceledOnTouchOutside(true);
        }
        changeQuantityDialog.init("数量", 2, entity.getBcount(),
                new ChangeQuantityDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        entity.setBcount(quantity);
                        entity.setAmount(entity.getBcount() * entity.getCostPrice());
                        entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());

                        CashierShopcartService.getInstance().saveOrUpdate(entity);
                        // TODO: 7/7/16
                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged(position, false);
                        }
                    }
                });
        changeQuantityDialog.show();
    }



    /**
     * 刷新上一单信息
     */
    private void refreshLastOrder(Double amount, Double count, Double discount, Double charge) {
        tvLastAmount.setText(String.format("合计: ¥%.2f", amount));
        tvLastQuantity.setText(String.format("数量: %.2f", count));
        tvLastDiscount.setText(String.format("优惠: ¥%.2f", discount));
        tvLastCharge.setText(String.format("找零: ¥%.2f", charge));
    }

    /**
     * 刷新挂起浮动按钮
     */
    private void refreshFloatHangup() {
        List<HangupOrder> hangupOrderList = CashierHelper.mergeHangupOrders(BizType.POS);
        if (hangupOrderList != null && hangupOrderList.size() > 0) {
            fabHangup.setText(String.valueOf(hangupOrderList.size()));
            fabHangup.setVisibility(View.VISIBLE);
        } else {
            fabHangup.setText("0");
            fabHangup.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化收银
     */
    private void initCashierOrder() {
        try {
            if (inlvBarcode != null) {
                inlvBarcode.clear();
                inlvBarcode.requestFocusEnd();
            }

            //刷新上一单数据
            refreshLastOrder(0D, 0D, 0D, 0D);

            //加载订单
            if (StringUtils.isEmpty(curPosTradeNo)){
                CashierShopcartService.getInstance()
                        .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
            }
            obtaincurPosTradeNo(null);
            productAdapter.setEntityList(null);

            updatePadDisplay(CashierOrderInfoWrapper.CMD_CLEAR_ORDER, null);
            //刷新挂单
            refreshFloatHangup();
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 挂单
     * <ol>
     * 挂单
     * <li>判断当前收银台购物车的商品是否为空，若不为空，则继续第2步，否则结束；</li>
     * <li>生成订单,［并拆单］；</li>
     * <li>更新订单明细（需要删除历史记录）；</li>
     * <li>结束</li>
     * </ol>
     */
    public void hangUpOrder() {
        inlvBarcode.clear();
        //清除屏幕上的字符
        SerialManager.clear();
        //TODO,清除客显屏幕
        //Step 1:
        CashierAgent.settle(curPosTradeNo, PosOrderEntity.ORDER_STATUS_HANGUP,
                productAdapter.getEntityList());
//刷新挂单
        refreshFloatHangup();
        //重新生成订单
        if (StringUtils.isEmpty(curPosTradeNo)){
            CashierShopcartService.getInstance()
                    .deleteBy(String.format("posTradeNo = '%s'", curPosTradeNo));
        }
        obtaincurPosTradeNo(null);
        productAdapter.setEntityList(null);
    }

    /**
     * 调单
     */
    private void resumeOrder(String posTradeNo) {
        inlvBarcode.clear();
        //清除屏幕上的字符
        SerialManager.clear();

        //挂起当前订单
        CashierAgent.settle(curPosTradeNo, PosOrderEntity.ORDER_STATUS_HANGUP,
                productAdapter.getEntityList());

        ZLogger.d(String.format("调单：%s", posTradeNo));

        //加载新订单
        obtaincurPosTradeNo(posTradeNo);
        //加载明细
        CashierShopcartService.getInstance().readOrderItems(posTradeNo,
                CashierAgent.resume(posTradeNo));
        List<CashierShopcartEntity> shopcartEntities = CashierShopcartService.getInstance()
                .queryAllBy(String.format("posTradeNo = '%s'", posTradeNo));
        productAdapter.setEntityList(shopcartEntities);

        //刷新挂单
        refreshFloatHangup();
    }


    /**
     * 根据条码查询商品
     */
    private void searchGoodsByBarcode(final String barCode) {
        if (StringUtils.isEmpty(barCode)) {
//            条码无效
            return;
        }

        // 清空二维码输入，避免下次扫描条码错误
        inlvBarcode.clear();

        cashierPresenter.findGoods(barCode);
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

    @Override
    public void onFindGoods(final PosProductEntity goods, int packFlag) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            return;
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            final Double weightVal = DataCacheHelper.getInstance().getNetWeight();
            if (weightVal > 0) {
                productAdapter.append(curPosTradeNo, goods, weightVal);

            } else {
                if (quantityCheckDialog == null) {
                    quantityCheckDialog = new ChangeQuantityDialog(getActivity());
                    quantityCheckDialog.setCancelable(true);
                    quantityCheckDialog.setCanceledOnTouchOutside(true);
                }
                quantityCheckDialog.init("重量", 3, weightVal, new ChangeQuantityDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        productAdapter.append(curPosTradeNo, goods, quantity);
                    }
                });
                quantityCheckDialog.show();
            }

        } else {
            if (packFlag == 1) {
                productAdapter.append(curPosTradeNo, goods, goods.getPackageNum());
            } else {
                productAdapter.append(curPosTradeNo, goods, 1D);
            }
        }
    }

    @Override
    public void onFindFreshGoods(PosProductEntity goods, Double weight) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            return;
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            //计重商品直接读取条码中的重量信息
            productAdapter.append(curPosTradeNo, goods, weight);
        } else {
            //计件商品默认商品数量加1
            productAdapter.append(curPosTradeNo, goods, 1D);
        }
    }

    @Override
    public void onFindGoodsEmpty(String barcode) {
        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_CREATE_PURCHASE_GOODS);
        extras.putString(StockScSkuGoodsFragment.EXTRY_KEY_BARCODE, barcode);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_CREATE_PURCHASE_GOODS);
    }
}