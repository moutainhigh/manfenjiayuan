package com.manfenjiayuan.pda_supermarket.ui.invreturn;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.database.entity.InvReturnGoodsEntity;
import com.bingshanguxue.pda.database.service.InvReturnGoodsService;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.QueryBarcodeFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.Date;
import java.util.List;

import butterknife.Bind;


/**
 * 商品退货－－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvReturnGoodsInspectFragment extends QueryBarcodeFragment implements IChainGoodsSkuView {

    private static final String TAG = "InvReturnGoodsInspectFragment";
    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    private final static int LABELVIEW_INDEX_BARCODE = 0;
    private final static int LABELVIEW_INDEX_NAME = 1;
    private final static int LABELVIEW_INDEX_TOTALCOUNT = 2;
    @Bind({R.id.label_barcode, R.id.label_productName, R.id.label_totalcount })
    List<TextLabelView> labelViews;
    @Bind(R.id.label_price)
    EditLabelView labelPrice;
    @Bind(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;

    private InvReturnGoodsEntity curGoods = null;
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;


    public static InvReturnGoodsInspectFragment newInstance(Bundle args) {
        InvReturnGoodsInspectFragment fragment = new InvReturnGoodsInspectFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_invreturn_goods;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);

//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelPrice.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                labelSignQuantity.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelSignQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                submit();
            }

            @Override
            public void onScan() {
                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });

        fabCancel.setVisibility(View.VISIBLE);

        Bundle args = getArguments();
        if (args != null) {
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

//            eqvBarcode.setInputString(barcode);
            queryByBarcode(barcode);
        }

    }

    /**
     * 查询商品
     * */
    @Override
    public void sendQueryReq(String barcode) {
        super.sendQueryReq(barcode);
        QueryGoodsAsyncTask queryGoodsAsyncTask = new QueryGoodsAsyncTask(barcode);
        queryGoodsAsyncTask.execute();
    }

    @Override
    public void cancel() {
        super.cancel();
        InvReturnGoodsService.get().reject(curGoods);

        refreshPackage(null);
    }

    @Override
    public void submit() {
        super.submit();
        String price = labelPrice.getInput();
        if (StringUtils.isEmpty(price)){
            onSubmitError("请输入发货价格");
            return;
        }

        String quantityStr = labelSignQuantity.getInput();
        if (StringUtils.isEmpty(quantityStr)){
            onSubmitError("请输入签收数量");
            return;
        }
        Double quantityCheck = Double.valueOf(quantityStr);

        if (curGoods != null && curGoods.getQuantityCheck() > 0){
            quantityCheckConfirmDialog(curGoods, Double.valueOf(price), quantityCheck);
        }
        else{
            InvReturnGoodsService.get().inspect(curGoods, Double.valueOf(price), quantityCheck);
            onSubmitSuccess();

        }
    }

    @Override
    public void onSubmitSuccess() {
        super.onSubmitSuccess();
        refreshPackage(null);
    }

    /**
     * 刷新信息
     * */
    private void refreshPackage(InvReturnGoodsEntity goods){
        refresh();
        curGoods = goods;
        if (curGoods == null){
            labelViews.get(LABELVIEW_INDEX_BARCODE).setTvSubTitle("");
            labelViews.get(LABELVIEW_INDEX_NAME).setTvSubTitle("");
            labelViews.get(LABELVIEW_INDEX_TOTALCOUNT).setTvSubTitle("");
            labelPrice.setInput("");
            labelSignQuantity.setInput("");

            fabCancel.setEnabled(false);
            btnSubmit.setEnabled(false);
        }
        else {
            labelViews.get(LABELVIEW_INDEX_BARCODE).setTvSubTitle(curGoods.getBarcode());
            labelViews.get(LABELVIEW_INDEX_NAME).setTvSubTitle(curGoods.getProductName());
            labelViews.get(LABELVIEW_INDEX_TOTALCOUNT).setTvSubTitle(MUtils.formatDouble(curGoods.getTotalCount(), ""));
//            labelSignQuantity.setEtContent(String.format("%.2f", curGoods.getSignQuantity()));
            labelPrice.setInput(MUtils.formatDouble(curGoods.getPrice(), ""));
            //默认签收数量为空，根据实际情况填写
            labelSignQuantity.setInput("");

            fabCancel.setEnabled(true);
            btnSubmit.setEnabled(true);

            labelSignQuantity.requestFocus();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelSignQuantity);
    }

    @Override
    public void onChainGoodsSkuViewProcess() {

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onChainGoodsSkuViewError(String errorMsg) {
        hideProgressDialog();
        refreshPackage(null);
    }

    @Override
    public void onChainGoodsSkuViewSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        hideProgressDialog();

        if (dataList != null && dataList.size() > 0) {
            saveChainGoodsSku(dataList.get(0));
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    @Override
    public void onChainGoodsSkuViewSuccess(ChainGoodsSku data) {
        hideProgressDialog();

        if (data != null) {
            saveChainGoodsSku(data);
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    class QueryGoodsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String barcode;
        private InvReturnGoodsEntity goodsEntity;

        public QueryGoodsAsyncTask( String barcode) {
            this.barcode = barcode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                goodsEntity = InvReturnGoodsService.get().queryEntityBy(barcode);
                if (goodsEntity != null){
                    return true;
                }
            }
            catch (Exception e){
                ZLogger.d("查询本地收货商品失败, " + e.toString());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean){
                onQuerySuccess();
                saveInvReturnGoodsEntity(goodsEntity);
            }
            else{
                queryNetGoods(barcode);
            }
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            onQueryProcess();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    private void queryNetGoods(String barcode){
        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

        chainGoodsSkuPresenter.getTenantSkuMust(null, barcode);
    }


    public void saveInvReturnGoodsEntity(InvReturnGoodsEntity goods){
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        InvReturnGoodsEntity entity = InvReturnGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvReturnGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

//        entity.setOrderId(productEntity.getOrderId());
            entity.setProductId(goods.getProductId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setChainSkuId(goods.getChainSkuId());
            entity.setProductName(goods.getProductName());
            entity.setPrice(goods.getPrice());
            entity.setUnitSpec(goods.getUnitSpec());
            entity.setBarcode(goods.getBarcode());
            entity.setProviderId(goods.getProviderId());
            entity.setIsPrivate(goods.getIsPrivate());

            entity.setTotalCount(1D);
            entity.setQuantityCheck(entity.getTotalCount());
            entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_NONE);

            //设置金额
            if (entity.getQuantityCheck() == null || entity.getPrice() == null) {
                entity.setAmount(0D);
            } else {
                entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            }
            entity.setUpdatedDate(new Date());

            InvReturnGoodsService.get().saveOrUpdate(entity);

        }
        refreshPackage(entity);
    }


    /**
     * 保存搜索商品
     * */
    private void saveChainGoodsSku(ChainGoodsSku goods){
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

//        if (goods.getSingleCostPrice() == null) {
//            //“如果singleCostPrice值为null，说明缺少箱规数，信息不完整，这种情况你不允许进行采购或收货
//            DialogUtil.showHint("商品未设置单件批发价，无法采购货收货");
//            return;
//        }

        InvReturnGoodsEntity entity = InvReturnGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvReturnGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

//        entity.setOrderId(productEntity.getOrderId());
            entity.setProductId(goods.getId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setChainSkuId(goods.getId());
            entity.setProductName(goods.getSkuName());
            entity.setPrice(goods.getSingleCostPrice());
            entity.setUnitSpec(goods.getUnit());
            entity.setBarcode(goods.getBarcode());
            entity.setProviderId(goods.getTenantId());
            entity.setIsPrivate(IsPrivate.PLATFORM);

            entity.setTotalCount(0D);
            entity.setQuantityCheck(0D);
            entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_NONE);

            //设置金额
            if (entity.getQuantityCheck() == null || entity.getPrice() == null) {
                entity.setAmount(0D);
            } else {
                entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            }
            entity.setUpdatedDate(new Date());

            InvReturnGoodsService.get().saveOrUpdate(entity);
        }
        refreshPackage(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;
    private void quantityCheckConfirmDialog(final InvReturnGoodsEntity entity,
                                            final Double price, final Double quantity){
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(true);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经签收%.2f件，请选择[覆盖]还是[累加]",
                entity.getQuantityCheck()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvReturnGoodsService.get().inspect(entity, price, quantity);

                onSubmitSuccess();
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvReturnGoodsService.get().inspect(entity, price, entity.getQuantityCheck() + quantity);
                onSubmitSuccess();
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
