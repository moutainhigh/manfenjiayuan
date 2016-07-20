package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.bingshanguxue.cashier.model.wrapper.PaymentInfo;
import com.bingshanguxue.cashier.model.wrapper.PaymentInfoImpl;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.PrintManager;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.presenter.CashierPresenter;
import com.mfh.litecashier.service.OrderSyncManager2;
import com.mfh.litecashier.ui.adapter.ReturnProductAdapter;
import com.mfh.litecashier.ui.view.ICashierView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.DataCacheHelper;

import java.util.List;


/**
 * 对话框 -- 退商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ReturnGoodsDialog extends CommonDialog implements ICashierView {

    private View rootView;
    private TextView tvTitle;
    private Button btnSubmit;
    private ImageButton btnClose;
    private TextView tvQuantity;
    private TextView tvAmount;
    private InputNumberLabelView inlvBarcode;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private ItemTouchHelper itemTouchHelper;
    private ReturnProductAdapter productAdapter;

    private String curOrderTradeNo;

    private CashierPresenter cashierPresenter;

    private ReturnGoodsDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ReturnGoodsDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_returngoods, null);
//        ButterKnife.bind(rootView);

        cashierPresenter = new CashierPresenter(this);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        tvQuantity = (TextView) rootView.findViewById(R.id.tv_quantity);
        tvAmount = (TextView) rootView.findViewById(R.id.tv_amount);
        inlvBarcode = (InputNumberLabelView) rootView.findViewById(R.id.inlv_barcode);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.product_list);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        tvTitle.setText("退货");
        initRecyclerView();
        inlvBarcode.setEnterKeySubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
        inlvBarcode.setOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                query(text);
            }
        });

        btnSubmit.setVisibility(View.VISIBLE);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrder();
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

        getWindow().setGravity(Gravity.CENTER);

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = d.getHeight();
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
                tvQuantity.setText(String.format("%.2f", Math.abs(productAdapter.getBcount())));
                tvAmount.setText(String.format("%.2f", Math.abs(productAdapter.getFinalAmount())));//成交价

                if (needScroll) {
                    //后来者居上
                    mRecyclerView.scrollToPosition(0);
                }

                if (productAdapter != null && productAdapter.getItemCount() > 0) {
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
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
     * 提交订单
     * */
    private void submitOrder(){
        btnSubmit.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        CashierOrderInfo cashierOrderInfo = CashierAgent.settle(curOrderTradeNo,
                PosOrderEntity.ORDER_STATUS_PROCESS, productAdapter.getEntityList());
        ZLogger.df(String.format("准备退单:%s", JSONObject.toJSONString(cashierOrderInfo)));

        //2016-07-09 需注意，这里的
//        PaymentInfo paymentInfo = PaymentInfoImpl.genPaymentInfo(curOrderTradeNo, WayType.CASH,
//                PosOrderPayEntity.PAY_STATUS_FINISH,
//                cashierOrderInfo.getFinalAmount(), 0D, 0D - cashierOrderInfo.getFinalAmount(),
//                null);
        PaymentInfo paymentInfo = PaymentInfoImpl.genPaymentInfo(curOrderTradeNo, WayType.CASH,
                PosOrderPayEntity.PAY_STATUS_FINISH,
                cashierOrderInfo.getFinalAmount(),
                cashierOrderInfo.getFinalAmount(), 0D,
                null);
        ZLogger.df(String.format("退单支付:%s", JSONObject.toJSONString(paymentInfo)));
        CashierAgent.updateCashierOrder(cashierOrderInfo, paymentInfo);

        cashierOrderInfo = CashierFactory.makeCashierOrderInfo(cashierOrderInfo.getBizType(),
                cashierOrderInfo.getPosTradeNo(),cashierOrderInfo.getVipMember());
        ZLogger.df(String.format("退单成功:%s", JSONObject.toJSONString(cashierOrderInfo)));

        CashierAgent.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_FINISH);

        //更新订单信息，同时打开钱箱，退钱给顾客
        SerialManager.openMoneyBox();

        List<PosOrderEntity> orderEntities = CashierFactory.fetchActiveOrderEntities(BizType.POS,
                cashierOrderInfo.getPosTradeNo());
        //同步订单信息
        OrderSyncManager2.get().stepUploadPosOrder(orderEntities);
        //打印订单
        PrintManager.printPosOrder(orderEntities, true);
        CashierShopcartService.getInstance()
                .deleteBy(String.format("posTradeNo = '%s'", curOrderTradeNo));

        btnSubmit.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        dismiss();
    }

    private ChangeQuantityDialog changeQuantityDialog = null;

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

        //检查订单条码
        if (StringUtils.isEmpty(curOrderTradeNo)) {
            curOrderTradeNo = MUtils.getOrderBarCode();
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            final Double weightVal = DataCacheHelper.getInstance().getNetWeight();
            if (weightVal > 0){
                productAdapter.append(curOrderTradeNo, goods, 0 - weightVal);
            }
            else{
                if (changeQuantityDialog == null) {
                    changeQuantityDialog = new ChangeQuantityDialog(getContext());
                    changeQuantityDialog.setCancelable(true);
                    changeQuantityDialog.setCanceledOnTouchOutside(true);
                }
                changeQuantityDialog.init("重量", 3, weightVal, new ChangeQuantityDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {

                        productAdapter.append(curOrderTradeNo, goods, 0 - quantity);
                    }
                });
                changeQuantityDialog.show();
            }
        } else {
            if (packFlag == 1) {
                productAdapter.append(curOrderTradeNo, goods, 0 - goods.getPackageNum());
            } else {
                productAdapter.append(curOrderTradeNo, goods, -1D);
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

        //检查订单条码
        if (StringUtils.isEmpty(curOrderTradeNo)) {
            curOrderTradeNo = MUtils.getOrderBarCode();
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            //计重商品直接读取条码中的重量信息
            productAdapter.append(curOrderTradeNo, goods, 0 - weight);
        } else {
            //计件商品默认商品数量加1
            productAdapter.append(curOrderTradeNo, goods, -1D);
        }
    }

    @Override
    public void onFindGoodsEmpty(String barcode) {
        DialogUtil.showHint("未找到商品");
    }
}
