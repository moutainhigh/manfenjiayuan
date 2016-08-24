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

import com.bingshanguxue.cashier.CashierAgent;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.entity.PosProductSkuEntity;
import com.bingshanguxue.cashier.model.wrapper.CashierOrderInfo;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.R;
import com.bingshanguxue.cashier.database.service.PosProductSkuService;
import com.mfh.litecashier.ui.adapter.ReturnProductAdapter;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.DataCacheHelper;

import java.util.List;


/**
 * 领取商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ReceiveGoodsDialog extends CommonDialog {
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

    public interface DialogListener{
        void onOrderConfirmed(String orderBarcode);
    }
    private DialogListener dialogListener;

    private ReceiveGoodsDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ReceiveGoodsDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_returngoods, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        tvQuantity = (TextView) rootView.findViewById(R.id.tv_quantity);
        tvAmount = (TextView) rootView.findViewById(R.id.tv_amount);
        inlvBarcode = (InputNumberLabelView) rootView.findViewById(R.id.inlv_barcode);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.product_list);

        tvTitle.setText("领取商品");
        initRecyclerView();
        inlvBarcode.setHintText("订单编号");
        inlvBarcode.setEnterKeySubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
        inlvBarcode.setOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
//                query(text);
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

    public ReceiveGoodsDialog(Context context) {
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

    public void init(DialogListener dialogListener){
        this.dialogListener = dialogListener;
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

        if (StringUtils.isEmpty(barCode)) {
            ZLogger.d("订单条码不能为空");
            return;
        }

        //TODO,查询订单明细
        int packFlag = 0;
        //查询商品
        PosProductEntity productEntity = CashierHelper.findProduct(barCode);
        if (productEntity == null) {
            // 查询主条码
            String mainBarcode = null;
            List<PosProductSkuEntity> entityList = PosProductSkuService.get().queryAllByDesc(String.format("otherBarcode = '%s'", barCode));
            if (entityList != null && entityList.size() > 0) {
                PosProductSkuEntity posProductSkuEntity = entityList.get(0);
                mainBarcode = posProductSkuEntity.getMainBarcode();
                packFlag = posProductSkuEntity.getPackFlag();
                ZLogger.d(String.format("找到%d个主条码%s", entityList.size(), mainBarcode));
            } else {
                ZLogger.d("未找到主条码:");
            }

            //根据主条码再次查询商品
            productEntity = CashierHelper.findProduct(mainBarcode);
            if (productEntity == null) {
//                Looper.prepare();
                DialogUtil.showHint(String.format("未查询到商品:%s", barCode));
//                Looper.loop();
                return;
            }
        }

        if (productEntity.getStatus() != 1) {
//            Looper.prepare();
            DialogUtil.showHint(String.format("商品无效，可能已经下架:%s", barCode));
//            Looper.loop();
            return;
        }

        //检查订单条码
        if (StringUtils.isEmpty(orderBarcode)) {
            orderBarcode = MUtils.getOrderBarCode();
        }

        //添加商品
        if (productEntity.getPriceType().equals(PriceType.WEIGHT)) {
            productAdapter.append(orderBarcode, productEntity, 0 - DataCacheHelper.getInstance().getNetWeight());
        } else {
            if (packFlag == 1) {
                productAdapter.append(orderBarcode, productEntity, 0 - productEntity.getPackageNum());
            } else {
                productAdapter.append(orderBarcode, productEntity, -1D);
            }
        }
    }

    /**
     * 提交订单,新建订单并保存订单明细，标记为挂单状态，然后设置调单。
     * */
    private void submitOrder(){
        CashierAgent.simpleSettle(orderBarcode, productAdapter.getEntityList());
        CashierOrderInfo cashierOrderInfo = CashierAgent.makeCashierOrderInfo(BizType.POS,
                orderBarcode, null);
        if (cashierOrderInfo == null) {
            DialogUtil.showHint("创建订单失败");
            return;
        }

        //更新订单状态：挂单
        CashierAgent.updateCashierOrder(cashierOrderInfo, PosOrderEntity.ORDER_STATUS_HANGUP);

        dismiss();
        //加载订单
        if (dialogListener != null){
            dialogListener.onOrderConfirmed(orderBarcode);
        }
    }
}
