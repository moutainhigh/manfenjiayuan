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
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfo;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;
import com.mfh.litecashier.database.entity.PosProductEntity;
import com.mfh.litecashier.database.logic.PosOrderItemService;
import com.mfh.litecashier.presenter.CashierPresenter;
import com.mfh.litecashier.service.OrderSyncManager;
import com.mfh.litecashier.ui.adapter.ReturnProductAdapter;
import com.mfh.litecashier.ui.view.ICashierView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.DataCacheHelper;


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

    private ItemTouchHelper itemTouchHelper;
    private ReturnProductAdapter productAdapter;

    private String orderBarcode;

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

        tvTitle.setText("退单");
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
                dismiss();
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
        orderBarcode = MUtils.getOrderBarCode();
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
        //结算订单
        CashierOrderInfo cashierOrderInfo = CashierHelper.settleCashierOrder(orderBarcode,
                BizType.POS, null, productAdapter.getEntityList());
//        PosOrderEntity orderEntity = CashierHelper.findPosOrder(curOrderBarCode, true);
        if (cashierOrderInfo == null) {
            DialogUtil.showHint("创建订单失败");
            return;
        }

        //更新订单信息，同时打开钱箱，退钱给顾客
        cashierOrderInfo.paid(WayType.CASH, 0D);
        CashierHelper.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_FINISH);
        cashierOrderInfo.setStatus(PosOrderEntity.ORDER_STATUS_FINISH);

        SerialManager.openMoneyBox();

        PosOrderEntity orderEntity = CashierHelper.findPosOrder(cashierOrderInfo.getOrderBarcode());
        if (orderEntity != null) {
            //同步订单信息
            OrderSyncManager.get().stepUploadPosOrder(orderEntity);
            //打印订单
            SerialManager.printPosOrder(orderEntity, true);
        }
    }

    @Override
    public void onFindGoods(PosProductEntity goods, int packFlag) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            return;
        }

        //检查订单条码
        if (StringUtils.isEmpty(orderBarcode)) {
            orderBarcode = MUtils.getOrderBarCode();
        }

        //添加商品
        final PosOrderItemEntity entity;
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            entity = PosOrderItemService.get().generate(orderBarcode, goods, 0 - DataCacheHelper.getInstance().getNetWeight());
        } else {
            if (packFlag == 1) {
                entity = PosOrderItemService.get().generate(orderBarcode, goods, 0 - goods.getPackageNum());
            } else {
                entity = PosOrderItemService.get().generate(orderBarcode, goods, -1D);
            }
        }

        //刷新订单列表
        productAdapter.append(entity);
    }

    @Override
    public void onFindGoodsEmpty(String barcode) {

    }
}
