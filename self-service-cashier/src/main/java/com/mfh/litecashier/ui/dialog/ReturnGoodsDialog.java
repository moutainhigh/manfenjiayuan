package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.bingshanguxue.cashier.CashierProvider;
import com.bingshanguxue.cashier.model.PaymentInfo;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.business.mvp.presenter.CustomerPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.mvp.view.ICustomerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.R;
import com.bingshanguxue.cashier.presenter.CashierPresenter;
import com.mfh.litecashier.service.DataUploadManager;
import com.mfh.litecashier.ui.adapter.ReturnProductAdapter;
import com.bingshanguxue.cashier.view.ICashierView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.GlobalInstance;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 对话框 -- 退商品
 * Created bingshanguxue on 15/8/30.
 */
public class ReturnGoodsDialog extends CommonDialog implements ICashierView, ICustomerView{

    private View rootView;
    private TextView tvTitle;
    private Button btnVip, btnCash;
    private ImageButton btnClose;
    private MultiLayerLabel labelBcount, labelAmount, labelCoustomerAmount;
    private InputNumberLabelView inlvBarcode;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private ItemTouchHelper itemTouchHelper;
    private ReturnProductAdapter productAdapter;

    private String curOrderTradeNo;

    private CashierPresenter cashierPresenter;
    private CustomerPresenter mCustomerPresenter;

    private NumberInputDialog customerDialog = null;
    private DoubleInputDialog changeQuantityDialog = null;

    private ReturnGoodsDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ReturnGoodsDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_returngoods, null);
//        ButterKnife.bind(rootView);

        cashierPresenter = new CashierPresenter(this);
        mCustomerPresenter = new CustomerPresenter(this);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        labelBcount = (MultiLayerLabel) rootView.findViewById(R.id.label_bcount);
        labelAmount = (MultiLayerLabel) rootView.findViewById(R.id.label_amount);
        labelCoustomerAmount = (MultiLayerLabel) rootView.findViewById(R.id.label_customer_amount);
        inlvBarcode = (InputNumberLabelView) rootView.findViewById(R.id.inlv_barcode);
        btnVip = (Button) rootView.findViewById(R.id.button_footer_negative);
        btnCash = (Button) rootView.findViewById(R.id.button_footer_positive);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.product_list);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        tvTitle.setText("退货");
        initRecyclerView();
        inlvBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ADD) {
                    //条码枪扫描结束后会自动触发回车键
                    query(text);
                }

            }
        });
        inlvBarcode.registerOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onClickAction1(String text) {
                query(text);
            }

            @Override
            public void onLongClickAction1(String text) {

            }
        });
        inlvBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
                            || inlvBarcode.isSoftKeyboardEnabled()) {
                        showBarcodeKeyboard();
                    }
                }

                inlvBarcode.requestFocusEnd();
                //返回true,不再继续传递事件
                return true;
            }
        });

        btnVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payBayCustomerStep1();
            }
        });
        btnCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payByCash();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setContent(rootView, 0);
    }

    public ReturnGoodsDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.CENTER);
        }
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = d.getHeight();
        p.width = DensityUtil.dip2px(getContext(), 895);

////        p.width = d.getWidth() * 2 / 3;
//        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();
        inlvBarcode.clear();
        curOrderTradeNo = MUtils.getOrderBarCode();
        productAdapter.setEntityList(null);
    }

    private NumberInputDialog barcodeInputDialog;

    /**
     * 显示条码输入界面
     * 相当于扫描条码
     */
    private void showBarcodeKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getContext());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.BARCODE, "退单", "商品条码", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        query(value);
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getContext(), LineItemDecoration.VERTICAL_LIST));
        productAdapter = new ReturnProductAdapter(getContext(), null);
        productAdapter.setOnAdapterListener(new ReturnProductAdapter.OnAdapterListener() {
            @Override
            public void onDataSetChanged(boolean needScroll) {
                labelBcount.setTopText(String.format("%.2f", Math.abs(productAdapter.getBcount())));
                labelAmount.setTopText(String.format("%.2f", Math.abs(productAdapter.getFinalAmount())));//成交价
                labelCoustomerAmount.setTopText(String.format("%.2f", Math.abs(productAdapter.getFinalCustomerAmount())));//成交价

                if (needScroll) {
                    //后来者居上
                    mRecyclerView.scrollToPosition(0);
                }

//                if (productAdapter != null && productAdapter.getItemCount() > 0) {
//                    btnSubmit.setEnabled(true);
//                } else {
//                    btnSubmit.setEnabled(false);
//                }
            }
        });

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(productAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 根据条码查询商品
     */
    private synchronized void query(final String barCode) {
        // 清空二维码输入，避免下次扫描条码错误
        inlvBarcode.clear();

        cashierPresenter.findGoods(barCode);
    }

    /**
     * 会员退款
     */
    private void payBayCustomerStep1() {
        if (productAdapter == null || productAdapter.getItemCount() <= 0) {
            DialogUtil.showHint("商品明细不能为空");
            return;
        }

        if (customerDialog == null) {
            customerDialog = new NumberInputDialog(getContext());
            customerDialog.setCancelable(false);
            customerDialog.setCanceledOnTouchOutside(false);
        }

        customerDialog.initializeBarcode(EditInputType.TEXT, "搜索会员", "会员帐号", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        btnVip.setEnabled(false);
                        mProgressBar.setVisibility(View.VISIBLE);

                        mCustomerPresenter.getCustomerByOther(value);
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {
//                        payBayCustomerStep4();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        if (!customerDialog.isShowing()) {
            customerDialog.show();
        }
    }

    private void payBayCustomerStep2(Human human, final Double amount) {
        final CashierOrderInfo cashierOrderInfo = CashierAgent.settle(PosType.POS_STANDARD,
                curOrderTradeNo, null, PosOrderEntity.ORDER_STATUS_PROCESS,
                productAdapter.getEntityList(), human, false);
        ZLogger.d(String.format("准备退单:%s", JSONObject.toJSONString(cashierOrderInfo)));

        PosOrderEntity orderEntity = CashierProvider.fetchOrderEntity(cashierOrderInfo.getPosTradeNo());

        Map<String, String> options = new HashMap<>();
        options.put("humanId", String.valueOf(human.getId()));
        options.put("amount", MUtils.formatDouble(amount, ""));
        options.put("bizType", String.valueOf(BizType.POS));
        if (orderEntity != null) {
            options.put("orderId", CashierFactory.genTradeNo(orderEntity.getId(), true));
        }
        options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        CommonUserAccountHttpManager.getInstance().payDirect(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        DialogUtil.showHint(e.getMessage());
                        payBayCustomerStep4();
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);

                        PaymentInfo paymentInfo = PaymentInfo.create(curOrderTradeNo, WayType.VIP,
                                PosOrderPayEntity.PAY_STATUS_FINISH,
                                amount, amount, 0D,
                                null);
                        ZLogger.d(String.format("退单支付:%s", JSONObject.toJSONString(paymentInfo)));

                        payBayCustomerStep3(cashierOrderInfo, paymentInfo);
                    }

                });
    }

    private void payBayCustomerStep3(CashierOrderInfo cashierOrderInfo, PaymentInfo paymentInfo) {
        //2016-07-09 需注意，这里的
//        PaymentInfo paymentInfo = PaymentInfoImpl.genPaymentInfo(curOrderTradeNo, WayType.CASH,
//                PosOrderPayEntity.PAY_STATUS_FINISH,
//                cashierOrderInfo.getFinalAmount(), 0D, 0D - cashierOrderInfo.getFinalAmount(),
//                null);
        if (cashierOrderInfo == null) {
            DialogUtil.showHint("订单信息无效");
            payBayCustomerStep4();
            return;
        }
        PayAmount payAmount = cashierOrderInfo.getPayAmount();

        CashierAgent.updateCashierOrder(cashierOrderInfo.getPosTradeNo(),
                cashierOrderInfo.getVipMember(), paymentInfo);
        cashierOrderInfo = CashierProvider.createCashierOrderInfo(cashierOrderInfo.getPosTradeNo(),
                cashierOrderInfo.getVipMember());

        CashierAgent.updateCashierOrder(cashierOrderInfo, payAmount, PosOrderEntity.ORDER_STATUS_FINISH);

//        //更新订单信息，同时打开钱箱，退钱给顾客
//        PrinterFactory.getPrinterManager().openMoneyBox();

        PosOrderEntity orderEntity = CashierProvider.fetchOrderEntity(cashierOrderInfo.getPosTradeNo());
        //同步订单信息
//        DataUploadManager.getInstance().stepUploadPosOrder(orderEntities);
        //打印订单
        PrinterFactory.getPrinterManager().printPosOrder(orderEntity);

        //同步订单信息
        if (SharedPreferencesUltimate.isUploadPosOrderRealtime()) {
            DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
        }

        CashierShopcartService.getInstance()
                .deleteBy(String.format("posTradeNo = '%s'", curOrderTradeNo));

        payBayCustomerStep4();
    }

    private void payBayCustomerStep4() {
        DialogUtil.showHint("退款成功");
        btnVip.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        dismiss();
    }


    /**
     * 非会员退款
     */
    private void payByCash() {
        if (productAdapter == null || productAdapter.getItemCount() <= 0) {
            DialogUtil.showHint("商品明细不能为空");
            return;
        }
        btnCash.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        CashierOrderInfo cashierOrderInfo = CashierAgent.settle(PosType.POS_STANDARD,
                curOrderTradeNo, null, PosOrderEntity.ORDER_STATUS_PROCESS,
                productAdapter.getEntityList(), null);
        ZLogger.d(String.format("准备退单:%s", JSONObject.toJSONString(cashierOrderInfo)));

        //2016-07-09 需注意，这里的
//        PaymentInfo paymentInfo = PaymentInfoImpl.genPaymentInfo(curOrderTradeNo, WayType.CASH,
//                PosOrderPayEntity.PAY_STATUS_FINISH,
//                cashierOrderInfo.getFinalAmount(), 0D, 0D - cashierOrderInfo.getFinalAmount(),
//                null);
        PaymentInfo paymentInfo = PaymentInfo.create(curOrderTradeNo, WayType.CASH,
                PosOrderPayEntity.PAY_STATUS_FINISH,
                cashierOrderInfo.getFinalAmount(),
                cashierOrderInfo.getFinalAmount(), 0D,
                null);
        ZLogger.d(String.format("退单支付:%s", JSONObject.toJSONString(paymentInfo)));

        PayAmount payAmount = cashierOrderInfo.getPayAmount();

        CashierAgent.updateCashierOrder(cashierOrderInfo.getPosTradeNo(),
                cashierOrderInfo.getVipMember(), paymentInfo);
        cashierOrderInfo = CashierProvider.createCashierOrderInfo(cashierOrderInfo.getPosTradeNo(),
                cashierOrderInfo.getVipMember());

        CashierAgent.updateCashierOrder(cashierOrderInfo, payAmount, PosOrderEntity.ORDER_STATUS_FINISH);

        //更新订单信息，同时打开钱箱，退钱给顾客
        PrinterFactory.getPrinterManager().openMoneyBox();

        PosOrderEntity orderEntity = CashierProvider.fetchOrderEntity(cashierOrderInfo.getPosTradeNo());
        //同步订单信息
//        DataUploadManager.getInstance().stepUploadPosOrder(orderEntities);
        //打印订单
        PrinterFactory.getPrinterManager().printPosOrder(orderEntity);

        //同步订单信息
        if (SharedPreferencesUltimate.isUploadPosOrderRealtime()) {
            DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
        }

        CashierShopcartService.getInstance()
                .deleteBy(String.format("posTradeNo = '%s'", curOrderTradeNo));


        DialogUtil.showHint("退款成功");
        btnCash.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        dismiss();
    }

    @Override
    public void onFindGoods(final PosProductEntity goods, int packFlag) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

//        if (goods.getStatus() != 1) {
//            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
//            return;
//        }

        //检查订单条码
        if (StringUtils.isEmpty(curOrderTradeNo)) {
            curOrderTradeNo = MUtils.getOrderBarCode();
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            final Double weightVal = GlobalInstance.getInstance().getNetWeight();
            if (weightVal > 0) {
                saveGoods2Cashier(curOrderTradeNo, goods, 0 - weightVal);
            } else {
                if (changeQuantityDialog == null) {
                    changeQuantityDialog = new DoubleInputDialog(getContext());
                    changeQuantityDialog.setCancelable(true);
                    changeQuantityDialog.setCanceledOnTouchOutside(true);
                }
                changeQuantityDialog.init("重量", 3, weightVal, new DoubleInputDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {

                        saveGoods2Cashier(curOrderTradeNo, goods, 0 - quantity);
                    }
                });
                changeQuantityDialog.show();
            }
        } else {
            if (packFlag == 1) {
                saveGoods2Cashier(curOrderTradeNo, goods, 0 - goods.getPackageNum());
            } else {
                saveGoods2Cashier(curOrderTradeNo, goods, -1D);
            }
        }


    }

    @Override
    public void onFindFreshGoods(PosProductEntity goods, Double weight) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

//        if (goods.getStatus() != 1) {
//            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
//            return;
//        }

        //检查订单条码
        if (StringUtils.isEmpty(curOrderTradeNo)) {
            curOrderTradeNo = MUtils.getOrderBarCode();
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            //计重商品直接读取条码中的重量信息
            saveGoods2Cashier(curOrderTradeNo, goods, 0 - weight);
        } else {
            //计件商品默认商品数量加1
            saveGoods2Cashier(curOrderTradeNo, goods, -1D);
        }
    }

    @Override
    public void onFindGoodsEmpty(String barcode) {
        DialogUtil.showHint("未找到商品");
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
                CashierShopcartService.getInstance().append(orderBarCode, goods, bCount);

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
                    }
                });
    }

    @Override
    public void onICustomerViewLoading() {

    }

    @Override
    public void onICustomerViewError(int type, String content, String errorMsg) {
        DialogUtil.showHint(errorMsg);
        payBayCustomerStep4();
    }

    @Override
    public void onICustomerViewSuccess(int type, String content, Human human) {
        if (human != null) {
            payBayCustomerStep2(human, productAdapter.getFinalCustomerAmount());
        } else {
            DialogUtil.showHint("未查询到用户");
        }
    }
}
