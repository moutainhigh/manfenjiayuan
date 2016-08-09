package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.framework.api.GoodsSupplyInfo;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.mfh.litecashier.ui.adapter.OrderGoodsSupplyAdapter;

import java.util.List;


/**
 * 对话框 --  订购库存商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SelectGoodsSupplyDialog extends CommonDialog {

    private View rootView;
    private ImageButton btnClose;
//    private Button btnSubmit;
    private TextView tvTitle;
    private ImageView ivHeader;
    private TextView tvName, tvBarcode;
    private ProgressBar progressBar;

    private RecyclerView mRecyclerView;
    private OrderGoodsSupplyAdapter supplyAdapter;

    private PurchaseShopcartGoodsWrapper shopcartGoodsWrapper = null;

    public interface OnDialogListener {
        void onSupplySelected(PurchaseShopcartGoodsWrapper goodsWrapper, GoodsSupplyInfo supplyInfo);
    }

    private OnDialogListener listener;

    private SelectGoodsSupplyDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectGoodsSupplyDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_order_stockgoods, null);
//        ButterKnife.bind(rootView);

        try {
            tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
            btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
            ivHeader = (ImageView) rootView.findViewById(R.id.iv_header);
            tvName = (TextView) rootView.findViewById(R.id.tv_product_name);
            tvBarcode = (TextView) rootView.findViewById(R.id.tv_barcode);
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.supply_list);
            progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
//            btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);

            tvTitle.setText("选择批发商");
            initRecyclerView();

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
//            btnSubmit.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    Long supplyId = scGoodsSku.getSupplyId();
//                    if (shopcartGoodsWrapper.getSupplyId() == null) {
//                        DialogUtil.showHint("未查到批发商信息，暂时无法加入购物车");
//                        return;
//                    }
//                    dismiss();
//                    //加入购物车
//                    if (listener != null) {
//                        listener.addToShopcart(shopcartGoodsWrapper);
//                    }
//                }
//            });
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }


        setContent(rootView, 0);
    }

    public SelectGoodsSupplyDialog(Context context) {
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
//////        p.width = d.getWidth() * 2 / 3;
//////        p.y = DensityUtil.dip2px(getContext(), 44);
//        p.height = d.getHeight();
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

        if (this.shopcartGoodsWrapper == null) {
            DialogUtil.showHint("商品无效");
            dismiss();
        }

//        if (StringUtils.isEmpty(this.rawGoods.getBarcode())){
//            DialogUtil.showHint("商品条码不能为空无效");
//            dismiss();
//            return;
//        }
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
        supplyAdapter = new OrderGoodsSupplyAdapter(getContext(), null);
        supplyAdapter.setOnAdapterListener(new OrderGoodsSupplyAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                GoodsSupplyInfo supplyInfo = supplyAdapter.getEntity(position);
                if (supplyInfo != null && shopcartGoodsWrapper != null && listener != null){
                    shopcartGoodsWrapper.setChainSkuId(supplyInfo.getOtherTenantSkuId());
                    shopcartGoodsWrapper.setBuyPrice(supplyInfo.getBuyPrice());
                    shopcartGoodsWrapper.setStartNum(supplyInfo.getStartNum());
                    shopcartGoodsWrapper.setSupplyId(supplyInfo.getSupplyId());
                    shopcartGoodsWrapper.setSupplyName(supplyInfo.getSupplyName());

                    listener.onSupplySelected(shopcartGoodsWrapper, supplyInfo);
                }
                dismiss();
            }

            @Override
            public void onDataSetChanged() {
//                if (supplyAdapter.getItemCount() > 0) {
//                    btnSubmit.setEnabled(true);
//                } else {
//                    btnSubmit.setEnabled(false);
//                }
            }
        });

        mRecyclerView.setAdapter(supplyAdapter);
    }

    public void init(ScGoodsSku scGoodsSku, List<GoodsSupplyInfo> supplyInfos,
                     OnDialogListener listener) {
        this.listener = listener;
        if (supplyInfos != null && supplyInfos.size() > 0){
            this.shopcartGoodsWrapper = PurchaseShopcartGoodsWrapper
                    .fromSupplyGoods(scGoodsSku, supplyInfos.get(0), IsPrivate.PLATFORM);
        }
        else {
            this.shopcartGoodsWrapper = PurchaseShopcartGoodsWrapper
                    .fromSupplyGoods(scGoodsSku, null, IsPrivate.PLATFORM);
        }

        refresh(supplyInfos, true);
    }

    /**
     * 开始加载
     */
    private void onLoadStart() {
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 加载完成
     */
    private void onLoadFinished() {
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 加载批发商数据
     */
    public void reload() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品信息。");
            onLoadFinished();
            return;
        }

        ZLogger.d("查询商品批发商信息");
        onLoadStart();

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
                NetProcessor.Processor<ScGoodsSku>>(
                new NetProcessor.Processor<ScGoodsSku>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载商品信息失败: " + errMsg);
                        //查询失败
                        saveSupplyInfo(null, false);
                        onLoadFinished();
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                            saveSupplyInfo(retValue.getValue(), false);
                        } else {
                            saveSupplyInfo(null, false);
                        }
                        onLoadFinished();
                    }
                }
                , ScGoodsSku.class
                , CashierApp.getAppContext()) {
        };

        ScGoodsSkuApiImpl.getLocalByBarcode(shopcartGoodsWrapper.getBarcode(), responseCallback);
    }

    /**
     * 刷新商品信息
     */
    private void refresh(List<GoodsSupplyInfo> supplyInfoList, boolean isNeedReload) {
        if (shopcartGoodsWrapper != null){
            tvName.setText(shopcartGoodsWrapper.getProductName());
            tvBarcode.setText(shopcartGoodsWrapper.getBarcode());
            Glide.with(getContext())
                    .load(shopcartGoodsWrapper.getImgUrl())
                    .error(R.mipmap.ic_image_error)
                    .into(ivHeader);
        }
        supplyAdapter.setEntityList(supplyInfoList);

        if (isNeedReload && (supplyInfoList == null || supplyInfoList.size() < 1)){
            reload();
        }
    }

    /**
     * 保存商品批发信息
     */
    private void saveSupplyInfo(ScGoodsSku scGoodsSku, boolean isNeedReload) {
        if (scGoodsSku == null) {
            DialogUtil.showHint("加载批发商信息失败");
            return;
        }

        List<GoodsSupplyInfo> supplyInfoList = scGoodsSku.getSupplyItems();
        if (supplyInfoList != null && supplyInfoList.size() > 0){
            refresh(supplyInfoList, isNeedReload);
        }
        else{
            DialogUtil.showHint("未查询到批发商信息");
        }
    }

}
