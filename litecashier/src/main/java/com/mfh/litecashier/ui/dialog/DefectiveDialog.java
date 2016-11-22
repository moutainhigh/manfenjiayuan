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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.StockTakeGoods;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.invOrder.InvOrderApiImpl;
import com.mfh.framework.api.invOrder.ScGoodsSkuApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.InvLossOrder;
import com.mfh.litecashier.bean.wrapper.LossOrderItem;
import com.mfh.litecashier.ui.adapter.DefectiveAdapter;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;

import java.util.List;


/**
 * 对话框 -- 商品报损
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class DefectiveDialog extends CommonDialog {

    public static final int HINT_THROW = 0;//告警抛出异常
    public static final int HINT_MERGER = 1;//合并相加
    public static final int HINT_OVERRIDE = 2;//覆盖
    public static final int HINT_IGNORE = 3;//忽略

    private View rootView;
    private ImageButton btnClose;
    private TextView tvTitle;
    private Button btnSubmit;
    private InputNumberLabelView inlvBarcode;
    private RecyclerView mRecyclerView;

    private ItemTouchHelper itemTouchHelper;
    private DefectiveAdapter productAdapter;

    private InvLossOrder invLossOrder = null;

    private ProgressDialog mProgressDialog = null;

    private DefectiveDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private DefectiveDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_defective, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        inlvBarcode = (InputNumberLabelView) rootView.findViewById(R.id.inlv_barcode);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.product_list);

        tvTitle.setText("报损");
        initRecyclerView();
        inlvBarcode.setEnterKeySubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
        inlvBarcode.setOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                query(text);

                inlvBarcode.clear();
            }
        });

        btnSubmit.setVisibility(View.VISIBLE);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
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

    public DefectiveDialog(Context context) {
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
////        p.y = DensityUtil.dip2px(getContext(), 44);
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

        loadLossOrder();
    }

    public void init(){
        invLossOrder = null;
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
        productAdapter = new DefectiveAdapter(getContext(), null);
        productAdapter.setOnAdapterListener(new DefectiveAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onDataSetChanged() {
                if (productAdapter.getItemCount() > 0) {
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

    private void loadLossOrder(){
        invLossOrder = null;

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            dismiss();
            return;
        }

        if (mProgressDialog != null) {
            mProgressDialog.setProgress(ProgressDialog.STATUS_PROCESSING, "正在请求报损单号...", false);
        }
        InvOrderApiImpl.invLossOrderGetCurrentOrder(MfhLoginService.get().getCurOfficeId(),
                StoreType.SUPERMARKET, queryRespCallback);
    }

    private NetCallBack.NetTaskCallBack queryRespCallback = new NetCallBack.NetTaskCallBack<InvLossOrder,
            NetProcessor.Processor<InvLossOrder>>(
            new NetProcessor.Processor<InvLossOrder>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"查询成功!","version":"1","data":null}
                    if (rspData != null){
                        RspBean<InvLossOrder> retValue = (RspBean<InvLossOrder>) rspData;
                        invLossOrder = retValue.getValue();
                    }

                    hideProgressDialog();

                    if (invLossOrder == null){
                        DialogUtil.showHint("获取报损单号失败");
                        dismiss();
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);

                    hideProgressDialog();
                    DialogUtil.showHint("获取报损单号失败");
                    dismiss();
                }
            }
            , InvLossOrder.class
            , CashierApp.getAppContext()) {
    };


    /**
     * 查询商品
     * */
    public void query(final String barcode){
        if (StringUtils.isEmpty(barcode)){
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        ScGoodsSkuApiImpl.findStockTakeGoodsByBarcode(barcode, queryResCallback);
    }

    private NetCallBack.NetTaskCallBack queryResCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
            NetProcessor.Processor<ScGoodsSku>>(
            new NetProcessor.Processor<ScGoodsSku>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"操作成功!","version":"1","data":""}
                    // {"code":"0","msg":"查询成功!","version":"1","data":null}
                    if (rspData == null){
                        DialogUtil.showHint("未找到商品");
                    }
                    else{
                        RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                        ScGoodsSku goods = retValue.getValue();
                        if (goods != null) {
                            productAdapter.addEntity(goods);
                        }
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
//                    ZLogger.d("查询失败: " + errMsg);
                    DialogUtil.showHint("未找到商品");
                }
            }
            , ScGoodsSku.class
            , CashierApp.getAppContext()) {
    };

    public void submit() {
        btnSubmit.setEnabled(false);
        if (invLossOrder == null){
//            DialogUtil.showHint("请点击屏幕右上角的'+'号新建盘点");
            DialogUtil.showHint("报损单号不能为空，请退出重试");

            btnSubmit.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }


        if (mProgressDialog != null) {
            mProgressDialog.setProgress(ProgressDialog.STATUS_PROCESSING, "正在报损...", false);
        }

        //保存商品到待盘点列表
//        StockTakeService.get().addNewEntity(curGoods, Double.valueOf(quantity));

        JSONArray items = new JSONArray();
        List<LossOrderItem> goodsList = productAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1){
            DialogUtil.showHint("报损明细不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        for (LossOrderItem goods : goodsList){
            JSONObject item = new JSONObject();
            item.put("proSkuId", goods.getProSkuId());
            item.put("barcode", goods.getBarcode());
            item.put("quantityCheck", goods.getQuantity());
            item.put("updateHint", HINT_MERGER);
            items.add(item);
        }

        InvOrderApiImpl.invLossOrderItemBatchCommit(invLossOrder.getId(), items.toJSONString(), submitCallback);
    }

    private NetCallBack.NetTaskCallBack submitCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"操作成功!","version":"1","data":""}
//                    RspValue<String> retValue = (RspValue<String>) rspData;
//                    String retStr = retValue.getValue();
//                    ZLogger.d("报损成功,冲突商品" + retStr);
                    DialogUtil.showHint("报损成功");
                    btnSubmit.setEnabled(true);
                    dismiss();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
//                    ZLogger.d("报损失败: " + errMsg);
                    DialogUtil.showHint("报损失败");
                    btnSubmit.setEnabled(true);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    /**
     * 显示同步数据对话框
     */
    public void showSyncDataDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }


    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
