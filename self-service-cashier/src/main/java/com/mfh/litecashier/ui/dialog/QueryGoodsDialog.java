package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.QueryGoodsAdapter;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;


/**
 * 对话框 -- 发送商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class QueryGoodsDialog extends CommonDialog {

    public static final int DIALOG_TYPE_SEND_GOODS = 2;//发送商品

    private View rootView;
    private TextView tvTitle;
    private Button btnSubmit;
    private ImageButton btnClose;
    private InputNumberLabelView inlvBarcode;
    private RecyclerView mRecyclerView;
    private QueryGoodsAdapter productAdapter;

    private int dialogType = DIALOG_TYPE_SEND_GOODS;

    private QueryGoodsDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private QueryGoodsDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_querygoods, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        inlvBarcode = (InputNumberLabelView) rootView.findViewById(R.id.inlv_barcode);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.product_list);


        initRecyclerView();
        inlvBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    query(text);

                    inlvBarcode.clear();
                }

            }
        });

        btnSubmit.setVisibility(View.VISIBLE);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //TODO,退货
                if (dialogType == DIALOG_TYPE_SEND_GOODS){
                    DialogUtil.showHint("发送商品");
                }
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

    public QueryGoodsDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);

//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
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
        inlvBarcode.clear();
        productAdapter.setEntityList(null);

        if (dialogType == DIALOG_TYPE_SEND_GOODS){
            tvTitle.setText("发送商品");
        }
    }

    public void init(int dialogType){
        this.dialogType = dialogType;

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
        productAdapter = new QueryGoodsAdapter(getContext(), null);
        productAdapter.setOnAdapterListener(new QueryGoodsAdapter.OnAdapterListener() {
            @Override
            public void onDataSetChanged(boolean needScroll) {

            }
        });
        mRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 根据条码查询商品
     */
    public void query(final String barcode){
        if (StringUtils.isEmpty(barcode)){
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
    }

        ScGoodsSkuApiImpl.getGoodsByBarCode(barcode, queryResCallback);
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

                        productAdapter.append(retValue.getValue());
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

}
